package com.example.projetmobileesir2.Defis;

import android.content.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.*;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.projetmobileesir2.Modes.MultiplayerGame.MultiplayerGameActivity;
import com.example.projetmobileesir2.R;
import com.example.projetmobileesir2.Modes.ScoreSoloTrainingFragment;

/**
 * activité du défi "Shake It Up!" basée sur l'accéléromètre
 * compte les secousses du téléphone pendant un temps limité, en solo ou multijoueur
 */

public class ShakeActivity extends AppCompatActivity {

    private static final int SEUIL_SENSIBILITE = 5000; // seuil de sensibilité

    private static final int DUREE_DEFI = 10000;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;

    private TextView shakeCountText, timerText, resultText;
    private int shakeCount = 0;
    private boolean challengeRunning = false;
    private boolean isMultiplayer, isHost;

    private float lastX, lastY, lastZ;
    private long lastUpdate = 0;

    private CountDownTimer countDownTimer;
    private long timeLeftMillis = DUREE_DEFI;
    private long timerStartedAt = 0;
    private long pauseTime = 0;
    private boolean isPaused = false;

    private  String mode;

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

                if (speed > SEUIL_SENSIBILITE) {
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

    // réception des messages Bluetooth (mode multijoueur)
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            //lorsque le message "GO_SHAKE" est reçu le défi commence
            if ("GO_SHAKE".equals(message)) {
                startChallenge();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        shakeCountText = findViewById(R.id.shakeCountText);
        timerText = findViewById(R.id.timerText);
        resultText = findViewById(R.id.resultText);

        mode = getIntent().getStringExtra("mode");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);
        isHost = getIntent().getBooleanExtra("isHost", false);

        if (isMultiplayer) {
            // Si c'est l'hôte on envoie le signal pour commencer le défi
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    bluetoothReceiver, new IntentFilter("BLUETOOTH_MESSAGE"));

            if (isHost) {
                new Handler().postDelayed(() -> {
                    com.example.projetmobileesir2.Bluetooth.BluetoothConnectionHolder.sendMessage("GO_SHAKE");
                    startChallenge();
                }, 700);
            }
        } else {
            // mode solo ou entrainement  démarrage immédiat du défi
            startChallenge();
        }
    }

    private void startChallenge() {
        shakeCount = 0;
        challengeRunning = true;
        registerSensor();
        startTimer(timeLeftMillis);
    }

    private void registerSensor() {
        sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    private void unregisterSensor() {
        sensorManager.unregisterListener(shakeListener);
    }

    private void startTimer(long millis) {
        timerStartedAt = SystemClock.elapsedRealtime();

        countDownTimer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                timerText.setText("Temps : " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                challengeRunning = false;
                unregisterSensor();

                // ajoute le score au score global (stocké dans SharedPreferences) pour le mode Solo

                SharedPreferences totalPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                int previousScore = totalPrefs.getInt("totalScore", 0);
                totalPrefs.edit().putInt("totalScore", previousScore + shakeCount).apply();
                playSound(R.raw.victory);

                finishDefi();
            }
        }.start();
    }
    private void playSound(int resId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }

    /**
     * fait vibrer l'appareil pour donner du feedback
     */
    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }


    }

    /**
     * ferme  l'activité en multijoueur
     * en mode solo ou entrainement : affiche un dialog avec le score
     */
    private void finishDefi() {
        if (isMultiplayer) {
            MultiplayerGameActivity.saveLocalScore(shakeCount);
            finish();
        } else {
            ScoreSoloTrainingFragment dialog = ScoreSoloTrainingFragment.newInstance(shakeCount,mode);
            dialog.show(getSupportFragmentManager(), "ScoreDialog");

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            pauseTime = SystemClock.elapsedRealtime();
            isPaused = true;
        }
        unregisterSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!challengeRunning || timeLeftMillis <= 0) return;

        if (isPaused) {
            long delta = SystemClock.elapsedRealtime() - pauseTime;
            timeLeftMillis = Math.max(0, timeLeftMillis - delta);
            isPaused = false;
        }

        registerSensor();
        startTimer(timeLeftMillis);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterSensor();
        if (countDownTimer != null) countDownTimer.cancel();
        if (isMultiplayer) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(bluetoothReceiver);
        }
    }
}
