package com.example.blockchain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.block.Chain;
import com.example.block.Transaction;
import com.example.database.DBManager;
import com.example.network.NetworkHandler;
import com.example.security.RSA;

import java.util.ArrayList;
import java.util.Random;


public class ScreenValidateAsPlayer extends AppCompatActivity {
    ArrayList<Transaction> listOfTransactionsToSign = new ArrayList<>();
    ArrayList<Transaction> listOfCheckedTransactions = new ArrayList<>();
    DBManager db = new DBManager(this, this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_validate_as_player);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.transactionLayout);
        addFake(db);



        for (Transaction transaction : db.getAllTransactions()) {
            logTransaction(transaction);
            addTransactionToLayout(linearLayout, transaction);
        }

    }

    private void addTransactionToLayout(LinearLayout linearLayout, Transaction transaction) {

        if (transaction.getPendingSignaturePlayerList().contains(Global.getInstance().publicKeyStr)) {
            listOfTransactionsToSign.add(transaction);
            linearLayout.addView(getTransactionRepresentation(transaction));
        }
    }

    private void logTransaction(Transaction transaction) {
        // todo extract to transaction if time else don't care
        Log.d("matchValidation", String.format("player1.signature : %s", transaction.getPlayer1Signature()));
        Log.d("matchValidation", String.format("player2.signature : %s", transaction.getPlayer2Signature()));
        Log.d("matchValidation", String.format("player1 pk : %s", transaction.getPlayer1().getPublicKeyString()));
        Log.d("matchValidation", String.format("player2 pk : %s", transaction.getPlayer2().getPublicKeyString()));
        Log.d("matchValidation", String.format("player1 username : %s", transaction.getPlayer1().getPseudo()));
        Log.d("matchValidation", String.format("player2 username : %s", transaction.getPlayer2().getPseudo()));
    }

    public void signAllChecked(View view) {
        RSA rsa = new RSA(Global.getInstance().publicKey, Global.getInstance().privateKey);
        DBManager dbManager = new DBManager(this, this);
        Chain chain = new Chain();

        for (Transaction transaction : listOfTransactionsToSign) {
            /*******
             * TOMODIFY
             */
            if (transaction.getPlayer1().getPublicKeyString().equals(Global.getInstance().publicKeyStr)) {
                transaction.setPlayer1Signature(rsa.EncryptUsingPrivate(Long.toString(transaction.getTimeStamp())));
                transaction.setPlayer2Signature(rsa.EncryptUsingPrivate(Long.toString(transaction.getTimeStamp())));
                if(listOfCheckedTransactions.contains(transaction)) {
                    db.updateFairnessTransaction(transaction, true, true);
                }
            }

            else {
                transaction.setPlayer1Signature(rsa.EncryptUsingPrivate(Long.toString(transaction.getTimeStamp())));
                transaction.setPlayer2Signature(rsa.EncryptUsingPrivate(Long.toString(transaction.getTimeStamp())));
                if(listOfCheckedTransactions.contains(transaction)) {
                    db.updateFairnessTransaction(transaction, true, true);
                }
            }

            dbManager.updateTransaction(transaction);
            try {
                NetworkHandler.getInstance().getRendezVousClient().sendTransactionToSign(transaction);
            } catch (Exception e) {
                Log.d("matchValidation", "transaction sending failed");
                e.printStackTrace();
            }
        }
        DBManager db = new DBManager(this, this);
        chain.updateAllFromTransactions(db);

        Intent intent = new Intent(ScreenValidateAsPlayer.this, TransactionValidationSuccess.class);
        startActivity(intent);

    }

    private View getTransactionRepresentation(Transaction transaction) {
        LinearLayout linearLayout = new LinearLayout(findViewById(R.id.transactionLayout).getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        String player1Nickname = transaction.getPlayer1().getPseudo();
        String player2Nickname = transaction.getPlayer2().getPseudo();

        TextView players = new TextView(linearLayout.getContext());
        players.setText(String.format("Joueurs : %s vs %s", player1Nickname, player2Nickname));

        TextView winner = new TextView(linearLayout.getContext());
        winner.setText(String.format("Gagnant : %s", transaction.getWinner().getPseudo()));

        CheckBox isLegit = new CheckBox(linearLayout.getContext());
        isLegit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) listOfCheckedTransactions.add(transaction);
                else try {
                    listOfCheckedTransactions.remove(transaction);
                } catch (Exception e) {
                    // todo nothing
                }
            }
        });

        linearLayout.addView(players);
        linearLayout.addView(winner);
        linearLayout.addView(isLegit);

        return linearLayout;
    }

    private void addFake(DBManager db) {

        RSA rsa = new RSA(2048);
        Pair<String, String> privateKeyTim = rsa.getPrivateKey();
        Pair<String, String> privateKeyJean = rsa.getPrivateKey();
        for(int i = 0; i < 10; i++) {
            addFakeTransactions(privateKeyTim, privateKeyJean, db);
        }
        for(int j = 0; j < 30; j++) {
            addFakeTransactions();
        }
    }

    private void addFakeTransactions(Pair<String, String> pkeyTim, Pair<String, String> pkeyJean, DBManager db) {
        User referee;
        User user2;
        RSA rsa;
        Random random = new Random();


        int arbitre = random.nextInt(2);
        int vainqueur = random.nextInt(2);

        if(arbitre == 0) {
            referee = new User("Tim", db.getPlayerPublicKey("Tim"));
            rsa = new RSA(db.getPlayerPublicKey("Tim"), pkeyTim);
            user2 = new User("Jean", db.getPlayerPublicKey("Jean"));
        }
        else {
            referee = new User("Jean", db.getPlayerPublicKey("Jean"));
            rsa = new RSA(db.getPlayerPublicKey("Jean"), pkeyJean);
            user2 = new User("Tim", db.getPlayerPublicKey("Tim"));
        }
        User user1 = new User(Global.getInstance().nickname, Global.getInstance().publicKey);
        Transaction transac;
        if(vainqueur == 0) {
            transac = new Transaction(user1, user2, referee, user1, rsa, System.currentTimeMillis());
        }
        else {
            transac = new Transaction(user1, user2, referee, user2, rsa, System.currentTimeMillis());
        }
        db.insertNewTransac(transac);


    }

    private void addFakeTransactions() {
        User user1;
        User user2;
        User user3;
        Long time = System.currentTimeMillis();
        Transaction transac;
        RSA rsa = new RSA(2048);
        user1 = getRandomUser();
        user2 = getRandomUser();
        while(user2.getPseudo().equals(user1.getPseudo())) {
            user2 = getRandomUser();
        }
        user3 = getRandomUser();
        while(user3.getPseudo().equals(user2.getPseudo())|| user3.getPseudo().equals(user1.getPseudo())) {
            user3 = getRandomUser();
        }
        String sign1 = rsa.EncryptUsingPrivate(Long.toString(time));
        String sign2 = rsa.EncryptUsingPrivate(Long.toString(time));
        String sign3 = rsa.EncryptUsingPrivate(Long.toString(time));

        transac = new Transaction(user1, user2, user3, getRandomWinner(user1, user2), sign1, sign2, sign3, true, true, time);
        transac.setMatchFairForPlayer1(true);
        transac.setMatchFairForPlayer2(true);
        db.insertNewTransac(transac);


    }

    private User getRandomUser()  {
        Random random = new Random();
        int num = random.nextInt(4);
        if(num == 0) {
            return new User("Jean", db.getPlayerPublicKey("Jean"));
        }
        else if(num == 1) {
            return new User("Tim", db.getPlayerPublicKey("Tim"));

        }
        else if(num == 2) {
            return new User("Charles", db.getPlayerPublicKey("Charles"));
        }
        else {
            return  new User("Pierre", db.getPlayerPublicKey("Pierre"));
        }
    }

    private boolean getRandomBoolean() {
        Random random = new Random();
        int num = random.nextInt(2);
        return num == 0;
    }

    private User getRandomWinner(User u1, User u2) {
        Random random = new Random();
        int num = random.nextInt(2);
        if(num == 0) {
            return u1;
        }
        else {
            return u2;
        }
    }
}