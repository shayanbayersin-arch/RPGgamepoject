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

    private Texture mapTexture;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;

    // Переменные для Джунгара
    private Texture jungarSheet;
    private TextureRegion jungarIdleFrame;

    // ПЕРЕДВИНУЛИ ТОЧНО В ЦЕНТР КОРЕЧНЕВОГО КРУГА (на основе 1280x720)
    private float jungarX = 1020; // Идеальная точка центра круга справа
    private float jungarY = 320;  // Высота центра круга

    private boolean isJungarSpawned = false;
    private int jungarLevel = 1;
    private float jungarScale = 1.0f;

    // Триггер-зона (начинается прямо перед заходом на круг)
    private float arenaTriggerX = 850;
    private float arenaTriggerY = 100;
    private float arenaTriggerWidth = 400;
    private float arenaTriggerHeight = 450;

    private float playerX = 300, playerY = 200;
    private float stateTime = 0f;
    private boolean isMoving = false;
    private boolean facingLeft = false;

    public PlayScreen(BatyrGame game) {
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        mapTexture = new Texture(Gdx.files.internal("map.png"));
        jungarSheet = new Texture(Gdx.files.internal("jungar_sheet.png"));

        // Вырезаем первый кадр рыцаря (64x64)
        jungarIdleFrame = new TextureRegion(jungarSheet, 0, 0, 64, 64);

        // Анимации Батыра
        Texture idleSheet = new Texture(Gdx.files.internal("_Idle.png"));
        TextureRegion[][] idleTmp = TextureRegion.split(idleSheet, 120, 80);
        TextureRegion[] idleFrames = new TextureRegion[10];
        for (int i = 0; i < 10; i++) idleFrames[i] = idleTmp[0][i];
        idleAnimation = new Animation<>(0.1f, idleFrames);

        Texture runSheet = new Texture(Gdx.files.internal("_Run.png"));
        TextureRegion[][] runTmp = TextureRegion.split(runSheet, 120, 80);
        TextureRegion[] runFrames = new TextureRegion[10];
        for (int i = 0; i < 10; i++) runFrames[i] = runTmp[0][i];
        runAnimation = new Animation<>(0.08f, runFrames);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stateTime += delta;

        // 1. ЛОГИКА ДВИЖЕНИЯ БАТЫРА
        isMoving = false;
        float speed = 250 * delta;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) { playerY += speed; isMoving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { playerY -= speed; isMoving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { playerX -= speed; isMoving = true; facingLeft = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { playerX += speed; isMoving = true; facingLeft = false; }

        // 2. ПРОВЕРКА ВХОДА НА АРЕНУ (СПАВН)
        if (!isJungarSpawned) {
            if (playerX >= arenaTriggerX && playerX <= (arenaTriggerX + arenaTriggerWidth) &&
                playerY >= arenaTriggerY && playerY <= (arenaTriggerY + arenaTriggerHeight)) {

                isJungarSpawned = true;
                jungarScale = 1.0f + (jungarLevel - 1) * 0.4f;

                // Возвращаем в центр круга при каждом новом спавне
                jungarX = 1020;
                jungarY = 320;
            }
        }

        // 3. ЛОГИКА ПРЕСЛЕДОВАНИЯ
        if (isJungarSpawned) {
            float jungarSpeed = 130 * delta; // Скорость бега Джунгара за тобой

            if (jungarX < playerX) jungarX += jungarSpeed;
            if (jungarX > playerX) jungarX -= jungarSpeed;
            if (jungarY < playerY) jungarY += jungarSpeed;
            if (jungarY > playerY) jungarY -= jungarSpeed;
        }

        // 4. ТЕСТОВАЯ ПОБЕДА (ПРОБЕЛ)
        if (isJungarSpawned && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            isJungarSpawned = false;
            jungarLevel++;
            playerX = 500; // Отбрасываем Батыра назад
        }

        batch.begin();

        // Рисуем карту на весь экран
        batch.draw(mapTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Рисуем Джунгара
        if (isJungarSpawned) {
            float baseWidth = 140;
            float baseHeight = 140;
            float currentWidth = baseWidth * jungarScale;
            float currentHeight = baseHeight * jungarScale;

            // Джунгар всегда смотрит на Батыра
            if (playerX < jungarX && !jungarIdleFrame.isFlipX()) jungarIdleFrame.flip(true, false);
            if (playerX > jungarX && jungarIdleFrame.isFlipX()) jungarIdleFrame.flip(false, false);

            batch.draw(jungarIdleFrame,
                jungarX - currentWidth/2,
                jungarY - currentHeight/2,
                currentWidth,
                currentHeight);
        }

        // Кадр Батыра
        TextureRegion currentFrame = isMoving ? runAnimation.getKeyFrame(stateTime, true) : idleAnimation.getKeyFrame(stateTime, true);

        // Поворот Батыра
        if (facingLeft && !currentFrame.isFlipX()) currentFrame.flip(true, false);
        if (!facingLeft && currentFrame.isFlipX()) currentFrame.flip(false, false);

        // Рисуем Батыра
        batch.draw(currentFrame, playerX - 60, playerY - 40, 120, 80);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        mapTexture.dispose();
        if (jungarSheet != null) jungarSheet.dispose();
    }
}
