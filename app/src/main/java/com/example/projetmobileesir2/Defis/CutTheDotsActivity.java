package com.example.projetmobileesir2.Defis;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.R;
import com.example.projetmobileesir2.ScoreDialogFragment;

public class CutTheDotsActivity extends AppCompatActivity {

    private TextView scoreText, timerText;
    private CutTheDotsView gameView;
    private int score = 0;
    private String mode;
    private MediaPlayer bgMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut_the_dots);

        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        mode = getIntent().getStringExtra("mode");

        // Musique de fond
        bgMusic = MediaPlayer.create(this, R.raw.fruit_ninja);
        bgMusic.setLooping(true);
        bgMusic.start();

        FrameLayout gameContainer = findViewById(R.id.cut_game_container);
        gameView = new CutTheDotsView(this, null, scoreText);
        gameContainer.addView(gameView);

        // Timer de 30 secondes
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Temps: " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                gameView.endGame();  // Stopper le jeu
                timerText.setText("Temps écoulé !");
                score = gameView.getScore();
                addScoreToTotal();

                // Musique de victoire
                MediaPlayer mediaPlayer = MediaPlayer.create(CutTheDotsActivity.this, R.raw.victory);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);

                // Afficher le score
                ScoreDialogFragment.newInstance(score, mode)
                        .show(getSupportFragmentManager(), "scoreDialog");
            }
        }.start();
    }

    private void addScoreToTotal() {
        int currentScore = getSharedPreferences("MyPrefs", MODE_PRIVATE).getInt("totalScore", 0);
        int newScore = currentScore + score;

        getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .edit()
                .putInt("totalScore", newScore)
                .apply();
    }

    // Gestion de la musique de fond
    @Override
    protected void onPause() {
        super.onPause();
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bgMusic != null && !bgMusic.isPlaying()) {
            bgMusic.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bgMusic != null) {
            bgMusic.release();
            bgMusic = null;
        }
    }
}
