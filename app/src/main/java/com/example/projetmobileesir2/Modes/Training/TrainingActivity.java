package com.example.projetmobileesir2.Modes.Training;

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
import com.example.projetmobileesir2.Modes.SoloGame.DefiAdapter;
import com.example.projetmobileesir2.R;

import java.util.Arrays;
import java.util.List;

/**
 * mode entraînement permettant de jouer librement tous les défis dispo
 */

public class TrainingActivity extends AppCompatActivity {

    private GridView trainingGrid;

    private static final List<String> ALL_DEFIS = Arrays.asList(
            "Shake It Up!",
            "Target Spin",
            "Guess It Right",
            "Mind Maze",
            "Snake",
            "Slice Dash"
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
            finish();
        });

        trainingGrid.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String selectedDefi = ALL_DEFIS.get(position);
            Intent intent = null;

            switch (selectedDefi) {
                case "Shake It Up!":
                    intent = new Intent(this, ShakeActivity.class);
                    break;
                case "Target Spin":
                    intent = new Intent(this, GyroscopeActivity.class);
                    break;
                case "Guess It Right":
                    intent = new Intent(this, QuizChoixActivity.class);
                    break;
                case "Mind Maze":
                    intent = new Intent(this, DevineMotActivity.class);
                    break;
                case "Snake":
                    intent = new Intent(this, SnakeActivity.class);
                    break;
                case "Slice Dash":
                    intent = new Intent(this, CutTheFruitActivity.class);
                    break;
            }
            if (intent != null) {
                intent.putExtra("mode", "entrainement");
                startActivity(intent);
            }
        });
    }
}
