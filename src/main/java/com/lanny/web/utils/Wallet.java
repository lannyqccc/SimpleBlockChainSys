package com.lanny.web.utils;

import com.lanny.web.model.TxOut;
import com.lanny.web.service.WalletService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.security.KeyPair;
import java.util.HashMap;

@Component
public class Wallet {

    private String publicKey;
    private String privateKey;
    private HashMap<String, TxOut> personalUTXOs;
    private double balance;
    @Value("${block.p2pPort}")
    private int p2pPort;
    private String receiver;

    public Wallet() {
        KeyPair keyPair = WalletService.generateKeyPair();
        this.publicKey = CryptoUtils.keyToString(keyPair.getPublic());
        this.privateKey = CryptoUtils.keyToString(keyPair.getPrivate());
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public HashMap<String, TxOut> getPersonalUTXOs() {
        return personalUTXOs;
    }

    public void setPersonalUTXOs(HashMap<String, TxOut> personalUTXOs) {
        this.personalUTXOs = personalUTXOs;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getP2pPort() {
        return p2pPort;
    }

    public void setP2pPort(int p2pPort) {
        this.p2pPort = p2pPort;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
