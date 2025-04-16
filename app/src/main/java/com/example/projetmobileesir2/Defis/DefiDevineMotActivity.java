package com.example.projetmobileesir2.Defis;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.R;

import java.util.ArrayList;
import java.util.List;

public class DefiDevineMotActivity extends AppCompatActivity {

    private TextView tvTimer, tvScore, tvEnigme;
    private EditText etReponse;
    private Button btnValider;

    private int score = 0;
    private int currentIndex = 0;
    private boolean partieTerminee = false;
    private CountDownTimer timer;

    private List<MotIndice> listeQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deviner_le_mot);

        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvEnigme = findViewById(R.id.tvEnigme);
        etReponse = findViewById(R.id.etRéponse);
        btnValider = findViewById(R.id.btnValider);

        initQuestions();
        afficherEnigme();
        startTimer();

        btnValider.setOnClickListener(v -> {
            if (partieTerminee) return;

            String reponse = etReponse.getText().toString().trim().toLowerCase();
            String bonneReponse = listeQuestions.get(currentIndex).reponse.toLowerCase();

            if (reponse.equals(bonneReponse)) {
                score++;
                tvScore.setText("Score : " + score);
                etReponse.setText("");

                // ✅ Fond vert temporaire
                etReponse.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                etReponse.postDelayed(() ->
                        etReponse.setBackgroundColor(getResources().getColor(android.R.color.transparent)), 600);

                passerQuestionSuivante();
            } else {
                etReponse.setText("");

                // ❌ Fond rouge temporaire
                etReponse.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                etReponse.postDelayed(() ->
                        etReponse.setBackgroundColor(getResources().getColor(android.R.color.transparent)), 600);
            }
        });


    }

    private void initQuestions() {
        listeQuestions = new ArrayList<>();
        listeQuestions.add(new MotIndice("Je suis jaune, courbé, et on me mange", "banane"));
        listeQuestions.add(new MotIndice("Je brille la nuit dans le ciel", "lune"));
        listeQuestions.add(new MotIndice("Je suis le roi des animaux", "lion"));
        listeQuestions.add(new MotIndice("J’ai des touches et fais de la musique", "piano"));
        listeQuestions.add(new MotIndice("J’ai quatre roues et un moteur", "voiture"));
    }

    private void passerQuestionSuivante() {
        currentIndex++;
        if (currentIndex < listeQuestions.size()) {
            afficherEnigme();
        } else {
            partieTerminee = true;
            tvEnigme.setText("Partie terminée !");
            etReponse.setEnabled(false);
            btnValider.setEnabled(false);
            playVictorySound();
        }
    }


    private void afficherEnigme() {
        tvEnigme.setText("Indice : " + listeQuestions.get(currentIndex).indice);
    }

    private void startTimer() {
        timer = new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText((millisUntilFinished / 1000) + "s");
            }

            public void onFinish() {
                partieTerminee = true;
                tvTimer.setText("0s");

                tvEnigme.setText("Partie terminée !");
                tvScore.setText("Score final : " + score);

                etReponse.setEnabled(false);
                btnValider.setEnabled(false);

                playVictorySound();
            }

        };
        timer.start();
    }

    private void playVictorySound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.victory);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) timer.cancel();
    }

    static class MotIndice {
        String indice;
        String reponse;

        MotIndice(String indice, String reponse) {
            this.indice = indice;
            this.reponse = reponse;
        }
    }
}
