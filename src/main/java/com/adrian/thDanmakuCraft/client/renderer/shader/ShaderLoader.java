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
import org.apache.commons.compress.utils.Lists;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID/*, bus = EventBusSubscriber.Bus.MOD*/, value = Dist.CLIENT)
public class ShaderLoader {

    //private static final ResourceManager RESOURCE_MANAGER = Minecraft.getInstance().getResourceManager();
    private static final List<ModShaderInstance> shaders = Lists.newArrayList();

    public static ModShaderInstance DANMAKU_DEPTH_OUTLINE_SHADER;
    public static ModShaderInstance DANMAKU_TEXTURE_SHADER;
    public static ModShaderInstance BACKGROUND_WARP_EFFECT;
    public static ModShaderInstance NORMAL_MAP_SHADER;
    public static ModShaderInstance RENDER_ELLIPSOID_SHADER;
    public static ModShaderInstance RENDER_ELLIPSOID_SHADER_2;

    public ShaderLoader() {
    }

    public static void registryShaders(ResourceProvider resourceProvider){
        DANMAKU_DEPTH_OUTLINE_SHADER = _registryShader(resourceProvider, ResourceLocationUtil.thdanmakucraft("danmaku/rendertype_danmaku_1"), RenderTypes.TEST_FORMAT);
        DANMAKU_TEXTURE_SHADER = _registryShader(resourceProvider,ResourceLocationUtil.thdanmakucraft("danmaku/rendertype_danmaku_texture"), ModVertexFormats.POSITION_COLOR_COLOR_TEX);
        BACKGROUND_WARP_EFFECT = _registryShader(resourceProvider,ResourceLocationUtil.thdanmakucraft("danmaku/spellcard_effect/rendertype_bgwarpef"), ModVertexFormats.POSITION_NORMAL);
        NORMAL_MAP_SHADER = _registryShader(resourceProvider,ResourceLocationUtil.thdanmakucraft("danmaku/rendertype_normalmap"), ModVertexFormats.POSITION_NORMAL);
        // 必須跟 RenderTypes.TEST_ELLIPSOID 的格式一致，否則 Position2/Rotation/Scale 不會被綁定
        RENDER_ELLIPSOID_SHADER = _registryShader(resourceProvider,
                ResourceLocationUtil.thdanmakucraft("danmaku/ellipsoid/rendertype_ellipsoid_1"),
                ModVertexFormats.TEST_ELLIPSOID_VERTEXTFORMAT);
        RENDER_ELLIPSOID_SHADER_2 = _registryShader(resourceProvider,
                ResourceLocationUtil.thdanmakucraft("danmaku/ellipsoid/rendertype_ellipsoid_2"),
                ModVertexFormats.TEST_ELLIPSOID_VERTEXTFORMAT_2);
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        registryShaders(event.getResourceProvider());
        THDanmakuCraftMod.LOGGER.info("Shaders loaded successfully!");
    }

    /*public static ModShaderInstance registryModShader(ResourceLocation resourceLocation, VertexFormat vertexFormat){
        return DANMAKU_DEPTH_OUTLINE_SHADER;
    }*/

    public static ModShaderInstance _registryShader(ResourceProvider resourceProvider, ResourceLocation resourceLocation, VertexFormat vertexFormat){
        ModShaderInstance shader = loadShader(resourceProvider,resourceLocation,vertexFormat);
        shaders.add(shader);
        return shader;
    }

    public static ModShaderInstance loadShader(ResourceProvider resourceProvider, ResourceLocation resourceLocation, VertexFormat vertexFormat) {
        try {
            return new ModShaderInstance(resourceProvider,resourceLocation,vertexFormat);
        } catch (IOException e) {
            THDanmakuCraftMod.LOGGER.info("Failed load shader {}",resourceLocation,e);
        }
        return null;
    }
}
