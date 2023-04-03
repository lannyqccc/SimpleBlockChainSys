package com.lanny.web.model;

import com.lanny.web.utils.BlockChain;
import com.lanny.web.utils.CryptoUtils;

import java.security.PublicKey;

public class TxOut {

    private String address;
    private double amount;
    private int txIndex;

    public TxOut() {
        this.txIndex = BlockChain.transactionNums++;
    }

    public TxOut(String address, double amount) {
        this.address = address;
        this.amount = amount;
        this.txIndex = BlockChain.transactionNums++;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(int txIndex) {
        this.txIndex = txIndex;
    }

    @Override
    public String toString() {
        return address + amount + txIndex;
    }
}
