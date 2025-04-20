package com.example.projetmobileesir2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3500; // 3 secondes

    /**
     * Méthode appelée lors de la création de l'activité.
     * Cette méthode sert à afficher l'écran de démarrage (Splash Screen) pendant un certain temps
     * avant de rediriger automatiquement vers la `MainActivity`.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Démarre MainActivity après 3 secondes
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // pour ne pas revenir à la splash screen
        }, SPLASH_TIME_OUT);
    }
}
