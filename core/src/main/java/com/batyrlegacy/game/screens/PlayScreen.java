package com.batyrlegacy.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.batyrlegacy.game.BatyrGame;

public class PlayScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture mapTexture;

    // Массив для хранения 10 текстур пиксельного HP Батыра
    private Texture[] playerHpTextures;

    // Анимации
    private Animation<TextureRegion> playerIdleAnim, playerRunAnim, playerAttack1Anim, playerAttack2Anim;
    private Animation<TextureRegion> jungarIdleAnim, jungarRunAnim, jungarAttackAnim;

    // Позиции
    private Vector2 playerPos = new Vector2(300, 200);
    private Vector2 jungarPos = new Vector2(1020, 320);

    // Здоровье (теперь у нас 10 делений по 10 единиц)
    private float playerMaxHp = 100;
    private float playerCurrentHp = 100;
    private float jungarMaxHp = 50;
    private float jungarCurrentHp = 50;

    // Состояния
    private float stateTime = 0f;
    private float playerAttackTime = 0f;
    private float jungarAttackTime = 0f;
    private int playerAttackType = 0;
    private boolean isPlayerMoving = false, playerFacingLeft = false;
    private boolean isJungarSpawned = false, isJungarMoving = false, isJungarAttacking = false;
    private boolean playerHitRegistered = false, jungarHitRegistered = false;

    private int jungarLevel = 1;
    private float jungarScale = 1.0f;
    private Rectangle arenaTrigger = new Rectangle(850, 100, 400, 450);

    public PlayScreen(BatyrGame game) {
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        mapTexture = new Texture(Gdx.files.internal("map.png"));

        // === ЗАГРУЗКА КАРТИНОК ХП СТРОГО ПО ТВОЕМУ СКРИНШОТУ ASSETS ===
        // У тебя в папке файлы названы по-разному (HP большими, hp маленькими)
        playerHpTextures = new Texture[10];
        playerHpTextures[9] = new Texture(Gdx.files.internal("hp100.png"));
        playerHpTextures[8] = new Texture(Gdx.files.internal("hp90.png"));
        playerHpTextures[7] = new Texture(Gdx.files.internal("hp80.png"));
        playerHpTextures[6] = new Texture(Gdx.files.internal("hp70.png"));
        playerHpTextures[5] = new Texture(Gdx.files.internal("hp60.png"));
        playerHpTextures[4] = new Texture(Gdx.files.internal("hp50.png"));
        playerHpTextures[3] = new Texture(Gdx.files.internal("hp40.png"));
        playerHpTextures[2] = new Texture(Gdx.files.internal("hp30.png"));
        playerHpTextures[1] = new Texture(Gdx.files.internal("hp20.png"));
        playerHpTextures[0] = new Texture(Gdx.files.internal("HP10.png")); // Единственный с БОЛЬШИХ букв на скрине

        // === ЗАГРУЗКА БАТЫРА ===
        playerIdleAnim = createAnimFromSheet(new Texture(Gdx.files.internal("_idle.png")), 0, 10, 120, 80, 0.1f);
        playerRunAnim = createAnimFromSheet(new Texture(Gdx.files.internal("_Run.png")), 0, 10, 120, 80, 0.08f);
        playerAttack1Anim = createAnimFromSheet(new Texture(Gdx.files.internal("_Attack.png")), 0, 4, 120, 80, 0.07f);
        playerAttack2Anim = createAnimFromSheet(new Texture(Gdx.files.internal("_Attack2.png")), 0, 4, 120, 80, 0.07f);

        // === ЗАГРУЗКА ДЖУНГАРА ===
        Texture jSheet = new Texture(Gdx.files.internal("jungar_sheet.png"));
        jungarIdleAnim = createAnimFromSheet(jSheet, 0, 6, 64, 64, 0.12f);
        jungarRunAnim = createAnimFromSheet(jSheet, 1, 6, 64, 64, 0.08f);
        jungarAttackAnim = createAnimFromSheet(jSheet, 3, 6, 64, 64, 0.08f);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stateTime += delta;

        // --- ЛОГИКА АТАКИ И УРОНА БАТЫРА ---
        if (playerAttackType == 0) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) { playerAttackType = 1; playerAttackTime = 0; playerHitRegistered = false; }
            if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) { playerAttackType = 2; playerAttackTime = 0; playerHitRegistered = false; }
        }

        if (playerAttackType != 0) {
            playerAttackTime += delta;
            if (!playerHitRegistered && playerAttackTime > 0.14f && isJungarSpawned) {
                float dist = playerPos.dst(jungarPos);
                if (dist < 90) {
                    float damage = (playerAttackType == 1) ? 10 : 20;
                    jungarCurrentHp -= damage;
                    playerHitRegistered = true;
                }
            }
            if (playerAttackTime > 0.28f) playerAttackType = 0;
        }

        // --- ДВИЖЕНИЕ БАТЫРА ---
        isPlayerMoving = false;
        if (playerAttackType == 0) {
            Vector2 input = new Vector2();
            if (Gdx.input.isKeyPressed(Input.Keys.W)) { input.y += 1; isPlayerMoving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) { input.y -= 1; isPlayerMoving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) { input.x -= 1; isPlayerMoving = true; playerFacingLeft = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) { input.x += 1; isPlayerMoving = true; playerFacingLeft = false; }
            if (isPlayerMoving) playerPos.add(input.nor().scl(250 * delta));
        }

        // --- ЛОГИКА ДЖУНГАРА ---
        if (!isJungarSpawned && arenaTrigger.contains(playerPos.x, playerPos.y)) {
            isJungarSpawned = true;
            jungarScale = 1.0f + (jungarLevel - 1) * 0.3f;
            jungarMaxHp = 50 + (jungarLevel - 1) * 30;
            jungarCurrentHp = jungarMaxHp;
            jungarPos.set(1020, 320);
        }

        isJungarMoving = false;
        if (isJungarSpawned) {
            float dist = jungarPos.dst(playerPos);
            if (isJungarAttacking) {
                jungarAttackTime += delta;
                if (!jungarHitRegistered && jungarAttackTime > 0.24f) {
                    if (dist < 60) playerCurrentHp -= 10; // Джунгар отнимает по 10 HP за удар
                    jungarHitRegistered = true;
                }
                if (jungarAttackTime > 0.48f) isJungarAttacking = false;
            } else {
                if (dist < 50) {
                    isJungarAttacking = true;
                    jungarAttackTime = 0;
                    jungarHitRegistered = false;
                } else {
                    isJungarMoving = true;
                    jungarPos.add(playerPos.cpy().sub(jungarPos).nor().scl(130 * delta));
                }
            }

            if (jungarCurrentHp <= 0) {
                isJungarSpawned = false;
                jungarLevel++;
                playerPos.set(500, 200);
            }
        }

        // === ОТРИСОВКА ВСЕХ ТЕКСТУР ===
        batch.begin();
        batch.draw(mapTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Рисуем Джунгара
        if (isJungarSpawned) {
            float w = 140 * jungarScale, h = 140 * jungarScale;
            Animation<TextureRegion> jAnim = isJungarAttacking ? jungarAttackAnim : (isJungarMoving ? jungarRunAnim : jungarIdleAnim);
            TextureRegion jFrame = jAnim.getKeyFrame(isJungarAttacking ? jungarAttackTime : stateTime, !isJungarAttacking);
            if (playerPos.x < jungarPos.x && !jFrame.isFlipX()) jFrame.flip(true, false);
            if (playerPos.x > jungarPos.x && jFrame.isFlipX()) jFrame.flip(false, false);
            batch.draw(jFrame, jungarPos.x - w/2, jungarPos.y - h/2, w, h);
        }

        // Рисуем Батыра
        Animation<TextureRegion> pAnim = (playerAttackType != 0) ? (playerAttackType == 1 ? playerAttack1Anim : playerAttack2Anim) : (isPlayerMoving ? playerRunAnim : playerIdleAnim);
        TextureRegion pFrame = pAnim.getKeyFrame(playerAttackType != 0 ? playerAttackTime : stateTime, playerAttackType == 0);
        if (playerFacingLeft && !pFrame.isFlipX()) pFrame.flip(true, false);
        if (!playerFacingLeft && pFrame.isFlipX()) pFrame.flip(false, false);
        batch.draw(pFrame, playerPos.x - 60, playerPos.y - 40, 120, 80);

        // === ВЫБОР ТЕКСТУРЫ ХП БАТЫРА НА ОСНОВЕ ЕГО ЗДОРОВЬЯ ===
        Texture currentHpTexture = playerHpTextures[9]; // По умолчанию 100%
        if (playerCurrentHp <= 10) currentHpTexture = playerHpTextures[0];
        else if (playerCurrentHp <= 20) currentHpTexture = playerHpTextures[1];
        else if (playerCurrentHp <= 30) currentHpTexture = playerHpTextures[2];
        else if (playerCurrentHp <= 40) currentHpTexture = playerHpTextures[3];
        else if (playerCurrentHp <= 50) currentHpTexture = playerHpTextures[4];
        else if (playerCurrentHp <= 60) currentHpTexture = playerHpTextures[5];
        else if (playerCurrentHp <= 70) currentHpTexture = playerHpTextures[6];
        else if (playerCurrentHp <= 80) currentHpTexture = playerHpTextures[7];
        else if (playerCurrentHp <= 90) currentHpTexture = playerHpTextures[8];

        // РИСУЕМ ШКАЛУ В ВЕРХНЕМ ПРАВОМ УГЛУ
        // Экран по ширине 1280. Ширина шкалы 160. 
        // 1280 - 160 - 20 (отступ) = 1100 позиция по X.
        batch.draw(currentHpTexture, 1100, 650, 160, 50);

        batch.end();

        // === ОТРИСОВКА ХП ДЖУНГАРА НАД ГОЛОВОЙ ===
        if (isJungarSpawned) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float jBarWidth = 70 * jungarScale;
            float jBarX = jungarPos.x - jBarWidth / 2;
            float jBarY = jungarPos.y + (65 * jungarScale);

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(jBarX, jBarY, jBarWidth, 6);

            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(jBarX, jBarY, (jungarCurrentHp / jungarMaxHp) * jBarWidth, 6);
            shapeRenderer.end();
        }

        // Система респавна при смерти Батыра
        if (playerCurrentHp <= 0) {
            playerCurrentHp = playerMaxHp;
            playerPos.set(300, 200);
            isJungarSpawned = false;
        }
    }

    private Animation<TextureRegion> createAnimFromSheet(Texture sheet, int row, int cols, int width, int height, float duration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, width, height);
        Array<TextureRegion> frames = new Array<>(cols);
        for (int i = 0; i < cols; i++) frames.add(tmp[row][i]);
        return new Animation<>(duration, frames, Animation.PlayMode.LOOP);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        mapTexture.dispose();
        for (Texture t : playerHpTextures) {
            if (t != null) t.dispose();
        }
    }
}
