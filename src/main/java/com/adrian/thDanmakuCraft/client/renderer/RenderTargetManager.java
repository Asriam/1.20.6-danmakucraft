package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RenderTargetManager {
    private static final Map<ResourceLocation, RenderTarget> renderTargetMap = new HashMap<>();

    public static RenderTarget createRenderTarget(ResourceLocation name, int width, int height){
        RenderTarget renderTarget = renderTargetMap.get(name);
        if (renderTarget!= null) {
            THDanmakuCraftMod.LOGGER.warn("Render Target {} already exits!",name);
            return renderTarget;
        }
        renderTargetMap.put(name,new TextureTarget(width,height,true, Minecraft.ON_OSX));
        return renderTargetMap.get(name);
    }

    public static RenderTarget createRenderTarget(String name, int width, int height){
        return createRenderTarget(ResourceLocationUtil.thdanmakucraft(name),width,height);
    }

    public static RenderTarget getRenderTarget(ResourceLocation name){
        return renderTargetMap.get(name);
    }

    public static RenderTarget getRenderTarget(String name){
        return getRenderTarget(ResourceLocationUtil.thdanmakucraft(name));
    }

    public static void releaseRenderTarget(ResourceLocation name){
        renderTargetMap.remove(name);
    }
}
