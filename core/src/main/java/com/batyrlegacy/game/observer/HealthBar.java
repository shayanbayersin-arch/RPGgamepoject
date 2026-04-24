package com.batyrlegacy.game.observer;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * ============================================================
 *  DESIGN PATTERN: OBSERVER  (Concrete Observer)
 * ============================================================
 *  Renders a health bar on-screen.
 *  Automatically updates whenever the Player notifies observers.
 *
 *  Usage in PlayScreen:
 *      healthBar = new HealthBar(20, 740, 200, 20);
 *      player.addObserver(healthBar);
 * ============================================================
 */
public class HealthBar implements HealthObserver {

    // HUD position and dimensions (in screen pixels)
    private final float x, y, width, height;

    // Current fill ratio  [0.0 … 1.0]
    private float fillRatio = 1.0f;

    private final ShapeRenderer shapeRenderer;

    /**
     * @param x       Left edge of the health bar
     * @param y       Bottom edge of the health bar
     * @param width   Total width when full
     * @param height  Bar height
     */
    public HealthBar(float x, float y, float width, float height) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.shapeRenderer = new ShapeRenderer();
    }

    // ---- HealthObserver -----------------------------------------

    /**
     * Called automatically by the Player when HP changes.
     * No polling needed — pure event-driven update.
     */
    @Override
    public void onHealthChanged(int currentHp, int maxHp) {
        fillRatio = (maxHp > 0) ? (float) currentHp / maxHp : 0f;
        Gdx.app.log("HealthBar", "HP updated → " + currentHp + "/" + maxHp
            + "  (" + (int)(fillRatio * 100) + "%)");
    }

    // ---- Rendering ----------------------------------------------

    /**
     * Draw the health bar. Call this inside PlayScreen#render().
     * NOTE: Do NOT call begin/end outside — this method handles them.
     */
    public void render() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background (dark red = missing HP)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        // Foreground (green → yellow → red based on HP ratio)
        shapeRenderer.setColor(getBarColor());
        shapeRenderer.rect(x, y, width * fillRatio, height);

        shapeRenderer.end();
    }

    /** Picks bar colour: green > 60%, yellow > 30%, red otherwise. */
    private Color getBarColor() {
        if (fillRatio > 0.6f) return Color.GREEN;
        if (fillRatio > 0.3f) return Color.YELLOW;
        return Color.RED;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
