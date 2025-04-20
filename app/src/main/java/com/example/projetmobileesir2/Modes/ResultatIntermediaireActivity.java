package com.example.projetmobileesir2.Modes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetmobileesir2.Defis.QuizChoixActivity;
import com.example.projetmobileesir2.Defis.DevineMotActivity;
import com.example.projetmobileesir2.R;

import java.util.Random;

public class ResultatIntermediaireActivity extends AppCompatActivity {

    private int scoreDefi1;
    private boolean isMultiplayer, isHost;

    /**
     * Récupération des données passées par l'Activity précédente
     * Récupèration du score du premier défi
     * Vérification si c'est un jeu multijoueur
     * Vérification si l'utilisateur est l'hôte
     * Lancement le deuxième défi après un délai
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultat_intermediaire);

        scoreDefi1 = getIntent().getIntExtra("scoreDefi1", 0);
        isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);
        isHost = getIntent().getBooleanExtra("isHost", false);

        TextView scoreText = findViewById(R.id.tvIntermediaireScore);
        scoreText.setText("Score du premier défi : " + scoreDefi1);

        TextView infoText = findViewById(R.id.tvSuite);
        infoText.setText("Préparation du deuxième défi...");

        new Handler().postDelayed(this::launchSecondDefi, 4000); // délai de 4 secondes
    }

    /**
     * Méthode pour lancer le deuxième défi
     * Choix aléatoire entre Quiz et DevineMot pour le deuxième défi
     * Passage des informations nécessaires à l'Activity du deuxième défi
     * Terminaison de l'Activity actuelle pour éviter qu'elle reste dans la pile des activités
     */
    private void launchSecondDefi() {
        Intent intent;

        // Choix aléatoire entre DevineMot et Quiz
        if (new Random().nextBoolean()) {
            intent = new Intent(this, QuizChoixActivity.class);// Si le choix est vrai, lancer le Quiz
        } else {
            intent = new Intent(this, DevineMotActivity.class); // Sinon, lancer l'activité DevineMot
        }

        intent.putExtra("scoreDefi1", scoreDefi1);// Passer le score du premier défi
        intent.putExtra("isMultiplayer", isMultiplayer);// Passer l'information sur le mode multijoueur

        intent.putExtra("isHost", isHost);// Passer l'information sur l'hôte
        startActivity(intent);
        finish();
    }
}
