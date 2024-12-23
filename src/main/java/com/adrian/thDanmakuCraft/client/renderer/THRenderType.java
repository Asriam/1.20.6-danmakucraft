package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BiFunction;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class THRenderType extends RenderType{
    public THRenderType(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
    }

    public static final Function<ResourceLocation, RenderType> BLEND_NONE = Util.memoize((texture) -> {
        RenderType.CompositeState compositestate = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                .setTextureState(new TextureStateShard(texture, true, true))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setOutputState(TRANSLUCENT_TARGET)
                .createCompositeState(false);
        return RenderType.create("blend_none", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, compositestate);
    });

    public static final Function<ResourceLocation, RenderType> BLEND_LIGHTEN = Util.memoize((texture) -> {
        RenderType.CompositeState compositestate = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                .setTextureState(new TextureStateShard(texture, true, true))
                .setTransparencyState(LIGHTNING_TRANSPARENCY)
                .setCullState(CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setOutputState(TRANSLUCENT_TARGET)
                .createCompositeState(false);
        return RenderType.create("blend_lighten", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, compositestate);
    });

    public static final Function<ResourceLocation, RenderType> BLEND_MULTIPLY = Util.memoize((texture) -> {
        RenderType.CompositeState compositestate = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                .setTextureState(new TextureStateShard(texture, true, true))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard("multiplying_transparency", () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.DST_COLOR, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
                }, () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                }))
                .setCullState(CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .setOutputState(TRANSLUCENT_TARGET)
                .createCompositeState(false);
        return RenderType.create("blend_multiply", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, compositestate);
    });

    public static final RenderType LIGHTNING = RenderType.create("lightning", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 786432, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    //.setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final ShaderStateShard DANMAKU_DEPTH_OUTLINE_SHADER = new ShaderStateShard(() -> {
        MyShaderInstance shader = ShaderLoader.DANMAKU_DEPTH_OUTLINE_SHADER;
        shader.setSampler("DepthBuffer",Minecraft.getInstance().getMainRenderTarget().getDepthTextureId());
        shader.safeGetUniform("Near").set(GameRenderer.PROJECTION_Z_NEAR);
        shader.safeGetUniform("Far").set(Minecraft.getInstance().gameRenderer.getDepthFar());
        //THDanmakuCraftCore.LOGGER.info(""+Minecraft.getInstance().gameRenderer.getDepthFar());
        return shader;
    });

     public static final VertexFormat TEST_FORMAT = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                    .put("Color"   , DefaultVertexFormat.ELEMENT_COLOR)
                    .put("Color2"  , DefaultVertexFormat.ELEMENT_COLOR)
                    .put("UV"      , DefaultVertexFormat.ELEMENT_UV)
                    .put("Normal"  , DefaultVertexFormat.ELEMENT_NORMAL)
                    .build()
    );

    public static final RenderType TEST_RENDER_TYPE = RenderType.create("lightning_3", TEST_FORMAT, VertexFormat.Mode.QUADS, 786432, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(DANMAKU_DEPTH_OUTLINE_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    //.setDepthTestState(EQUAL_DEPTH_TEST)
                    //.setWriteMaskState(COLOR_WRITE)
                    //.setOutputState(TRANSLUCENT_TARGET)
                    .createCompositeState(false)
    );

    public static final BiFunction<BlendMode,Boolean,RenderType> TEST_RENDER_TYPE_FUNCTION = Util.memoize((blendMode,shouldCull) -> RenderType.create("lightning_3", TEST_FORMAT, VertexFormat.Mode.QUADS, 786432, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(DANMAKU_DEPTH_OUTLINE_SHADER
                            /*
                            new ShaderStateShard(() -> {
                                MyShaderInstance shader = ShaderLoader.DANMAKU_DEPTH_OUTLINE_SHADER;
                                shader.setSampler("DepthBuffer",Minecraft.getInstance().getMainRenderTarget().getDepthTextureId());
                                shader.safeGetUniform("Near").set(GameRenderer.PROJECTION_Z_NEAR);
                                shader.safeGetUniform("Far").set(Minecraft.getInstance().gameRenderer.getDepthFar());
                                shader.setBlendMode(blendMode);
                                return shader;
                            })*/)
                    .setTransparencyState(/*LIGHTNING_TRANSPARENCY*/
                        new RenderStateShard.TransparencyStateShard("custom_transparency", () -> {
                            RenderSystem.enableBlend();
                            blendMode.apply();
                        }, () -> {
                            RenderSystem.disableBlend();
                            RenderSystem.defaultBlendFunc();
                        }))
                    .setCullState(new RenderStateShard.CullStateShard(shouldCull))
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    //.setWriteMaskState(COLOR_DEPTH_WRITE)
                    //.setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setOutputState(TRANSLUCENT_TARGET)
                    .createCompositeState(false)
            )
    );

    public static final RenderType LIGHTNING2 = RenderType.create("lightning_2", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 786432, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    //.setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType LIGHTNING_NO_CULL = RenderType.create("lightning_no_cull", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 786432, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    //.setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType TRANSLUCENT = RenderType.create("translucent", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 786432, true, true,
            RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP)
                    .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(TRANSLUCENT_TARGET)
                    .createCompositeState(true)
    );

    public enum BLEND{
        NONE(BLEND_NONE),
        LIGHTEN(BLEND_LIGHTEN),
        MULTIPLY(BLEND_MULTIPLY);

        public final Function<ResourceLocation, RenderType> renderType;

        BLEND(Function<ResourceLocation, RenderType> renderType){
            this.renderType = renderType;
        }
    }
}
