package com.batyrlegacy.game.input;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/**
 * ============================================================
 *  DESIGN PATTERN: COMMAND  (Invoker)
 * ============================================================
 *  Maps keyboard keys to Command objects and executes them.
 *  To rebind a key: call rebind() — zero changes to game logic.
 *
 *  Call inputHandler.handleInput(delta) every frame from PlayScreen.
 *  DEVELOPER: Developer A
 * ============================================================
 */
public class InputHandler {

    // --- The five bindable commands ---
    private Command moveUp;
    private Command moveDown;
    private Command moveLeft;
    private Command moveRight;
    private Command attack;

    // --- Key bindings (easily swappable) ---
    private int keyUp    = Keys.W;
    private int keyDown  = Keys.S;
    private int keyLeft  = Keys.A;
    private int keyRight = Keys.D;
    private int keyAtk   = Keys.SPACE;

    private static final float BASE_SPEED = 150f; // pixels per second

    /**
     * Wires up commands to the player entity.
     *
     * @param mover    The entity that will be moved (implements PlayerMover).
     * @param attacker The entity that will attack (implements Attacker).
     */
    public InputHandler(PlayerMover mover, Attacker attacker) {
        float speed = BASE_SPEED;
        moveUp    = new MoveUpCommand(mover,    speed);
        moveDown  = new MoveDownCommand(mover,  speed);
        moveLeft  = new MoveLeftCommand(mover,  speed);
        moveRight = new MoveRightCommand(mover, speed);
        attack    = new AttackCommand(attacker);
    }

    /**
     * Poll keyboard every frame and execute the matching command.
     * MoveCommands pass delta so movement is frame-rate independent.
     *
     * @param delta Seconds since last frame.
     */
    public void handleInput(float delta) {
        // Rebuild move commands each frame so speed includes delta
        // (alternatively you can pass delta into move() itself)
        if (Gdx.input.isKeyPressed(keyUp))    { moveUp.execute();    }
        if (Gdx.input.isKeyPressed(keyDown))  { moveDown.execute();  }
        if (Gdx.input.isKeyPressed(keyLeft))  { moveLeft.execute();  }
        if (Gdx.input.isKeyPressed(keyRight)) { moveRight.execute(); }
        if (Gdx.input.isKeyJustPressed(keyAtk)) { attack.execute();  }
    }

    /**
     * Rebind any key at runtime — Command Pattern shines here.
     *
     * @param action  "up" | "down" | "left" | "right" | "attack"
     * @param newKey  LibGDX Keys constant (e.g. Keys.UP)
     */
    public void rebind(String action, int newKey) {
        switch (action) {
            case "up":     keyUp    = newKey; break;
            case "down":   keyDown  = newKey; break;
            case "left":   keyLeft  = newKey; break;
            case "right":  keyRight = newKey; break;
            case "attack": keyAtk   = newKey; break;
            default: Gdx.app.log("InputHandler", "Unknown action: " + action);
        }
    }
}
