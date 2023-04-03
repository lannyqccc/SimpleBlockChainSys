package com.lanny.web.model;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Transaction {

    private String id;
    private String sender;
    private String receiver;
    private String signature;
    private double amount;
    private List<TxOut> txOuts;
    private List<TxIn> txIns;

    public Transaction() {}

    public Transaction(String sender, String receiver, double amount, List<TxIn> txIns) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.txIns = txIns;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public List<TxOut> getTxOuts() {
        return txOuts;
    }

    public void setTxOuts(List<TxOut> txOuts) {
        this.txOuts = txOuts;
    }

    public List<TxIn> getTxIns() {
        return txIns;
    }

    public void setTxIns(List<TxIn> txIns) {
        this.txIns = txIns;
    }
}

