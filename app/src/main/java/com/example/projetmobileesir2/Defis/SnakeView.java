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

public class SnakeView extends View implements Runnable {

    private List<Point> snake = new ArrayList<>();
    private Point food = new Point();
    private int direction = 1; // 0 = Haut, 1 = Droite, 2 = Bas, 3 = Gauche
    private int cellSize = 30;
    private Paint snakePaint, foodPaint, textPaint;
    private TextView scoreText;
    private int score = 0;
    private TextView timeLeftText;

    private long startTime;  // Temps de début du jeu
    private boolean isMoving = false;
    private float touchStartX, touchStartY;
    private boolean isGameOver = false; // Indicateur si le jeu est terminé

    // Interface pour notifier la fin du jeu
    public interface OnGameOverListener {
        void onGameOver(int finalScore);  // Méthode à appeler avec le score final
    }

    private OnGameOverListener onGameOverListener;

    public SnakeView(Context context, TextView scoreText) {
        super(context);
        this.scoreText = scoreText;
        init(); // Initialiser le jeu
    }

    // Méthode pour définir le listener de fin de jeu
    public void setOnGameOverListener(OnGameOverListener listener) {
        this.onGameOverListener = listener;
    }

    /**
     * Initialisation des jeu
     * Réinitialisation le serpent à une position de départ aléatoire
     * Création un paint pour dessiner le serpent, la nourriture et le texte
     * Initialisation le score et l'affichage
     */
    private void init() {
        // Réinitialiser le serpent à une position de départ aléatoire
        snake.clear();
        snake.add(new Point(5, 5));

        // Créer un paint pour dessiner le serpent et la nourriture
        snakePaint = new Paint();
        snakePaint.setColor(Color.GREEN);

        foodPaint = new Paint();
        foodPaint.setColor(Color.RED);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#734158"));
        textPaint.setTextSize(50);

        // Initialiser le score et le temps de départ
        score = 0;
        scoreText.setText("Score: " + score);
        startTime = System.currentTimeMillis();  // Démarre le compteur de temps
        spawnFood(); // Placer la nourriture à une position aléatoire
        postDelayed(this, 300);
    }

    /**
     * Méthode pour faire apparaître de la nourriture à une position aléatoire
     *
     */
    private void spawnFood() {
        int cols = getWidth() / cellSize;
        int rows = getHeight() / cellSize;
        if (cols <= 0 || rows <= 0) return;

        Random rand = new Random();

        // Générer la nourriture à une position aléatoire en excluant les bords
        food.set(rand.nextInt(cols - 2) + 1, rand.nextInt(rows - 2) + 1);
    }

    /**
     * Méthode pour définir le TextView pour afficher le temps restant
     * @param timeLeftText
     */
    public void setTimeLeftText(TextView timeLeftText) {
        this.timeLeftText = timeLeftText;
    }

    /**
     * Méthode pour dessiner le serpent, la nourriture et le texte sur le canvas
     * Si le jeu est terminé, on arrête le dessin du serpent et de la nourriture
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (isGameOver) {
            //canvas.drawText("Partie Terminée", getWidth() / 4, getHeight() / 2 - 100, textPaint);
            //canvas.drawText("Score Final: " + score, getWidth() / 4, getHeight() / 2, textPaint);
            return;  // Empêche le dessin du serpent et de la nourriture
        }

        // Dessiner le serpent
        for (Point p : snake) {
            canvas.drawRect(p.x * cellSize, p.y * cellSize,
                    (p.x + 1) * cellSize, (p.y + 1) * cellSize, snakePaint);
        }

        // Dessiner la nourriture
        canvas.drawRect(food.x * cellSize, food.y * cellSize,
                (food.x + 1) * cellSize, (food.y + 1) * cellSize, foodPaint);

        // Mettre à jour le score dans le TextView
        if (scoreText != null) {
            scoreText.setText("Score: " + score);
        }

        // Calculer le temps restant
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = 30 - (elapsedTime / 1000); // 30 secondes de temps total
        if (remainingTime < 0) remainingTime = 0;

        // Mettre à jour le temps restant dans le TextView
        if (timeLeftText != null) {
            timeLeftText.setText("Temps restant: " + remainingTime + "s");
        }


    }

    /**
     * Méthode pour déplacer le serpent en fonction de la direction
     * Copie de la tête du serpent
     * Calcul de la prochaine position de la tête selon la direction
     */
    private void moveSnake() {
        Point head = new Point(snake.get(0));
        switch (direction) {
            case 0: head.y--; break; // Haut
            case 1: head.x++; break; // Droite
            case 2: head.y++; break; // Bas
            case 3: head.x--; break; // Gauche
        }

        int maxCols = getWidth() / cellSize;
        int maxRows = getHeight() / cellSize;

        // Collision avec le mur
        if (head.x < 0 || head.y < 0 || head.x >= maxCols || head.y >= maxRows) {
            Log.d("SnakeGame", "Collision avec le mur: " + head.toString());
            return;  // Ne fait rien si on touche le mur
        }

        // Collision avec le serpent lui-même
        /*for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                isGameOver = true; // Fin du jeu si on touche soi-même
                Log.d("SnakeGame", "Collision avec soi-même: " + head.toString());
                if (onGameOverListener != null) {
                    onGameOverListener.onGameOver(score); // Appel du listener avec le score
                }
                return;
            }
        }

         */

        snake.add(0, head); // Ajoute la tête du serpent
        if (head.equals(food)) {
            score++;
            scoreText.setText("Score: " + score);
            spawnFood();
        } else {
            snake.remove(snake.size() - 1); // Retire la dernière partie du serpent si il n'a pas mangé
        }
    }

    /**
     * Méthode pour gérer les événements de déplacements
     * Récupère la position X du toucher
     * Récupère la position Y du toucher
     * Détection du swipe pour changer de direction
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - touchStartX;// Calcul du mouvement horizontal
                float dy = event.getY() - touchStartY; // Calcul du mouvement vertical

                // Seuil pour les déplacements horizontaux et verticaux
                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0 && direction != 3) direction = 1; // Droite
                    else if (dx < 0 && direction != 1) direction = 3; // Gauche
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

    /**
     * Méthode principale pour la boucle de jeu
     * Si le jeu est terminé, on arrête la boucle
     * Déplace le serpent
     */
    @Override
    public void run() {
        if (isGameOver) return;  // Si le jeu est terminé, on arrête tout

        if (isMoving) moveSnake();

        // Vérifie si le temps est écoulé (30s)
        if ((System.currentTimeMillis() - startTime) / 1000 > 30) {
            isGameOver = true; // Si le temps est écoulé, on termine la partie
            if (onGameOverListener != null) {
                onGameOverListener.onGameOver(score); // Appel du listener avec le score
            }
        }

        invalidate();  // Redessine l'écran
        postDelayed(this, 300);  // Met à jour toutes les 300ms
    }

    // Méthode pour redémarrer le jeu après la fin de la partie
    public void restartGame() {
        isGameOver = false;
        init(); // Réinitialise tous les éléments du jeu
    }
}
