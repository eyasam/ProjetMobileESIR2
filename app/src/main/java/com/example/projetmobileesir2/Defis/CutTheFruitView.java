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

public class CutTheFruitView extends View {


    // === LOGIQUE DE JEU ===
    private final List<Fruit> fruits = new ArrayList<>();// Liste des fruits actuellement à l'écran

    private final List<SplashEffect> splashes = new ArrayList<>(); // Liste des effets de "splash" temporaires après qu’un fruit a été découpé


    private final List<Bitmap> fruitImages = new ArrayList<>(); // Liste des images disponibles pour les fruits

    private final Random random = new Random();

    private final Handler handler = new Handler();// Handler utilisé pour planifier les boucles de jeu et apparitions

    private final Runnable gameLoop;  //s’exécute toutes les 30ms pour animer le jeu

    private boolean isGameRunning = true; // état général du jeu
    private int score = 0;

    private final TextView scoreText; //références pour afficher le score et le timer
    private TextView timerText;

    // === RESSOURCES ===
    private Bitmap splashBitmap; // Bitmaps pour les effets visuels et bombes
    private Bitmap bombBitmap;

    private final Context context;


    // === APPELLE DE FIN DE JEU ===
    public interface OnGameEndListener {
        void onGameEnd(int score);
    }

    private final OnGameEndListener endListener;

