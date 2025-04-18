package com.example.projetmobileesir2.Modes;

import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.projetmobileesir2.Bluetooth.BluetoothConnectionHolder;
import com.example.projetmobileesir2.R;

/**
 * Activité qui gère le mode multijoueur.
 * Elle coordonne l’envoi/réception des scores entre les deux joueurs via Bluetooth
 * et affiche les statuts en temps réel avant de rediriger vers l’écran de résultats.
 */
public class MultiplayerGameActivity extends AppCompatActivity {

    public static MultiplayerGameActivity instance;

    private int scoreLocal = -1, scoreAdverse = -1;
    private boolean isHost, isReady = false, readySent = false;
    private Integer pendingScore = null;

    private TextView multiStatusText, multiInstructionText;

    /**
     * Initialisation de l’activité, configuration du mode hôte/client
     * et enregistrement du receveur de messages Bluetooth.
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
            updateUI("Connexion réussie !", "Démarrage du défi...");
            BluetoothConnectionHolder.sendMessage("START");
            lancerDefi();
        } else {
            updateUI("Connexion réussie !", "En attente de l’hôte...");
        }
    }


    private void lancerDefi() {
        updateUI("Défi en cours", "Bonne chance !");
        Intent i = new Intent(this, com.example.projetmobileesir2.Defis.ShakeActivity.class);
        i.putExtra("isMultiplayer", true);
        i.putExtra("isHost", isHost);
        startActivity(i);
    }

    /**
     * Méthode statique appelée depuis l’activité de défi
     * pour envoyer le score local à l’autre joueur.
     */
    public static void saveLocalScore(int score) {
        if (instance == null) return;

        instance.scoreLocal = score;
        BluetoothConnectionHolder.sendMessage("SCORE:" + score);
        BluetoothConnectionHolder.sendMessage("READY_DONE");

        instance.readySent = true;
        instance.updateUI("Défi terminé !", "En attente du score adverse...");
        instance.checkScores();
    }

    /**
     * Vérifie si les deux scores sont reçus et passe à l’écran de résultats.
     */
    private void checkScores() {
        Log.d("APPPP_CHECK", "Scores : local=" + scoreLocal + " / adverse=" + scoreAdverse);
        if (scoreLocal != -1 && scoreAdverse != -1) {
            Log.d("APPPP_CHECK", "Les deux scores sont prêts ! Passage à l’écran de résultats.");
            Intent i = new Intent(this, ResultatsActivity.class);
            i.putExtra("scoreLocal", scoreLocal);
            i.putExtra("scoreAdverse", scoreAdverse);
            startActivity(i);
            finish();
        }
    }


    /**
     * Met à jour dynamiquement le texte de statut à l’écran.
     */
    private void updateUI(String status, String instruction) {
        runOnUiThread(() -> {
            if (multiStatusText != null) multiStatusText.setText(status);
            if (multiInstructionText != null) multiInstructionText.setText(instruction);
        });
    }

    /**
     * Réception des messages Bluetooth.
     * Traite les messages de type SCORE, READY_DONE ou START.
     */
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("message");
            if (msg == null) return;

            Log.d("APPPP_BT_RECEIVE", "Reçu : " + msg);

            if (msg.startsWith("SCORE:")) {
                try {
                    int score = Integer.parseInt(msg.substring(6));
                    Log.d("APPPP_SCORE", "Score adverse reçu : " + score);
                    scoreAdverse = score;

                    if (isReady || readySent) {
                        checkScores();
                    } else {
                        pendingScore = score;
                    }
                } catch (Exception e) {
                    Log.e("APPPP_SCORE", "Erreur parsing : " + msg);
                }

            } else if (msg.equals("READY_DONE")) {
                Log.d("APPPP_SYNC", "READY_DONE reçu");
                isReady = true;

                if (pendingScore != null) {
                    scoreAdverse = pendingScore;
                    pendingScore = null;
                }

                checkScores();

            } else if (msg.equals("START")) {
                lancerDefi();
            }
        }
    };

    /**
     * Nettoyage à la fermeture de l’activité.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btReceiver);
        instance = null;
    }
}
