package com.example.projetmobileesir2.Modes.SoloGame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.MainActivity;
import com.example.projetmobileesir2.R;

import java.util.ArrayList;

/**
 * affiche le score final et les défis joués en fin de partie solo
 * */

public class ResultatsSoloGameActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_partie);

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        TextView defisTextView = findViewById(R.id.defisTextView);
        Button rejouerButton = findViewById(R.id.btnRejouer);
        Button quitterButton = findViewById(R.id.btnQuitter);

        int totalScore = getIntent().getIntExtra("totalScore", 0);
        ArrayList<String> defisJoues = getIntent().getStringArrayListExtra("defisJoues");

        mediaPlayer = MediaPlayer.create(this, R.raw.fin_partie);
        mediaPlayer.start();

        scoreTextView.setText("Score final : " + totalScore);
        StringBuilder defisTexte = new StringBuilder("Défis joués :\n");
        for (String defi : defisJoues) {
            defisTexte.append("• ").append(defi).append("\n");
        }
        defisTextView.setText(defisTexte.toString());

        rejouerButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putInt("totalScore", 0)
                    .putInt("nbDefisJoues", 0)
                    .putString("defisJoues", "")
                    .apply();

            startActivity(new Intent(this, SoloGameActivity.class));
            finish();
        });

        quitterButton.setOnClickListener(v -> {

            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putInt("totalScore", 0)
                    .putInt("nbDefisJoues", 0)
                    .putString("defisJoues", "")
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
