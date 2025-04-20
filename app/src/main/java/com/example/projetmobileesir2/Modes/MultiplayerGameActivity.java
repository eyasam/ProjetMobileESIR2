package com.example.projetmobileesir2.Modes;

import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.projetmobileesir2.Bluetooth.BluetoothConnectionHolder;
import com.example.projetmobileesir2.Defis.*;
import com.example.projetmobileesir2.R;

import java.util.Random;

public class MultiplayerGameActivity extends AppCompatActivity {

    public static MultiplayerGameActivity instance;

    private int scoreDefi1 = -1, scoreDefi2 = -1, scoreDefi3 = -1;
    private int scoreAdverse1 = -1, scoreAdverse2 = -1, scoreAdverse3 = -1;

    private boolean isHost, isReady = false, readySent = false;
    private boolean premierDefiFait = false;

    private String premierDefi = "", deuxiemeDefi = "", troisiemeDefi = "";

    private TextView multiStatusText, multiInstructionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game);
        instance = this;

        multiStatusText = findViewById(R.id.multiStatusText);
        multiInstructionText = findViewById(R.id.multiInstructionText);

        isHost = getIntent().getBooleanExtra("isHost", false);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(btReceiver, new IntentFilter("BLUETOOTH_MESSAGE"));

        if (isHost) {
            choisirDefisAleatoires();
            BluetoothConnectionHolder.sendMessage("DEFIS:" + premierDefi + "," + deuxiemeDefi + "," + troisiemeDefi);
            BluetoothConnectionHolder.sendMessage("START");
            lancerPremierDefi();
        } else {
            updateUI("Connexion réussie !", "En attente de l’hôte...");
        }
    }

    private void choisirDefisAleatoires() {
        premierDefi = new Random().nextBoolean() ? "SHAKE" : "GYRO";
        deuxiemeDefi = new Random().nextBoolean() ? "QUIZ" : "DEVINE";
        troisiemeDefi = new Random().nextBoolean() ? "SNAKE" : "FRUIT";
    }

    private void lancerPremierDefi() {
        updateUI("1er défi en cours", "Bonne chance !");
        Intent i;

        if ("SHAKE".equals(premierDefi)) {
            i = new Intent(this, ShakeActivity.class);
        } else {
            i = new Intent(this, GyroscopeActivity.class);
        }

        i.putExtra("isMultiplayer", true);
        i.putExtra("isHost", isHost);
        startActivity(i);
    }

    private void lancerDeuxiemeDefi() {
        updateUI("2e défi en cours", "Prépare-toi !");
        Intent i;

        if ("QUIZ".equals(deuxiemeDefi)) {
            i = new Intent(this, QuizChoixActivity.class);
        } else {
            i = new Intent(this, DevineMotActivity.class);
        }

        i.putExtra("isMultiplayer", true);
        i.putExtra("isHost", isHost);
        startActivity(i);
    }

    private void lancerTroisiemeDefi() {
        updateUI("3e défi en cours", "Donne tout !");
        Intent i;

        if ("SNAKE".equals(troisiemeDefi)) {
            i = new Intent(this, SnakeActivity.class);
        } else {
            i = new Intent(this, CutTheFruitActivity.class);
        }

        i.putExtra("isMultiplayer", true);
        i.putExtra("isHost", isHost);
        startActivity(i);
    }

    public static void saveLocalScore(int score) {
        if (instance == null) return;

        if (!instance.premierDefiFait) {
            instance.scoreDefi1 = score;
            instance.premierDefiFait = true;
            BluetoothConnectionHolder.sendMessage("SCORE1:" + score);
            BluetoothConnectionHolder.sendMessage("READY1_DONE");
            instance.updateUI("1er défi terminé", "En attente de l'adversaire...");
        } else if (instance.scoreDefi2 == -1) {
            instance.scoreDefi2 = score;
            BluetoothConnectionHolder.sendMessage("SCORE2:" + score);
            BluetoothConnectionHolder.sendMessage("READY2_DONE");
            instance.updateUI("2e défi terminé", "En attente du score adverse...");
        } else {
            instance.scoreDefi3 = score;
            BluetoothConnectionHolder.sendMessage("SCORE3:" + score);
            BluetoothConnectionHolder.sendMessage("READY3_DONE");
            instance.updateUI("3e défi terminé", "Calcul du score final...");
        }

        instance.readySent = true;
        instance.checkScores();
    }

    private void checkScores() {
        // Lancer 2e défi
        if (scoreDefi1 != -1 && scoreAdverse1 != -1 &&
                scoreDefi2 == -1) {
            lancerDeuxiemeDefi();
        }

        // Lancer 3e défi
        if (scoreDefi2 != -1 && scoreAdverse2 != -1 &&
                scoreDefi3 == -1) {
            lancerTroisiemeDefi();
        }

        // Fin du jeu : afficher les résultats
        if (scoreDefi1 != -1 && scoreDefi2 != -1 && scoreDefi3 != -1 &&
                scoreAdverse1 != -1 && scoreAdverse2 != -1 && scoreAdverse3 != -1) {

            int scoreTotal = scoreDefi1 + scoreDefi2 + scoreDefi3;
            int scoreAdverseTotal = scoreAdverse1 + scoreAdverse2 + scoreAdverse3;

            Intent i = new Intent(this, ResultatsActivity.class);
            i.putExtra("scoreLocal", scoreTotal);
            i.putExtra("scoreAdverse", scoreAdverseTotal);
            startActivity(i);
            finish();
        }
    }

    private void updateUI(String status, String instruction) {
        runOnUiThread(() -> {
            if (multiStatusText != null) multiStatusText.setText(status);
            if (multiInstructionText != null) multiInstructionText.setText(instruction);
        });
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("message");
            if (msg == null) return;

            Log.d("APPPP_BT_RECEIVE", "Reçu : " + msg);

            if (msg.startsWith("DEFIS:")) {
                String[] parts = msg.substring(6).split(",");
                if (parts.length == 3) {
                    premierDefi = parts[0];
                    deuxiemeDefi = parts[1];
                    troisiemeDefi = parts[2];
                }
            } else if (msg.startsWith("SCORE1:")) {
                scoreAdverse1 = Integer.parseInt(msg.substring(7));
                checkScores();
            } else if (msg.startsWith("SCORE2:")) {
                scoreAdverse2 = Integer.parseInt(msg.substring(7));
                checkScores();
            } else if (msg.startsWith("SCORE3:")) {
                scoreAdverse3 = Integer.parseInt(msg.substring(7));
                checkScores();
            } else if (msg.equals("READY1_DONE") || msg.equals("READY2_DONE") || msg.equals("READY3_DONE")) {
                isReady = true;
                checkScores();
            } else if (msg.equals("START")) {
                lancerPremierDefi();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btReceiver);
        instance = null;
    }
}