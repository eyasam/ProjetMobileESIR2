package com.example.projetmobileesir2.Defis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * gère le rendu les déplacements, les collisions et le score en temps réel
 */

public class SnakeView extends View implements Runnable {

    private List<Point> snake = new ArrayList<>();
    private Point food = new Point();
    private int direction = 1;
    private int cellSize = 30;
    private Paint snakePaint, foodPaint, textPaint;
    private TextView scoreText;
    private int score = 0;
    private TextView timeLeftText;

    private long startTime;
    private boolean isMoving = false;
    private float touchStartX, touchStartY;
    private boolean isGameOver = false;

    public interface OnGameOverListener {
        void onGameOver(int finalScore);
    }

    private OnGameOverListener onGameOverListener;

    public SnakeView(Context context, TextView scoreText) {
        super(context);
        this.scoreText = scoreText;
        init();
    }

    public void setOnGameOverListener(OnGameOverListener listener) {
        this.onGameOverListener = listener;
    }

    private void init() {
        // Réinitialiser le serpent à une position de départ aléatoire
        snake.clear();
        snake.add(new Point(5, 5));

        snakePaint = new Paint();
        snakePaint.setColor(Color.GREEN);//serp

        foodPaint = new Paint();
        foodPaint.setColor(Color.RED);//nouriture

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#734158"));
        textPaint.setTextSize(50);

        // Initialiser le score et le temps de départ
        score = 0;
        scoreText.setText("Score: " + score);
        startTime = System.currentTimeMillis();  // Démarre le compteur de temps
        spawnFood(); //placer la nourriture à une position aléatoire
        postDelayed(this, 300);
    }

    /**
     *  nourriture à une position aléatoire
     */
    private void spawnFood() {
        int cols = getWidth() / cellSize;
        int rows = getHeight() / cellSize;
        if (cols <= 0 || rows <= 0) return;

        Random rand = new Random();

        // genere la nourriture à une position aléatoire en excluant les bords
        food.set(rand.nextInt(cols - 2) + 1, rand.nextInt(rows - 2) + 1);
    }

    public void setTimeLeftText(TextView timeLeftText) {
        this.timeLeftText = timeLeftText;
    }

    /**
     * Méthode pour dessiner le serpent, la nourriture et le texte sur le canvas
     * Si le jeu est terminé, on arrête le dessin du serpent et de la nourriture
     */
    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);


        if (isGameOver) {
            return;
        }

        for (Point p : snake) {
            c.drawRect(p.x * cellSize, p.y * cellSize,
                    (p.x + 1) * cellSize, (p.y + 1) * cellSize, snakePaint);
        }

        c.drawRect(food.x * cellSize, food.y * cellSize,
                (food.x + 1) * cellSize, (food.y + 1) * cellSize, foodPaint);

        // Mettre à jour le score
        if (scoreText != null) {
            scoreText.setText("Score: " + score);
        }

        // Calculer le temps restant
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = 30 - (elapsedTime / 1000);
        if (remainingTime < 0) remainingTime = 0;

        if (timeLeftText != null) {
            timeLeftText.setText("Temps restant: " + remainingTime + "s");
        }


    }

    /**
     * déplacer le serpent en fonction de la direction
     * Copie de la tête du serpent
     * calcul de la prochaine position de la tête selon la direction
     */
    private void moveSnake() {
        Point head = new Point(snake.get(0));
        switch (direction) {
            case 0: head.y--; break; // H
            case 1: head.x++; break; // D
            case 2: head.y++; break; // B
            case 3: head.x--; break; // G
        }

        int maxCols = getWidth() / cellSize;
        int maxRows = getHeight() / cellSize;

        // collision avec le mur
        if (head.x < 0 || head.y < 0 || head.x >= maxCols || head.y >= maxRows) {
            Log.d("SnakeGame", "Collision avec le mur: " + head.toString());
            return;  // Ne fait rien si on touche le mur
        }


        snake.add(0, head); // Ajoute la tête du serpent
        if (head.equals(food)) {
            score++;
            scoreText.setText("Score: " + score);
            spawnFood();
        } else {
            snake.remove(snake.size() - 1); // Retire la dernière partie du serpent si il n'a pas mangé
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - touchStartX;
                float dy = event.getY() - touchStartY;

                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0 && direction != 3) direction = 1; // D
                    else if (dx < 0 && direction != 1) direction = 3; // G
                } else {
                    if (dy > 0 && direction != 0) direction = 2; // Bas
                    else if (dy < 0 && direction != 2) direction = 0; // Haut
                }

                isMoving = true; // si le serpent est en mouvement
                touchStartX = event.getX();  // Mise à jour de la position
                touchStartY = event.getY();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void run() {
        if (isGameOver) return;

        if (isMoving) moveSnake();

        if ((System.currentTimeMillis() - startTime) / 1000 > 30) {
            isGameOver = true;
            if (onGameOverListener != null) {
                onGameOverListener.onGameOver(score);
            }
        }

        invalidate();
        postDelayed(this, 300);
    }

}
