package com.batyrlegacy.game.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.batyrlegacy.game.BatyrGame;

public class PlayScreen extends ScreenAdapter {
    private SpriteBatch batch;

    // Текстуры и анимации
    private Texture mapTexture;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;

    // Координаты и состояние игрока
    private float playerX = 100, playerY = 100;
    private float stateTime = 0f;
    private boolean isMoving = false;
    private boolean facingLeft = false;

    public PlayScreen(BatyrGame game) {
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        // 1. Загружаем твою карту из LDtk
        mapTexture = new Texture(Gdx.files.internal("map.png"));

        // 2. Загружаем анимацию покоя (_Idle.png)
        Texture idleSheet = new Texture(Gdx.files.internal("_Idle.png"));
        TextureRegion[][] idleTmp = TextureRegion.split(idleSheet, 120, 80);
        TextureRegion[] idleFrames = new TextureRegion[10];
        for (int i = 0; i < 10; i++) idleFrames[i] = idleTmp[0][i];
        idleAnimation = new Animation<>(0.1f, idleFrames);
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // 3. Загружаем анимацию бега (_Run.png)
        Texture runSheet = new Texture(Gdx.files.internal("_Run.png"));
        TextureRegion[][] runTmp = TextureRegion.split(runSheet, 120, 80);
        TextureRegion[] runFrames = new TextureRegion[10];
        for (int i = 0; i < 10; i++) runFrames[i] = runTmp[0][i];
        runAnimation = new Animation<>(0.08f, runFrames);
        runAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    @Override
    public void render(float delta) {
        // Очистка экрана
        ScreenUtils.clear(0, 0, 0, 1);
        stateTime += delta;

        // Логика управления
        isMoving = false;
        float speed = 200 * delta;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) { playerY += speed; isMoving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { playerY -= speed; isMoving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= speed;
            isMoving = true;
            facingLeft = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += speed;
            isMoving = true;
            facingLeft = false;
        }

        batch.begin();

        // РИСУЕМ КАРТУ (на весь экран)
        batch.draw(mapTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // ВЫБИРАЕМ КАДР (Бег или Покой)
        TextureRegion currentFrame;
        if (isMoving) {
            currentFrame = runAnimation.getKeyFrame(stateTime);
        } else {
            currentFrame = idleAnimation.getKeyFrame(stateTime);
        }

        // ПОВОРОТ ГЕРОЯ (влево/вправо)
        if (facingLeft && !currentFrame.isFlipX()) currentFrame.flip(true, false);
        if (!facingLeft && currentFrame.isFlipX()) currentFrame.flip(false, false);

        // РИСУЕМ БАТЫРА
        batch.draw(currentFrame, playerX - 60, playerY - 40, 120, 80);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        mapTexture.dispose();
    }
}
