package com.example.projetmobileesir2.Defis;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.R;

public class DefiActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float totalForce = 0;
    private boolean challengeRunning = false;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defi);

        textView = findViewById(R.id.defiText);
        textView.setText("Bouge le téléphone pendant 5 secondes !");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        startChallenge();
    }

    private void startChallenge() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            challengeRunning = true;

            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    textView.setText("Temps restant : " + (millisUntilFinished / 1000) + "s");
                }

                public void onFinish() {
                    challengeRunning = false;
                    sensorManager.unregisterListener(DefiActivity.this);
                    textView.setText("Défi terminé ! Score : " + (int) totalForce);
                }
            }.start();
        } else {
            textView.setText("Accéléromètre non disponible !");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (challengeRunning) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float force = Math.abs(x) + Math.abs(y) + Math.abs(z) - SensorManager.GRAVITY_EARTH;
            if (force > 2) { // Filtre un peu le bruit
                totalForce += force;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Rien à faire ici pour ce défi
    }
}
