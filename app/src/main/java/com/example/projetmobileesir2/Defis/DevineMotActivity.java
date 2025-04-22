package com.example.projetmobileesir2.Defis;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.Modes.MultiplayerGameActivity;
import com.example.projetmobileesir2.R;
import com.example.projetmobileesir2.ScoreDialogFragment;

import java.util.List;

public class DevineMotActivity extends AppCompatActivity {

    private TextView tvTimer, tvScore, tvEnigme;
    private EditText etReponse;
    private Button btnValider;

    private int score = 0, currentIndex = 0;
    private boolean partieTerminee = false;
    private boolean isMultiplayer;

    private long totalTimeMillis = 20000;
    private long timeLeftMillis = totalTimeMillis;
    private long timerStartedAt = 0;

    private boolean isGameEnded = false;
    private  String mode; //utiliser pour identifier quel mode de jeu on est solo ou entrainement

    private CountDownTimer timer;

    // === Liste fixe des énigmes à deviner ===
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

        mode = getIntent().getStringExtra("mode");

        // Affiche la première énigme
        afficherEnigme();

        btnValider.setOnClickListener(v -> {
            if (partieTerminee) return;

            String input = etReponse.getText().toString().trim().toLowerCase();
            boolean correct = input.equals(questions.get(currentIndex).reponse.toLowerCase());

            if (correct) {
                score++;
                tvScore.setText("Score : " + score);

                // Passe à l’énigme suivante s’il en reste, sinon fin de partie
                if (++currentIndex < questions.size()) {
                    afficherEnigme();
                } else {
                    terminerPartie();
                }
            }

            // Réinitialise l'input + effet de couleur (vert si bon, rouge sinon)
            etReponse.setText("");
            int color = getResources().getColor(correct ? android.R.color.holo_green_light : android.R.color.holo_red_light);
            etReponse.setBackgroundColor(color);

            // Efface la couleur après 600ms
            etReponse.postDelayed(() ->
                    etReponse.setBackgroundColor(getResources().getColor(android.R.color.transparent)), 600);
        });
    }

    /**
     * Cette méthode affiche l’énigme actuelle à partir de l’index courant.
     */
    private void afficherEnigme() {
        tvEnigme.setText("Indice : " + questions.get(currentIndex).indice);
    }

    /**
     * Démarre un compte à rebours visuel et logique.
     * Màj l’affichage chaque seconde.
     */
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

    /**
     * Cette méthode est appelée quand la partie est terminée :
     * - Arrête le timer
     * - Enregistre le score
     * - Désactive les interactions
     * - Lance la suite selon le mode de jeu
     */

    private void terminerPartie() {
        partieTerminee = true;
        if (timer != null) timer.cancel();
        //tvEnigme.setText("Partie terminée !");
        //tvScore.setText("Score final : " + score);
        //tvTimer.setText("0s");

        // Ajoute le score à la mémoire locale
        SharedPreferences totalPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int previousScore = totalPrefs.getInt("totalScore", 0);
        totalPrefs.edit().putInt("totalScore", previousScore + score).apply(); // Utiliser 'score' ici

        playSound(R.raw.victory);

        etReponse.setEnabled(false);
        btnValider.setEnabled(false);

        // Multijoueur : on revient à la partie principale avec le score
        if (isMultiplayer) {
            MultiplayerGameActivity.saveLocalScore(score);
            finish();
        } else {
            /*Intent intent = new Intent(this, ResultatsActivity.class);
            intent.putExtra("scoreLocal", score);
            startActivity(intent);

             */
            // Mode solo ou entrainement : on affiche une boîte de score
            ScoreDialogFragment.newInstance(score, mode)
                    .show(getSupportFragmentManager(), "scoreDialog");

        }

    }

    /**
     * Cette méthode pour jouer un son de victoire .
     * Le MediaPlayer est libéré à la fin du son.
     */
    private void playSound(int resId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }


    /**
     * Quand l’activité reprend , relance le timer si nécessaire.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!partieTerminee) {
            startTimer(timeLeftMillis);
        }
    }

    /**
     * À la mise en pause, le timer s'arrête.
     * On calcule combien de temps s’est écoulé pour le reprendre plus tard.
     */
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

    /**
     * Petite classe interne pour représenter une énigme :
     * une phrase ("indice") et sa réponse attendue.
     */

    static class MotIndice {
        final String indice, reponse;
        MotIndice(String i, String r) {
            indice = i;
            reponse = r;
        }
    }
}
