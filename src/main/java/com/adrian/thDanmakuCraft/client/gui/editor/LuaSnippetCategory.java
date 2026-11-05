package com.adrian.thDanmakuCraft.client.gui.editor;

/** 樣板分類（對應 data/thdanmakucraft/lua/snippet 下的資料夾）。 */
enum LuaSnippetCategory {
    BULLET("Bullet"), OBJECT("Object"), LASER("Laser"), TASK("Task"), SPELLCARD("SpellCard");

    private final String label;

    LuaSnippetCategory(String label) {
        this.label = label;
    }

    String label() {
        return this.label;
    }
}
