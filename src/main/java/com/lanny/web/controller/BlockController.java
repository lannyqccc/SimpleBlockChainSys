package com.lanny.web.controller;

import com.google.gson.GsonBuilder;

import com.lanny.web.service.BlockService;
import com.lanny.web.service.MineService;
import com.lanny.web.service.NetworkService;
import com.lanny.web.service.WalletService;
import com.lanny.web.utils.BlockChain;
import com.lanny.web.utils.CryptoUtils;
import com.lanny.web.utils.Wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



@Controller
public class BlockController {

    @Autowired
    BlockChain blockChain;

    @Autowired
    BlockService blockService;

    @Autowired
    MineService mineService;

    @Autowired
    Wallet wallet;

    @Autowired
    WalletService walletService;

    @Autowired
    NetworkService networkService;


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

    @GetMapping("/puKey")
    @ResponseBody
    public String getPublicKey() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(wallet.getPublicKey());
    }

    @GetMapping("/prKey")
    @ResponseBody
    public String getPrivateKey() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(wallet.getPrivateKey());
    }

    @GetMapping("/wallet")
    @ResponseBody
    public String getWallet() {
        walletService.getWalletBalances(wallet);
        return new GsonBuilder().setPrettyPrinting().create().toJson(wallet);
    }

    @GetMapping("/receiver/{port}")
    @ResponseBody
    public String getReceiver(@PathVariable(name = "port") int port) {
        networkService.getReceiver(port);
        return new GsonBuilder().setPrettyPrinting().create().toJson(wallet);
    }

    @GetMapping("/send/{amount}")
    @ResponseBody
    public String applyTrans(@PathVariable(name = "amount") double amount) {
        walletService.createTransaction(amount);
        return new GsonBuilder().setPrettyPrinting().create().toJson(blockChain.getBlockList());
    }

}
