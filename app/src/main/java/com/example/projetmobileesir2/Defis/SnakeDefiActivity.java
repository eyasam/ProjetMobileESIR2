package com.example.projetmobileesir2.Defis;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.media.MediaPlayer;


import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.Modes.TrainingActivity;
import com.example.projetmobileesir2.R;
import com.example.projetmobileesir2.ScoreDialogFragment;
import com.example.projetmobileesir2.SelectionDefiActivity;

import android.content.SharedPreferences;

public class SnakeDefiActivity extends AppCompatActivity {

    private TextView scoreText;
    private SnakeView snakeView;
    private TextView timeLeftText;
    private String mode;


    private int finalScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake_defi);

        scoreText = findViewById(R.id.scoreText);
        timeLeftText = findViewById(R.id.timeLeftText);
        mode = getIntent().getStringExtra("mode");

        FrameLayout container = findViewById(R.id.snake_game_container);
        snakeView = new SnakeView(this, scoreText);
        container.addView(snakeView);
        snakeView.setTimeLeftText(timeLeftText);

        // Ajouter un listener pour vérifier si le jeu est terminé
        snakeView.setOnGameOverListener(score -> {
            finalScore = score;
            addScoreToTotal(finalScore);

            // Lancer la musique de victoire
            MediaPlayer mediaPlayer = MediaPlayer.create(SnakeDefiActivity.this, R.raw.victory);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);

            // Afficher le popup de fin
            ScoreDialogFragment.newInstance(finalScore, mode)
                    .show(getSupportFragmentManager(), "scoreDialog");

        });
    }

    // Ajouter le score actuel au score total dans SharedPreferences
    private void addScoreToTotal(int score) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int totalScore = prefs.getInt("totalScore", 0);
        prefs.edit().putInt("totalScore", totalScore + score).apply();
    }


}
