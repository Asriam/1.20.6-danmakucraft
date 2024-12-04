package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class THRenderType extends RenderStateShard{
    public THRenderType(String p_110161_, Runnable p_110162_, Runnable p_110163_) {
        super(p_110161_, p_110162_, p_110163_);
    }

    public static final Function<ResourceLocation, RenderType> BLEND_NONE = Util.memoize((texture) -> {
        RenderType.CompositeState compositestate = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                .setTextureState(new TextureStateShard(texture, true, true))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
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
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
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
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return RenderType.create("blend_multiply", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, compositestate);
    });

    public static final RenderType LIGHTNING = RenderType.create("lightning", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 786432, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    //.setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final ShaderStateShard DANMAKU_DEPTH_OUTLINE_SHADER = new ShaderStateShard(() -> {
        ShaderInstance shader = ShaderLoader.DANMAKU_DEPTH_OUTLINE_SHADER;
        shader.setSampler("DepthBuffer",Minecraft.getInstance().getMainRenderTarget().getDepthTextureId());
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
                    .setOverlayState(OVERLAY)
                    //.setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setWriteMaskState(COLOR_WRITE)
                    //.setOutputState(TRANSLUCENT_TARGET)
                    .createCompositeState(false)
    );

    public static final Function<ShaderStateShard,RenderType> TEST_RENDER_TYPE_FUNCTION = Util.memoize((shader) -> {
            RenderType.CompositeState compositestate = RenderType.CompositeState.builder()
                    .setShaderState(shader)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    //.setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setWriteMaskState(COLOR_WRITE)
                    //.setOutputState(TRANSLUCENT_TARGET)
                    .createCompositeState(false);
            return RenderType.create("lightning_3", TEST_FORMAT, VertexFormat.Mode.QUADS, 786432, false, true,compositestate);
    });

    public static final RenderType LIGHTNING2 = RenderType.create("lightning_2", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 786432, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    //.setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType LIGHTNING_NO_CULL = RenderType.create("lightning_no_cull", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 786432, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(OVERLAY)
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
