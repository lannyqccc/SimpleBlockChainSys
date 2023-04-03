package com.lanny.web.service;

import com.lanny.web.model.Block;
import com.lanny.web.model.Transaction;
import com.lanny.web.model.TxIn;
import com.lanny.web.model.TxOut;
import com.lanny.web.utils.BlockChain;
import com.lanny.web.utils.CryptoUtils;
import com.lanny.web.utils.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    WalletService walletService;
    @Autowired
    BlockChain blockChain;
    @Autowired
    BlockService blockService;
    @Autowired
    TransactionService transactionService;
    @Autowired
    MineService mineService;
    @Autowired
    Wallet wallet;


    public void makeTransaction(Transaction transaction) {

        try {
            Block newBlock = blockService.generateNextBlock();
            newBlock.setTransactions(new ArrayList<>());
            newBlock.getTransactions().add(transactionService.coinBaseTrans());

            if(transactionService.addTransaction(newBlock, transaction)) {
                mineService.mine(newBlock);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addTransaction(Block block, Transaction transaction) {

        if (transaction == null) return false;
        if (!block.getPreviousHash().equals("0")) {
            if(!transactionProcessing(transaction)) {
                return false;
            }
        }
        block.getTransactions().add(transaction);
        return true;
    }

    public boolean transactionProcessing(Transaction transaction) {

        try {
            if(!walletService.verifySignature(transaction)) {
                System.out.println("Signature failed to verify.");
                return false;
            }

            for(TxIn txIn : transaction.getTxIns()) {
                txIn.setTxOuts(blockChain.getUTXOs().get(txIn.getTxOutId()));
            }

            List<TxOut> txOuts = new ArrayList<>();

            double leftOver = getTxInsAmounts(transaction.getTxIns()) - transaction.getAmount();
            txOuts.add(new TxOut(transaction.getReceiver(), transaction.getAmount()));
            txOuts.add(new TxOut(transaction.getSender(), leftOver));

            transaction.setTxOuts(txOuts);

            transaction.setId(getTransactionId(transaction));

            for(TxOut txOut : txOuts) {
                blockChain.getUTXOs().put(CryptoUtils.SHA256(txOut.toString()), txOut);
            }

            for(TxIn txIn : transaction.getTxIns()) {
                if(txIn.getTxOuts() == null) continue;
                blockChain.getUTXOs().remove(CryptoUtils.SHA256(txIn.getTxOuts().toString()));
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Transaction coinBaseTrans() {

        Transaction transaction = new Transaction();
        List<TxOut> txOuts = new ArrayList<>();
        txOuts.add(new TxOut(wallet.getPublicKey(), 50));
        for(TxOut txOut : txOuts) {
            blockChain.getUTXOs().put(CryptoUtils.SHA256(txOut.toString()), txOut);
        }
        transaction.setAmount(50);
        transaction.setReceiver(wallet.getPublicKey());
        transaction.setTxOuts(txOuts);
        transaction.setId(CryptoUtils.SHA256(transactionService.getTxOutsString(transaction)));

        return transaction;
    }

    public double getTxInsAmounts(List<TxIn> txIns) {
        double total = 0;
        for(TxIn txIn : txIns) {
            if(txIn.getTxOuts() == null) continue;
            total += txIn.getTxOuts().getAmount();
        }
        return total;
    }

    public String getTransactionId(Transaction transaction) {
        String txInContent = getTxInsString(transaction);
        String txOutContent = getTxOutsString(transaction);
        return CryptoUtils.SHA256(txInContent + txOutContent);
    }

    public String getTxInsString(Transaction transaction) {
        StringBuilder TxInsString = new StringBuilder();
        for (TxIn txIn : transaction.getTxIns()) {
            TxInsString.append(txIn);
        }
        return TxInsString.toString();
    }

    public String getTxOutsString(Transaction transaction) {
        StringBuilder TxOutsString = new StringBuilder();
        for (TxOut txOut : transaction.getTxOuts()) {
            TxOutsString.append(txOut);
        }
        return TxOutsString.toString();
    }
}
