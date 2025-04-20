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
import com.example.projetmobileesir2.ScoreDialogFragment;

import java.util.Random;

public class GyroscopeActivity extends AppCompatActivity {

    private static final int DUREE_JEU = 10000;
    private static final int MARGE = 10; // Marge d’erreur autorisée en degrés

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
    private  String mode;

    // Écouteur des événements du capteur de rotation
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!challengeRunning || event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) return;
            // Conversion de la rotation en azimut (orientation autour de l’axe Z)
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimuth = (float) Math.toDegrees(orientation[0]);
            if (azimuth < 0) azimuth += 360;

            updateAngleUI(azimuth);
            rotateArrow(azimuth);
            // Vérifie si l'angle est dans la bonne plage pour valider le point
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

    // Récepteur (bluetooth) pour gérer le lancement du défi en multijoueur
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

        mode = getIntent().getStringExtra("mode");

        isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);
        isHost = getIntent().getBooleanExtra("isHost", false);

        if (isMultiplayer) {
            // On prépare la synchronisation avec les autres joueurs
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

    /**
     * Initialisation des vues
     */
    private void initViews() {
        angleView = findViewById(R.id.currentAngle);
        targetView = findViewById(R.id.targetAngle);
        scoreView = findViewById(R.id.scoreText);
        resultView = findViewById(R.id.resultText);
        timerView = findViewById(R.id.timerText);
        arrowImage = findViewById(R.id.fleche);
        flashView = findViewById(R.id.flashView);
    }

    /**
     * Initialisation du capteur de rotation
     */
    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    /**
     * Lancement du défi capteur+timer
     */
    private void startChallenge() {
        challengeRunning = true;
        generateNewTarget();
        registerSensor();
        startTimer(timeLeftMillis);
    }

    /**
     * Calcul du temps restant lors d'une partie
     * @param millis
     */
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

    /**
     * Enregistrement du capteur pour écouter les mouvements
     */
    private void registerSensor() {
        if (rotationSensor != null) {
            sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Arrêt de l'écoute du capteur
     */
    private void unregisterSensor() {
        sensorManager.unregisterListener(sensorListener);
    }

    /**
     * Génération d'un nouvel angle cible aléatoire
     */
    private void generateNewTarget() {
        targetAngle = random.nextInt(360);
        targetView.setText("Cible : " + targetAngle + "°");
    }

    /**
     * Vérification de si l’utilisateur est dans la bonne plage angulaire
     * @param angle
     * @return
     */
    private boolean isWithinTarget(float angle) {
        float delta = Math.abs(angle - targetAngle);
        return delta <= MARGE || delta >= (360 - MARGE);
    }

    /**
     * On met à jour l’affichage de l’angle courant
     * @param angle
     */
    private void updateAngleUI(float angle) {
        angleView.setText(String.format("Ton angle : %.0f°", angle));
    }

    /**
     * On fait tourner l’image de la flèche selon l’angle détecté
     * @param angle
     */
    private void rotateArrow(float angle) {
        arrowImage.setPivotX(arrowImage.getWidth() / 2f);
        arrowImage.setPivotY(arrowImage.getHeight() / 2f);
        arrowImage.setRotation(angle);
    }

    /**
     * Affichage d'un flash vert rapide à l’écran
     */
    private void flashGreen() {
        flashView.setAlpha(1f);
        flashView.setVisibility(View.VISIBLE);
        flashView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> flashView.setVisibility(View.GONE))
                .start();
    }

    /**
     * Fin du défi, on sauvegarde du score et arrêt des capteurs
     */
    private void endChallenge() {
        challengeRunning = false;
        unregisterSensor();
        //resultView.setText("Défi terminé ! Score : " + score);
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int previousScore = prefs.getInt("totalScore", 0);
        prefs.edit().putInt("totalScore", previousScore + score).apply();

        playSound(R.raw.victory);
    }

    /**
     * Affichage du score final (multijoueur ou solo)
     */
    private void finishDefi() {
        if (isMultiplayer) {
            MultiplayerGameActivity.saveLocalScore(score);
            finish();
        } else {
            //Ancien code
            /*Intent intent = new Intent(this, ResultatsActivity.class);
            intent.putExtra("scoreLocal", score);
            startActivity(intent);

             */

            // Affichage du ScoreDialogFragment
            ScoreDialogFragment.newInstance(score, mode)
                    .show(getSupportFragmentManager(), "score_dialog");

        }

    }


    /**
     * Méthode pour retentir le son de victoire
     * @param resId
     */
    private void playSound(int resId) {
        MediaPlayer mp = MediaPlayer.create(this, resId);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }

    // === Cycle de vie ===

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
