package com.lanny.web.service;

import com.google.gson.GsonBuilder;
import com.lanny.web.model.Block;
import com.lanny.web.model.Transaction;
import com.lanny.web.model.TxIn;
import com.lanny.web.model.TxOut;
import com.lanny.web.utils.BlockChain;
import com.lanny.web.utils.CryptoUtils;
import com.lanny.web.utils.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.tree.TreeNode;
import java.time.temporal.Temporal;
import java.util.*;

@Service
public class BlockService {
    @Autowired
    BlockChain blockChain;
    @Autowired
    TransactionService transactionService;
    @Autowired
    MineService mineService;
    @Autowired
    Wallet wallet;
    @Autowired
    WalletService walletService;


    public String createGenesisBlock() {

        try {
            Block genesisBlock = new Block(0, "0");
            List<Transaction> transactions = new ArrayList<>();
            genesisBlock.setTransactions(transactions);

            Transaction transaction = transactionService.coinBaseTrans();

            transactionService.addTransaction(genesisBlock, transaction);

            mineService.mine(genesisBlock);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return new GsonBuilder().setPrettyPrinting().create().toJson(blockChain.getBlockList());
    }

    public Block generateNextBlock() {

        Block previousBlock = blockChain.getLatestBlock();

        int nextIndex = previousBlock.getIndex() + 1;
        String previousHash = previousBlock.getHash();

        return new Block(nextIndex, previousHash);
    }

    public Block addBlock(Block newBlock) {

        if (blockChain.getBlockList().isEmpty()) {
            blockChain.getBlockList().add(newBlock);
            return newBlock;
        }
        if (isValidNewBlock(newBlock, blockChain.getLatestBlock())) {
            blockChain.getBlockList().add(newBlock);
            System.out.println("Block added successful.\n");
            return newBlock;
        } else {
            System.out.println("Adding failed, block is invalid.\n");
            return null;
        }
    }

    public String calculateHash(Block block) {
        String dataToHash = block.getIndex() + block.getPreviousHash() + block.getMerkleRoot()
                + block.getTimeStamp() + block.getDifficulty() + block.getNonce();
        return CryptoUtils.SHA256(dataToHash);
    }

    public int getDifficulty() {
        if (blockChain.getBlockList().isEmpty()) {
            return blockChain.getBLOCK_GENERATION_INTERVAL();
        }
        Block latestBlock = blockChain.getLatestBlock();
        if (latestBlock.getIndex() % blockChain.getBLOCK_GENERATION_INTERVAL() == 0 && latestBlock.getIndex() != 0) {
            return getAdjustedDifficulty(latestBlock, blockChain);
        } else {
            return latestBlock.getDifficulty();
        }
    }

    public static int getAdjustedDifficulty(Block latestBlock, BlockChain aBlockchain) {
        List<Block> blockList = aBlockchain.getBlockList();
        Block prevAdjustmentBlock = blockList.get(blockList.size() - aBlockchain.getDIFFICULTY_ADJUSTMENT_INTERVAL());
        int timeExpected = aBlockchain.getBLOCK_GENERATION_INTERVAL() * aBlockchain.getDIFFICULTY_ADJUSTMENT_INTERVAL();
        long timeTaken = latestBlock.getTimeStamp() - prevAdjustmentBlock.getTimeStamp();
        if (timeTaken < timeExpected / 2) {
            return prevAdjustmentBlock.getDifficulty() + 1;
        } else if (timeTaken > timeExpected * 2) {
            return prevAdjustmentBlock.getDifficulty() - 1;
        } else {
            return prevAdjustmentBlock.getDifficulty();
        }
    }

    public boolean hashMatchesDifficulty(String hash, int difficulty) {
        String requiredPrefix = new String(new char[difficulty]).replace('\0', '0');
        return hash.startsWith(requiredPrefix);
    }

    public boolean isValidNewBlock(Block newBlock, Block previousBlock) {

        if (previousBlock.getIndex() + 1 != newBlock.getIndex())
            return false;
        else if (!previousBlock.getHash().equals(newBlock.getPreviousHash()))
            return false;
        else return newBlock.getHash().equals(CryptoUtils.SHA256(newBlock.getIndex()
                    + newBlock.getPreviousHash() + newBlock.getMerkleRoot() + newBlock.getTimeStamp()
                    + newBlock.getDifficulty() + newBlock.getNonce()));
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

    public void synUTXOs() {
        List<Transaction> transactions = blockChain.getLatestBlock().getTransactions();
        for (Transaction transaction: transactions) {
            if (transaction.getTxIns() != null) {
                for (TxIn txIn : transaction.getTxIns()) {
                    blockChain.getUTXOs().remove(txIn.getTxOutId());
                }
            }
            for (TxOut txOut: transaction.getTxOuts()) {
                blockChain.getUTXOs().put(CryptoUtils.SHA256(txOut.toString()), txOut);
            }
        }
    }

    public void replaceChain(List<Block> newBlocks) {
        List<Block> localBlockChain = blockChain.getBlockList();
        HashMap<String, TxOut> localUTXOs = blockChain.getUTXOs();
        if (isValidChain(newBlocks) && newBlocks.size() > localBlockChain.size()) {
            localBlockChain = newBlocks;

            localUTXOs.clear();

            List<TxOut> txOuts = new ArrayList<>();
            List<TxIn> txIns = new ArrayList<>();

            for (Block block: localBlockChain) {
                List<Transaction> transactions = block.getTransactions();
                for (Transaction transaction: transactions) {
                    if (transaction.getTxIns() != null) {
                        List<TxIn> txIns1 = transaction.getTxIns();
                        for (TxIn txIn: txIns1) {
                            if (!txIns.contains(txIn)) txIns.add(txIn);
                        }
                    }
                    List<TxOut> txOuts1 = transaction.getTxOuts();
                    for (TxOut txOut: txOuts1) {
                        if (!txOuts.contains(txOut)) txOuts.add(txOut);
                    }
                }
            }

            for (TxOut txOut: txOuts) {
                localUTXOs.put(CryptoUtils.SHA256(txOut.toString()), txOut);
            }
            for (TxIn txIn: txIns) {
                localUTXOs.remove(txIn.getTxOuts().toString());
            }

            blockChain.setBlockList(localBlockChain);
            blockChain.setUTXOs(localUTXOs);
            System.out.println("replaced blockChain is ："
                    + new GsonBuilder().setPrettyPrinting().create().toJson(blockChain.getBlockList()));
        } else {
            System.out.println("Received blockChain is invalid");
        }
    }
}
