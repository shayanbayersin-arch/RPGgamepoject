package com.batyrlegacy.game.assets;

import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;

public class ResourceManager {
    private static ResourceManager instance;
    private HashMap<String, Texture> textures;

    private ResourceManager() {
        textures = new HashMap<>();
        // Пока здесь пусто, картинки добавим позже
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    public void dispose() {
        for (Texture tex : textures.values()) {
            tex.dispose();
        }
        textures.clear();
    }
}
