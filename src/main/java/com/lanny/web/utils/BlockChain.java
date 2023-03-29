package com.lanny.web.utils;

import com.lanny.web.model.Block;
import com.lanny.web.model.TxOut;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Component
public class BlockChain {

    public static int transactionNums = 0;
    public static final int BLOCK_GENERATION_INTERVAL = 5;
    public static final int DIFFICULTY_ADJUSTMENT_INTERVAL = 60;
    public HashMap<String, String> UTXOs = new HashMap<String,String>();
    private List<Block> blockList = new ArrayList<>();

    public Block getLatestBlock() {
        return !blockList.isEmpty() ? blockList.get(blockList.size() - 1) : null;
    }
    public static int getTransactionNums() {
        return transactionNums;
    }

    public static void setTransactionNums(int transactionNums) {
        BlockChain.transactionNums = transactionNums;
    }

    public HashMap<String, String> getUTXOs() {
        return UTXOs;
    }

    public void setUTXOs(HashMap<String, String> UTXOs) {
        this.UTXOs = UTXOs;
    }

    public List<Block> getBlockList() {
        return blockList;
    }

    public void setBlockList(List<Block> blockList) {
        this.blockList = blockList;
    }

}
