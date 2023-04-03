package com.lanny.web.model;

public class TxIn {

    private String txOutId;
    private TxOut txOuts;

    public TxIn() {}

    public TxIn(String txOutId) {
        this.txOutId = txOutId;
    }

    public String getTxOutId() {
        return txOutId;
    }

    public void setTxOutId(String txOutId) {
        this.txOutId = txOutId;
    }

    public TxOut getTxOuts() {
        return txOuts;
    }

    public void setTxOuts(TxOut txOuts) {
        this.txOuts = txOuts;
    }

    @Override
    public String toString() {
        return txOutId + txOuts;
    }

}
