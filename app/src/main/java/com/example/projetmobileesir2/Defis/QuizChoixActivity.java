package com.example.projetmobileesir2.Defis;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobileesir2.R;

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

    private CountDownTimer timer;

    private final List<Question> questions = new ArrayList<>();

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

        initQuestions();
        startTimer();
        showNextQuestion();
    }

    private void initQuestions() {
        questions.add(new Question("Quel est le seul animal qui ne peut pas sauter ?", 0, "L'éléphant", "Le crocodile", "La baleine", "La tortue", "L'éléphant"));
        questions.add(new Question("Combien de cœurs a une pieuvre ?", 0, "1", "2", "3", "8", "3"));
        questions.add(new Question("Que signifie 'karaoké' en japonais ?", 0, "Chanter mal", "Orchestre vide", "Danse en duo", "Boisson sucrée", "Orchestre vide"));
        questions.add(new Question("Quelle est la capitale du pays avec une feuille d’érable sur son drapeau ?", 0, "Toronto", "Vancouver", "Ottawa", "Montréal", "Ottawa"));
        questions.add(new Question("Quel est le métal liquide à température ambiante ?", 0, "Mercure", "Plomb", "Aluminium", "Zinc", "Mercure"));
        questions.add(new Question("Combien de pattes a une araignée ?", 0, "6", "8", "10", "12", "8"));
        questions.add(new Question("Lequel est un fruit ?", 0, "Carotte", "Tomate", "Céleri", "Poireau", "Tomate"));
        questions.add(new Question("Qui a peint La Joconde ?", 0, "Picasso", "Michel-Ange", "Léonard de Vinci", "Van Gogh", "Léonard de Vinci"));
        questions.add(new Question("Combien y a-t-il de planètes dans le système solaire ?", 0, "7", "8", "9", "10", "8"));
        questions.add(new Question("Combien de couleurs dans l’arc-en-ciel ?", 0, "6", "7", "8", "5", "7"));

        Collections.shuffle(questions);
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

    private void startTimer() {
        timer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                endGame();
            }
        };
        timer.start();
    }

    private void endGame() {
        gameOver = true;
        tvQuestion.setText("Fin du jeu !");
        tvTimer.setText("0s");
        imageViewQuestion.setVisibility(View.GONE);
        btnA.setEnabled(false);
        btnB.setEnabled(false);
        btnC.setEnabled(false);
        btnD.setEnabled(false);
        playSound(R.raw.victory);
    }

    private void playSound(int resId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) timer.cancel();
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
