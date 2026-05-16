package com.batyrlegacy.game.combat;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface AttackStrategy {
    Animation<TextureRegion> getAnimation();
    float getDamage();
    float getDuration(); // Сколько длится взмах/выстрел
}
