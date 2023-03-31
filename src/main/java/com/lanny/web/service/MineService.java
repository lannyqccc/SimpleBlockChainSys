package com.lanny.web.service;

import com.google.gson.GsonBuilder;
import com.lanny.web.model.Block;
import com.lanny.web.model.Message;
import com.lanny.web.model.Transaction;
import com.lanny.web.utils.BlockChain;
import com.lanny.web.utils.MsgSyn;
import com.lanny.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.netty.NettyWebServer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MineService {

    @Autowired
    BlockChain blockChain;
    @Autowired
    BlockService blockService;

    @Autowired
    NetworkService networkService;

    public Block mine(){

        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction = new Transaction();
        transaction.setId("1");
        transaction.setTrInfo("这是IP为："+ WebUtils.getLocalIp()+"，端口号为："+blockChain.getP2pPort()+"的节点挖矿生成的区块");
        transactions.add(transaction);

        // 定义每次哈希函数的结果
        String newBlockHash = "";
        int nonce = 0;
        long start = System.currentTimeMillis();
        System.out.println("开始挖矿");
        while (true) {

            newBlockHash = blockService.calculateHash(blockChain.getLatestBlock().getHash(), nonce);

            if (blockService.isValidHash(newBlockHash)) {
                System.out.println("挖矿完成，正确的hash值：" + newBlockHash);
                System.out.println("挖矿耗费时间：" + (System.currentTimeMillis() - start) + "ms");
                break;
            }
            System.out.println("第"+(nonce+1)+"次尝试计算的hash值：" + newBlockHash);
            nonce++;
        }

        Block block = blockService.createNewBlock(nonce, blockChain.getLatestBlock().getHash(), newBlockHash, transactions);

        Message msg = new Message();
        msg.setType(MsgSyn.RESPONSE_LATEST_BLOCK);
        msg.setData(new GsonBuilder().setPrettyPrinting().create().toJson(block));
        networkService.broadcast(new GsonBuilder().setPrettyPrinting().create().toJson(msg));

        return block;

    }

}
