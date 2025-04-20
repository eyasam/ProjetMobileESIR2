package com.example.projetmobileesir2.Modes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.Defis.CutTheFruitActivity;
import com.example.projetmobileesir2.Defis.ShakeActivity;
import com.example.projetmobileesir2.Defis.DevineMotActivity;

import com.example.projetmobileesir2.Defis.GyroscopeActivity;
import com.example.projetmobileesir2.Defis.QuizChoixActivity;

import com.example.projetmobileesir2.Defis.SnakeActivity;
import com.example.projetmobileesir2.R;

import java.util.Arrays;
import java.util.List;

public class TrainingActivity extends AppCompatActivity {

    private GridView trainingGrid;

    private static final List<String> ALL_DEFIS = Arrays.asList(
            "Défi Secouer",
            "Défi Gyroscope",
            "Défi Quizz",
            "Défi Mot",
            "Défi Snake",
            "Défi Fruit"
    );

    /**
     * Cette méthode est appelée lors de la création de l'activité.
     * Elle initialise la GridView, l'adaptateur pour afficher les défis et configure le bouton de retour.
     * Un écouteur est aussi ajouté sur la GridView pour détecter le défi sélectionné.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        trainingGrid = findViewById(R.id.trainingGrid);

        // Création de l'adaptateur avec la liste des défis et assignation à la GridView
        DefiAdapter adapter = new DefiAdapter(this, ALL_DEFIS);
        trainingGrid.setAdapter(adapter);

        // Initialisation du bouton de retour et ajout d'un écouteur pour terminer l'activité
        Button retourButton = findViewById(R.id.btnRetourAccueil);
        retourButton.setOnClickListener(v -> {
            finish(); // Ferme cette activité et retourne automatiquement à MainActivity
        });

        trainingGrid.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            // Récupère le défi sélectionné en fonction de la position dans la GridView
            String selectedDefi = ALL_DEFIS.get(position);
            // Création de l'intent pour démarrer l'activité appropriée selon le défi sélectionné
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
            // Si un défi est sélectionné, on indique que c'est un mode entrainement et lance l'activité
            if (intent != null) {
                intent.putExtra("mode", "entrainement");
                startActivity(intent);
            }
        });
    }
}
