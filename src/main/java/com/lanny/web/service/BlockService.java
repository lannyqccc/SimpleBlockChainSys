package com.lanny.web.service;

import com.google.gson.GsonBuilder;
import com.lanny.web.model.Block;
import com.lanny.web.model.Transaction;
import com.lanny.web.model.TxOut;
import com.lanny.web.utils.BlockChain;
import com.lanny.web.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class BlockService {
    @Autowired
    BlockChain blockChain;

    public String createGenesisBlock() {

        Block genesisBlock = new Block();

        genesisBlock.setIndex(0);
        genesisBlock.setPreviousHash("null");
        genesisBlock.setTimeStamp(new Date().getTime() / 1000);
        genesisBlock.setNonce(0);

        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction = new Transaction();
        transaction.setId("0");
        transaction.setTrInfo("这是创世区块");
        transactions.add(transaction);

        genesisBlock.setTransactions(transactions);
        genesisBlock.setHash(calculateHash(genesisBlock.getPreviousHash(), genesisBlock.getNonce()));

        blockChain.getUTXOs().put(transactions.get(0).getId(), transactions.get(0).getTrInfo());

        blockChain.getBlockList().add(genesisBlock);
        return new GsonBuilder().setPrettyPrinting().create().toJson(blockChain.getBlockList());
    }


    public Block createNewBlock(int nonce, String previousHash, String hash, List<Transaction> blockTxs) {
        Block block = new Block();
        block.setIndex(blockChain.getBlockList().size());
        //时间戳
        block.setTimeStamp(System.currentTimeMillis());
        block.setTransactions(blockTxs);
        //工作量证明，计算正确hash值的次数
        block.setNonce(nonce);
        //上一区块的哈希
        block.setPreviousHash(previousHash);
        //当前区块的哈希
        block.setHash(hash);
        if (addBlock(block)) {
            return block;
        }
        return null;
    }

    public boolean addBlock(Block newBlock) {
        //先对新区块的合法性进行校验
        if (isValidNewBlock(newBlock, blockChain.getLatestBlock())) {
            blockChain.getBlockList().add(newBlock);
            // 新区块的业务数据需要加入到已打包的业务数据集合里去
            blockChain.getUTXOs().put(newBlock.getTransactions().get(0).getId(), newBlock.getTransactions().get(0).getTrInfo());
            return true;
        }
        return false;
    }

//    public Block generateNextBlock() {
//
//        Block newBlock = new Block();
//        if (!blockChain.getBlockList().isEmpty()) {
//            Block previousBlock = blockChain.getLatestBlock();
//            int nextIndex = previousBlock.getIndex() + 1;
//            String previousHash = previousBlock.getHash();
//            return newBlock;
//        }
//        newBlock.setIndex();
//        return newBlock;
//    }

    public String calculateHash(String previousHash, int nonce) {
        String dataToHash = previousHash + nonce;
        return CryptoUtils.SHA256(dataToHash);
    }

    public boolean isValidHash(String hash) {
        //System.out.println("难度系数："+blockCache.getDifficulty());
        return hash.startsWith("0000");
    }

    public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
            System.out.println("新区块的前一个区块hash验证不通过");
            return false;
        } else {
            // 验证新区块hash值的正确性
            String hash = calculateHash(newBlock.getPreviousHash(), newBlock.getNonce());
            if (!hash.equals(newBlock.getHash())) {
                System.out.println("新区块的hash无效: " + hash + " " + newBlock.getHash());
                return false;
            }
            if (!isValidHash(newBlock.getHash())) {
                return false;
            }
        }

        return true;
    }

    public boolean isValidChain(List<Block> chain) {
        Block block = null;
        Block lastBlock = chain.get(0);
        int currentIndex = 1;
        while (currentIndex < chain.size()) {
            block = chain.get(currentIndex);

            if (!isValidNewBlock(block, lastBlock)) {
                return false;
            }

            lastBlock = block;
            currentIndex++;
        }
        return true;
    }

    public void replaceChain(List<Block> newBlocks) {
        List<Block> localBlockChain = blockChain.getBlockList();
        HashMap<String, String> localUTXOs = blockChain.getUTXOs();
        if (isValidChain(newBlocks) && newBlocks.size() > localBlockChain.size()) {
            localBlockChain = newBlocks;

            localUTXOs.clear();
            localBlockChain.forEach(block -> {
                localUTXOs.put(block.getTransactions().get(0).getId(), block.getTransactions().get(0).getTrInfo());
            });
            blockChain.setBlockList(localBlockChain);
            blockChain.setUTXOs(localUTXOs);
            System.out.println("替换后的本节点区块链："
                    + new GsonBuilder().setPrettyPrinting().create().toJson(blockChain.getBlockList()));
        } else {
            System.out.println("接收的区块链无效");
        }
    }
}
