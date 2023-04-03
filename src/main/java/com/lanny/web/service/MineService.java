package com.lanny.web.service;

import com.google.gson.GsonBuilder;
import com.lanny.web.model.Block;
import com.lanny.web.model.Message;
import com.lanny.web.model.Transaction;
import com.lanny.web.utils.BlockChain;
import com.lanny.web.utils.CryptoUtils;
import com.lanny.web.utils.MsgSyn;
import com.lanny.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MineService {

    @Autowired
    BlockChain blockChain;
    @Autowired
    BlockService blockService;
    @Autowired
    NetworkService networkService;


    public Block mine(Block block) {

        findBlock(block, blockService.getDifficulty());

        Block newBlock = blockService.addBlock(block);
        System.out.println("block is mined by " + WebUtils.getLocalIp() + ":" + blockChain.getP2pPort());

        Message message = new Message();
        message.setType(MsgSyn.RESPONSE_LATEST_BLOCK);
        message.setData(new GsonBuilder().setPrettyPrinting().create().toJson(newBlock));
        networkService.broadcast(new GsonBuilder().setPrettyPrinting().create().toJson(message));

        return block;

    }

    public void findBlock(Block block, int difficulty) {
        block.setMerkleRoot(CryptoUtils.getMerkleRoot(block.getTransactions()));
        block.setDifficulty(difficulty);
        block.setTimeStamp(new Date().getTime() / 1000);
        block.setNonce(0);
        block.setHash(blockService.calculateHash(block));
        System.out.println("block is mining...");
        while (!blockService.hashMatchesDifficulty(block.getHash(), difficulty)) {
            block.setTimeStamp(new Date().getTime() / 1000);
            block.setNonce(block.getNonce()+1);
            block.setHash(blockService.calculateHash(block));
        }
        System.out.println("block mined, hash:" + block.getHash());
    }

}
