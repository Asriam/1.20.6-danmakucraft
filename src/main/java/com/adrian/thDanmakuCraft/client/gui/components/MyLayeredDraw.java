package com.adrian.thDanmakuCraft.client.gui.components;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MyLayeredDraw {
    public static final float Z_SEPARATION = 200.0F;
    private final List<IGuiOverlay> layers = new ArrayList<>();

    public MyLayeredDraw add(IGuiOverlay layer) {
        this.layers.add(layer);
        return this;
    }

    public Window getWindow(){
        return Minecraft.getInstance().getWindow();
    }

    public void render(GuiGraphics guiGraphics,Window window,float partialTick) {
        guiGraphics.pose().pushPose();
        this.renderInner(guiGraphics, window, partialTick);
        guiGraphics.pose().popPose();
    }

    private void renderInner(GuiGraphics guiGraphics,Window window,float partialTick) {
        for (IGuiOverlay layer : this.layers) {
            if (layer.shouldRender()) {
                layer.render(guiGraphics, window, partialTick);
                guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
            }
        }
    }

    /*@OnlyIn(Dist.CLIENT)
    public interface Layer {
        boolean shouldRender();

        void render(GuiGraphics guiGraphics, Window window, float partialTick);
    }*/
}