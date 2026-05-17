package com.batyrlegacy.game.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20; // Для поддержки прозрачности плашки
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.batyrlegacy.game.BatyrGame;

public class MenuScreen extends ScreenAdapter {
    private BatyrGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer; // Для рисования защитной плашки под текстом
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    private Texture backgroundTexture; // Текстура казахского орнамента

    private OrthographicCamera camera;
    private Viewport viewport;

    public MenuScreen(BatyrGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        glyphLayout = new GlyphLayout();

        font = new BitmapFont();
        font.getData().setScale(2.5f);

        // ЗАГРУЗКА КАРТИНКИ: Сохрани орнамент в assets под именем menu_bg.png
        backgroundTexture = new Texture(Gdx.files.internal("menu_bg.jpg"));

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply();
        camera.position.set(1280 / 2f, 720 / 2f, 0);
    }

    @Override
    public void render(float delta) {
        // Очищаем буфер экрана в черный цвет
        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Обработка нажатий клавиш
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new PlayScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        // --- 1. РИСУЕМ КАЗАХСКИЙ ОРНАМЕНТ НА ВЕСЬ ЭКРАН ---
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 1280, 720);
        batch.end();

        // --- 2. РИСУЕМ ПОЛУПРОЗРАЧНУЮ ЧЕРНУЮ ПОДЛОЖКУ ДЛЯ ЧИТАЕМОСТИ ТЕКСТА ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Черный цвет с альфа-прозрачностью 75% (0.75f) — идеально затенит центр
        shapeRenderer.setColor(new Color(0, 0, 0, 0.75f));
        // Рисуем плашку по центру шириной 700 пикселей и высотой 400 пикселей
        shapeRenderer.rect(640f - 350f, 360f - 200f, 700, 400);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- 3. ОТРИСОВКА МАТЕМАТИЧЕСКИ ОТЦЕНТРИРОВАННОГО ТЕКСТА ПОВЕРХ ПЛАШКИ ---
        batch.begin();

        // Название игры "BATYR LEGACY"
        font.setColor(Color.GOLD);
        String title = "BATYR LEGACY";
        glyphLayout.setText(font, title);
        float titleX = 640f - (glyphLayout.width / 2f);
        font.draw(batch, title, titleX, 480);

        // Кнопка "PRESS ENTER TO START"
        font.setColor(Color.WHITE);
        String startText = "PRESS ENTER TO START";
        glyphLayout.setText(font, startText);
        float startX = 640f - (glyphLayout.width / 2f);
        font.draw(batch, startText, startX, 350);

        // Кнопка "PRESS ESC TO EXIT"
        String exitText = "PRESS ESC TO EXIT";
        glyphLayout.setText(font, exitText);
        float exitX = 640f - (glyphLayout.width / 2f);
        font.draw(batch, exitText, exitX, 250);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        backgroundTexture.dispose();
    }
}
