package com.example.projetmobileesir2.Modes;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetmobileesir2.R;

/**
 * Activité qui affiche les résultats finaux
 * Elle compare les scores et joue un son de victoire ou de défaite.
 */
public class ResultatsActivity extends AppCompatActivity {

    private TextView tvScore, tvMessage;
    private int scoreLocal, scoreAdverse;

    /**
     * Initialisation des éléments de l'interface
     * Récupération des scores passés dans l'Intent
     * Appel de la méthode pour afficher le résultat
     * @param savedInstanceState
     */
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
     * Méthode pour afficher le résultat du jeu
     * En comparant les scores pour déterminer le message et le son à jouer
     */
    private void afficherResultat() {
        String msg = "🤝 Égalité !"; // Message par défaut (égalité)
        int son = 0; // Valeur par défaut pour le son (aucun son joué)

        if (scoreLocal > scoreAdverse) {
            msg = "🎉 Tu as gagné !";
            son = R.raw.victory;
        } else if (scoreLocal < scoreAdverse) {
            msg = "😢 Tu as perdu...";
            son = R.raw.defeat;
        }

        tvScore.setText("Ton score total : " + scoreLocal + "\nScore adverse : " + scoreAdverse);
        tvMessage.setText(msg);

        // Si un son est à jouer, on appelle la méthode playSound
        if (son != 0) playSound(son);
    }

    /**
     * Méthode pour jouer un son
     * @param resId
     */
    private void playSound(int resId) {
        MediaPlayer mp = MediaPlayer.create(this, resId);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }
}
