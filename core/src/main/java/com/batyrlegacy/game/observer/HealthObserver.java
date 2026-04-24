package com.batyrlegacy.game.observer;



/**
 * ============================================================
 *  DESIGN PATTERN: OBSERVER  (Interfaces)
 * ============================================================
 *  HealthSubject  — the entity whose HP can change (Player).
 *  HealthObserver — anything that reacts to HP changes (HUD).
 *
 *  How it works:
 *    1. Player implements HealthSubject.
 *    2. HealthBar implements HealthObserver and registers itself.
 *    3. When player.takeDamage() is called, it notifies all observers.
 *    4. HealthBar.onHealthChanged() redraws the bar — zero coupling.
 *
 *  DEVELOPER: Assign to Developer B
 * ============================================================
 */

public interface HealthObserver {
    /**
     * Called whenever the subject's HP changes.
     *
     * @param currentHp  New HP value.
     * @param maxHp      Maximum possible HP.
     */
    void onHealthChanged(int currentHp, int maxHp);
}
