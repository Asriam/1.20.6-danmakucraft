package com.adrian.thDanmakuCraft.client.renderer.shader;

import com.adrian.thDanmakuCraft.client.renderer.type.ModVertexFormats;
import com.adrian.thDanmakuCraft.client.renderer.type.RenderTypes;
import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.util.io.ResourceLocationUtil;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID/*, bus = EventBusSubscriber.Bus.MOD*/, value = Dist.CLIENT)
public class ShaderLoader {

    //private static final ResourceManager RESOURCE_MANAGER = Minecraft.getInstance().getResourceManager();
    private static final Map<ResourceLocation,ModShaderInstance> shaderMap = new HashMap<>();

    public static ModShaderInstance DANMAKU_DEPTH_OUTLINE_SHADER;
    public static ModShaderInstance DANMAKU_TEXTURE_SHADER;
    public static ModShaderInstance BACKGROUND_WARP_EFFECT;
    public static ModShaderInstance NORMAL_MAP_SHADER;

    public ShaderLoader() {
    }

    public static void registryShaders(ResourceProvider resourceProvider){
        DANMAKU_DEPTH_OUTLINE_SHADER = registryShader(resourceProvider, ResourceLocationUtil.thdanmakucraft("danmaku/rendertype_danmaku_1"), RenderTypes.TEST_FORMAT);
        DANMAKU_TEXTURE_SHADER = registryShader(resourceProvider,ResourceLocationUtil.thdanmakucraft("danmaku/rendertype_danmaku_texture"), ModVertexFormats.POSITION_COLOR_COLOR_TEX);
        BACKGROUND_WARP_EFFECT = registryShader(resourceProvider,ResourceLocationUtil.thdanmakucraft("danmaku/rendertype_bgwarpef"), ModVertexFormats.POSITION_NORMAL);
        NORMAL_MAP_SHADER = registryShader(resourceProvider,ResourceLocationUtil.thdanmakucraft("danmaku/rendertype_normalmap"), ModVertexFormats.POSITION_NORMAL);
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        registryShaders(event.getResourceProvider());
        THDanmakuCraftMod.LOGGER.info("Shaders loaded successfully!");
    }

    public static ModShaderInstance registryShader(ResourceProvider resourceProvider, ResourceLocation resourceLocation, VertexFormat vertexFormat){
        shaderMap.put(resourceLocation,loadShader(resourceProvider,resourceLocation,vertexFormat));
        return shaderMap.get(resourceLocation);
    }

    public static void clearShaders(){
        shaderMap.clear();
    }

    public static ModShaderInstance loadShader(ResourceProvider resourceProvider, ResourceLocation resourceLocation, VertexFormat vertexFormat) {
        try {
            return new ModShaderInstance(resourceProvider,resourceLocation,vertexFormat);
        } catch (IOException e) {
            THDanmakuCraftMod.LOGGER.info("Failed load shader {}",resourceLocation,e);
        }
        return null;
    }

    public static ModShaderInstance getShader(ResourceLocation resourceLocation){
        return shaderMap.get(resourceLocation);
    }
}
