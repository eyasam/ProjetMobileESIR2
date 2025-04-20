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

    // Vue du jeu implémenté dans le View
    private CutTheFruitView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut_the_fruit);

        //Initialiser les vues
        gameContainer = findViewById(R.id.cutFruitContainer);
        tvScore = findViewById(R.id.cutFruitScore);
        tvTimer = findViewById(R.id.cutFruitTimer);

        // On récupère les données depuis l'Intent
        mode = getIntent().getStringExtra("mode");
        isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);

        // On crée et on ajoute la vue personnalisée du défi
        gameView = new CutTheFruitView(this, score -> {
            this.score = score;
            showFinalScore(); //on affiche le score final de la partie
        }, tvScore);

        // On passe la TextView timer à la vue pour màj directe
        gameView.setTimerText(tvTimer);
        gameContainer.addView(gameView);

        // On démarre le compte à rebours
        startTimer(timeLeftMillis);
    }

    /**
     * Démarre le timer du jeu.
     */
    private void startTimer(long millis) {
        timer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                tvTimer.setText("Temps : " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                gameView.endGame(); // On appelle la fin du jeu
            }
        }.start();
    }

    /**
     * Cette méthode affiche le score final et gère la fin de partie.
     * en mettant à jour le score total pour le stocker dans les shared Preferences, en enregistrant le score localement
     * si c'est en mode Multijoueur, et elle affiche une boite de dialogue avec le score actuelle de la partie ternminé si c'est en mode Solo ou entrainement
     */
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

    /**
     * Ajoute le score courant au score total stocké dans les SharedPreferences pour calculer le score total de la Partie complètes des trois défis .
     */
    private void addScore(int s) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int previous = prefs.getInt("totalScore", 0);
        prefs.edit().putInt("totalScore", previous + s).apply();
    }


    /**
     * Joue le son de victoire via MediaPlayer.
     */
    private void playSound(int resId) {
        MediaPlayer mp = MediaPlayer.create(this, resId);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }

    /**
     * Cette méthode gère la pause du jeu : arrête le timer et mémorise l'heure de pause.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) timer.cancel();
        pauseTime = SystemClock.elapsedRealtime();
        isPaused = true;
    }

    /**
     * Cette méthode reprend le jeu après une pause : ajuste le timer et le relance.
     */
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
