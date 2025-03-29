package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ShaderLoader {

    //private static final ResourceManager RESOURCE_MANAGER = Minecraft.getInstance().getResourceManager();
    private static final Map<ResourceLocation,MyShaderInstance> shaderMap = new HashMap<>();

    public static MyShaderInstance DANMAKU_DEPTH_OUTLINE_SHADER;
    public static MyShaderInstance DANMAKU_TEXTURE_SHADER;
    public static MyShaderInstance BACKGROUND_WARP_EFFECT;

    public ShaderLoader() {
    }

    public static void registryShaders(ResourceProvider resourceProvider){
        DANMAKU_DEPTH_OUTLINE_SHADER = registryShader(resourceProvider, ResourceLocationUtil.thdanmakucraft("danmaku/rendertype_danmaku_1"), MyRenderTypes.TEST_FORMAT);
        BACKGROUND_WARP_EFFECT = registryShader(resourceProvider,ResourceLocationUtil.thdanmakucraft("danmaku/rendertype_bgwarpef"), MyVertexFormats.POSITION_NORMAL);
        DANMAKU_TEXTURE_SHADER = registryShader(resourceProvider,ResourceLocationUtil.thdanmakucraft("danmaku/rendertype_danmaku_texture"), MyVertexFormats.POSITION_COLOR_COLOR_TEX);
        /*registryShader(resourceProvider,ResourceLocationUtil.thdanmakucraft("box_blur"), DefaultVertexFormat.POSITION);
        registryShader(resourceProvider,ResourceLocationUtil.thdanmakucraft("test_shader"), new VertexFormat(
                ImmutableMap.<String, VertexFormatElement>builder()
                        .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                        .put("Color"   , DefaultVertexFormat.ELEMENT_COLOR)
                        .put("UV0"     , DefaultVertexFormat.ELEMENT_UV0)
                        .put("UV1"     , DefaultVertexFormat.ELEMENT_UV1)
                        .put("UV2"     , DefaultVertexFormat.ELEMENT_UV2)
                        .put("Normal"  , DefaultVertexFormat.ELEMENT_NORMAL)
                        .build()
        ));*/

    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        registryShaders(event.getResourceProvider());
        THDanmakuCraftMod.LOGGER.info("Shaders loaded successfully!");
    }

    public static MyShaderInstance registryShader(ResourceProvider resourceProvider, ResourceLocation resourceLocation, VertexFormat vertexFormat){
        shaderMap.put(resourceLocation,loadShader(resourceProvider,resourceLocation,vertexFormat));
        return shaderMap.get(resourceLocation);
    }

    public static void clearShaders(){
        shaderMap.clear();
    }

    public static MyShaderInstance loadShader(ResourceProvider resourceProvider, ResourceLocation resourceLocation, VertexFormat vertexFormat) {
        try {
            return new MyShaderInstance(resourceProvider,resourceLocation,vertexFormat);
        } catch (IOException e) {
            THDanmakuCraftMod.LOGGER.info("Failed load shader {}",resourceLocation,e);
        }
        return null;
    }

    public static MyShaderInstance getShader(ResourceLocation resourceLocation){
        return shaderMap.get(resourceLocation);
    }
}
