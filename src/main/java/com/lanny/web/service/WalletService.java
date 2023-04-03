package com.lanny.web.service;

import com.google.gson.GsonBuilder;
import com.lanny.web.model.*;
import com.lanny.web.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WalletService {

    @Autowired
    BlockService blockService;
    @Autowired
    TransactionService transactionService;
    @Autowired
    MineService mineService;
    @Autowired
    BlockChain blockChain;
    @Autowired
    Wallet wallet;
    @Autowired
    WalletService walletService;
    @Autowired
    NetworkService networkService;

    public String createTransaction(double amount) {
        Message message = new Message();
        try {
            Transaction transaction = sendToAddress(wallet, wallet.getReceiver(), amount);
            if (transaction != null) {
                message.setType(MsgSyn.TRANS_INFO);
                message.setData(new GsonBuilder().setPrettyPrinting().create().toJson(transaction));
                String msg = new GsonBuilder().setPrettyPrinting().create().toJson(message);
                networkService.broadcast(msg);
                return new GsonBuilder().setPrettyPrinting().create().toJson(msg);
            } else {
                message.setData("Sending failed, has no enough balances.");
                return new GsonBuilder().setPrettyPrinting().create().toJson(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Transaction sendToAddress(Wallet sender, String receiver, double amount) throws Exception {

        if (getWalletBalances(sender) < amount) {
            System.out.println("Sending failed, has no enough balances.");
            return null;
        }

        List<TxIn> txIns = new ArrayList<>();

        double total = 0;
        for (Map.Entry<String, TxOut> item: sender.getPersonalUTXOs().entrySet()) {
            TxOut txOut = item.getValue();
            total += txOut.getAmount();
            txIns.add(new TxIn(item.getKey()));
            if(total > amount) break;
        }

        Transaction transaction = new Transaction(sender.getPublicKey(), receiver, amount, txIns);
        String signature = generateSignature(transaction, sender.getPrivateKey());
        transaction.setSignature(signature);

        for(TxIn txIn: txIns){
            sender.getPersonalUTXOs().remove(txIn.getTxOutId());
        }

        return transaction;
    }

    public double getWalletBalances(Wallet wallet) {

        double total = 0;
        HashMap<String, TxOut> UTXOs = new HashMap<>();

        for (Map.Entry<String, TxOut> item: blockChain.getUTXOs().entrySet()){
            TxOut txOut = item.getValue();
            if (txOut.getAddress().equals(wallet.getPublicKey())) {
                UTXOs.put(item.getKey(), txOut);
                total += txOut.getAmount();
            }
        }
        wallet.setPersonalUTXOs(UTXOs);
        wallet.setBalance(total);
        return total;
    }

    public static KeyPair generateKeyPair() {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyGen.initialize(ecSpec, random);
            return keyGen.generateKeyPair();

        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String generateSignature(Transaction transaction, String privateKey) {
        String data = transaction.getSender()
                + transaction.getReceiver() + transaction.getAmount();
        return CryptoUtils.SHA256(data);
    }

    public boolean verifySignature(Transaction transaction) throws Exception {
        String data = transaction.getSender() + transaction.getReceiver() + transaction.getAmount();
        return CryptoUtils.SHA256(data).equals(transaction.getSignature());
    }


}
