package com.lanny.web.utils;

import com.lanny.web.model.Transaction;

import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CryptoUtils {

    public static String SHA256(String dataToHash) {
        MessageDigest digest = null;
        byte[] bytes = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes(UTF_8));
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes) {
                buffer.append(String.format("%02x", b));
            }
            return buffer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] outPut = null;
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            outPut = dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return outPut;
    }

    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static String keyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String encryptBASE64(byte[] key) throws Exception {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(key);
    }

    public static byte[] decryptBASE64(String key) throws Exception {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(key);
    }

    public static String getMerkleRoot(List<Transaction> transactions) {
        List<String> txsLists = new ArrayList<>();
        for (Transaction transaction: transactions) {
            txsLists.add(transaction.getId());
        }
        List<String> merkleRoot = merkleTree(txsLists);
        if (merkleRoot.isEmpty()) {
            return "";
        }
        return merkleRoot.get(0);
    }

    private static List<String> merkleTree(List<String> hashList) {

        if (hashList.size() == 1 || hashList.size() == 0) {
            return hashList;
        }
        List<String> parentHashList = new ArrayList<>();

        for (int i = 0; i < hashList.size(); i += 2) {
            String hashedString = CryptoUtils.SHA256(hashList.get(i).concat(hashList.get(i + 1)));
            parentHashList.add(hashedString);
        }

        if (hashList.size() % 2 == 1) {
            String lastHash = hashList.get(hashList.size() - 1);
            String hashedString = CryptoUtils.SHA256(lastHash.concat(lastHash));
            parentHashList.add(hashedString);
        }
        return merkleTree(parentHashList);
    }
}
