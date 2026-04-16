package com.batyrlegacy.game;

import com.badlogic.gdx.Game;
import com.batyrlegacy.game.screens.MenuScreen;
import com.batyrlegacy.game.assets.ResourceManager;


/** * Теперь это MainGame.
 * Он наследует Game, что позволяет использовать метод setScreen().
 */
public class BatyrGame extends Game {

    @Override
    public void create() {
        // 1. Инициализируем синглтон ресурсов (загружаем картинки)
        ResourceManager.getInstance();
        this.setScreen(new MenuScreen(this));
        // 2. Устанавливаем самый первый экран (Состояние: Меню)
        // Мы передаем 'this', чтобы экран мог переключить состояние обратно на игру
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        // Это важно! Метод super.render() заставляет текущий экран рисоваться
        super.render();
    }

    @Override
    public void dispose() {
        // Очищаем память при закрытии
        super.dispose();
        ResourceManager.getInstance().dispose();
    }
}
