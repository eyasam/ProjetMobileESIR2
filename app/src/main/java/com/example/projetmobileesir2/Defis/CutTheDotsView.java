package com.example.projetmobileesir2.Defis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.projetmobileesir2.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CutTheDotsView extends View {

    private class Fruit {
        float x, y;
        Bitmap image;
        boolean sliced = false;
        float speed; // Vitesse du fruit
        List<Explosion> explosions = new ArrayList<>(); // Liste des explosions

        Fruit(float x, float y, Bitmap image, float speed) {
            this.x = x;
            this.y = y;
            this.image = image;
            this.speed = speed;
        }

        RectF getBounds() {
            return new RectF(x, y, x + image.getWidth(), y + image.getHeight());
        }
    }

    private class Explosion {
        float x, y, velocityX, velocityY;
        Bitmap image;
        int alpha;
        Paint paint;

        Explosion(float x, float y, Bitmap image) {
            this.x = x;
            this.y = y;
            this.velocityX = (new Random().nextFloat() - 0.5f) * 10; // Direction aléatoire
            this.velocityY = (new Random().nextFloat() - 0.5f) * 10; // Direction aléatoire
            this.image = image;
            this.alpha = 255;
            this.paint = new Paint();
            paint.setAlpha(alpha);
        }

        void update() {
            x += velocityX;
            y += velocityY;
            alpha -= 10; // Diminution de la transparence
            paint.setAlpha(alpha);
        }
    }

    private List<Fruit> fruits = new ArrayList<>();
    private Paint paint = new Paint();
    private Random random = new Random();
    private Handler handler = new Handler();
    private Runnable gameLoop;
    private boolean isRunning = true;

    private TextView scoreText;
    private TextView timerText;
    private int score = 0;
    private MediaPlayer slashSound, explosionSound;

    private int[] fruitIds = {
            R.drawable.apple, R.drawable.banana, R.drawable.strawberry, R.drawable.watermelon
    };

    private long gameStartTime;

    public CutTheDotsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CutTheDotsView(Context context, AttributeSet attrs, TextView scoreText) {
        super(context, attrs);
        this.scoreText = scoreText;  // Initialiser le TextView ici
        init();
    }


    private void init() {
        slashSound = MediaPlayer.create(getContext(), R.raw.slash_sound);
        //explosionSound = MediaPlayer.create(getContext(), R.raw.explosion_sound);
        startGameLoop();
        gameStartTime = System.currentTimeMillis(); // Capture le début du jeu
    }

    public void setScoreText(TextView scoreText) {
        this.scoreText = scoreText;
    }

    public void setTimerText(TextView timerText) {
        this.timerText = timerText;
    }

    private void startGameLoop() {
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    spawnFruit();
                    updatePositions();
                    invalidate();
                    handler.postDelayed(this, 30);
                }
            }
        };
        handler.post(gameLoop);
    }

    private void spawnFruit() {
        if (getWidth() <= 120) return; // Ne rien faire si la largeur est insuffisante

        if (fruits.size() < 5 && random.nextInt(10) > 7) {
            int fruitId = fruitIds[random.nextInt(fruitIds.length)];
            Bitmap fruit = BitmapFactory.decodeResource(getResources(), fruitId);
            Bitmap resized = Bitmap.createScaledBitmap(fruit, 120, 120, false);
            float x = random.nextInt(getWidth() - 120);
            float speed = 5 + random.nextFloat() * 5; // Vitesse aléatoire de base
            fruits.add(new Fruit(x, 0, resized, speed)); // Start fruit from the top (y = 0)
        }
    }

    private void updatePositions() {
        Iterator<Fruit> iterator = fruits.iterator();
        long elapsedTime = System.currentTimeMillis() - gameStartTime;
        float speedMultiplier = 1 + (elapsedTime / 10000f); // Accélérer au fil du temps

        while (iterator.hasNext()) {
            Fruit fruit = iterator.next();
            fruit.y += fruit.speed * speedMultiplier; // Vitesse variable avec accélération
            if (fruit.y > getHeight()) {
                iterator.remove(); // Enlever les fruits hors écran
            }

            // Update explosions
            Iterator<Explosion> explosionIterator = fruit.explosions.iterator();
            while (explosionIterator.hasNext()) {
                Explosion explosion = explosionIterator.next();
                explosion.update();
                if (explosion.alpha <= 0) {
                    explosionIterator.remove(); // Enlever les explosions terminées
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Fruit fruit : fruits) {
            if (!fruit.sliced) {
                canvas.drawBitmap(fruit.image, fruit.x, fruit.y, paint);
            }

            // Draw explosions
            for (Explosion explosion : fruit.explosions) {
                canvas.drawBitmap(explosion.image, explosion.x, explosion.y, explosion.paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isRunning) return false;

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();

            for (Fruit fruit : fruits) {
                if (!fruit.sliced && fruit.getBounds().contains(x, y)) {
                    fruit.sliced = true;
                    fruit.explosions.add(new Explosion(fruit.x, fruit.y, BitmapFactory.decodeResource(getResources(), R.drawable.fruit_splash))); // Explosion avec des fragments
                    playSlashSound();
                    playExplosionSound();
                    score++;
                    if (scoreText != null) {
                        scoreText.setText("Score: " + score);
                    }
                    break;  // Ne couper qu'un fruit à la fois
                }
            }
        }

        return true;
    }

    private void playSlashSound() {
        if (slashSound != null) {
            slashSound.start();
        }
    }

    public int getScore() {
        return score;
    }

    private void playExplosionSound() {
        if (explosionSound != null) {
            explosionSound.start();
        }
    }

    public void endGame() {
        isRunning = false;
        handler.removeCallbacks(gameLoop);
    }
}
