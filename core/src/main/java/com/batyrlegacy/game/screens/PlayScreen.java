package com.batyrlegacy.game.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.batyrlegacy.game.BatyrGame;

public class PlayScreen implements Screen {
    private final BatyrGame game;

    public PlayScreen(BatyrGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        // Очистка экрана (синий фон для теста)
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void show() {}

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
