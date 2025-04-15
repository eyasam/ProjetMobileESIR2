package com.example.projetmobileesir2.Defis;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.R;

import java.util.Random;


public class GyroscopeActivity extends AppCompatActivity {

    private static final int Duree_jeux = 60000;
    private static final int Marge = 10;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private TextView angleView, targetView, scoreView, resultView, timerView;
    private ImageView arrowImage;
    private View flashView;

    private final Random random = new Random();
    private int targetAngle = 0;
    private int score = 0;
    private boolean challengeRunning = false;

    private CountDownTimer gameTimer;

    // Écouteur pour le gyroscope :
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (!challengeRunning || event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) return;

            // Transformation des données en angle d’orientation (azimuth)
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimuth = (float) Math.toDegrees(orientation[0]);
            if (azimuth < 0) azimuth += 360;

            // Mise à jour de l'interface avec l'angle
            updateAngleUI(azimuth);
            rotationFleche(azimuth);

            // Si l'utilisateur vise correctement mise à jour du score
            if (isWithinTarget(azimuth)) {
                score++;
                scoreView.setText("Score : " + score);
                playSound(R.raw.victory);
                vertFlash();
                nouvelleCible();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);

        initViews();
        initSensor();
        startChallenge();
    }

    private void initViews() {
        angleView = findViewById(R.id.currentAngle);
        targetView = findViewById(R.id.targetAngle);
        scoreView = findViewById(R.id.scoreText);
        resultView = findViewById(R.id.resultText);
        timerView = findViewById(R.id.timerText);
        arrowImage = findViewById(R.id.fleche);
        flashView = findViewById(R.id.flashView);

    }

    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    private void startChallenge() {
        // Enregistrement du listener du capteur
        if (rotationSensor != null) {
            sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI);
        }

        challengeRunning = true;
        nouvelleCible(); // Première cible

        gameTimer = new CountDownTimer(Duree_jeux, 1000) {
            public void onTick(long millisUntilFinished) {
                timerView.setText("Temps : " + (millisUntilFinished / 1000) + "s");
            }

            public void onFinish() {
                endChallenge();
            }
        }.start();
    }

    // Affiche un flash vert pendant 200ms
    private void vertFlash() {
        flashView.setAlpha(1f);
        flashView.setVisibility(View.VISIBLE);
        flashView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> flashView.setVisibility(View.GONE))
                .start();
    }

    private void nouvelleCible() {
        targetAngle = random.nextInt(360);
        targetView.setText("Cible : " + targetAngle + "°");
    }

    // Vérifie si l'utilisateur est dans la bonne zone angulaire
    private boolean isWithinTarget(float angle) {
        float delta = Math.abs(angle - targetAngle);
        return delta <= Marge || delta >= (360 - Marge);
    }

    // Affiche l'angle actuel de l'utilisateur
    private void updateAngleUI(float angle) {
        angleView.setText(String.format("Ton angle : %.0f°", angle));
    }

    // Fait tourner l’image de la fleche pour correspondre à l'orientation
    private void rotationFleche(float angle) {
        arrowImage.setPivotX(arrowImage.getWidth() / 2f);
        arrowImage.setPivotY(arrowImage.getHeight() / 2f);
        arrowImage.setRotation(angle);
    }

    private void playSound(int soundResId) {
        MediaPlayer mp = MediaPlayer.create(this, soundResId);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }

    private void endChallenge() {
        challengeRunning = false;
        sensorManager.unregisterListener(sensorListener);
        timerView.setText("Temps : 0s");
        resultView.setText("Défi terminé ! Score : " + score);
        playSound(R.raw.victory);
    }

    // Arrêt du capteur et du timer si l'activité est quittée
    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(sensorListener);

        if (gameTimer != null) {
            gameTimer.cancel();
        }
    }
}