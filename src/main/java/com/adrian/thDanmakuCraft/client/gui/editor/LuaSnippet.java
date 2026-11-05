package com.adrian.thDanmakuCraft.client.gui.editor;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** 一個可插入的樣板。 */
record LuaSnippet(String label, ResourceLocation icon, String iconFile, String code,
                  String description, List<LuaSnippetLibrary.Parameter> parameters) {

    boolean hasParameters() {
        return !this.parameters.isEmpty();
    }

    static ResourceLocation icon(String name) {
        return ResourceLocation.fromNamespaceAndPath("thdanmakucraft",
                "textures/gui/editor/icons/" + name);
    }
}
