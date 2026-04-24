package com.batyrlegacy.game.observer;


/**
 * ============================================================
 *  DESIGN PATTERN: OBSERVER  (Subject interface)
 * ============================================================
 */
public interface HealthSubject {
    void addObserver(HealthObserver observer);
    void removeObserver(HealthObserver observer);
    void notifyObservers();
}
