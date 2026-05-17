package com.batyrlegacy.game.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.batyrlegacy.game.BatyrGame;
import com.batyrlegacy.game.combat.AttackStrategy;
import com.batyrlegacy.game.combat.BowAttack;
import com.batyrlegacy.game.combat.ComboAttack;
import com.batyrlegacy.game.combat.SwordAttack;
import com.batyrlegacy.game.enemy.Enemy;
import com.batyrlegacy.game.enemy.EnemyFactory;

public class PlayScreen extends ScreenAdapter {
    private BatyrGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private Texture mapTexture1;
    private Texture mapTexture2;
    private int currentMap = 1;

    private BitmapFont pixelFont;
    private GlyphLayout glyphLayout;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture attackSheet1, attackSheet2, jungarSheet, deathSheet;
    private Texture[] playerHpTextures;
    private Animation<TextureRegion> playerIdleAnim, playerRunAnim, playerDeathAnim;

    private AttackStrategy currentStrategy;
    private BowAttack activeBowAttack;
    private Enemy currentEnemy;

    private final Vector2 spawnPos = new Vector2(260, 560);
    private Vector2 playerPos = new Vector2(spawnPos.x, spawnPos.y);

    private float playerMaxHp = 100;
    private float playerCurrentHp = 100;

    private float stateTime = 0f;
    private float playerAttackTime = 0f;
    private int playerAttackType = 0;
    private boolean isPlayerMoving = false, playerFacingLeft = false;
    private boolean isJungarSpawned = false;
    private boolean playerHitRegistered = false;

    private boolean isPlayerDead = false;
    private float deathTimer = 0f;
    private boolean showDeathMessage = false;

    private int jungarLevel = 1;
    private final int maxLevelsOnFirstMap = 5;
    private boolean showWinMessage = false;
    private Rectangle arenaTrigger = new Rectangle(850, 100, 400, 450);

