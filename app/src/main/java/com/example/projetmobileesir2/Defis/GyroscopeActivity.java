package com.example.projetmobileesir2.Defis;

import android.content.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.*;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.projetmobileesir2.Modes.MultiplayerGameActivity;
import com.example.projetmobileesir2.Modes.ResultatsActivity;
import com.example.projetmobileesir2.R;

import java.util.Random;

public class GyroscopeActivity extends AppCompatActivity {

    private static final int DUREE_JEU = 60000;
    private static final int MARGE = 10;

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private TextView angleView, targetView, scoreView, resultView, timerView;
    private ImageView arrowImage;
    private View flashView;

    private final Random random = new Random();
    private int targetAngle = 0;
    private int score = 0;
    private boolean challengeRunning = false;
    private boolean isMultiplayer, isHost;

    private CountDownTimer gameTimer;
    private long timeLeftMillis = DUREE_JEU;
    private long timerStartedAt = 0;
    private boolean timerWasRunning = false;

    private long pauseTime = 0;
    private boolean isPaused = false;

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!challengeRunning || event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) return;

            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimuth = (float) Math.toDegrees(orientation[0]);
            if (azimuth < 0) azimuth += 360;

            updateAngleUI(azimuth);
            rotateArrow(azimuth);

            if (isWithinTarget(azimuth)) {
                score++;
                scoreView.setText("Score : " + score);
                playSound(R.raw.victory);
                flashGreen();
                generateNewTarget();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if ("GO_GYRO".equals(message)) {
                startChallenge();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);

        initViews();
        initSensor();

        isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);
        isHost = getIntent().getBooleanExtra("isHost", false);

        if (isMultiplayer) {
            LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothReceiver, new IntentFilter("BLUETOOTH_MESSAGE"));

            if (isHost) {
                new Handler().postDelayed(() -> {
                    com.example.projetmobileesir2.Bluetooth.BluetoothConnectionHolder.sendMessage("GO_GYRO");
                    startChallenge();
                }, 700);
            }
        } else {
            startChallenge();
        }
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
        challengeRunning = true;
        generateNewTarget();
        registerSensor();
        startTimer(timeLeftMillis);
    }

    private void startTimer(long millis) {
        timerStartedAt = SystemClock.elapsedRealtime();
        timerWasRunning = true;

        gameTimer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                timerView.setText("Temps : " + (millisUntilFinished / 1000) + "s");
            }

            public void onFinish() {
                timeLeftMillis = 0;
                timerView.setText("0s");
                endChallenge();
                finishDefi();
            }
        }.start();
    }

    private void registerSensor() {
        if (rotationSensor != null) {
            sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void unregisterSensor() {
        sensorManager.unregisterListener(sensorListener);
    }

    private void generateNewTarget() {
        targetAngle = random.nextInt(360);
        targetView.setText("Cible : " + targetAngle + "°");
    }

    private boolean isWithinTarget(float angle) {
        float delta = Math.abs(angle - targetAngle);
        return delta <= MARGE || delta >= (360 - MARGE);
    }

    private void updateAngleUI(float angle) {
        angleView.setText(String.format("Ton angle : %.0f°", angle));
    }

    private void rotateArrow(float angle) {
        arrowImage.setPivotX(arrowImage.getWidth() / 2f);
        arrowImage.setPivotY(arrowImage.getHeight() / 2f);
        arrowImage.setRotation(angle);
    }

    private void flashGreen() {
        flashView.setAlpha(1f);
        flashView.setVisibility(View.VISIBLE);
        flashView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> flashView.setVisibility(View.GONE))
                .start();
    }

    private void endChallenge() {
        challengeRunning = false;
        unregisterSensor();
        resultView.setText("Défi terminé ! Score : " + score);
        playSound(R.raw.victory);
    }

    private void finishDefi() {
        if (isMultiplayer) {
            MultiplayerGameActivity.saveLocalScore(score);
        } else {
            Intent intent = new Intent(this, ResultatsActivity.class);
            intent.putExtra("scoreLocal", score);
            startActivity(intent);
        }
        finish();
    }

    private void playSound(int resId) {
        MediaPlayer mp = MediaPlayer.create(this, resId);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timerWasRunning && gameTimer != null) {
            gameTimer.cancel();
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
        if (gameTimer != null) gameTimer.cancel();
        if (isMultiplayer) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(bluetoothReceiver);
        }
    }
}
