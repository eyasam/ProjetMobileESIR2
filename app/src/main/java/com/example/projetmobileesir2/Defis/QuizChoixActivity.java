package com.example.projetmobileesir2.Defis;

import android.content.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.projetmobileesir2.Modes.MultiplayerGameActivity;
import com.example.projetmobileesir2.Modes.ResultatsActivity;
import com.example.projetmobileesir2.R;
import com.example.projetmobileesir2.ScoreDialogFragment;
import com.example.projetmobileesir2.SelectionDefiActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizChoixActivity extends AppCompatActivity {

    private TextView tvScore, tvTimer, tvQuestion;
    private ImageView imageViewQuestion;
    private Button btnA, btnB, btnC, btnD;

    private int score = 0;
    private int currentQuestionIndex = 0;
    private boolean gameOver = false;

    private boolean isMultiplayer, isHost;

    private CountDownTimer timer;
    private long timeLeftMillis = 30000;
    private long timerStartedAt = 0;
    private long pauseTime = 0;
    private boolean isPaused = false;

    private final List<Question> questions = new ArrayList<>();

    private  String mode;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if ("START_QUIZ".equals(message)) {
                startQuiz();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_choix);

        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestion = findViewById(R.id.tvQuestion);
        imageViewQuestion = findViewById(R.id.imageViewQuestion);
        btnA = findViewById(R.id.btnAnswerA);
        btnB = findViewById(R.id.btnAnswerB);
        btnC = findViewById(R.id.btnAnswerC);
        btnD = findViewById(R.id.btnAnswerD);

        isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);
        isHost = getIntent().getBooleanExtra("isHost", false);

        mode = getIntent().getStringExtra("mode");

        initQuestions();

        if (isMultiplayer) {
            LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothReceiver, new IntentFilter("BLUETOOTH_MESSAGE"));
            if (isHost) {
                com.example.projetmobileesir2.Bluetooth.BluetoothConnectionHolder.sendMessage("START_QUIZ");
                startQuiz();
            }
        } else {
            startQuiz();
        }
    }

    private void initQuestions() {
        questions.add(new Question("Combien de cœurs a une pieuvre ?", R.drawable.pieuvre, "1", "2", "3", "8", "3"));
        questions.add(new Question("Que signifie 'karaoké' en japonais ?", R.drawable.karaoke, "Chanter mal", "Orchestre vide", "Danse en duo", "Boisson sucrée", "Orchestre vide"));
        questions.add(new Question("Quelle est la capitale de ce pays ?", R.drawable.canada, "Toronto", "Vancouver", "Ottawa", "Montréal", "Ottawa"));
        questions.add(new Question("Qui a peint La Joconde ?", R.drawable.jocande, "Picasso", "Michel-Ange", "Léonard de Vinci", "Van Gogh", "Léonard de Vinci"));
        questions.add(new Question("Combien y a-t-il de planètes dans le système solaire ?", R.drawable.system_solaire, "7", "8", "9", "10", "8"));
        questions.add(new Question("Combien de championnats du monde de Formule 1 Lewis Hamilton a-t-il gagnés ?", R.drawable.hamilton, "6", "7", "8", "5", "7"));
        questions.add(new Question( "Laquelle de ces affirmations sur ce chanteur est vraie ?", R.drawable.cardib,"Elle a joué dans le film Hustlers", "Elle a lancé une marque de parfums appelée CardEssence", "Elle a été juge dans The Voice USA pendant deux saisons", "Elle a remporté un Oscar pour une chanson originale", "Elle a joué dans le film Hustlers"));
        questions.add(new Question( "Dans quelle équipe joue Travis Kelce ?", R.drawable.travis_kelce,"Dallas Cowboys", "Green Bay Packers", "New York Giants", "Kansas City Chiefs", "Kansas City Chiefs"));
        questions.add(new Question( "Lequel de ces films met en vedette cet acteur",R.drawable.michael_b_jordan, "Silent Horizon", "Creed", "Shadow Protocol", "Crimson District", "Creed"));
        questions.add(new Question( "Contre quelle équipe Declan Rice a-t-il joué lors de son dernier match avec son pays", R.drawable.declan_rice,"Allemagne", "Finlande", "Italie", "Portugal", "Finlande"));
        questions.add(new Question( "Laquelle de ces chansons appartient à ce chanteur?", R.drawable.roddy_ricch,"No Love Tonight", "Diamonds in the Rain", "Money Flow", "The Box", "The Box"));
        Collections.shuffle(questions);
    }

    private void startQuiz() {
        startTimer(timeLeftMillis);
        showNextQuestion();
    }

    private void startTimer(long millis) {
        timerStartedAt = SystemClock.elapsedRealtime();

        timer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                tvTimer.setText("Temps restant : " + (millisUntilFinished / 1000 + "s"));
            }

            public void onFinish() {
                timeLeftMillis = 0;
                endGame();
            }
        }.start();
    }

    private void showNextQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            endGame();
            return;
        }

        Question q = questions.get(currentQuestionIndex);
        tvQuestion.setText(q.getText());

        if (q.getImageResId() != 0) {
            imageViewQuestion.setVisibility(View.VISIBLE);
            imageViewQuestion.setImageResource(q.getImageResId());
        } else {
            imageViewQuestion.setVisibility(View.GONE);
        }

        btnA.setText(q.getOptionA());
        btnB.setText(q.getOptionB());
        btnC.setText(q.getOptionC());
        btnD.setText(q.getOptionD());

        setButtonListener(btnA);
        setButtonListener(btnB);
        setButtonListener(btnC);
        setButtonListener(btnD);
    }

    private void setButtonListener(Button btn) {
        btn.setOnClickListener(v -> {
            if (gameOver) return;

            String selected = btn.getText().toString();
            String correct = questions.get(currentQuestionIndex).getCorrectAnswer();

            if (selected.equalsIgnoreCase(correct)) {
                score++;
                tvScore.setText("Score : " + score);
                playSound(R.raw.victory);
            } else {
                playSound(R.raw.defeat);
            }

            currentQuestionIndex++;
            showNextQuestion();
        });
    }

    private void endGame() {
        gameOver = true;
        if (timer != null) timer.cancel();
        //tvQuestion.setText("Fin du jeu !");
        //tvTimer.setText("0s");
        // ✅ Ajouter au score total
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int previousScore = prefs.getInt("totalScore", 0);
        prefs.edit().putInt("totalScore", previousScore + score).apply();
        imageViewQuestion.setVisibility(View.GONE);
        btnA.setEnabled(false);
        btnB.setEnabled(false);
        btnC.setEnabled(false);
        btnD.setEnabled(false);
        playSound(R.raw.victory);
        finishDefi();
    }

    private void playSound(int resId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }

    private void finishDefi() {
        if (isMultiplayer) {
            MultiplayerGameActivity.saveLocalScore(score);
            finish();
        } else {
            /*Intent intent = new Intent(this, ResultatsActivity.class);
            intent.putExtra("scoreLocal", score);
            startActivity(intent);

             */
            ScoreDialogFragment.newInstance(score, mode)
                    .show(getSupportFragmentManager(), "scoreDialog");

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            pauseTime = SystemClock.elapsedRealtime();
            isPaused = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameOver || timeLeftMillis <= 0) return;

        if (isPaused) {
            long delta = SystemClock.elapsedRealtime() - pauseTime;
            timeLeftMillis = Math.max(0, timeLeftMillis - delta);
            isPaused = false;
        }

        startTimer(timeLeftMillis);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) timer.cancel();
        if (isMultiplayer) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(bluetoothReceiver);
        }
    }

    static class Question {
        private final String text;
        private final int imageResId;
        private final String optionA, optionB, optionC, optionD;
        private final String correctAnswer;

        public Question(String text, int imageResId, String a, String b, String c, String d, String correct) {
            this.text = text;
            this.imageResId = imageResId;
            this.optionA = a;
            this.optionB = b;
            this.optionC = c;
            this.optionD = d;
            this.correctAnswer = correct;
        }

        public String getText() { return text; }
        public int getImageResId() { return imageResId; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public String getCorrectAnswer() { return correctAnswer; }
    }
}
