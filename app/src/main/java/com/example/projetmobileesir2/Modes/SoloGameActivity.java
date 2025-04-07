package com.example.projetmobileesir2.Modes;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.R;

public class SoloGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solo_game);

        Toast.makeText(this, "Mode solo lancé : 3 défis aléatoires", Toast.LENGTH_SHORT).show();
        // TODO : logique pour enchaîner 3 défis ici
    }
}
