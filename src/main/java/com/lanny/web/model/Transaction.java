package com.lanny.web.model;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Transaction {

    private String id;
    private String trInfo;
//    private List<TxOut> txOuts;
//    private List<TxIn> txIns;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrInfo() {
        return trInfo;
    }

    public void setTrInfo(String trInfo) {
        this.trInfo = trInfo;
    }

    //
//    public List<TxOut> getTxOuts() {
//        return txOuts;
//    }
//
//    public void setTxOuts(List<TxOut> txOuts) {
//        this.txOuts = txOuts;
//    }
//
//    public List<TxIn> getTxIns() {
//        return txIns;
//    }
//
//    public void setTxIns(List<TxIn> txIns) {
//        this.txIns = txIns;
//    }
}

