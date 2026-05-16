package com.batyrlegacy.game.combat;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class BowAttack implements AttackStrategy {
    private Animation<TextureRegion> animation;

    public BowAttack(Texture sheet) {
        // Используем твой второй лист атак
        TextureRegion[][] tmp = TextureRegion.split(sheet, 120, 80);
        Array<TextureRegion> frames = new Array<>(4);
        for (int i = 0; i < 4; i++) frames.add(tmp[0][i]);
        this.animation = new Animation<>(0.07f, frames, Animation.PlayMode.NORMAL);
    }

    @Override public Animation<TextureRegion> getAnimation() { return animation; }
    @Override public float getDamage() { return 20f; } // Наносит больше урона!
    @Override public float getDuration() { return 4 * 0.07f; }
}
