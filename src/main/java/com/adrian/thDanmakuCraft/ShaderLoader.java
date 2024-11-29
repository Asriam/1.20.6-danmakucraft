package com.adrian.thDanmakuCraft;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ShaderLoader {

    private static final ResourceManager resourceManager = THDanmakuCraftCore.RESOURCE_MANAGER;
    private static final Map<ResourceLocation,ShaderInstance> shaderMap = new HashMap<>();

    public ShaderLoader() {
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        registryShader(new ResourceLocation(THDanmakuCraftCore.MODID,"box_blur"), DefaultVertexFormat.POSITION);


        registryShader(new ResourceLocation(THDanmakuCraftCore.MODID,"test_shader"), new VertexFormat(
                ImmutableMap.<String, VertexFormatElement>builder()
                        .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                        .put("Color"   , DefaultVertexFormat.ELEMENT_COLOR)
                        .put("UV0"     , DefaultVertexFormat.ELEMENT_UV0)
                        .put("UV1"     , DefaultVertexFormat.ELEMENT_UV1)
                        .put("UV2"     , DefaultVertexFormat.ELEMENT_UV2)
                        .put("Normal"  , DefaultVertexFormat.ELEMENT_NORMAL)
                        .build()
        ));
        /*
        Map<ResourceLocation, Resource> resourceMap =  resourceManager.listResources(new ResourceLocation(THDanmakuCraftCore.MODID,"shaders/core").getPath(), path -> path.toString().startsWith(THDanmakuCraftCore.MODID) && path.toString().endsWith("json"));
        resourceMap.forEach((resourceLocation, resource) -> {
            THDanmakuCraftCore.LOGGER.info("loading shader file {}",resourceLocation.toString());
            registryShader(new ResourceLocation(THDanmakuCraftCore.MODID,resourceLocation.getPath().replace("shaders/core/","").replace(".json","")), DefaultVertexFormat.POSITION_TEX_COLOR);
        });*/

        for(ShaderInstance shaderInstance:shaderMap.values()){
            event.registerShader(shaderInstance, shader -> {
                THDanmakuCraftCore.LOGGER.info("Shader loaded successfully!");
            });
        }
    }

    public static void registryShader(ResourceLocation resourceLocation, VertexFormat vertexFormat){
        shaderMap.put(resourceLocation,loadShader(resourceLocation,vertexFormat));
    }

    public static ShaderInstance loadShader(ResourceLocation resourceLocation, VertexFormat vertexFormat) {
        try {
            return new ShaderInstance(resourceManager,resourceLocation,vertexFormat);
        } catch (IOException e) {
            THDanmakuCraftCore.LOGGER.info("Failed load shader {}",resourceLocation,e);
        }
        return null;
    }

    public static ShaderInstance getShader(ResourceLocation resourceLocation){
        return shaderMap.get(resourceLocation);
    }
}
