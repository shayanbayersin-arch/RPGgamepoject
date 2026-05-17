package com.batyrlegacy.game.screens;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.batyrlegacy.game.BatyrGame;
import com.batyrlegacy.game.combat.AttackStrategy;
import com.batyrlegacy.game.combat.BowAttack;
import com.batyrlegacy.game.combat.ComboAttack;
import com.batyrlegacy.game.combat.SwordAttack;
import com.batyrlegacy.game.enemy.Enemy;
import com.batyrlegacy.game.enemy.EnemyFactory;

public class PlayScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture mapTexture;
    private BitmapFont pixelFont;

    private Texture attackSheet1, attackSheet2, jungarSheet, deathSheet;
    private Texture[] playerHpTextures;
    private Animation<TextureRegion> playerIdleAnim, playerRunAnim, playerDeathAnim;

    private AttackStrategy currentStrategy;
    private BowAttack activeBowAttack;
    private Enemy currentEnemy;

    private Vector2 playerPos = new Vector2(300, 200);
    private float playerMaxHp = 100;
    private float playerCurrentHp = 100;

    private float stateTime = 0f;
    private float playerAttackTime = 0f;
    private int playerAttackType = 0;
    private boolean isPlayerMoving = false, playerFacingLeft = false;
    private boolean isJungarSpawned = false;
    private boolean playerHitRegistered = false;

    // Новая система смерти
    private boolean isPlayerDead = false;
    private float deathTimer = 0f;
    private boolean showDeathMessage = false;

    // Система уровней и победы
    private int jungarLevel = 1;
    private boolean showWinMessage = false;
    private float winMessageTimer = 0f;
    private Rectangle arenaTrigger = new Rectangle(850, 100, 400, 450);

    public PlayScreen(BatyrGame game) {
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        mapTexture = new Texture(Gdx.files.internal("map.png"));

        pixelFont = new BitmapFont();
        pixelFont.getData().setScale(2.5f);

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
        playerHpTextures[0] = new Texture(Gdx.files.internal("HP10.png"));

        playerIdleAnim = createAnim(new Texture(Gdx.files.internal("_idle.png")), 0, 10, 120, 80, 0.1f);
        playerRunAnim = createAnim(new Texture(Gdx.files.internal("_Run.png")), 0, 10, 120, 80, 0.08f);

        // ЗАГРУЗКА АНИМАЦИИ СМЕРТИ (10 кадров по 0.1 сек)
        deathSheet = new Texture(Gdx.files.internal("_Death.png"));
        playerDeathAnim = createAnim(deathSheet, 0, 10, 120, 80, 0.1f);
        playerDeathAnim.setPlayMode(Animation.PlayMode.NORMAL); // Проигрывается один раз

        attackSheet1 = new Texture(Gdx.files.internal("_Attack.png"));
        attackSheet2 = new Texture(Gdx.files.internal("_Attack2.png"));
        jungarSheet = new Texture(Gdx.files.internal("jungar_sheet.png"));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stateTime += delta;

        // --- ЛОГИКА ТАЙМЕРА ПОБЕДЫ ---
        if (showWinMessage) {
            winMessageTimer += delta;
            if (winMessageTimer > 3.0f) {
                showWinMessage = false;
                winMessageTimer = 0f;
            }
        }

        // --- ЛОГИКА ТАЙМЕРА СМЕРТИ ---
        if (isPlayerDead) {
            deathTimer += delta;

            // 1.5 сек. — Батыр застывает на месте и ждет
            // С 1.5 сек. до 2.5 сек. — проигрывается анимация смерти (падание)
            // С 2.5 сек. до 6.5 сек. — включается затемнение и текст «You Died!»

            if (deathTimer > 2.5f && !showDeathMessage) {
                showDeathMessage = true; // Включаем притемнение и надпись
            }

            if (deathTimer > 6.5f) { // Спустя 6.5 секунд — РЕСПАВН
                isPlayerDead = false;
                showDeathMessage = false;
                deathTimer = 0f;
                playerCurrentHp = playerMaxHp; // Восстанавливаем HP
                playerPos.set(300, 200);       // Телепорт на старт
                isJungarSpawned = false;       // Сбрасываем босса

                // Сбрасываем анимацию смерти, чтобы она могла проиграться снова
                stateTime = 0f;
                playerDeathAnim.setPlayMode(Animation.PlayMode.NORMAL);
            }
        }

        // --- БЛОКИРОВКА ВВОДА (Отключаем атаки и движение, если мертвы или празднуем победу) ---
        boolean controlsBlocked = showWinMessage || isPlayerDead;

        if (playerAttackType == 0 && !controlsBlocked) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                currentStrategy = new SwordAttack(attackSheet1);
                playerAttackType = 1;
                playerAttackTime = 0;
                playerHitRegistered = false;
            } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                currentStrategy = new ComboAttack(attackSheet2);
                playerAttackType = 2;
                playerAttackTime = 0;
                playerHitRegistered = false;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                BowAttack bow = new BowAttack(attackSheet1);
                bow.spawnArrow(playerPos, playerFacingLeft);
                activeBowAttack = bow;
                currentStrategy = bow;
                playerAttackType = 3;
                playerAttackTime = 0;
                playerHitRegistered = false;
            }
        }

        // Логика физики стрелы
        if (activeBowAttack != null && activeBowAttack.hasArrow) {
            activeBowAttack.updateArrow(delta);
            if (isJungarSpawned && activeBowAttack.arrowPos.dst(currentEnemy.pos) < 50) {
                currentEnemy.currentHp -= activeBowAttack.getDamage();
                activeBowAttack.hasArrow = false;
                activeBowAttack = null;
            } else if (activeBowAttack.arrowPos.x < 0 || activeBowAttack.arrowPos.x > 1280) {
                activeBowAttack.hasArrow = false;
                activeBowAttack = null;
            }
        }

        // Урон ближнего боя
        if (playerAttackType != 0 && playerAttackType != 3 && currentStrategy != null && !isPlayerDead) {
            playerAttackTime += delta;
            if (!playerHitRegistered && playerAttackTime > 0.14f && isJungarSpawned) {
                if (playerPos.dst(currentEnemy.pos) < 90) {
                    currentEnemy.currentHp -= currentStrategy.getDamage();
                    playerHitRegistered = true;
                }
            }
            if (playerAttackTime > currentStrategy.getDuration()) playerAttackType = 0;
        }

        if (playerAttackType == 3 && currentStrategy != null && !isPlayerDead) {
            playerAttackTime += delta;
            if (playerAttackTime > currentStrategy.getDuration()) playerAttackType = 0;
        }

        // Движение игрока
        isPlayerMoving = false;
        if (playerAttackType == 0 && !controlsBlocked) {
            Vector2 input = new Vector2();
            if (Gdx.input.isKeyPressed(Input.Keys.W)) { input.y += 1; isPlayerMoving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) { input.y -= 1; isPlayerMoving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) { input.x -= 1; isPlayerMoving = true; playerFacingLeft = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) { input.x += 1; isPlayerMoving = true; playerFacingLeft = false; }
            if (isPlayerMoving) playerPos.add(input.nor().scl(250 * delta));
        }

        // Спавн моба
        if (!isJungarSpawned && !controlsBlocked && arenaTrigger.contains(playerPos.x, playerPos.y) && jungarLevel <= 3) {
            isJungarSpawned = true;
            currentEnemy = EnemyFactory.createEnemy(jungarLevel, jungarSheet);
            currentEnemy.pos.set(1020, 320);
        }

        // Логика врага
        if (isJungarSpawned) {
            float dist = currentEnemy.pos.dst(playerPos);
            currentEnemy.isMoving = false;

            if (currentEnemy.isAttacking) {
                currentEnemy.attackTime += delta;
                if (!currentEnemy.hitRegistered && currentEnemy.attackTime > 0.45f) {
                    if (dist < 60) {
                        playerCurrentHp -= currentEnemy.damage;
                        // ТРИГГЕР СМЕРТИ БАТЫРА: Здоровье упало до 0
                        if (playerCurrentHp <= 0 && !isPlayerDead) {
                            playerCurrentHp = 0;
                            isPlayerDead = true; // Включаем режим смерти
                            deathTimer = 0f;
                            stateTime = 0f;    // Сбрасываем время анимации, чтобы смерть началась с 0 кадра
                        }
                    }
                    currentEnemy.hitRegistered = true;
                }
                if (currentEnemy.attackTime > 0.90f) currentEnemy.isAttacking = false;
            } else {
                if (dist < 50) {
                    // Босс перестает атаковать, если Батыр уже мертв
                    if (!isPlayerDead) {
                        currentEnemy.isAttacking = true;
                        currentEnemy.attackTime = 0;
                        currentEnemy.hitRegistered = false;
                    }
                } else {
                    currentEnemy.isMoving = true;
                    // Если Батыр мертв, босс тоже замирает, а не бегает вокруг трупа
                    if (!isPlayerDead) {
                        currentEnemy.pos.add(playerPos.cpy().sub(currentEnemy.pos).nor().scl(130 * delta));
                    }
                }
            }

            // Победный триггер
            if (currentEnemy.currentHp <= 0 && !isPlayerDead) {
                isJungarSpawned = false;
                showWinMessage = true;
                winMessageTimer = 0f;
                jungarLevel++;
                playerPos.set(400, 250);
            }
        }

        // --- 1. ОТРИСОВКА ИГРОВОГО МИРА ---
        batch.begin();
        batch.draw(mapTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (isJungarSpawned) {
            float w = 140 * currentEnemy.scale, h = 140 * currentEnemy.scale;
            Animation<TextureRegion> jAnim = currentEnemy.isAttacking ? currentEnemy.attackAnim : (currentEnemy.isMoving ? currentEnemy.runAnim : currentEnemy.idleAnim);
            TextureRegion jFrame = jAnim.getKeyFrame(currentEnemy.isAttacking ? currentEnemy.attackTime : stateTime, !currentEnemy.isAttacking);
            if (playerPos.x < currentEnemy.pos.x && !jFrame.isFlipX()) jFrame.flip(true, false);
            if (playerPos.x > currentEnemy.pos.x && jFrame.isFlipX()) jFrame.flip(false, false);
            batch.draw(jFrame, currentEnemy.pos.x - w/2, currentEnemy.pos.y - h/2, w, h);
        }

        // Отрисовка Батыра
        Animation<TextureRegion> pAnim;
        float pTime;
        boolean pLoop;

        // ВЫБОР АНИМАЦИИ: Мертв, бьет или бежит?
        if (isPlayerDead) {
            if (deathTimer < 1.5f) { // 1.5 сек задержки перед падением
                pAnim = playerIdleAnim; // Застыл в Idle
                pTime = 0f;            // 0 кадр застывший
                pLoop = false;
            } else { // Анимация падения пошла
                pAnim = playerDeathAnim;
                pTime = deathTimer - 1.5f; // Вычитаем время задержки
                pLoop = false; // Проигрывается 1 раз
            }
        } else if (playerAttackType != 0) {
            pAnim = currentStrategy.getAnimation();
            pTime = playerAttackTime;
            pLoop = false;
        } else if (isPlayerMoving) {
            pAnim = playerRunAnim;
            pTime = stateTime;
            pLoop = true;
        } else {
            pAnim = playerIdleAnim;
            pTime = stateTime;
            pLoop = true;
        }

        TextureRegion pFrame = pAnim.getKeyFrame(pTime, pLoop);

        // Поворот, только если живой. Мертвый труп не крутится.
        if (!isPlayerDead) {
            if (playerFacingLeft && !pFrame.isFlipX()) pFrame.flip(true, false);
            if (!playerFacingLeft && pFrame.isFlipX()) pFrame.flip(false, false);
        }

        batch.draw(pFrame, playerPos.x - 60, playerPos.y - 40, 120, 80);

        // Рисуем UI здоровья Батыра
        int uiIndex = Math.max(0, Math.min(9, (int)(playerCurrentHp / 10) - 1));
        if (playerCurrentHp > 0) {
            batch.draw(playerHpTextures[uiIndex], 1100, 650, 160, 50);
        }
        batch.end();

        // --- 2. ЭФФЕКТЫ ПРИТЕМНЕНИЯ ЭКРАНА (YOU WIN ИЛИ YOU DIED) ---
        boolean shouldDarken = showWinMessage || showDeathMessage;

        if (shouldDarken) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            // Черный оверлей на 50% прозрачности
            shapeRenderer.setColor(new Color(0, 0, 0, 0.5f));
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // --- 3. ОТРИСОВКА ТЕКСТА ПОВЕРХ ЗАТЕМНЕНИЯ ---
        batch.begin();
        if (showWinMessage) {
            pixelFont.setColor(Color.GOLD);
            pixelFont.draw(batch, "YOU WIN!", 540, 420);

            pixelFont.setColor(Color.WHITE);
            if (jungarLevel > 3) {
                pixelFont.draw(batch, "ALL LEVELS COMPLETED!", 400, 350);
            } else {
                pixelFont.draw(batch, "Moving to Level " + jungarLevel + "...", 440, 350);
            }
        } else if (showDeathMessage) { // ТЕКСТ ПОРАЖЕНИЯ
            pixelFont.setColor(Color.GOLD);
            pixelFont.draw(batch, "YOU DIED!", 540, 420);

            pixelFont.setColor(Color.WHITE);
            pixelFont.draw(batch, "Defeat is not the end. Respawning...", 360, 350);
        }
        batch.end();

        // Обычные интерфейсы стрел и здоровья босса
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (activeBowAttack != null && activeBowAttack.hasArrow) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(activeBowAttack.arrowPos.x, activeBowAttack.arrowPos.y, 15, 4);
        }
        if (isJungarSpawned) {
            float jBarWidth = 70 * currentEnemy.scale;
            float jBarX = currentEnemy.pos.x - jBarWidth / 2;
            float jBarY = currentEnemy.pos.y + (65 * currentEnemy.scale);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(jBarX, jBarY, jBarWidth, 6);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(jBarX, jBarY, (currentEnemy.currentHp / currentEnemy.maxHp) * jBarWidth, 6);
        }
        shapeRenderer.end();
    }

    private Animation<TextureRegion> createAnim(Texture sheet, int row, int cols, int width, int height, float duration) {
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
        shapeRenderer.dispose();
        mapTexture.dispose();
        pixelFont.dispose();
        attackSheet1.dispose();
        attackSheet2.dispose();
        jungarSheet.dispose();
        deathSheet.dispose(); // Не забываем очистить текстуру смерти
        for (Texture t : playerHpTextures) if (t != null) t.dispose();
    }
}
