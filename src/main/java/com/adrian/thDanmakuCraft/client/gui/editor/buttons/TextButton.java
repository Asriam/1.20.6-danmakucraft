package com.adrian.thDanmakuCraft.client.gui.editor.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextButton extends EditorButton {
    private String text;
    private static final Font font = Minecraft.getInstance().font;

    public TextButton(String text, int xPos, int yPos, int width, int height) {
        super(xPos, yPos, width, height);
        this.text = text;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick){
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, Component.translatable(text), xPos, yPos, 0xFFFFFF, false);
    }

    public String getText() {
        return text;
    }
}
