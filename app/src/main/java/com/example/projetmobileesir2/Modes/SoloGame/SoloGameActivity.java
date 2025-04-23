package com.example.projetmobileesir2.Modes.SoloGame;

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
import com.example.projetmobileesir2.MainActivity;
import com.example.projetmobileesir2.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * gère une partie en mode solo avec 3 défis aléatoires
 * suit la progression, affiche le score et redirige vers la fin de partie après 3 défis
 */

public class SoloGameActivity extends AppCompatActivity {

    private ListView defiListView;
    private TextView scoreTextView;
    private List<String> randomDefis;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_defi);

        defiListView = findViewById(R.id.defiListView);
        scoreTextView = findViewById(R.id.scoreTextView);

        //récupérer les sharedPreferences pour obtenir le nombre de défis joués
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int nbDefisJoues = prefs.getInt("nbDefisJoues", 0);

        //si 3 défis déjà joués on affiche la fin de partie
        if (nbDefisJoues >= 3) {
            int totalScore = prefs.getInt("totalScore", 0);
            String defisJouesStr = prefs.getString("defisJoues", "");
            ArrayList<String> defisJoues = new ArrayList<>(Arrays.asList(defisJouesStr.split(";")));

            Intent intent = new Intent(this, ResultatsSoloGameActivity.class);
            intent.putExtra("totalScore", totalScore);
            intent.putStringArrayListExtra("defisJoues", defisJoues);

            startActivity(intent);
            finish();
            return;
        }


        updateScoreDisplay();// màj de  l'affichage du score à l'initialisation

        Button quitButton = findViewById(R.id.btnQuitter);
        quitButton.setVisibility(View.VISIBLE);
        quitButton.setOnClickListener(v -> {
            prefs.edit()
                    .putInt("totalScore", 0)
                    .putInt("nbDefisJoues", 0)
                    .putString("defisJoues", "")
                    .apply();
            Toast.makeText(this, "Vous avez quitté la partie. Score remis à zéro.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        List<String> defisCapteur = Arrays.asList("Shake It Up!", "Target Spin");
        List<String> defisTactile = Arrays.asList("Snake", "Slice Dash");
        List<String> defisQuestion = Arrays.asList("Guess It Right", "Mind Maze");

        String defiCapteur = defisCapteur.get((int)(Math.random() * defisCapteur.size()));
        String defiTactile = defisTactile.get((int)(Math.random() * defisTactile.size()));
        String defiQuestion = defisQuestion.get((int)(Math.random() * defisQuestion.size()));

        randomDefis = new ArrayList<>();
        randomDefis.add(defiCapteur);
        randomDefis.add(defiTactile);
        randomDefis.add(defiQuestion);
        Collections.shuffle(randomDefis);

        DefiAdapter adapter = new DefiAdapter(this, randomDefis);
        defiListView.setAdapter(adapter);

        defiListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String selectedDefi = randomDefis.get(position);

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
                intent.putExtra("mode", "jouer");
                intent.putExtra("isMultiplayer", false);

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

    @Override
    protected void onResume() {
        super.onResume();
        updateScoreDisplay();
    }

    private void updateScoreDisplay() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int totalScore = prefs.getInt("totalScore", 0);
        scoreTextView.setText("Score : " + totalScore);
    }
}
