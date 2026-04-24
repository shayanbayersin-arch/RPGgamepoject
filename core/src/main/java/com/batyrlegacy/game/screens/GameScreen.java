package com.batyrlegacy.game.screens;


import com.badlogic.gdx.Screen;

/**
 * ============================================================
 *  DESIGN PATTERN: STATE  (Screen-level State)
 * ============================================================
 *  Each game screen (Menu, Play, GameOver…) is a STATE.
 *  MainGame is the Context that holds the active screen/state.
 *
 *  LibGDX's Screen interface already gives us enter/exit hooks:
 *    show()  → enter()
 *    hide()  → exit()
 *
 *  This marker interface lets us add game-specific helpers later.
 *  DEVELOPER: Developer B
 * ============================================================
 */
public interface GameScreen extends Screen {
    // Inherits: show, hide, render, resize, pause, resume, dispose
    // Add shared screen methods here as the project grows
}
