package com.lanny.web.utils;

import com.lanny.web.model.Block;
import com.lanny.web.model.TxOut;
import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;


@ConfigurationProperties(prefix = "block")
@Component
public class BlockChain {

    public static int transactionNums = 0;
    public static final int BLOCK_GENERATION_INTERVAL = 5;
    public static final int DIFFICULTY_ADJUSTMENT_INTERVAL = 60;
    public HashMap<String, String> UTXOs = new HashMap<String,String>();
    private List<Block> blockList = new ArrayList<>();
    private List<WebSocket> socketsList = new ArrayList<>();
    @Value("${block.p2pPort}")
    private int p2pPort;
    @Value("${block.address}")
    private String address;

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

    public int getP2pPort() {
        return p2pPort;
    }

    public void setP2pPort(int p2pPort) {
        this.p2pPort = p2pPort;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<WebSocket> getSocketsList() {
        return socketsList;
    }

    public void setSocketsList(List<WebSocket> socketsList) {
        this.socketsList = socketsList;
    }
}
