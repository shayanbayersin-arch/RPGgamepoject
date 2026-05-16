package com.batyrlegacy.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.batyrlegacy.game.BatyrGame;

public class PlayScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private Texture mapTexture;

    // Анимации Батыра
    private Animation<TextureRegion> playerIdleAnim;
    private Animation<TextureRegion> playerRunAnim;
    private Animation<TextureRegion> playerAttack1Anim; // ЛКМ
    private Animation<TextureRegion> playerAttack2Anim; // ПКМ

    // Анимации Джунгара
    private Animation<TextureRegion> jungarIdleAnim;
    private Animation<TextureRegion> jungarRunAnim;
    private Animation<TextureRegion> jungarAttackAnim;

    // Позиции и состояния персонажей
    private Vector2 playerPos = new Vector2(300, 200);
    private Vector2 jungarPos = new Vector2(1020, 320);

    private float stateTime = 0f;
    private float playerAttackTime = 0f;
    private float jungarAttackTime = 0f;

    private boolean isPlayerMoving = false;
    private int playerAttackType = 0; // 0 - покой/бег, 1 - ЛКМ, 2 - ПКМ
    private boolean playerFacingLeft = false;

    private boolean isJungarSpawned = false;
    private boolean isJungarMoving = false;
    private boolean isJungarAttacking = false;
    private int jungarLevel = 1;
    private float jungarScale = 1.0f;

    // Зона триггера арены для окна 1280x720
    private Rectangle arenaTrigger = new Rectangle(850, 100, 400, 450);

    public PlayScreen(BatyrGame game) {
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        mapTexture = new Texture(Gdx.files.internal("map.png"));

        // === 1. ЗАГРУЗКА АНИМАЦИЙ БАТЫРА ===
        // Загружаем файлы, строго соблюдая регистр букв как в твоей папке assets
        playerIdleAnim = createAnimFromSheet(new Texture(Gdx.files.internal("_idle.png")), 0, 10, 120, 80, 0.1f);
        playerRunAnim = createAnimFromSheet(new Texture(Gdx.files.internal("_Run.png")), 0, 10, 120, 80, 0.08f);
        playerAttack1Anim = createAnimFromSheet(new Texture(Gdx.files.internal("_Attack.png")), 0, 4, 120, 80, 0.07f);
        playerAttack2Anim = createAnimFromSheet(new Texture(Gdx.files.internal("_Attack2.png")), 0, 4, 120, 80, 0.07f);

        // === 2. ЗАГРУЗКА ДЖУНГАРА ===
        // Исправлено: игра берёт jungar_sheet.png, который реально лежит в твоей папке assets
        Texture jSheet = new Texture(Gdx.files.internal("jungar_sheet.png"));
        jungarIdleAnim = createAnimFromSheet(jSheet, 0, 6, 64, 64, 0.12f);   // 1 строка - покой
        jungarRunAnim = createAnimFromSheet(jSheet, 1, 6, 64, 64, 0.08f);    // 2 строка - бег
        jungarAttackAnim = createAnimFromSheet(jSheet, 3, 6, 64, 64, 0.08f); // 4 строка - удар мечом
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stateTime += delta;

        // --- ЛОГИКА СЧИТЫВАНИЯ КЛИКОВ МЫШИ (БОЙ) ---
        if (playerAttackType == 0) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                playerAttackType = 1;
                playerAttackTime = 0f;
            } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                playerAttackType = 2;
                playerAttackTime = 0f;
            }
        }

        // Обработка таймера анимации удара Батыра
        if (playerAttackType != 0) {
            playerAttackTime += delta;
            if (playerAttackTime > 0.28f) { // 4 кадра * 0.07 сек = 0.28
                playerAttackType = 0;
            }
        }

        // --- ЛОГИКА ДВИЖЕНИЯ БАТЫРА (Только если не бьет) ---
        isPlayerMoving = false;
        if (playerAttackType == 0) {
            Vector2 input = new Vector2();
            if (Gdx.input.isKeyPressed(Input.Keys.W)) { input.y += 1; isPlayerMoving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) { input.y -= 1; isPlayerMoving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) { input.x -= 1; isPlayerMoving = true; playerFacingLeft = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) { input.x += 1; isPlayerMoving = true; playerFacingLeft = false; }

            float playerSpeed = 250 * delta;
            if (isPlayerMoving) {
                playerPos.add(input.nor().scl(playerSpeed));
            }
        }

        // --- СПАВН ДЖУНГАРА ---
        if (!isJungarSpawned) {
            if (arenaTrigger.contains(playerPos.x, playerPos.y)) {
                isJungarSpawned = true;
                jungarScale = 1.0f + (jungarLevel - 1) * 0.4f;
                jungarPos.set(1020, 320); // Центр арены
            }
        }

        // --- ЛОГИКА ПРЕСЛЕДОВАНИЯ И АТАКИ ДЖУНГАРА ---
        isJungarMoving = false;
        if (isJungarSpawned) {
            float distanceToPlayer = jungarPos.dst(playerPos);

            if (isJungarAttacking) {
                jungarAttackTime += delta;
                if (jungarAttackTime > 0.48f) { // В анимации удара 6 кадров по 0.08 сек
                    isJungarAttacking = false;
                }
            } else {
                if (distanceToPlayer < 50) { // Подошел вплотную — бьет Батыра
                    isJungarAttacking = true;
                    jungarAttackTime = 0f;
                } else { // Если далеко — бежит за Батыром
                    isJungarMoving = true;
                    float jungarSpeed = 130 * delta;
                    Vector2 direction = playerPos.cpy().sub(jungarPos).nor();
                    jungarPos.add(direction.scl(jungarSpeed));
                }
            }
        }

        // СМЕРТЬ ДЖУНГАРА НА ПРОБЕЛ (Для теста левелов)
        if (isJungarSpawned && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            isJungarSpawned = false;
            isJungarAttacking = false;
            jungarLevel++;
            playerPos.set(500, 200);
        }

        batch.begin();

        // 1. Рисуем карту
        batch.draw(mapTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 2. Рисуем Джунгара
        if (isJungarSpawned) {
            float currentWidth = 140 * jungarScale;
            float currentHeight = 140 * jungarScale;

            Animation<TextureRegion> activeJungarAnim = jungarIdleAnim;
            float timeToUse = stateTime;

            if (isJungarAttacking) {
                activeJungarAnim = jungarAttackAnim;
                timeToUse = jungarAttackTime;
            } else if (isJungarMoving) {
                activeJungarAnim = jungarRunAnim;
            }

            TextureRegion jFrame = activeJungarAnim.getKeyFrame(timeToUse, !isJungarAttacking);

            // Джунгар следит за направлением героя
            if (playerPos.x < jungarPos.x && !jFrame.isFlipX()) jFrame.flip(true, false);
            if (playerPos.x > jungarPos.x && jFrame.isFlipX()) jFrame.flip(false, false);

            batch.draw(jFrame, jungarPos.x - currentWidth/2, jungarPos.y - currentHeight/2, currentWidth, currentHeight);
        }

        // 3. Рисуем Батыра
        Animation<TextureRegion> activePlayerAnim;
        float playerTime;

        if (playerAttackType == 1) {
            activePlayerAnim = playerAttack1Anim;
            playerTime = playerAttackTime;
        } else if (playerAttackType == 2) {
            activePlayerAnim = playerAttack2Anim;
            playerTime = playerAttackTime;
        } else if (isPlayerMoving) {
            activePlayerAnim = playerRunAnim;
            playerTime = stateTime;
        } else {
            activePlayerAnim = playerIdleAnim;
            playerTime = stateTime;
        }

        boolean shouldLoopPlayer = (playerAttackType == 0);
        TextureRegion pFrame = activePlayerAnim.getKeyFrame(playerTime, shouldLoopPlayer);

        if (playerFacingLeft && !pFrame.isFlipX()) pFrame.flip(true, false);
        if (!playerFacingLeft && pFrame.isFlipX()) pFrame.flip(false, false);

        batch.draw(pFrame, playerPos.x - 60, playerPos.y - 40, 120, 80);

        batch.end();
    }

    private Animation<TextureRegion> createAnimFromSheet(Texture sheet, int row, int cols, int width, int height, float duration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, width, height);
        Array<TextureRegion> frames = new Array<>(cols);
        for (int i = 0; i < cols; i++) {
            frames.add(tmp[row][i]);
        }
        return new Animation<>(duration, frames, Animation.PlayMode.LOOP);
    }

    @Override
    public void dispose() {
        batch.dispose();
        mapTexture.dispose();
    }
}
