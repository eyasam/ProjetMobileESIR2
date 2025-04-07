package com.example.projetmobileesir2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.Bluetooth.BluetoothActivity;
import com.example.projetmobileesir2.Modes.SoloGameActivity;
import com.example.projetmobileesir2.Modes.TrainingActivity;

public class MainActivity extends AppCompatActivity {

    private Button playButton, trainingMode;
    private ImageButton soundToggleButton;
    private MediaPlayer bgMusic, clickSound;
    private boolean isSoundOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Liens avec le layout
        playButton = findViewById(R.id.playButton);
        trainingMode = findViewById(R.id.trainingMode);
        soundToggleButton = findViewById(R.id.soundToggleButton);

        // Musique de fond
        bgMusic = MediaPlayer.create(this, R.raw.background_music);
        bgMusic.setLooping(true);
        bgMusic.start();

        // Son de clic
        clickSound = MediaPlayer.create(this, R.raw.click);

        // Toggle du son via le bouton image
        soundToggleButton.setOnClickListener(view -> {
            isSoundOn = !isSoundOn;
            if (isSoundOn) {
                soundToggleButton.setImageResource(R.drawable.ic_sound_on);
                bgMusic.start();
            } else {
                soundToggleButton.setImageResource(R.drawable.ic_sound_off);
                bgMusic.pause();
            }
        });

        // Bouton Jouer → Choix Solo / Multijoueur
        playButton.setOnClickListener(view -> {
            if (isSoundOn) clickSound.start();
            showModeDialog();
        });

        // Bouton Entraînement
        trainingMode.setOnClickListener(view -> {
            if (isSoundOn) clickSound.start();
            Intent intent = new Intent(MainActivity.this, TrainingActivity.class);
            startActivity(intent);
        });
    }

    // Affiche une boîte de dialogue pour choisir le mode de jeu
    private void showModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choisissez un mode de jeu")
                .setItems(new CharSequence[]{"Solo", "Multijoueur"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Mode Solo
                            Intent soloIntent = new Intent(MainActivity.this, SoloGameActivity.class);
                            startActivity(soloIntent);
                        } else {
                            // Mode Multijoueur
                            Intent multiIntent = new Intent(MainActivity.this, BluetoothActivity.class);
                            startActivity(multiIntent);
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        if (bgMusic != null) {
            bgMusic.release();
            bgMusic = null;
        }
        if (clickSound != null) {
            clickSound.release();
            clickSound = null;
        }
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSoundOn && bgMusic != null) {
            bgMusic.start();
        }
    }

}
