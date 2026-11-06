package com.adrian.thDanmakuCraft.client.gui.widgets;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IGuiOverlay{

    boolean shouldRender();

    void render(GuiGraphics graphics, Window window, float partialTick);
}
