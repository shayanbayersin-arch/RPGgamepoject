package com.batyrlegacy.game.combat;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class BowAttack implements AttackStrategy {
    private Animation<TextureRegion> animation;
    // Переменные для летящей стрелы
    public boolean hasArrow = false;
    public Vector2 arrowPos = new Vector2();
    public Vector2 arrowVelocity = new Vector2();

    public BowAttack(Texture sheet) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, 120, 80);
        Array<TextureRegion> frames = new Array<>(4);
        for (int i = 0; i < 4; i++) frames.add(tmp[0][i]);
        this.animation = new Animation<>(0.07f, frames, Animation.PlayMode.NORMAL);
    }

    public void spawnArrow(Vector2 playerPos, boolean facingLeft) {
        hasArrow = true;
        arrowPos.set(playerPos.x, playerPos.y);
        // Скорость полета стрелы (положительная направо, отрицательная налево)
        arrowVelocity.set(facingLeft ? -500f : 500f, 0f);
    }

    public void updateArrow(float delta) {
        if (hasArrow) {
            arrowPos.add(arrowVelocity.x * delta, arrowVelocity.y * delta);
        }
    }

    @Override public Animation<TextureRegion> getAnimation() { return animation; }
    @Override public float getDamage() { return 15f; } // Ranged arrow damage
    @Override public float getDuration() { return 4 * 0.07f; }
}
