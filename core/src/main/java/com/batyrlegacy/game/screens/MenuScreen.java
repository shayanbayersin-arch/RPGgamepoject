package com.batyrlegacy.game.screens;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout; // Для идеального центрирования текста
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.batyrlegacy.game.BatyrGame;

public class MenuScreen extends ScreenAdapter {
    private BatyrGame game;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    // Камера и Вьюпорт для центрирования меню на полный экран
    private OrthographicCamera camera;
    private Viewport viewport;

    public MenuScreen(BatyrGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        glyphLayout = new GlyphLayout();

        font = new BitmapFont();
        font.getData().setScale(2.5f); // Крупный красивый шрифт

        // Настраиваем камеру и FitViewport на наше стандартное разрешение 1280x720
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply();
        camera.position.set(1280 / 2f, 720 / 2f, 0);
    }

    @Override
    public void render(float delta) {
        // Заливаем фон темно-зеленым/болотным цветом, как у тебя на скрине
        ScreenUtils.clear(0.1f, 0.2f, 0.1f, 1);

        // Обновляем камеру и привязываем её матрицу к отрисовке
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Переход по экранам
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new PlayScreen(game)); // Запуск игры
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit(); // Выход из игры
        }

        batch.begin();

        // 1. Отрисовка названия игры "BATYR LEGACY"
        font.setColor(Color.GOLD);
        String title = "BATYR LEGACY";
        glyphLayout.setText(font, title);
        // Виртуальный центр по X = 1280 / 2 = 640. Вычитаем половину ширины текста
        float titleX = 640f - (glyphLayout.width / 2f);
        font.draw(batch, title, titleX, 450);

        // 2. Отрисовка кнопки "PRESS ENTER TO START"
        font.setColor(Color.WHITE);
        String startText = "PRESS ENTER TO START";
        glyphLayout.setText(font, startText);
        float startX = 640f - (glyphLayout.width / 2f);
        font.draw(batch, startText, startX, 320);

        // 3. Отрисовка кнопки "PRESS ESC TO EXIT"
        String exitText = "PRESS ESC TO EXIT";
        glyphLayout.setText(font, exitText);
        float exitX = 640f - (glyphLayout.width / 2f);
        font.draw(batch, exitText, exitX, 220);

        batch.end();
    }

    // Обязательный метод, чтобы вьюпорт пересчитывал пропорции при разворачивании на весь экран
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
