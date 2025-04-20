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

    private void launchSecondDefi() {
        Intent intent;

        // Choix aléatoire entre DevineMot et Quiz
        if (new Random().nextBoolean()) {
            intent = new Intent(this, QuizChoixActivity.class);
        } else {
            intent = new Intent(this, DevineMotActivity.class);
        }

        intent.putExtra("scoreDefi1", scoreDefi1);
        intent.putExtra("isMultiplayer", isMultiplayer);
        intent.putExtra("isHost", isHost);
        startActivity(intent);
        finish();
    }
}
