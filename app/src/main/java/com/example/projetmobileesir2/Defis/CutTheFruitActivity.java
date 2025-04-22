package com.example.projetmobileesir2.Defis;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.*;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.Modes.MultiplayerGameActivity;
import com.example.projetmobileesir2.R;
import com.example.projetmobileesir2.ScoreDialogFragment;

public class CutTheFruitActivity extends AppCompatActivity {

    private FrameLayout gameContainer;
    private TextView tvScore, tvTimer;
    private int score = 0;
    private String mode;
    private boolean isMultiplayer;
    private CountDownTimer timer;
    private long timeLeftMillis = 30000;
    private long pauseTime = 0;
    private boolean isPaused = false;

    private CutTheFruitView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut_the_fruit);

        gameContainer = findViewById(R.id.cutFruitContainer);
        tvScore = findViewById(R.id.cutFruitScore);
        tvTimer = findViewById(R.id.cutFruitTimer);

        mode = getIntent().getStringExtra("mode");
        isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);

        gameView = new CutTheFruitView(this, score -> {
            this.score = score;
            showFinalScore();
        }, tvScore);

        gameView.setTimerText(tvTimer);
        gameContainer.addView(gameView);

        startTimer(timeLeftMillis);
    }

    private void startTimer(long millis) {
        timer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                tvTimer.setText("Temps : " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                gameView.endGame();
            }
        }.start();
    }

    private void showFinalScore() {
        addScore(score);
        playSound(R.raw.victory);

        if (isMultiplayer) {
            MultiplayerGameActivity.saveLocalScore(score);
            finish();
        } else {
            ScoreDialogFragment.newInstance(score, mode)
                    .show(getSupportFragmentManager(), "scoreDialog");
        }
    }

    private void addScore(int s) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int previous = prefs.getInt("totalScore", 0);
        prefs.edit().putInt("totalScore", previous + s).apply();
    }

    private void playSound(int resId) {
        MediaPlayer mp = MediaPlayer.create(this, resId);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) timer.cancel();
        pauseTime = SystemClock.elapsedRealtime();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPaused) {
            long delta = SystemClock.elapsedRealtime() - pauseTime;
            timeLeftMillis = Math.max(0, timeLeftMillis - delta);
            isPaused = false;
            startTimer(timeLeftMillis);
        }
    }
}