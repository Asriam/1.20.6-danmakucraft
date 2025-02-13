package com.adrian.thDanmakuCraft.client.gui.components;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphics;

public interface IGuiOverlay{

    boolean shouldRender();

    void render(GuiGraphics graphics, Window window, float partialTick);
}
