package com.adrian.thDanmakuCraft.client.renderer.type;

import com.adrian.thDanmakuCraft.client.renderer.shader.ShaderLoader;
import com.adrian.thDanmakuCraft.events.TickEvents;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.Blend;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Function;

import static net.minecraft.client.renderer.RenderType.*;
@OnlyIn(Dist.CLIENT)
public class RenderTypes extends RenderStateShard{

    public RenderTypes(String p_110161_, Runnable p_110162_, Runnable p_110163_) {
        super(p_110161_, p_110162_, p_110163_);
    }

    public record RENDER_TYPE_2D_DANMAKU_CONTEXT(ResourceLocation textureLocation, BlendMode blendMode) {
    }

    public static final ShaderStateShard DANMAKU_DEPTH_OUTLINE_SHADER = new ShaderStateShard(() -> ShaderLoader.DANMAKU_DEPTH_OUTLINE_SHADER);
    public static RenderTarget TEST_RENDER_TARGET = new TextureTarget(2000,2000,true,true);
    public static RenderTarget DEPTH_BUFFER_TARGET = new TextureTarget(2000,2000,true,true);
    public static final ShaderStateShard BACKGROUND_WARP_EFFECT_SHADER = new ShaderStateShard(() -> {
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        ShaderInstance shader = ShaderLoader.BACKGROUND_WARP_EFFECT;
        float timer = TickEvents.clientTicks+Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        shader.safeGetUniform("Timer").set(timer);
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        shader.safeGetUniform("CameraPosition").set((float) cameraPos.x,(float) cameraPos.y,(float) cameraPos.z);
        //System.out.println(timer);
        shader.setSampler("DepthBuffer",DEPTH_BUFFER_TARGET.getDepthTextureId());
        shader.setSampler("ScreenBuffer",target.getColorTextureId());
        return shader;
    });

    public static final ShaderStateShard NORMAL_MAP_SHADER = new ShaderStateShard(() -> {
        return (ShaderInstance) ShaderLoader.NORMAL_MAP_SHADER;
    });

    public static final VertexFormat TEST_FORMAT = VertexFormat.builder()
                    .add("Position", VertexFormatElement.POSITION)
                    .add("Color"   , VertexFormatElement.COLOR)
                    .add("Color2"  , ModVertexFormats.COLOR2)
                    //.add("UV"      , VertexFormatElement.UV0)
                    .add("UV2"     , VertexFormatElement.UV0)
                    .add("UV3"     , VertexFormatElement.UV0)
                    .add("Normal"  , VertexFormatElement.NORMAL)
                    .build();

