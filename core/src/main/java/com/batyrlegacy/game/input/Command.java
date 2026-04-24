package com.batyrlegacy.game.input;


// ============================================================
//  DESIGN PATTERN: COMMAND
// ============================================================
//  Encapsulates each player action as an object.
//  Benefits:
//    • Easy to rebind keys (just swap which Command a key triggers)
//    • Supports undo/redo history if added later
//    • Input logic is decoupled from entity logic
//
//  DEVELOPER: Assign to Developer A
// ============================================================

/**
 * Base Command interface — every player action implements this.
 */
public interface Command {
    /** Execute the action. */
    void execute();
}

// ============================================================
//  Concrete Commands
// ============================================================

/** Move the player upward (W key). */
class MoveUpCommand implements Command {
    private final float speed;
    private final PlayerMover mover;

    MoveUpCommand(PlayerMover mover, float speed) {
        this.mover = mover;
        this.speed = speed;
    }

    @Override
    public void execute() {
        mover.move(0, speed);
    }
}

/** Move the player downward (S key). */
class MoveDownCommand implements Command {
    private final float speed;
    private final PlayerMover mover;

    MoveDownCommand(PlayerMover mover, float speed) {
        this.mover = mover;
        this.speed = speed;
    }

    @Override
    public void execute() {
        mover.move(0, -speed);
    }
}

/** Move the player left (A key). */
class MoveLeftCommand implements Command {
    private final float speed;
    private final PlayerMover mover;

    MoveLeftCommand(PlayerMover mover, float speed) {
        this.mover = mover;
        this.speed = speed;
    }

    @Override
    public void execute() {
        mover.move(-speed, 0);
    }
}

/** Move the player right (D key). */
class MoveRightCommand implements Command {
    private final float speed;
    private final PlayerMover mover;

    MoveRightCommand(PlayerMover mover, float speed) {
        this.mover = mover;
        this.speed = speed;
    }

    @Override
    public void execute() {
        mover.move(speed, 0);
    }
}

/** Trigger the player's attack (Space key). */
class AttackCommand implements Command {
    private final Attacker attacker;

    AttackCommand(Attacker attacker) {
        this.attacker = attacker;
    }

    @Override
    public void execute() {
        attacker.performAttack();
    }
}

// ============================================================
//  Helper interfaces (implemented by Player)
// ============================================================

/** Implemented by any entity that can be moved via commands. */
interface PlayerMover {
    void move(float dx, float dy);
}

/** Implemented by any entity that can perform an attack. */
interface Attacker {
    void performAttack();
}
