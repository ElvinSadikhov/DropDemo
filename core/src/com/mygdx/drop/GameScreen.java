package com.mygdx.drop;
/*
Main Game Screen
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {

    final Drop game;
    OrthographicCamera camera;
    Texture dropImage;
    Texture bucketImage;
    Sound dropSound;
    Music rainMusic;
    Rectangle bucket;
    Vector3 touchPos;
    Array<Rectangle> raindrops; // better than ArrayList (grabage collector stuff)
    long lastDropTime;
    int dropsGatheredSoFar;

    private void spawnRainDrop() {
        Rectangle drop = new Rectangle();
        drop.x = MathUtils.random(0, 800 - 64);
        drop.y = 480;
        drop.width = 64;
        drop.height = 64;
        raindrops.add(drop);
        lastDropTime = TimeUtils.nanoTime();
    }

    public GameScreen(final Drop game) {

        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        touchPos = new Vector3();

        dropImage = new Texture("droplet.png");
        bucketImage = new Texture("bucket.png");

        dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));

        rainMusic.setLooping(true);
        rainMusic.play();

        // creating and placing a bucket
        bucket = new Rectangle();
        bucket.width = 64;
        bucket.height = 64;
        bucket.x = 800 / 2 - 64 / 2;
        bucket.y = 20;

        raindrops = new Array<>();
        spawnRainDrop();
    }

    @Override
    public void render(float delta) {
        // screen is set to be blue
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.begin();
        game.font.draw(game.batch, "Drops collected: "+dropsGatheredSoFar,0,480);
        game.batch.setProjectionMatrix(camera.combined);
        // drawing a bucket
        game.batch.draw(bucketImage, bucket.x, bucket.y);

        // drawing drops
        for (Rectangle drop : raindrops) {
            game.batch.draw(dropImage, drop.x, drop.y);
        }

        game.batch.end();

        // if a user touches the screen
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }

        // if user uses LEFT and RIGHT keys on desktop
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

        // making sure that the bucket won't go away from the screen
        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > 800 - 64) bucket.x = 800 - 64;

        // frequency of spawning drops
        if (TimeUtils.nanoTime() - lastDropTime > 500000000) spawnRainDrop();

        Iterator<Rectangle> iter = raindrops.iterator();

        // drop logic
        while (iter.hasNext()) {
            Rectangle drop = iter.next();
            drop.y -= 400 * Gdx.graphics.getDeltaTime();

            if (drop.y + 64 < 0) iter.remove();

            // if drop and bucket overlap
            if (drop.overlaps(bucket)) {
                dropsGatheredSoFar++;
                dropSound.play();
                iter.remove();
            }
        }
    }

    @Override
    public void show() {
        rainMusic.play();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        dropSound.dispose();
        dropImage.dispose();
        bucketImage.dispose();
        rainMusic.dispose();
    }
}
