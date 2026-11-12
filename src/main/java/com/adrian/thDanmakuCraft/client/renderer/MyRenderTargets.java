package com.adrian.thDanmakuCraft.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;

public class MyRenderTargets {
    public static final RenderTarget TEST_RENDER_TARGET = new TextureTarget(1000,1000,true,true);
    public static final RenderTarget DEPTH_BUFFER = new TextureTarget(1000,1000,true,true);
    public static RenderTarget SPELLCARD_AURA_TARGET = new TextureTarget(2000,2000,true, Minecraft.ON_OSX);
    public static RenderTarget DEPTH_BUFFER_TARGET = new TextureTarget(2000,2000,true,Minecraft.ON_OSX);
    public static RenderTarget AURA_DEPTH_BUFFER_TARGET = new TextureTarget(2000,2000,true,Minecraft.ON_OSX);

    public static void onResizeScreen(){
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        int[] window = new int[]{target.width, target.height};
        SPELLCARD_AURA_TARGET.resize(window[0], window[1], Minecraft.ON_OSX);
        AURA_DEPTH_BUFFER_TARGET.resize(window[0], window[1], Minecraft.ON_OSX);
        DEPTH_BUFFER_TARGET.resize(window[0], window[1], Minecraft.ON_OSX);
        DEPTH_BUFFER.resize(window[0], window[1], Minecraft.ON_OSX);
    }
}
