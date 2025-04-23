package com.example.projetmobileesir2.Defis;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetmobileesir2.Modes.MultiplayerGame.MultiplayerGameActivity;
import com.example.projetmobileesir2.R;
import com.example.projetmobileesir2.Modes.ScoreSoloTrainingFragment;
import android.content.SharedPreferences;

/**
 * activité du défi Snake jouable en solo ou multijoueur
 * affiche le score le temps restant et gère la fin de partie
 */

public class SnakeActivity extends AppCompatActivity {

    private TextView scoreText, timeLeftText;
    private FrameLayout container;
    private SnakeView snakeView;
    private String mode;
    private int finalScore = 0;

    private boolean isMultiplayer;
    private long timeLeftMillis = 30000;
    private long timerStartedAt;
    private long pauseTime = 0;
    private boolean isPaused = false;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake_defi);

        scoreText = findViewById(R.id.scoreText);
        timeLeftText = findViewById(R.id.timeLeftText);
        container = findViewById(R.id.snake_game_container);

        mode = getIntent().getStringExtra("mode");
        isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);

        snakeView = new SnakeView(this, scoreText);
        snakeView.setTimeLeftText(timeLeftText);
        container.addView(snakeView);

        snakeView.setOnGameOverListener(score -> {
            finalScore = score;
            addScoreToTotal(score);
            playVictorySound();
            showScorePopup();
        });

    }


    private void showScorePopup() {
        if (isMultiplayer) {
            MultiplayerGameActivity.saveLocalScore(finalScore);
            finish();
        } else {
            ScoreSoloTrainingFragment.newInstance(finalScore, mode)
                    .show(getSupportFragmentManager(), "scoreDialog");
        }
    }

    private void addScoreToTotal(int score) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int totalScore = prefs.getInt("totalScore", 0);
        prefs.edit().putInt("totalScore", totalScore + score).apply();
    }

    private void playVictorySound() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.victory);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            pauseTime = SystemClock.elapsedRealtime();
            isPaused = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPaused) {
            long delta = SystemClock.elapsedRealtime() - pauseTime;
            timeLeftMillis = Math.max(0, timeLeftMillis - delta);
            isPaused = false;
        }
    }
}
