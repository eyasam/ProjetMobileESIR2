package com.example.projetmobileesir2.Defis;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.Modes.MultiplayerGameActivity;
import com.example.projetmobileesir2.Modes.ResultatsActivity;
import com.example.projetmobileesir2.R;

import java.util.List;

public class DefiDevineMotActivity extends AppCompatActivity {

    private TextView tvTimer, tvScore, tvEnigme;
    private EditText etReponse;
    private Button btnValider;

    private int score = 0, currentIndex = 0;
    private boolean partieTerminee = false;
    private boolean isMultiplayer;

    private long totalTimeMillis = 20000;
    private long timeLeftMillis = totalTimeMillis;
    private long timerStartedAt = 0;

    private CountDownTimer timer;

    private final List<MotIndice> questions = List.of(
            new MotIndice("Je suis jaune, courbé, et on me mange", "banane"),
            new MotIndice("Je brille la nuit dans le ciel", "lune"),
            new MotIndice("Je suis le roi des animaux", "lion"),
            new MotIndice("J’ai des touches et fais de la musique", "piano"),
            new MotIndice("J’ai quatre roues et un moteur", "voiture")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deviner_le_mot);

        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvEnigme = findViewById(R.id.tvEnigme);
        etReponse = findViewById(R.id.etRéponse);
        btnValider = findViewById(R.id.btnValider);

        isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);

        afficherEnigme();

        btnValider.setOnClickListener(v -> {
            if (partieTerminee) return;

            String input = etReponse.getText().toString().trim().toLowerCase();
            boolean correct = input.equals(questions.get(currentIndex).reponse.toLowerCase());

            if (correct) {
                score++;
                tvScore.setText("Score : " + score);
                if (++currentIndex < questions.size()) {
                    afficherEnigme();
                } else {
                    terminerPartie();
                }
            }

            etReponse.setText("");
            int color = getResources().getColor(correct ? android.R.color.holo_green_light : android.R.color.holo_red_light);
            etReponse.setBackgroundColor(color);
            etReponse.postDelayed(() ->
                    etReponse.setBackgroundColor(getResources().getColor(android.R.color.transparent)), 600);
        });
    }

    private void afficherEnigme() {
        tvEnigme.setText("Indice : " + questions.get(currentIndex).indice);
    }

    private void startTimer(long millis) {
        timerStartedAt = System.currentTimeMillis();
        timer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                tvTimer.setText((millisUntilFinished / 1000) + "s");
            }

            public void onFinish() {
                timeLeftMillis = 0;
                tvTimer.setText("0s");
                terminerPartie();
            }
        };
        timer.start();
    }

    private void terminerPartie() {
        partieTerminee = true;
        if (timer != null) timer.cancel();
        tvEnigme.setText("Partie terminée !");
        tvScore.setText("Score final : " + score);
        tvTimer.setText("0s");
        etReponse.setEnabled(false);
        btnValider.setEnabled(false);

        if (isMultiplayer) {
            MultiplayerGameActivity.saveLocalScore(score);
        } else {
            Intent intent = new Intent(this, ResultatsActivity.class);
            intent.putExtra("scoreLocal", score);
            startActivity(intent);
        }

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!partieTerminee) {
            startTimer(timeLeftMillis);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            // met à jour timeLeftMillis en fonction du temps réellement passé
            long elapsed = System.currentTimeMillis() - timerStartedAt;
            timeLeftMillis = Math.max(0, timeLeftMillis - elapsed);
        }
    }

    static class MotIndice {
        final String indice, reponse;
        MotIndice(String i, String r) {
            indice = i;
            reponse = r;
        }
    }
}
