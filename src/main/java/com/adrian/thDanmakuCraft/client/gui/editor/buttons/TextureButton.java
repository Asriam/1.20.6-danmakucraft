package com.adrian.thDanmakuCraft.client.gui.editor.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public abstract class TextureButton extends EditorButton {
    private ResourceLocation textureLocation;

    public TextureButton(ResourceLocation textureLocation, int xPos, int yPos, int width, int height) {
        super(xPos, yPos, width, height);
        this.textureLocation = textureLocation;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick){
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }
}
