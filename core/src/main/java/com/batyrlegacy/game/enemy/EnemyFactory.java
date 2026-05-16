package com.batyrlegacy.game.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class EnemyFactory {

    public static Enemy createEnemy(int level, Texture sheet) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, 64, 64);

        Animation<TextureRegion> idle = createAnim(tmp, 0, 6, 0.12f);
        Animation<TextureRegion> run = createAnim(tmp, 1, 6, 0.08f);
        Animation<TextureRegion> attack = createAnim(tmp, 3, 6, 0.15f); // Slow balanced attack

        float scale = 1.0f + (level - 1) * 0.3f;
        float maxHp = 50f + (level - 1) * 40f;
        float damage = 1f + level * 3f; // Level 1 = 4 dmg, Level 2 = 7 dmg, Level 3 = 10 dmg

        return new Enemy(maxHp, scale, damage, idle, run, attack);
    }

    private static Animation<TextureRegion> createAnim(TextureRegion[][] tmp, int row, int cols, float speed) {
        Array<TextureRegion> frames = new Array<>(cols);
        for (int i = 0; i < cols; i++) {
            frames.add(tmp[row][i]);
        }
        return new Animation<>(speed, frames, Animation.PlayMode.LOOP);
    }
}
