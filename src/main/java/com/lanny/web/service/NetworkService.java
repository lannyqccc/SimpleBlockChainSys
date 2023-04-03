package com.lanny.web.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lanny.web.model.Block;
import com.lanny.web.model.Message;
import com.lanny.web.model.Transaction;
import com.lanny.web.model.TxOut;
import com.lanny.web.utils.BlockChain;
import com.lanny.web.utils.MsgSyn;
import com.lanny.web.utils.Wallet;
import com.lanny.websocket.Client;
import com.lanny.websocket.Server;
import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Service
public class NetworkService implements ApplicationRunner {
    @Autowired
    BlockChain blockChain;
    @Autowired
    BlockService blockService;
    @Autowired
    Server server;
    @Autowired
    Client client;
    @Autowired
    Wallet wallet;
    @Autowired
    TransactionService transactionService;


    public void messageHandler(WebSocket webSocket, String message, List<WebSocket> webSockets) {

        Message msg = new Gson().fromJson(message, Message.class);
        System.out.println("Receiving " + new GsonBuilder().setPrettyPrinting().create().toJson(msg)
                + " from " + webSocket.getRemoteSocketAddress().getAddress().toString()
                + ":" + webSocket.getRemoteSocketAddress().getPort());

        switch (msg.getType()) {
            case MsgSyn.QUERY_LATEST_BLOCK:
                write(webSocket, receiveLatestBlock());
                break;
            case MsgSyn.RESPONSE_LATEST_BLOCK:
                resBlockHandler(msg.getData());
                break;
            case MsgSyn.QUERY_BLOCKCHAIN:
                write(webSocket, receiveBlockChain());
                break;
            case MsgSyn.RESPONSE_BLOCKCHAIN:
                resBlockChainHandler(msg.getData());
                break;
            case MsgSyn.QUERY_RECEIVER:
                if (Integer.parseInt(msg.getData()) == blockChain.getP2pPort()) {
                    write(webSocket, receiveReceiver());
                }
                break;
            case MsgSyn.RESPONSE_RECEIVER:
                resReceiverHandler(msg.getData());
                break;
            case MsgSyn.TRANS_INFO:
                Transaction transaction = getTransInfo(msg.getData());
                transactionService.makeTransaction(transaction);
                break;
        }

    }

    public synchronized void resBlockHandler(String data) {

        Block receivedBlock = new Gson().fromJson(data, Block.class);
        Block theLatestBlock = blockChain.getLatestBlock();

        if (receivedBlock != null) {
            if(theLatestBlock != null) {

                if(receivedBlock.getIndex() > theLatestBlock.getIndex() + 1) {
                    broadcast(applyBlockChain());
                    System.out.println("reapply blockchain in all nodes");
                }else if (receivedBlock.getIndex() > theLatestBlock.getIndex() &&
                        theLatestBlock.getHash().equals(receivedBlock.getPreviousHash())) {
                    if (blockService.isValidNewBlock(receivedBlock, theLatestBlock)) {
                        blockChain.getBlockList().add(receivedBlock);
                        System.out.println("Block added successful.");
                        blockService.synUTXOs();
                        broadcast(receiveLatestBlock());
                    }
                    System.out.println("add block to local chain");
                }
            } else {
                broadcast(applyBlockChain());
                System.out.println("reapply blockchain in all nodes");
            }
        }

    }

    public synchronized void resBlockChainHandler(String data) {
        List<Block> receivedBlockchain = new Gson().fromJson(data, new TypeToken<List<Block>>() {}.getType());
        if(!receivedBlockchain.isEmpty() && blockService.isValidChain(receivedBlockchain)) {
            receivedBlockchain.sort(new Comparator<Block>() {
                public int compare(Block block1, Block block2) {
                    return block1.getIndex() - block2.getIndex();
                }
            });

            Block resLatestBlock = receivedBlockchain.get(receivedBlockchain.size() - 1);
            Block latestBlock = blockChain.getLatestBlock();

            if(latestBlock == null) {
                blockService.replaceChain(receivedBlockchain);
            }else {

                if (resLatestBlock.getIndex() > latestBlock.getIndex()) {
                    if (latestBlock.getHash().equals(resLatestBlock.getPreviousHash())) {
                        if (blockService.isValidNewBlock(resLatestBlock, latestBlock)) {
                            blockChain.getBlockList().add(resLatestBlock);
                            System.out.println("Block added successful.");
                            blockService.synUTXOs();
                            broadcast(receiveLatestBlock());
                        }
                        System.out.println("add block to local chain");
                    } else {
                        blockService.replaceChain(receivedBlockchain);
                    }
                }
            }
        }

    }

    public void broadcast(String message) {
        List<WebSocket> webSockets = this.getSockets();
        if (webSockets.isEmpty()) {
            return;
        }
        System.out.println("======Broadcasting：");
        for (WebSocket socket : webSockets) {
            this.write(socket, message);
        }
        System.out.println("======Broadcast completed。");
    }

    public void write(WebSocket webSocket, String message) {
        System.out.println(webSocket.getLocalSocketAddress().getAddress().toString()
                + ":" + webSocket.getLocalSocketAddress().getPort() + " is sending " + message + " to "
                + webSocket.getRemoteSocketAddress().getAddress().toString() + ":" + webSocket.getRemoteSocketAddress().getPort());
        webSocket.send(message);
    }

    public String applyTheLatestBlock() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(new Message(MsgSyn.QUERY_LATEST_BLOCK));
    }

    public String receiveLatestBlock() {
        Message message = new Message();
        message.setType(MsgSyn.RESPONSE_LATEST_BLOCK);
        Block block = blockChain.getLatestBlock();
        message.setData(new GsonBuilder().setPrettyPrinting().create().toJson(block));
        return new GsonBuilder().setPrettyPrinting().create().toJson(message);
    }

    public String applyBlockChain() {
        Message message = new Message(MsgSyn.QUERY_BLOCKCHAIN);
        return new GsonBuilder().setPrettyPrinting().create().toJson(message);
    }

    public String receiveBlockChain() {
        Message message = new Message();
        message.setType(MsgSyn.RESPONSE_BLOCKCHAIN);
        message.setData(new GsonBuilder().setPrettyPrinting().create().toJson(blockChain.getBlockList()));
        return new GsonBuilder().setPrettyPrinting().create().toJson(message);
    }

    public void getReceiver(int port) {
        broadcast(applyReceiver(port));
    }
    public String applyReceiver(int port) {
        Message message = new Message(MsgSyn.QUERY_RECEIVER);
        message.setData(Integer.toString(port));
        return new GsonBuilder().setPrettyPrinting().create().toJson(message);
    }

    public String receiveReceiver() {
        Message message = new Message(MsgSyn.RESPONSE_RECEIVER);
        message.setData(new GsonBuilder().setPrettyPrinting().create().toJson(wallet.getPublicKey()));
        return new GsonBuilder().setPrettyPrinting().create().toJson(message);
    }

    public void resReceiverHandler(String data) {

        String receiver = new Gson().fromJson(data, String.class);
        wallet.setReceiver(receiver);
        System.out.println("get receiver address successful.");
    }

    public Transaction getTransInfo(String data) {
        return new Gson().fromJson(data, Transaction.class);
    }

    public List<WebSocket> getSockets(){
        return blockChain.getSocketsList();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        server.initServer(blockChain.getP2pPort());
        client.connectToPeer(blockChain.getAddress());

    }
}
