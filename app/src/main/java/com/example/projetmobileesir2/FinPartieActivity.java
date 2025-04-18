package com.example.projetmobileesir2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FinPartieActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_partie);

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        TextView defisTextView = findViewById(R.id.defisTextView);
        Button rejouerButton = findViewById(R.id.btnRejouer);
        Button quitterButton = findViewById(R.id.btnQuitter);

        // Récupérer les extras
        int totalScore = getIntent().getIntExtra("totalScore", 0);
        ArrayList<String> defisJoues = getIntent().getStringArrayListExtra("defisJoues");

        // 🎵 Musique de victoire
        mediaPlayer = MediaPlayer.create(this, R.raw.fin_partie);
        mediaPlayer.start();

        // 🎯 Afficher les infos
        scoreTextView.setText("Score final : " + totalScore);
        StringBuilder defisTexte = new StringBuilder("Défis joués :\n");
        for (String defi : defisJoues) {
            defisTexte.append("• ").append(defi).append("\n");
        }
        defisTextView.setText(defisTexte.toString());

        // 🔁 Rejouer
        rejouerButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putInt("totalScore", 0)
                    .putInt("nbDefisJoues", 0)
                    .putString("defisJoues", "")
                    .apply();

            startActivity(new Intent(this, SelectionDefiActivity.class));
            finish();
        });

        // ❌ Quitter
        quitterButton.setOnClickListener(v -> {
            // Réinitialisation du score total à zéro au moment où l'utilisateur quitte
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putInt("totalScore", 0)  // Remise à zéro du score total
                    .putInt("nbDefisJoues", 0)  // Réinitialisation du nombre de défis
                    .putString("defisJoues", "")  // Réinitialisation des défis joués
                    .apply();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