    public static final Function<RENDER_TYPE_2D_DANMAKU_CONTEXT, RenderType> RENDER_TYPE_THOBJECT = Util.memoize((context) ->
            RenderType.create("render_type_thobject", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
            CompositeState.builder()
                    .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                    .setTextureState(new TextureStateShard(context.textureLocation, true, true))
                    .setTransparencyState(
                            new TransparencyStateShard("custom_transparency", () -> {
                                RenderSystem.enableBlend();
                                context.blendMode.apply();
                            }, () -> {
                                RenderSystem.disableBlend();
                                RenderSystem.defaultBlendFunc();
                            }))
                    .setCullState(CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    //.setOutputState(TRANSLUCENT_TARGET)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .createCompositeState(true)
    ));

    public static final Function<RENDER_TYPE_2D_DANMAKU_CONTEXT, RenderType> RENDER_TYPE_SPELLCARD_AURA = Util.memoize((context) ->
            RenderType.create("render_type_thobject", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
                    CompositeState.builder()
                            .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                            .setTextureState(new TextureStateShard(context.textureLocation, true, true))
                            .setTransparencyState(
                                    new TransparencyStateShard("custom_transparency", () -> {
                                        RenderSystem.enableBlend();
                                        context.blendMode.apply();
                                    }, () -> {
                                        RenderSystem.disableBlend();
                                        RenderSystem.defaultBlendFunc();
                                    }))
                            .setCullState(CULL)
                            .setLightmapState(NO_LIGHTMAP)
                            .setOverlayState(NO_OVERLAY)
                            .setWriteMaskState(COLOR_DEPTH_WRITE)
                            //.setOutputState(TRANSLUCENT_TARGET)
                            .setOutputState(ITEM_ENTITY_TARGET)
                            .createCompositeState(true)
            ));

    public static final RenderType RENDER_TYPE_BACKGROUND_WARP_EFFECT = RenderType.create("render_type_background_warp_effect", ModVertexFormats.POSITION_NORMAL, VertexFormat.Mode.QUADS, 256, false, false,
                    CompositeState.builder()
                            .setShaderState(BACKGROUND_WARP_EFFECT_SHADER)
                            .setTransparencyState(
                                    new TransparencyStateShard("custom_transparency", () -> {
                                        RenderSystem.enableBlend();
                                        THBlendMode.getBlendMode(Blend.normal).apply();
                                    }, () -> {
                                        RenderSystem.disableBlend();
                                        RenderSystem.defaultBlendFunc();
                                    }))
                            .setCullState(NO_CULL)
                            .setLightmapState(NO_LIGHTMAP)
                            .setOverlayState(NO_OVERLAY)
                            .setWriteMaskState(COLOR_WRITE)
                            .setDepthTestState(NO_DEPTH_TEST)
                            //.setOutputState(TRANSLUCENT_TARGET)
                            //.setOutputState(ITEM_ENTITY_TARGET)
                            .setOutputState(ITEM_ENTITY_TARGET)
                            .createCompositeState(true)
            );

    public static final Function<RENDER_TYPE_2D_DANMAKU_CONTEXT, RenderType> RENDER_TYPE_2D_DANMAKU = Util.memoize((context) ->
            RenderType.create("render_type_2d_danmaku", ModVertexFormats.POSITION_COLOR_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, false,
                    CompositeState.builder()
                            .setShaderState(new ShaderStateShard(() -> ShaderLoader.DANMAKU_TEXTURE_SHADER))
                            .setTextureState(new TextureStateShard(context.textureLocation, true, true))
                            .setTransparencyState(/*TRANSLUCENT_TRANSPARENCY*/
                                    new TransparencyStateShard("custom_transparency", () -> {
                                        RenderSystem.enableBlend();
                                        context.blendMode.apply();
                                    }, () -> {
                                        RenderSystem.disableBlend();
                                        RenderSystem.defaultBlendFunc();
                                    }))
                            .setCullState(CULL)
                            .setLightmapState(NO_LIGHTMAP)
                            .setOverlayState(NO_OVERLAY)
                            .setWriteMaskState(COLOR_DEPTH_WRITE)
                            .setOutputState(ITEM_ENTITY_TARGET)
                            //.setOutputState(ITEM_ENTITY_TARGET)
                            .createCompositeState(true)
            ));

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

    public record TEST_RENDER_TYPE_FUNCTION_CONTEXT(BlendMode blendMode, boolean shouldCull) {

        public boolean equals(Object object){
            if(this == object) return true;
            if(object instanceof TEST_RENDER_TYPE_FUNCTION_CONTEXT(BlendMode mode, boolean cull)){
                return this.blendMode.equals(mode) && this.shouldCull == cull;
            }
            return false;
        }
    }

    public static final Function<TEST_RENDER_TYPE_FUNCTION_CONTEXT,RenderType> TEST_RENDER_TYPE_FUNCTION = Util.memoize((context) -> RenderType.create("lightning_3", TEST_FORMAT, VertexFormat.Mode.QUADS, 786432, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(DANMAKU_DEPTH_OUTLINE_SHADER)
                    .setTransparencyState(/*LIGHTNING_TRANSPARENCY*/
                        new RenderStateShard.TransparencyStateShard("custom_transparency", () -> {
                            RenderSystem.enableBlend();
                            context.blendMode.apply();
                        }, () -> {
                            RenderSystem.disableBlend();
                            RenderSystem.defaultBlendFunc();
                        }))
                    .setCullState(new RenderStateShard.CullStateShard(context.shouldCull))
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    //.setWriteMaskState(COLOR_WRITE)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    //.setWriteMaskState(COLOR_DEPTH_WRITE)
                    //.setDepthTestState(NO_DEPTH_TEST)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    //.setOutputState(TRANSLUCENT_TARGET)
                    .createCompositeState(true)
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

    public static final RenderType DEBUG_FILLED_BOX = create(
            "debug_aaa",
            ModVertexFormats.POSITION_NORMAL_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(NORMAL_MAP_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(false)
    );
}
