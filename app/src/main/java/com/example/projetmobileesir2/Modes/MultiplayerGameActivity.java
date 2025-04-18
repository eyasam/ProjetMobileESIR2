package com.example.projetmobileesir2.Modes;

import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.projetmobileesir2.Bluetooth.BluetoothConnectionHolder;
import com.example.projetmobileesir2.R;

public class MultiplayerGameActivity extends AppCompatActivity {

    public static MultiplayerGameActivity instance;

    private int scoreDefi1 = -1, scoreDefi2 = -1;
    private int scoreAdverse1 = -1, scoreAdverse2 = -1;
    private boolean isHost, isReady = false, readySent = false;
    private boolean premierDefiFait = false;

    private String premierDefi = "", deuxiemeDefi = "";

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
            BluetoothConnectionHolder.sendMessage("DEFIS:" + premierDefi + "," + deuxiemeDefi);
            BluetoothConnectionHolder.sendMessage("START");
            lancerPremierDefi();
        } else {
            updateUI("Connexion réussie !", "En attente de l’hôte...");
        }
    }

    private void choisirDefisAleatoires() {
        premierDefi = new java.util.Random().nextBoolean() ? "SHAKE" : "GYRO";
        deuxiemeDefi = new java.util.Random().nextBoolean() ? "QUIZ" : "DEVINE";
    }

    private void lancerPremierDefi() {
        updateUI("1er défi en cours", "Bonne chance !");
        Intent i;

        if ("SHAKE".equals(premierDefi)) {
            i = new Intent(this, com.example.projetmobileesir2.Defis.ShakeActivity.class);
        } else {
            i = new Intent(this, com.example.projetmobileesir2.Defis.GyroscopeActivity.class);
        }

        i.putExtra("isMultiplayer", true);
        i.putExtra("isHost", isHost);
        startActivity(i);
    }

    private void lancerDeuxiemeDefi() {
        updateUI("2e défi en cours", "Prépare-toi !");
        Intent i;

        if ("QUIZ".equals(deuxiemeDefi)) {
            i = new Intent(this, com.example.projetmobileesir2.Defis.QuizChoixActivity.class);
        } else {
            i = new Intent(this, com.example.projetmobileesir2.Defis.DefiDevineMotActivity.class);
        }

        i.putExtra("isMultiplayer", true);
        i.putExtra("isHost", isHost);
        i.putExtra("scoreDefi1", scoreDefi1);
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
        } else {
            instance.scoreDefi2 = score;
            BluetoothConnectionHolder.sendMessage("SCORE2:" + score);
            BluetoothConnectionHolder.sendMessage("READY2_DONE");
            instance.updateUI("2e défi terminé", "En attente du score adverse...");
        }

        instance.readySent = true;
        instance.checkScores();
    }

    private void checkScores() {
        if (scoreDefi1 != -1 && scoreDefi2 != -1 && scoreAdverse1 != -1 && scoreAdverse2 != -1) {
            int scoreTotal = scoreDefi1 + scoreDefi2;
            int scoreAdverseTotal = scoreAdverse1 + scoreAdverse2;

            Intent i = new Intent(this, ResultatsActivity.class);
            i.putExtra("scoreLocal", scoreTotal);
            i.putExtra("scoreAdverse", scoreAdverseTotal);
            startActivity(i);
            finish();
        }

        if (scoreDefi1 != -1 && scoreAdverse1 != -1 && scoreDefi2 == -1) {
            lancerDeuxiemeDefi();
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
                if (parts.length == 2) {
                    premierDefi = parts[0];
                    deuxiemeDefi = parts[1];
                }
            } else if (msg.startsWith("SCORE1:")) {
                scoreAdverse1 = Integer.parseInt(msg.substring(7));
                checkScores();
            } else if (msg.startsWith("SCORE2:")) {
                scoreAdverse2 = Integer.parseInt(msg.substring(7));
                checkScores();
            } else if (msg.equals("READY1_DONE") || msg.equals("READY2_DONE")) {
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
