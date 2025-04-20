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

    // Instance de l'Activity pour un accès global
    public static MultiplayerGameActivity instance;

    // Scores des défis pour l'utilisateur et l'adversaire
    private int scoreDefi1 = -1, scoreDefi2 = -1, scoreDefi3 = -1;
    private int scoreAdverse1 = -1, scoreAdverse2 = -1, scoreAdverse3 = -1;

    // Statut du joueur : hôte ou non, et variables de synchronisation pour les défis
    private boolean isHost, isReady = false, readySent = false;
    private boolean premierDefiFait = false;

    private String premierDefi = "", deuxiemeDefi = "", troisiemeDefi = "";

    private TextView multiStatusText, multiInstructionText;

    /**
     * Initialisation de l'instance de l'activité
     * Récupération des éléments de l'interface
     * Vérification si l'utilisateur est l'hôte ou non
     * Enregistrement du récepteur Bluetooth pour recevoir des messages
     * Si c'est l'hôte, choisir les défis aléatoires et envoyer un message Bluetooth
     * @param savedInstanceState
     */
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
            choisirDefisAleatoires(); // Choisir les défis à jouer
            BluetoothConnectionHolder.sendMessage("DEFIS:" + premierDefi + "," + deuxiemeDefi + "," + troisiemeDefi); // Envoie des défis à l'adversaire
            BluetoothConnectionHolder.sendMessage("START"); // Demande de démarrer le jeu
            lancerPremierDefi(); // Lance le premier défi
        } else {
            updateUI("Connexion réussie !", "En attente de l’hôte..."); // Si ce n'est pas l'hôte, afficher un message d'attente
        }
    }

    /**
     * Choisir des défis aléatoires pour le jeu
     */
    private void choisirDefisAleatoires() {
        premierDefi = new Random().nextBoolean() ? "SHAKE" : "GYRO";
        deuxiemeDefi = new Random().nextBoolean() ? "QUIZ" : "DEVINE";
        troisiemeDefi = new Random().nextBoolean() ? "SNAKE" : "FRUIT";
    }

    /**
     * Lancement du premier défi en fonction du défi sélectionné
     * Passage des informations nécessaires à l'activité (multijoueur et hôte)
     */
    private void lancerPremierDefi() {
        updateUI("1er défi en cours", "Bonne chance !");
        Intent i;
        // Si le premier défi est "SHAKE", lance l'activité correspondante
        if ("SHAKE".equals(premierDefi)) {
            i = new Intent(this, ShakeActivity.class);
        } else { // Sinon, lance l'activité "Gyroscope"
            i = new Intent(this, GyroscopeActivity.class);
        }


        i.putExtra("isMultiplayer", true);
        i.putExtra("isHost", isHost);
        startActivity(i); // Lance l'activité du défi
    }

    /**
     * Lancement du deuxième défi après le premier
     * Choisir l'activité en fonction du défi
     */
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

    /**
     * Lancement du troisième défi après les deux premiers
     * Choisir l'activité en fonction du défi
     */
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

    /**
     * Enregistrement du score local après un défi et envoie un message Bluetooth à l'adversaire
     * Vérification si tous les scores sont reçus et lance les étapes suivantes
     * @param score
     */
    public static void saveLocalScore(int score) {
        if (instance == null) return;

        // Enregistrement du score pour le premier défi
        if (!instance.premierDefiFait) {
            instance.scoreDefi1 = score;
            instance.premierDefiFait = true;
            BluetoothConnectionHolder.sendMessage("SCORE1:" + score);
            BluetoothConnectionHolder.sendMessage("READY1_DONE");
            instance.updateUI("1er défi terminé", "En attente de l'adversaire...");
        } else if (instance.scoreDefi2 == -1) {
            // Enregistrement du score pour le deuxième défi
            instance.scoreDefi2 = score;
            BluetoothConnectionHolder.sendMessage("SCORE2:" + score);
            BluetoothConnectionHolder.sendMessage("READY2_DONE");
            instance.updateUI("2e défi terminé", "En attente du score adverse...");
        } else {
            // Enregistrement du score pour le troisième défi
            instance.scoreDefi3 = score;
            BluetoothConnectionHolder.sendMessage("SCORE3:" + score);
            BluetoothConnectionHolder.sendMessage("READY3_DONE");
            instance.updateUI("3e défi terminé", "Calcul du score final...");
        }

        instance.readySent = true;
        instance.checkScores();
    }

    /**
     * Vérification si tous les scores ont été reçus et lance les défis suivants
     * Si tous les défis sont terminés, on calcule les scores totaux et on affiche les résultats
     */
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

        // Fin du jeu : afficher les résultats et retour à l'écran des résultats
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

    /**
     * Màj l'interface utilisateur avec les messages de statut et d'instruction
     * @param status
     * @param instruction
     */
    private void updateUI(String status, String instruction) {
        runOnUiThread(() -> {
            if (multiStatusText != null) multiStatusText.setText(status);
            if (multiInstructionText != null) multiInstructionText.setText(instruction);
        });
    }

    /**
     * Récepteur Bluetooth qui reçoit les messages de l'adversaire
     */
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

    /**
     * Désenregistrement du récepteur Bluetooth lorsque l'activité est détruite
     * Libération de l'instance de l'activité
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btReceiver);
        instance = null;
    }
}