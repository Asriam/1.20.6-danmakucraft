package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RenderTargetManager {
    private static final Map<ResourceLocation, RenderTarget> renderTargetMap = new HashMap<>();

    public static RenderTarget createRenderTarget(ResourceLocation name, int width, int height){
        RenderTarget renderTarget = renderTargetMap.get(name);
        if (renderTarget!= null) {
            THDanmakuCraftCore.LOGGER.warn("Render Target {} already exits!",name);
            return renderTarget;
        }
        renderTargetMap.put(name,new TextureTarget(width,height,true, Minecraft.ON_OSX));
        return renderTargetMap.get(name);
    }

    public static RenderTarget createRenderTarget(String name, int width, int height){
        return createRenderTarget(ResourceLocationUtil.mod(name),width,height);
    }

    public static RenderTarget getRenderTarget(ResourceLocation name){
        return renderTargetMap.get(name);
    }

    public static RenderTarget getRenderTarget(String name){
        return getRenderTarget(ResourceLocationUtil.mod(name));
    }

    public static void releaseRenderTarget(ResourceLocation name){
        renderTargetMap.remove(name);
    }
}
