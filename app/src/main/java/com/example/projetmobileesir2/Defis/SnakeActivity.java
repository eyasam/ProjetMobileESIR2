package com.example.projetmobileesir2.Defis;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.media.MediaPlayer;


import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.Modes.MultiplayerGameActivity;
import com.example.projetmobileesir2.R;
import com.example.projetmobileesir2.ScoreDialogFragment;

import android.content.SharedPreferences;
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

    /**
     * Charge le layout XML
     * Récupère les données envoyées via l'intent
     * Instancie la vue Snake, lui passe la référence vers le TextView score
     * Quand le jeu se termine (collision ou temps écoulé), cette callback est déclenchée avec le setOnGameOverListener
     * @param savedInstanceState
     */
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
        snakeView.setTimeLeftText(timeLeftText);// Lie le TextView du timer
        container.addView(snakeView); // Ajoute le SnakeView au conteneur

        snakeView.setOnGameOverListener(score -> {
            finalScore = score;
            addScoreToTotal(score); // Ajoute le score au total sauvegardé
            playVictorySound(); // Joue un son de victoire
            showScorePopup(); // Affiche un popup avec le score
        });

        //startTimer(timeLeftMillis);
    }

    /*private void startTimer(long millis) {
        timerStartedAt = SystemClock.elapsedRealtime();

        timer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                timeLeftText.setText("Temps: " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                timeLeftMillis = 0;
                snakeView.restartGame(); // en cas de besoin
            }
        }.start();
    }


     */

    /**
     * Affichage du popup de score en mode solo ou entrainement, ou envoie le score au gestionnaire multijoueur (eregistrer localement)
     * et on ferme l'activité
     */
    private void showScorePopup() {
        if (isMultiplayer) {
            MultiplayerGameActivity.saveLocalScore(finalScore);
            finish();
        } else {
            ScoreDialogFragment.newInstance(finalScore, mode)
                    .show(getSupportFragmentManager(), "scoreDialog");
        }
    }


    /**
     * Màj du score total stocké localement dans les sharedPreferences pour calculer le score total pour le mode solo
     * @param score
     */
    private void addScoreToTotal(int score) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int totalScore = prefs.getInt("totalScore", 0);
        prefs.edit().putInt("totalScore", totalScore + score).apply();
    }

    /**
     * Joue un son de victoire à la fin de la partie
     */
    private void playVictorySound() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.victory);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }

    /**
     * Gère la mise en pause de l’activité
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            pauseTime = SystemClock.elapsedRealtime();
            isPaused = true;
        }
    }

    /**
     * Gère la reprise de l’activité après une pause
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isPaused) {
            long delta = SystemClock.elapsedRealtime() - pauseTime;
            timeLeftMillis = Math.max(0, timeLeftMillis - delta);
            isPaused = false;
            //startTimer(timeLeftMillis);
        }
    }
}
