package com.batyrlegacy.game.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public Vector2 pos = new Vector2();
    public float maxHp;
    public float currentHp;
    public float scale;
    public float damage;

    public Animation<TextureRegion> idleAnim;
    public Animation<TextureRegion> runAnim;
    public Animation<TextureRegion> attackAnim;

    public boolean isMoving = false;
    public boolean isAttacking = false;
    public float attackTime = 0f;
    public boolean hitRegistered = false;

    public Enemy(float maxHp, float scale, float damage,
                 Animation<TextureRegion> idle, Animation<TextureRegion> run, Animation<TextureRegion> attack) {
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.scale = scale;
        this.damage = damage;
        this.idleAnim = idle;
        this.runAnim = run;
        this.attackAnim = attack;
    }
}