    public CutTheFruitView(Context context, OnGameEndListener listener, TextView scoreText) {
        super(context);
        this.context = context;
        this.endListener = listener;
        this.scoreText = scoreText;

        loadBitmaps(context); // charger toutes les images nécessaires une fois au démarrage

        /*
          Définition de la boucle principale du jeu, elle tourne en arrière-plan toutes les 30ms :
          elle met à jour le déplacement des fruits , déclenche un onDraw
         */
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!isGameRunning) return;
                updateFruits();
                invalidate(); // demande à redessiner l'écran
                handler.postDelayed(this, 30); // replanification dans 30ms
            }
        };

        spawnFruit(); // faire apparaitre les fruits
        handler.post(gameLoop); // pour démarrer la boucle de jeu immédiatement
    }

    /**
     * Cette méthode permet de connecter une TextView timer externe pour l'afficher depuis la vue.
     */
    public void setTimerText(TextView timerText) {
        this.timerText = timerText;
    }

    /**
     * Méthode appelée à la fin du jeu.
     * Elle stoppe la boucle et appelle le callback pour signaler le score final.
     */

    public void endGame() {
        isGameRunning = false;
        if (endListener != null) {
            endListener.onGameEnd(score);
        }
    }

    /**
     * Charge et redimensionne les images utilisées dans le jeu (fruits, bombes, effet splash).
     * On stocke les bitmaps une seule fois pour éviter des rechargements inutiles.
     */

    private void loadBitmaps(Context context) {
        fruitImages.add(resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.banana)));
        fruitImages.add(resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.apple)));
        fruitImages.add(resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry)));
        fruitImages.add(resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.watermelon)));

        // Splash lorsqu'on découpe le fruit
        splashBitmap = resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.fruit_splash), 200, 200);

        // Bombe si on la découpe on mets score -1
        bombBitmap = resize(BitmapFactory.decodeResource(context.getResources(), R.drawable.bomb));
    }

    // Redimensionne un bitmap à une taille standard pour régler la taille des fruits
    private Bitmap resize(Bitmap src) {
        return Bitmap.createScaledBitmap(src, 150, 160, true);
    }

    // Surcharge de la méthode resize avec dimensions personnalisées
    private Bitmap resize(Bitmap src, int w, int h) {
        return Bitmap.createScaledBitmap(src, w, h, true);
    }

    /**
     * Cette méthode est appelée en boucle pour créer des fruits ou des bombes.
     * Elle crée un petit lot de 3 à 5 fruits, espacés de quelques millisecondes,
     * puis se rappelle elle-même après un petit délai pour créer le prochain lot.
     */
    private void spawnFruit() {
        handler.postDelayed(() -> {
            if (!isGameRunning || getWidth() == 0) return;

            int howMany = 3 + random.nextInt(3); // 3 à 5 fruits
            int delayBetween = 200; // délai entre chaque apparition 100ms d'écart entre chaque fruit

            for (int i = 0; i < howMany; i++) {
                final int index = i;
                handler.postDelayed(() -> {
                    boolean spawnBomb = random.nextFloat() < 0.3f; // 30% de chance de bombe

                    Bitmap bmp = spawnBomb
                            ? bombBitmap
                            : fruitImages.get(random.nextInt(fruitImages.size()));

                    float x = random.nextInt(Math.max(1, getWidth() - bmp.getWidth()));
                    fruits.add(new Fruit(x, 0, bmp, spawnBomb)); // Ajout du fruit à la liste pour qu’il soit affiché et animé
                }, index * delayBetween); // ⏱ décalage
            }

            spawnFruit(); // replanifie le lot suivant
        }, 800); // toutes les 0.7s ≈ un lot de fruits
    }


    /** Cette méthode met à jour la position des fruits en les faisant descendre.
     * Supprime les fruits qui sortent de l'écran.
     * Met aussi à jour les effets splash (durée de vie limitée).
     */
    private void updateFruits() {
        Iterator<Fruit> iterator = fruits.iterator();
        while (iterator.hasNext()) {
            Fruit fruit = iterator.next();
            fruit.y += 20; // le fruit descend de 20px
            if (fruit.y > getHeight()) {
                iterator.remove(); // fruit perdu
            }
        }

        // Màj et suppression des effets splash expirés
        Iterator<SplashEffect> splashIterator = splashes.iterator();
        while (splashIterator.hasNext()) {
            SplashEffect s = splashIterator.next();
            s.lifetime--; // la durée diminue chaque frame
            if (s.lifetime <= 0) splashIterator.remove();
        }
    }

    /**
     * Cette méthode dessine tous les éléments graphiques à l’écran :
     * - splash
     * - les fruits en cours de descente
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // On dessine les splash
        for (SplashEffect s : splashes) {
            Paint splashPaint = new Paint();
            splashPaint.setAlpha(s.lifetime * 17); // fade out
            canvas.drawBitmap(splashBitmap, s.x, s.y, splashPaint);
        }

        // On dessine chaque fruit/bombe à sa position
        for (Fruit fruit : fruits) {
            canvas.drawBitmap(fruit.bitmap, fruit.x, fruit.y, null);
        }
    }

    /**
     * Cette méthode est appelée lors des gestes de l’utilisateur.
     * On détecte si le doigt "touche" un fruit on le considère comme si il l'a découpé.
     * Si oui : on le retire, on met à jour le score, on joue un son.
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isGameRunning) return false;

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float touchX = event.getX();
            float touchY = event.getY();

            Iterator<Fruit> iterator = fruits.iterator();
            while (iterator.hasNext()) {
                Fruit fruit = iterator.next();

                // Ici on calcule  la distance entre le doigt et le centre du fruit
                float dx = fruit.getCenterX() - touchX;
                float dy = fruit.getCenterY() - touchY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                // Si le doigt est assez proche → fruit touché
                if (dist < fruit.bitmap.getWidth() / 2f + 20) {
                    iterator.remove();

                    if (fruit.isBomb) {
                        // Bombe touchée →  on perd un point
                        score = Math.max(0, score - 1); // éviter les scores négatifs
                        updateScore();
                        playSound(R.raw.defeat);
                        splashes.add(new SplashEffect(fruit.x, fruit.y));
                    } else {
                        // Fruit normal →  +1 point
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

    /**
     * Màj l’affichage du score dans la TextView associée.
     */
    private void updateScore() {
        if (scoreText != null) {
            scoreText.setText("Score : " + score);
        }
    }

    /**
     * Joue un son, lorsqu’un fruit est découpé ou une bombe est touchée.
     */

    private void playSound(int x) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, x);
        mediaPlayer.start();


    }

    /**
     * Représente un fruit (ou une bombe) avec sa position, son image et son type.
     */

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

    /**
     * Représente le splash du fruit.
     */

    private static class SplashEffect {
        float x, y;
        int lifetime = 15;

        SplashEffect(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
