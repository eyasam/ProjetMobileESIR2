package com.example.projetmobileesir2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.Defis.CutTheFruitActivity;
import com.example.projetmobileesir2.Defis.ShakeActivity;
import com.example.projetmobileesir2.Defis.DevineMotActivity;
import com.example.projetmobileesir2.Defis.GyroscopeActivity;
import com.example.projetmobileesir2.Defis.QuizChoixActivity;
import com.example.projetmobileesir2.Defis.SnakeActivity;
import com.example.projetmobileesir2.Modes.DefiAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SelectionDefiActivity extends AppCompatActivity {

    private ListView defiListView;
    private TextView scoreTextView;
    private List<String> randomDefis;

    /**
     * Cette méthode est appelée lors de la création de l'activité.
     * Elle sert à initialiser l'interface utilisateur, récupérer les données des préférences partagées,
     * sélectionner les défis à afficher et gérer l'affichage du score ainsi que le bouton "Quitter".
     *
     * Cette méthode permet :
     * - L'Initialisation des vues (ListView pour les défis et TextView pour le score).
     * - La vérification du nombre de défis joués dans les préférences partagées. Si plus de 3 défis ont été joués,
     *   la fin de la partie est affichée avec les scores et défis déjà joués.
     * - La sélection aléatoire de 3 défis parmi plusieurs catégories : capteur, tactile, et question.
     * - L'Affichage des défis dans une liste mélangée.
     * - La configuration du bouton "Quitter" pour réinitialiser les données de jeu dans les préférences partagées.
     * - La gestion de la sélection d'un défi dans la liste pour démarrer l'activité correspondante.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_defi);

        defiListView = findViewById(R.id.defiListView);
        scoreTextView = findViewById(R.id.scoreTextView);

        // Récupérer les sharedPreferences pour obtenir le nombre de défis joués.
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int nbDefisJoues = prefs.getInt("nbDefisJoues", 0);

        // 🎯 Si 3 défis déjà joués, afficher fin de partie
        if (nbDefisJoues >= 3) {
            int totalScore = prefs.getInt("totalScore", 0);
            String defisJouesStr = prefs.getString("defisJoues", "");
            ArrayList<String> defisJoues = new ArrayList<>(Arrays.asList(defisJouesStr.split(";")));

            Intent intent = new Intent(this, FinPartieActivity.class);
            intent.putExtra("totalScore", totalScore);
            intent.putStringArrayListExtra("defisJoues", defisJoues);

            startActivity(intent);
            finish();
            return;
        }


        updateScoreDisplay();// Màj de  l'affichage du score à l'initialisation.

        Button quitButton = findViewById(R.id.btnQuitter);
        quitButton.setVisibility(View.VISIBLE);
        quitButton.setOnClickListener(v -> {
            // Réinitialisation des données du jeu dans les sharedPreferences.
            prefs.edit()
                    .putInt("totalScore", 0)
                    .putInt("nbDefisJoues", 0)
                    .putString("defisJoues", "")
                    .apply();
            Toast.makeText(this, "Vous avez quitté la partie. Score remis à zéro.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // --- 1. Groupes par catégorie ---
        List<String> defisCapteur = Arrays.asList("Défi Secouer", "Défi Gyroscope");
        List<String> defisTactile = Arrays.asList("Défi Snake", "Défi Fruit");
        List<String> defisQuestion = Arrays.asList("Défi Quizz", "Défi Mot");

        // --- 2. Sélectionner un défi aléatoire par catégorie ---
        String defiCapteur = defisCapteur.get((int)(Math.random() * defisCapteur.size()));
        String defiTactile = defisTactile.get((int)(Math.random() * defisTactile.size()));
        String defiQuestion = defisQuestion.get((int)(Math.random() * defisQuestion.size()));

        // --- 3. Ajouter et mélanger ---
        randomDefis = new ArrayList<>();
        randomDefis.add(defiCapteur);
        randomDefis.add(defiTactile);
        randomDefis.add(defiQuestion);
        Collections.shuffle(randomDefis);

        // --- 4. Adapter la liste ---
        DefiAdapter adapter = new DefiAdapter(this, randomDefis);
        defiListView.setAdapter(adapter);

        // --- 5. Gestion du clic ---
        defiListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String selectedDefi = randomDefis.get(position);

            // Lancement de l'activité correspondante au défi sélectionné.
            Intent intent = null;
            switch (selectedDefi) {
                case "Défi Secouer":
                    intent = new Intent(this, ShakeActivity.class);
                    break;
                case "Défi Gyroscope":
                    intent = new Intent(this, GyroscopeActivity.class);
                    break;
                case "Défi Quizz":
                    intent = new Intent(this, QuizChoixActivity.class);
                    break;
                case "Défi Mot":
                    intent = new Intent(this, DevineMotActivity.class);
                    break;
                case "Défi Snake":
                    intent = new Intent(this, SnakeActivity.class);
                    break;
                case "Défi Fruit":
                    intent = new Intent(this, CutTheFruitActivity.class);
                    break;
            }

            // démarrer l'activité correspondante
            if (intent != null) {
                intent.putExtra("mode", "jouer");            // mode "jouer"
                intent.putExtra("isMultiplayer", false);     // et c'est SOLO ici

                // Enregistrer le défi joué
                SharedPreferences.Editor editor = prefs.edit();
                int count = prefs.getInt("nbDefisJoues", 0);
                String defisJoues = prefs.getString("defisJoues", "");

                editor.putInt("nbDefisJoues", count + 1);
                editor.putString("defisJoues", defisJoues + selectedDefi + ";");
                editor.apply();

                startActivity(intent);
            }

        });
    }

    /**
     * Méthode appelée lorsque l'activité reprend après une pause (par exemple après une rotation d'écran).
     * Elle permet de mettre à jour l'affichage du score chaque fois que l'utilisateur revient à cette activité.
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateScoreDisplay();
    }

    /**
     * Cette méthode met à jour l'affichage du score actuel de l'utilisateur.
     * Elle récupère la valeur du score total stocké dans les SharedPreferences et l'affiche dans le TextView.
     *
     */
    private void updateScoreDisplay() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int totalScore = prefs.getInt("totalScore", 0);
        scoreTextView.setText("Score : " + totalScore);
    }
}
