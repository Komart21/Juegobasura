package com.mpes;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    Texture backgroundTexture;
    Texture gameOverBackgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Sound gameOverSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite;
    Vector2 touchPos;
    Array<Sprite> dropSprites;
    float dropTimer;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;
    int dropCount; // Contador de gotas capturadas
    boolean gameOver; // Indica si el juego terminó
    BitmapFont font; // Para mostrar texto en pantalla
    float dropSpeed = 2f; // Velocidad de las gotas, aumentará con el tiempo
    private Texture resetButtonTexture; // Imagen del botón de reinicio

    @Override
    public void create() {
        backgroundTexture = new Texture("fondoini.jpeg");
        gameOverBackgroundTexture = new Texture("game_over_background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");
        resetButtonTexture = new Texture("restart.png"); // Cargar la imagen del botón de reinicio
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        gameOverSound = Gdx.audio.newSound(Gdx.files.internal("game_over.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1, 1);
        touchPos = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        dropCount = 0; // Inicializa el contador
        font = new BitmapFont(); // Carga una fuente por defecto
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
        gameOver = false; // Juego empieza sin estar en modo game over
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        if (!gameOver) {
            input();
            logic();
        } else {
            // Detectar si se toca para reiniciar
            if (Gdx.input.isTouched()) {
                touchPos.set(Gdx.input.getX(), Gdx.input.getY());
                viewport.unproject(touchPos);

                // Detección de toque para reiniciar
                float resetButtonWidth = 1.5f;
                float resetButtonHeight = 1.5f;

                float resetButtonX = viewport.getWorldWidth() / 2 - resetButtonWidth / 2;
                float resetButtonY = viewport.getWorldHeight() / 2 - 2.5f;

                if (touchPos.x >= resetButtonX && touchPos.x <= resetButtonX + resetButtonWidth &&
                    touchPos.y >= resetButtonY && touchPos.y <= resetButtonY + resetButtonHeight) {
                    resetGame(); // Reiniciar el juego si se toca el área del reinicio
                }
            }
        }
        draw();
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }
    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime();
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-dropSpeed * delta); // Aumentar la velocidad con el tiempo
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            if (dropSprite.getY() < -dropHeight) {
                gameOver(); // Si una gota toca el suelo, el juego termina
            } else if (bucketRectangle.overlaps(dropRectangle) && dropSprite.getY() > bucketSprite.getY() + bucketHeight / 2) {
                dropSprites.removeIndex(i);
                dropSound.play();
                dropCount++; // Incrementa el contador de gotas capturadas
            }
        }

        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
            dropSpeed += 0.1f; // Aumenta la velocidad conforme pasa el tiempo
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);  // Limpiar la pantalla
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);  // Establecer la cámara para la proyección
        spriteBatch.begin();  // Iniciar el dibujo de los elementos

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // Dibujar fondo y elementos solo si no está en Game Over
        if (!gameOver) {
            // 1. Dibujar el fondo del juego primero
            spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);

            // 2. Dibujar la cubeta y las gotas encima del fondo
            bucketSprite.draw(spriteBatch);

            // Dibujar las gotas
            for (Sprite dropSprite : dropSprites) {
                dropSprite.draw(spriteBatch);
            }

        } else {
            // 1. Dibujar el fondo de Game Over primero
            spriteBatch.draw(gameOverBackgroundTexture, 0, 0, worldWidth, worldHeight);

            // 2. Dibujar solo el dropCount en la parte superior derecha de la pantalla de Game Over
            font.setColor(Color.WHITE);
            font.getData().setScale(0.1f); // Ajusta el tamaño del texto
            font.draw(spriteBatch, "" + dropCount, worldWidth - 1.8f, worldHeight - 0.3f);  // Mostrar solo el número en la parte superior derecha

            // 3. Dibujar el botón de reinicio (con la imagen) más abajo en la pantalla
            float resetButtonWidth = 1.5f;
            float resetButtonHeight = 1.5f;

            float resetButtonX = worldWidth / 2 - resetButtonWidth / 2;
            float resetButtonY = worldHeight / 2 - 2.5f;

            spriteBatch.draw(resetButtonTexture, resetButtonX, resetButtonY, resetButtonWidth, resetButtonHeight);  // Dibuja el botón de reinicio
        }

        spriteBatch.end();  // Finalizar el dibujo
    }

    private void createDroplet() {
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }

    private void gameOver() {
        gameOver = true;
        music.stop(); // Detener la música
        gameOverSound.play(); // Sonido de fin de juego
        dropSprites.clear(); // Limpia las gotas en pantalla
    }

    // Reiniciar el juego
    private void resetGame() {
        gameOver = false;
        dropCount = 0;
        dropSpeed = 2f;
        music.play();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        gameOverBackgroundTexture.dispose();
        bucketTexture.dispose();
        dropTexture.dispose();
        resetButtonTexture.dispose(); // Asegúrate de liberar la textura del botón de reinicio
        dropSound.dispose();
        gameOverSound.dispose();
        music.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
