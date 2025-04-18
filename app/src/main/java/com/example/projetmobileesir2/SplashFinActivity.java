package com.example.projetmobileesir2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SplashFinActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3500;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Réutilise ton layout existant

        // Jouer le son de fin (victoire)
        mediaPlayer = MediaPlayer.create(this, R.raw.fin_partie); // mets victoire.mp3 ou .wav dans res/raw
        mediaPlayer.start();

        // Récupère les données
        Intent prevIntent = getIntent();
        int totalScore = prevIntent.getIntExtra("totalScore", 0);
        ArrayList<String> defisJoues = prevIntent.getStringArrayListExtra("defisJoues");

        // Attendre l'animation puis passer à l'écran final
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashFinActivity.this, FinPartieActivity.class);
            intent.putExtra("totalScore", totalScore);
            intent.putStringArrayListExtra("defisJoues", defisJoues);

            mediaPlayer.stop();
            mediaPlayer.release();

            startActivity(intent);
            finish();
        }, SPLASH_TIME_OUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
}
