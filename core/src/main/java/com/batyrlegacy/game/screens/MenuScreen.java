package com.batyrlegacy.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.batyrlegacy.game.BatyrGame; // Твой главный класс игры

public class MenuScreen implements Screen {
    private final BatyrGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    public MenuScreen(BatyrGame game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        font.getData().setScale(2); // Крупный текст для меню
    }

    @Override
    public void render(float delta) {
        // Очищаем экран (сделаем фон темно-зеленым в стиле Батыра)
        Gdx.gl.glClearColor(0.1f, 0.2f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "BATYR LEGACY", 250, 400);
        font.draw(batch, "PRESS ENTER TO START", 200, 300);
        font.draw(batch, "PRESS ESC TO EXIT", 200, 200);
        batch.end();

        // Логика управления меню
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            // Переходим на игровой экран (PlayScreen)
            game.setScreen(new PlayScreen(game));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
