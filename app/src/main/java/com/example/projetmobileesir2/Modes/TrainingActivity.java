package com.example.projetmobileesir2.Modes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.Defis.CutTheDotsActivity;
import com.example.projetmobileesir2.Defis.ShakeActivity;
import com.example.projetmobileesir2.Defis.DefiDevineMotActivity;

import com.example.projetmobileesir2.Defis.GyroscopeActivity;
import com.example.projetmobileesir2.Defis.QuizChoixActivity;

import com.example.projetmobileesir2.Defis.SnakeDefiActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        trainingGrid = findViewById(R.id.trainingGrid);




        DefiAdapter adapter = new DefiAdapter(this, ALL_DEFIS);
        trainingGrid.setAdapter(adapter);


        Button retourButton = findViewById(R.id.btnRetourAccueil);
        retourButton.setOnClickListener(v -> {
            finish(); // Ferme cette activité et retourne automatiquement à MainActivity
        });

        trainingGrid.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String selectedDefi = ALL_DEFIS.get(position);
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
                    intent = new Intent(this, DefiDevineMotActivity.class);
                    break;
                case "Défi Snake":
                    intent = new Intent(this, SnakeDefiActivity.class);
                    break;
                case "Défi Fruit":
                    intent = new Intent(this, CutTheDotsActivity.class);
                    break;
            }

            if (intent != null) {
                intent.putExtra("mode", "entrainement");
                startActivity(intent);
            }
        });
    }
}
