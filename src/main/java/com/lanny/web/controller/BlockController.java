package com.lanny.web.controller;

import com.google.gson.GsonBuilder;
import com.lanny.web.service.BlockService;
import com.lanny.web.service.MineService;
import com.lanny.web.utils.BlockChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BlockController {

    @Autowired
    BlockChain blockChain;

    @Autowired
    BlockService blockService;

    @Autowired
    MineService mineService;


    @RequestMapping("/scan")
    @ResponseBody
    public String scanBlockChain() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(blockChain.getBlockList());
    }

    @GetMapping("/data")
    @ResponseBody
    public String scanData() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(blockChain.getUTXOs());
    }

    @GetMapping("/create")
    @ResponseBody
    public String createFirstBlock() {
        blockService.createGenesisBlock();
        return new GsonBuilder().setPrettyPrinting().create().toJson(blockChain.getBlockList());
    }

    @GetMapping("/mine")
    @ResponseBody
    public String createNewBlock() {
        mineService.mine();
        return new GsonBuilder().setPrettyPrinting().create().toJson(blockChain.getBlockList());
    }


}
