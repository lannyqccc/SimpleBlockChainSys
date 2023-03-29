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
}