    public PlayScreen(BatyrGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        glyphLayout = new GlyphLayout();

        mapTexture1 = new Texture(Gdx.files.internal("map.png"));

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply();
        camera.position.set(1280 / 2f, 720 / 2f, 0);

        pixelFont = new BitmapFont();
        // Снизили масштаб до 1.3f (раньше было 2.5f) — теперь текст маленький и аккуратный!
        pixelFont.getData().setScale(1.3f);

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

        deathSheet = new Texture(Gdx.files.internal("_Death.png"));
        playerDeathAnim = createAnim(deathSheet, 0, 10, 120, 80, 0.1f);
        playerDeathAnim.setPlayMode(Animation.PlayMode.NORMAL);

        attackSheet1 = new Texture(Gdx.files.internal("_Attack.png"));
        attackSheet2 = new Texture(Gdx.files.internal("_Attack2.png"));
        jungarSheet = new Texture(Gdx.files.internal("jungar_sheet.png"));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        stateTime += delta;

        if (isPlayerDead) {
            deathTimer += delta;
            if (deathTimer > 1.5f) showDeathMessage = true;
            if (showDeathMessage && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                restartLevel();
            }
        }

        if (showWinMessage && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (currentMap == 1) {
                if (jungarLevel < maxLevelsOnFirstMap) {
                    jungarLevel++;
                    showWinMessage = false;
                    isJungarSpawned = false;
                } else {
                    currentMap = 2;
                    jungarLevel = 6;
                    showWinMessage = false;
                    isJungarSpawned = false;
                    playerPos.set(spawnPos.x, spawnPos.y);
                }
            } else {
                currentMap = 1;
                jungarLevel = 1;
                showWinMessage = false;
                isJungarSpawned = false;
                playerCurrentHp = playerMaxHp;
                playerPos.set(spawnPos.x, spawnPos.y);
            }
        }

        boolean controlsBlocked = showWinMessage || showDeathMessage || isPlayerDead;

        if (playerAttackType == 0 && !controlsBlocked) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                currentStrategy = new SwordAttack(attackSheet1);
                playerAttackType = 1; playerAttackTime = 0; playerHitRegistered = false;
            } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                currentStrategy = new ComboAttack(attackSheet2);
                playerAttackType = 2; playerAttackTime = 0; playerHitRegistered = false;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                BowAttack bow = new BowAttack(attackSheet1);
                bow.spawnArrow(playerPos, playerFacingLeft);
                activeBowAttack = bow; currentStrategy = bow;
                playerAttackType = 3; playerAttackTime = 0; playerHitRegistered = false;
            }
        }

        if (activeBowAttack != null && activeBowAttack.hasArrow) {
            activeBowAttack.updateArrow(delta);
            if (isJungarSpawned && activeBowAttack.arrowPos.dst(currentEnemy.pos) < 50) {
                currentEnemy.currentHp -= activeBowAttack.getDamage();
                activeBowAttack.hasArrow = false; activeBowAttack = null;
            } else if (activeBowAttack.arrowPos.x < 0 || activeBowAttack.arrowPos.x > 1280) {
                activeBowAttack.hasArrow = false; activeBowAttack = null;
            }
        }

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

        isPlayerMoving = false;
        if (playerAttackType == 0 && !controlsBlocked) {
            Vector2 input = new Vector2();
            if (Gdx.input.isKeyPressed(Input.Keys.W)) { input.y += 1; isPlayerMoving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) { input.y -= 1; isPlayerMoving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) { input.x -= 1; isPlayerMoving = true; playerFacingLeft = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) { input.x += 1; isPlayerMoving = true; playerFacingLeft = false; }
            if (isPlayerMoving) playerPos.add(input.nor().scl(250 * delta));
        }

        if (!isJungarSpawned && !controlsBlocked && arenaTrigger.contains(playerPos.x, playerPos.y)) {
            isJungarSpawned = true;
            currentEnemy = EnemyFactory.createEnemy(jungarLevel, jungarSheet);
            currentEnemy.pos.set(1020, 320);
        }

        if (isJungarSpawned && !showWinMessage) {
            float dist = currentEnemy.pos.dst(playerPos);
            currentEnemy.isMoving = false;

            if (currentEnemy.isAttacking) {
                currentEnemy.attackTime += delta;
                if (!currentEnemy.hitRegistered && currentEnemy.attackTime > 0.45f) {
                    if (dist < 60) {
                        playerCurrentHp -= currentEnemy.damage;
                        if (playerCurrentHp <= 0 && !isPlayerDead) {
                            playerCurrentHp = 0; isPlayerDead = true; deathTimer = 0f; stateTime = 0f;
                        }
                    }
                    currentEnemy.hitRegistered = true;
                }
                if (currentEnemy.attackTime > 0.90f) currentEnemy.isAttacking = false;
            } else {
                if (dist < 50) {
                    if (!isPlayerDead) { currentEnemy.isAttacking = true; currentEnemy.attackTime = 0; currentEnemy.hitRegistered = false; }
                } else {
                    currentEnemy.isMoving = true;
                    if (!isPlayerDead) currentEnemy.pos.add(playerPos.cpy().sub(currentEnemy.pos).nor().scl(130 * delta));
                }
            }

            if (currentEnemy.currentHp <= 0 && !isPlayerDead) {
                showWinMessage = true;
                playerPos.set(400, 250);
            }
        }

        // --- ГРАФИКА МИРА ---
        batch.begin();
        if (currentMap == 1) {
            batch.draw(mapTexture1, 0, 0, 1280, 720);
        } else {
            if (mapTexture2 != null) batch.draw(mapTexture2, 0, 0, 1280, 720);
        }

        if (isJungarSpawned && !showWinMessage) {
            float w = 140 * currentEnemy.scale, h = 140 * currentEnemy.scale;
            Animation<TextureRegion> jAnim = currentEnemy.isAttacking ? currentEnemy.attackAnim : (currentEnemy.isMoving ? currentEnemy.runAnim : currentEnemy.idleAnim);
            TextureRegion jFrame = jAnim.getKeyFrame(currentEnemy.isAttacking ? currentEnemy.attackTime : stateTime, !currentEnemy.isAttacking);
            if (playerPos.x < currentEnemy.pos.x && !jFrame.isFlipX()) jFrame.flip(true, false);
            if (playerPos.x > currentEnemy.pos.x && jFrame.isFlipX()) jFrame.flip(false, false);
            batch.draw(jFrame, currentEnemy.pos.x - w/2, currentEnemy.pos.y - h/2, w, h);

            // --- ИСПРАВЛЕННЫЙ ТЕКСТ НАД ГОЛОВОЙ (НА АНГЛИЙСКОМ И МЕНЬШЕ) ---
            String jungarTitle;
            Color titleColor;

            // Масштабируем размер шрифта специально для головы (делаем его компактным)
            pixelFont.getData().setScale(1.0f);

            if (jungarLevel == maxLevelsOnFirstMap || jungarLevel == 6) {
                jungarTitle = "JUNGAR WAR CHIEF (BOSS)";
                titleColor = Color.RED;
            } else {
                jungarTitle = "JUNGAR LVL " + jungarLevel;
                titleColor = new Color(1, 1 - (jungarLevel * 0.15f), 0, 1);
            }

            pixelFont.setColor(titleColor);
            glyphLayout.setText(pixelFont, jungarTitle);
            float titleX = currentEnemy.pos.x - glyphLayout.width / 2;
            float titleY = currentEnemy.pos.y + (50 * currentEnemy.scale);

            pixelFont.draw(batch, jungarTitle, titleX, titleY);

            // Возвращаем масштаб обратно для системных экранов
            pixelFont.getData().setScale(2.5f);
        }

        Animation<TextureRegion> pAnim;
        float pTime;
        boolean pLoop;

        if (isPlayerDead) {
            if (deathTimer < 1.5f) { pAnim = playerIdleAnim; pTime = 0f; pLoop = false; }
            else { pAnim = playerDeathAnim; pTime = deathTimer - 1.5f; pLoop = false; }
        } else if (playerAttackType != 0) {
            pAnim = currentStrategy.getAnimation(); pTime = playerAttackTime; pLoop = false;
        } else if (isPlayerMoving) {
            pAnim = playerRunAnim; pTime = stateTime; pLoop = true;
        } else {
            pAnim = playerIdleAnim; pTime = stateTime; pLoop = true;
        }

        TextureRegion pFrame = pAnim.getKeyFrame(pTime, pLoop);
        if (!isPlayerDead) {
            if (playerFacingLeft && !pFrame.isFlipX()) pFrame.flip(true, false);
            if (!playerFacingLeft && pFrame.isFlipX()) pFrame.flip(false, false);
        }
        batch.draw(pFrame, playerPos.x - 60, playerPos.y - 40, 120, 80);

        int uiIndex = Math.max(0, Math.min(9, (int)(playerCurrentHp / 10) - 1));
        if (playerCurrentHp > 0) {
            batch.draw(playerHpTextures[uiIndex], 1100, 650, 160, 50);
        }
        batch.end();

        if (showWinMessage || showDeathMessage) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(new Color(0, 0, 0, 0.65f));
            shapeRenderer.rect(0, 0, 1280, 720);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        batch.begin();
        if (showWinMessage) {
            pixelFont.setColor(Color.GOLD);
            pixelFont.draw(batch, "YOU WIN!", 540, 420);
            pixelFont.setColor(Color.WHITE);

            if (currentMap == 1) {
                if (jungarLevel < maxLevelsOnFirstMap) {
                    pixelFont.draw(batch, "Stage " + jungarLevel + " Passed! Press ENTER for next wave", 300, 350);
                } else {
                    pixelFont.draw(batch, "Press ENTER to go to the next map", 380, 350);
                }
            } else {
                pixelFont.draw(batch, "ALL LEVELS COMPLETED! Press ENTER to restart game", 250, 350);
            }
        } else if (showDeathMessage) {
            pixelFont.setColor(Color.RED);
            pixelFont.draw(batch, "YOU DIED!", 540, 420);
            pixelFont.setColor(Color.WHITE);
            pixelFont.draw(batch, "Press ENTER to restart", 460, 350);
        }
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (activeBowAttack != null && activeBowAttack.hasArrow) {
            shapeRenderer.setColor(Color.YELLOW); shapeRenderer.rect(activeBowAttack.arrowPos.x, activeBowAttack.arrowPos.y, 15, 4);
        }
        if (isJungarSpawned && !showWinMessage) {
            float jBarWidth = 70 * currentEnemy.scale;
            float jBarX = currentEnemy.pos.x - jBarWidth / 2;
            float jBarY = currentEnemy.pos.y + (65 * currentEnemy.scale);
            shapeRenderer.setColor(Color.RED); shapeRenderer.rect(jBarX, jBarY, jBarWidth, 6);
            shapeRenderer.setColor(Color.GREEN); shapeRenderer.rect(jBarX, jBarY, (currentEnemy.currentHp / currentEnemy.maxHp) * jBarWidth, 6);
        }
        shapeRenderer.end();
    }

    private void restartLevel() {
        isPlayerDead = false;
        showDeathMessage = false;
        deathTimer = 0f;
        playerCurrentHp = playerMaxHp;
        playerPos.set(spawnPos.x, spawnPos.y);
        isJungarSpawned = false;
        stateTime = 0f;
        playerDeathAnim.setPlayMode(Animation.PlayMode.NORMAL);

        if (currentMap == 1) jungarLevel = 1;
        else jungarLevel = 6;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private Animation<TextureRegion> createAnim(Texture sheet, int row, int cols, int width, int height, float duration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, width, height);
        Array<TextureRegion> frames = new Array<>(cols);
        for (int i = 0; i < cols; i++) frames.add(tmp[row][i]);
        return new Animation<>(duration, frames, Animation.PlayMode.LOOP);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        mapTexture1.dispose();
        if (mapTexture2 != null) mapTexture2.dispose();
        pixelFont.dispose();
        attackSheet1.dispose(); attackSheet2.dispose();
        jungarSheet.dispose(); deathSheet.dispose();
        for (Texture t : playerHpTextures) if (t != null) t.dispose();
    }
}
