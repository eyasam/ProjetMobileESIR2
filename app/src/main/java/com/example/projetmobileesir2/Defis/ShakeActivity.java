package com.example.projetmobileesir2.Defis;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.R;

public class ShakeActivity extends AppCompatActivity {

    private static final int Seuil_sensibilite = 5000;
    private static final int duree_defi = 10000;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;

    private TextView shakeCountText, timerText, resultText;
    private int shakeCount = 0;
    private boolean challengeRunning = false;

    private float lastX, lastY, lastZ;
    private long lastUpdate = 0;

    // Listener pour les événements de mouvement
    private final SensorEventListener shakeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!challengeRunning) return;

            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = curTime - lastUpdate;
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float deltaX = x - lastX;
                float deltaY = y - lastY;
                float deltaZ = z - lastZ;

                float speed = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / diffTime * 10000;

                if (speed > Seuil_sensibilite) {
                    shakeCount++;
                    shakeCountText.setText("Secousses : " + shakeCount);
                    vibrate();
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        shakeCountText = findViewById(R.id.shakeCountText);
        timerText = findViewById(R.id.timerText);
        resultText = findViewById(R.id.resultText);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        startChallenge();
    }

    private void startChallenge() {
        shakeCount = 0;
        challengeRunning = true;
        sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI);

        new CountDownTimer(duree_defi, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Temps : " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                challengeRunning = false;
                sensorManager.unregisterListener(shakeListener);
                timerText.setText("Temps : 0s");
                resultText.setText("Défi terminé !\nScore final : " + shakeCount);
            }
        }.start();
    }

    // déclencher une vibration courte
    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    // On arrête les capteurs si l'activité se ferme
    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(shakeListener);
        challengeRunning = false;
    }
}
