package com.adrian.thDanmakuCraft.client.gui.components;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IGuiOverlay{

    boolean shouldRender();

    void render(GuiGraphics graphics, Window window, float partialTick);
}
