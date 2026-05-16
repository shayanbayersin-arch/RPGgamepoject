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

    private Texture attackSheet1, attackSheet2, jungarSheet;
    private Texture[] playerHpTextures;
    private Animation<TextureRegion> playerIdleAnim, playerRunAnim;

    private AttackStrategy currentStrategy;
    private BowAttack activeBowAttack; // For arrow updates
    private Enemy currentEnemy;

    private Vector2 playerPos = new Vector2(300, 200);
    private float playerMaxHp = 100;
    private float playerCurrentHp = 100;

    private float stateTime = 0f;
    private float playerAttackTime = 0f;
    private int playerAttackType = 0; // 1-LMB (Sword), 2-RMB (Combo), 3-Q (Bow)
    private boolean isPlayerMoving = false, playerFacingLeft = false;
    private boolean isJungarSpawned = false;
    private boolean playerHitRegistered = false;

    private int jungarLevel = 1;
    private Rectangle arenaTrigger = new Rectangle(850, 100, 400, 450);

    public PlayScreen(BatyrGame game) {
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        mapTexture = new Texture(Gdx.files.internal("map.png"));

        // Loading pixel HP bar textures from assets
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

        // Fixed method calls by matching 6-argument signature
        playerIdleAnim = createAnim(new Texture(Gdx.files.internal("_idle.png")), 0, 10, 120, 80, 0.1f);
        playerRunAnim = createAnim(new Texture(Gdx.files.internal("_Run.png")), 0, 10, 120, 80, 0.08f);

        attackSheet1 = new Texture(Gdx.files.internal("_Attack.png"));
        attackSheet2 = new Texture(Gdx.files.internal("_Attack2.png"));
        jungarSheet = new Texture(Gdx.files.internal("jungar_sheet.png"));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stateTime += delta;

        // --- COMBAT CONTROLS (STRATEGY SELECTION) ---
        if (playerAttackType == 0) {
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

        // Projectile Arrow Physical Update
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

        // Execution of Melee Attacks (LMB / RMB)
        if (playerAttackType != 0 && playerAttackType != 3 && currentStrategy != null) {
            playerAttackTime += delta;
            if (!playerHitRegistered && playerAttackTime > 0.14f && isJungarSpawned) {
                if (playerPos.dst(currentEnemy.pos) < 90) {
                    currentEnemy.currentHp -= currentStrategy.getDamage();
                    playerHitRegistered = true;
                }
            }
            if (playerAttackTime > currentStrategy.getDuration()) playerAttackType = 0;
        }

        // Ranged Attack Duration Timer
        if (playerAttackType == 3 && currentStrategy != null) {
            playerAttackTime += delta;
            if (playerAttackTime > currentStrategy.getDuration()) playerAttackType = 0;
        }

        // --- PLAYER CONTROLS & MOVEMENT ---
        isPlayerMoving = false;
        if (playerAttackType == 0) {
            Vector2 input = new Vector2();
            if (Gdx.input.isKeyPressed(Input.Keys.W)) { input.y += 1; isPlayerMoving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) { input.y -= 1; isPlayerMoving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) { input.x -= 1; isPlayerMoving = true; playerFacingLeft = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) { input.x += 1; isPlayerMoving = true; playerFacingLeft = false; }
            if (isPlayerMoving) playerPos.add(input.nor().scl(250 * delta));
        }

        // --- ENEMY IN-ZONE SPAWN (FACTORY METHOD) ---
        if (!isJungarSpawned && arenaTrigger.contains(playerPos.x, playerPos.y) && jungarLevel <= 3) {
            isJungarSpawned = true;
            currentEnemy = EnemyFactory.createEnemy(jungarLevel, jungarSheet);
            currentEnemy.pos.set(1020, 320);
        }

        // --- ENEMY ARTIFICIAL INTELLIGENCE ---
        if (isJungarSpawned) {
            float dist = currentEnemy.pos.dst(playerPos);
            currentEnemy.isMoving = false;

            if (currentEnemy.isAttacking) {
                currentEnemy.attackTime += delta;
                if (!currentEnemy.hitRegistered && currentEnemy.attackTime > 0.45f) {
                    if (dist < 60) playerCurrentHp -= currentEnemy.damage;
                    currentEnemy.hitRegistered = true;
                }
                if (currentEnemy.attackTime > 0.90f) currentEnemy.isAttacking = false;
            } else {
                if (dist < 50) {
                    currentEnemy.isAttacking = true;
                    currentEnemy.attackTime = 0;
                    currentEnemy.hitRegistered = false;
                } else {
                    currentEnemy.isMoving = true;
                    currentEnemy.pos.add(playerPos.cpy().sub(currentEnemy.pos).nor().scl(130 * delta));
                }
            }

            if (currentEnemy.currentHp <= 0) {
                isJungarSpawned = false;
                jungarLevel++;
                playerPos.set(500, 200);
            }
        }

        // --- TEXTURE AND SPRITE RENDERING ---
        batch.begin();
        batch.draw(mapTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Draw Boss Sprite
        if (isJungarSpawned) {
            float w = 140 * currentEnemy.scale, h = 140 * currentEnemy.scale;
            Animation<TextureRegion> jAnim = currentEnemy.isAttacking ? currentEnemy.attackAnim : (currentEnemy.isMoving ? currentEnemy.runAnim : currentEnemy.idleAnim);
            TextureRegion jFrame = jAnim.getKeyFrame(currentEnemy.isAttacking ? currentEnemy.attackTime : stateTime, !currentEnemy.isAttacking);
            if (playerPos.x < currentEnemy.pos.x && !jFrame.isFlipX()) jFrame.flip(true, false);
            if (playerPos.x > currentEnemy.pos.x && jFrame.isFlipX()) jFrame.flip(false, false);
            batch.draw(jFrame, currentEnemy.pos.x - w/2, currentEnemy.pos.y - h/2, w, h);
        }

        // Draw Player Sprite
        Animation<TextureRegion> pAnim = (playerAttackType != 0) ? currentStrategy.getAnimation() : (isPlayerMoving ? playerRunAnim : playerIdleAnim);
        TextureRegion pFrame = pAnim.getKeyFrame(playerAttackType != 0 ? playerAttackTime : stateTime, playerAttackType == 0);
        if (playerFacingLeft && !pFrame.isFlipX()) pFrame.flip(true, false);
        if (!playerFacingLeft && pFrame.isFlipX()) pFrame.flip(false, false);
        batch.draw(pFrame, playerPos.x - 60, playerPos.y - 40, 120, 80);

        // Draw Player UI Health Bar
        int uiIndex = Math.max(0, Math.min(9, (int)(playerCurrentHp / 10) - 1));
        if (playerCurrentHp > 0) {
            batch.draw(playerHpTextures[uiIndex], 1100, 650, 160, 50);
        }
        batch.end();

        // --- SHAPE RENDERING (PROJECTILES & BOSS HP) ---
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

        // Player Defeat Logic
        if (playerCurrentHp <= 0) {
            playerCurrentHp = playerMaxHp;
            playerPos.set(300, 200);
            isJungarSpawned = false;
        }
    }

    // Fixed: Takes 6 arguments to resolve internal compiler error
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
        attackSheet1.dispose();
        attackSheet2.dispose();
        jungarSheet.dispose();
        for (Texture t : playerHpTextures) if (t != null) t.dispose();
    }
}
