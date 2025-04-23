package com.example.projetmobileesir2.Modes.MultiplayerGame;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetmobileesir2.R;

/**
 * affiche le résultat final d'une partie multijoueur
 * compare les scores et joue un son selon le résultat
 */

public class ResultatsMultiplayerGameActivity extends AppCompatActivity {

    private TextView tvScore, tvMessage;
    private int scoreLocal, scoreAdverse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultats);

        tvScore = findViewById(R.id.tvScore);
        tvMessage = findViewById(R.id.tvMessage);

        scoreLocal = getIntent().getIntExtra("scoreLocal", 0);
        scoreAdverse = getIntent().getIntExtra("scoreAdverse", 0);

        afficherResultat();
    }

    /**
     * afficher le résultat du jeu en comparant les scores pour déterminer le message et le son à jouer
     */
    private void afficherResultat() {
        String msg = "🤝 Égalité !";
        int son = 0;

        if (scoreLocal > scoreAdverse) {
            msg = "🎉 Tu as gagné !";
            son = R.raw.victory;
        } else if (scoreLocal < scoreAdverse) {
            msg = "😢 Tu as perdu...";
            son = R.raw.defeat;
        }

        tvScore.setText("Ton score total : " + scoreLocal + "\nScore adverse : " + scoreAdverse);
        tvMessage.setText(msg);

        if (son != 0) playSound(son);
    }

    private void playSound(int resId) {
        MediaPlayer mp = MediaPlayer.create(this, resId);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }
}
