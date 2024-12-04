package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RenderTargetManager {
    private static Map<ResourceLocation, RenderTarget> renderTargetMap = new HashMap();

    public static RenderTarget createRenderTarget(ResourceLocation name, int width, int height, boolean overlay){
        RenderTarget renderTarget = renderTargetMap.get(name);
        if (!overlay && renderTarget!= null) {
            THDanmakuCraftCore.LOGGER.warn("Render Target {} already exits!",name);
            return renderTarget;
        }
        renderTargetMap.put(name,new TextureTarget(width,height,true, Minecraft.ON_OSX));
        return renderTargetMap.get(name);
    }

    public static RenderTarget createRenderTarget(String name, int width, int height, boolean overlay){
        return createRenderTarget(new ResourceLocation(THDanmakuCraftCore.MODID, name),width,height,overlay);
    }

    public static RenderTarget getRenderTarget(ResourceLocation name){
        return renderTargetMap.get(name);
    }

    public static RenderTarget getRenderTarget(String name){
        return getRenderTarget(new ResourceLocation(THDanmakuCraftCore.MODID,name));
    }

    public static void releaseRenderTarget(ResourceLocation name){
        renderTargetMap.remove(name);
    }
}
