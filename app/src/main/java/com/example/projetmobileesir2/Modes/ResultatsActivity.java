package com.example.projetmobileesir2.Modes;

import android.content.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.projetmobileesir2.R;

/**
 * Activité qui affiche les résultats de la partie (en solo ou en multijoueur).
 * Elle attend un score adverse en cas de multijoueur, et déclenche un son de victoire ou défaite.
 */
public class ResultatsActivity extends AppCompatActivity {

    private TextView tvScore, tvMessage;
    private int scoreLocal, scoreAdverse = -1;
    private boolean resultatAffiche = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultats);

        tvScore = findViewById(R.id.tvScore);
        tvMessage = findViewById(R.id.tvMessage);

        scoreLocal = getIntent().getIntExtra("scoreLocal", 0);
        scoreAdverse = getIntent().getIntExtra("scoreAdverse", -1);

        if (scoreAdverse == -1) {
            tvScore.setText("Ton score : " + scoreLocal + "\nEn attente du score adverse...");
        } else {
            afficherResultat();
        }
    }


    /**
     * Enregistre le récepteur Bluetooth à la reprise de l’activité.
     */
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("BLUETOOTH_MESSAGE"));
    }


    /**
     * Supprime l’écouteur Bluetooth pour éviter les fuites mémoire.
     */
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }


    /**
     * Récepteur Bluetooth pour capter un score reçu depuis l’autre joueur.
     * Ne déclenche `afficherResultat()` qu'une seule fois.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message != null && message.startsWith("SCORE:") && !resultatAffiche) {
                try {
                    scoreAdverse = Integer.parseInt(message.split(":")[1]);
                    afficherResultat();
                } catch (Exception e) {
                    Log.e("APPPP_BT_SCORE", "Erreur parsing : " + message);
                }
            }
        }
    };

    private void afficherResultat() {
        resultatAffiche = true;

        String msg = "🤝 Égalité !";
        int son = 0;

        if (scoreLocal > scoreAdverse) {
            msg = "🎉 Tu as gagné !";
            son = R.raw.victory;
        } else if (scoreLocal < scoreAdverse) {
            msg = "😢 Tu as perdu...";
            son = R.raw.defeat;
        }

        tvScore.setText("Toi : " + scoreLocal + "\nAdversaire : " + scoreAdverse);
        tvMessage.setText(msg);

        if (son != 0) playSound(son);
    }

    private void playSound(int resId) {
        MediaPlayer mp = MediaPlayer.create(this, resId);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }
}
