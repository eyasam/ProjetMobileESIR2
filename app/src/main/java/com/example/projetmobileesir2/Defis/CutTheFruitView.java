package com.example.projetmobileesir2.Defis;

import android.content.Context;
import android.graphics.*;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.projetmobileesir2.R;

import java.util.*;

/**
 * gère l'affichage, les collisions, les effets visuels et le score en temps réel.
 */

public class CutTheFruitView extends View {

    private final List<Fruit> fruits = new ArrayList<>();
    private final List<SplashEffect> splashes = new ArrayList<>();
    private final List<Bitmap> fruitImages = new ArrayList<>();

    private final Random random = new Random();
    private final Handler handler = new Handler();
    private final Runnable gameLoop;

    private boolean isGameRunning = true;
    private int score = 0;

    private final TextView scoreText;
    private TextView timerText;

    private Bitmap splashBitmap;
    private Bitmap bombBitmap;

    private final Context context;

    public interface OnGameEndListener {
        void onGameEnd(int score);
    }

    private final OnGameEndListener endListener;

    public CutTheFruitView(Context context, OnGameEndListener listener, TextView scoreText) {
        super(context);
        this.context = context;
        this.endListener = listener;
        this.scoreText = scoreText;

        loadBitmaps(context);

        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!isGameRunning) return;
                updateFruits();
                invalidate();
                handler.postDelayed(this, 30);
            }
        };

        spawnFruit();
        handler.post(gameLoop);
    }

    public void setTimerText(TextView timerText) {
        this.timerText = timerText;
    }

    public void endGame() {
        isGameRunning = false;
        if (endListener != null) {
            endListener.onGameEnd(score);
        }
    }

    private void loadBitmaps(Context context) {
        fruitImages.add(resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.banana)));
        fruitImages.add(resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.apple)));
        fruitImages.add(resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry)));
        fruitImages.add(resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.watermelon)));

        splashBitmap = resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.fruit_splash), 200, 200);
        bombBitmap = resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.bomb));
    }

    private Bitmap resize(Bitmap src) {
        return Bitmap.createScaledBitmap(src, 150, 160, true);
    }

    private Bitmap resize(Bitmap src, int w, int h) {
        return Bitmap.createScaledBitmap(src, w, h, true);
    }
    private void spawnFruit() {
        handler.postDelayed(() -> {
            if (!isGameRunning || getWidth() == 0) return;

            int howMany = 3 + random.nextInt(3);
            int delayBetween = 200;

            for (int i = 0; i < howMany; i++) {
                final int index = i;
                handler.postDelayed(() -> {
                    boolean spawnBomb = random.nextFloat() < 0.3f;

                    Bitmap bmp = spawnBomb
                            ? bombBitmap
                            : fruitImages.get(random.nextInt(fruitImages.size()));

                    float x = random.nextInt(Math.max(1, getWidth() - bmp.getWidth()));
                    fruits.add(new Fruit(x, 0, bmp, spawnBomb));
                }, index * delayBetween);
            }

            spawnFruit();
        }, 800);
    }



    private void updateFruits() {
        Iterator<Fruit> iterator = fruits.iterator();
        while (iterator.hasNext()) {
            Fruit fruit = iterator.next();
            fruit.y += 20;
            if (fruit.y > getHeight()) {
                iterator.remove();
            }
        }

        Iterator<SplashEffect> splashIterator = splashes.iterator();
        while (splashIterator.hasNext()) {
            SplashEffect s = splashIterator.next();
            s.lifetime--;
            if (s.lifetime <= 0) splashIterator.remove();
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        for (SplashEffect s : splashes) {
            Paint splashPaint = new Paint();
            splashPaint.setAlpha(s.lifetime * 17);
            c.drawBitmap(splashBitmap, s.x, s.y, splashPaint);
        }

        for (Fruit fruit : fruits) {
            c.drawBitmap(fruit.bitmap, fruit.x, fruit.y, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isGameRunning) return false;

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float touchX = event.getX();
            float touchY = event.getY();

            Iterator<Fruit> iterator = fruits.iterator();
            while (iterator.hasNext()) {
                Fruit fruit = iterator.next();

                float dx = fruit.getCenterX() - touchX;
                float dy = fruit.getCenterY() - touchY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < fruit.bitmap.getWidth() / 2f + 20) {
                    iterator.remove();

                    if (fruit.isBomb) {
                        score = Math.max(0, score - 1); // eviter les scores négati
                        updateScore();
                        playSound(R.raw.defeat);
                        splashes.add(new SplashEffect(fruit.x, fruit.y));
                    } else {
                        score++;
                        updateScore();
                        playSound(R.raw.pick);
                        splashes.add(new SplashEffect(fruit.x, fruit.y));
                    }
                }
            }
        }
        return true;
    }

    private void updateScore() {
        if (scoreText != null) {
            scoreText.setText("Score : " + score);
        }
    }

    private void playSound(int x) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, x);
        mediaPlayer.start();


    }

    private static class Fruit {
        float x, y;
        final Bitmap bitmap;
        final boolean isBomb;

        Fruit(float x, float y, Bitmap bitmap, boolean isBomb) {
            this.x = x;
            this.y = y;
            this.bitmap = bitmap;
            this.isBomb = isBomb;
        }

        float getCenterX() {
            return x + bitmap.getWidth() / 2f;
        }

        float getCenterY() {
            return y + bitmap.getHeight() / 2f;
        }
    }

    private static class SplashEffect {
        float x, y;
        int lifetime = 15;

        SplashEffect(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}