
package com.example.block;

import android.util.Pair;

import com.example.blockchain.Score;
import com.example.blockchain.User;
import com.example.security.RSA;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.io.Serializable;

public class Transaction implements Serializable {
    private final String id;
    private final long timeStamp;

    private User player1;
    private User player2;
    private User referee;
    private User winner;

    private String refereeSignature;
    private String player1Signature = null;
    private String player2Signature = null;

    private boolean matchFairForPlayer1 = false;
    private boolean matchFairForPlayer2 = false;


    public Transaction(User player1, User player2, User referee, User winner, RSA rsa, long timestamp) {
        this.player1 = player1;
        this.player2 = player2;
        this.referee = referee;
        this.winner = winner;
        this.timeStamp = timestamp;
        this.refereeSignature = rsa.EncryptUsingPrivate(Long.toString(timeStamp));
        this.id = getTransactionId();
    }

    public Transaction(User player1, User player2, User referee, User winner, String refereeSignature, String player1Signature, String player2Signature, boolean matchFairForPlayer1, boolean matchFairForPlayer2, long timestamp) {
        this.player1 = player1;
        this.player2 = player2;
        this.referee = referee;
        this.winner = winner;
        this.refereeSignature = refereeSignature;
        this.player1Signature = player1Signature;
        this.player2Signature = player2Signature;
        this.timeStamp = timestamp;

        this.id = getTransactionId();
        this.matchFairForPlayer1 = matchFairForPlayer1;
        this.matchFairForPlayer2 = matchFairForPlayer2;
    }

    public Transaction(User player1, User player2, User referee, User winner, String refereeSignature, String player1Signature, String player2Signature, long timestamp){
        this.player1 = player1;
        this.player2 = player2;
        this.referee = referee;
        this.winner = winner;
        this.refereeSignature = refereeSignature;
        this.player1Signature = player1Signature;
        this.player2Signature = player2Signature;
        this.timeStamp = timestamp;

        this.id = getTransactionId();
    }

    private String getTransactionId() {
                MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] hash = digest.digest(Long.toString(this.getTimeStamp()).getBytes(StandardCharsets.UTF_8));

        // Convert the byte array to a hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public ArrayList<String> getPendingSignaturePlayerList() {
        ArrayList<String> playerList = new ArrayList<>();

        if (player1Signature==null) playerList.add(getPlayer1().getPublicKeyString());
        if (player2Signature==null) playerList.add(getPlayer2().getPublicKeyString());

        return playerList;
    }

    //Getters
    public User getPlayer1() {
        return player1;
    }

    public User getPlayer2(){
        return player2;
    }

    public User getReferee() {
        return referee;
    }

    public String getRefereeSignature(){return refereeSignature;}

    public String getPlayer1Signature() {
        return player1Signature;
    }

    public String getPlayer2Signature() {
        return player2Signature;
    }

    public User getWinner() {
        return winner;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setPlayer1Signature(String player1Signature) {
        this.player1Signature = player1Signature;
    }

    public void setPlayer2Signature(String player2Signature) {
        this.player2Signature = player2Signature;
    }

    public void setMatchFairForPlayer1(boolean matchFairForPlayer1) {
        this.matchFairForPlayer1 = matchFairForPlayer1;
    }

    public void setMatchFairForPlayer2(boolean matchFairForPlayer2) {
        this.matchFairForPlayer2 = matchFairForPlayer2;
    }

    public boolean isResultAcceptedByBoth() {
        return this.matchFairForPlayer1 && this.matchFairForPlayer2;
    }

    public boolean isResultUnfairForBoth() {
        return !isMatchFairForPlayer1() && !isMatchFairForPlayer2();
    }

    public boolean isResultUnfairForLoserOnly() {
        if(winner.getPublicKeyString().equals(player1.getPublicKeyString())) {
            return isMatchFairForPlayer1() && !isMatchFairForPlayer2();
        }
        else {return !isMatchFairForPlayer1() && isMatchFairForPlayer2();}
    }


    public boolean isMatchFairForPlayer1() {
        return matchFairForPlayer1;
    }

    public boolean isMatchFairForPlayer2() {
        return matchFairForPlayer2;
    }


    public String getId() {
        return this.id;
    }

    public User getLoser() {
        if(winner.getPublicKeyString().equals(player1.getPublicKeyString())) {
            return player2;
        }
        else {
            return player1;
        }
    }
}