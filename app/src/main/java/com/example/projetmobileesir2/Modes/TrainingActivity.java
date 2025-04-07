package com.example.projetmobileesir2.Modes;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.R;

public class TrainingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        Toast.makeText(this, "Mode entraînement lancé", Toast.LENGTH_SHORT).show();
        // TODO : ajouter un défi d'entraînement ici
    }
}
