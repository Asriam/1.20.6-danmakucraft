package com.adrian.thDanmakuCraft.client.renderer.danmaku;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.client.renderer.*;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.AbstractTHObjectRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.THObjectRendererProvider;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.THObjectRenderers;
import com.adrian.thDanmakuCraft.events.RenderEvents;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.*;

public class THObjectContainerRenderer {

    private static final RenderTarget MAIN_RENDER_TARGET = Minecraft.getInstance().getMainRenderTarget();
    private static final RenderTarget TEST_RENDER_TARGET = new TextureTarget(1000,1000,true,true);
    private static final RenderTarget DEPTH_BUFFER = new TextureTarget(1000,1000,true,true);
    //private static final RenderTarget DEPTH_BUFFER = new TextureTarget(MAIN_RENDER_TARGET.width,MAIN_RENDER_TARGET.height,true,true);
    private static final Map<THObjectType<?>, AbstractTHObjectRenderer<?>> THOBJECT_RENDERERS = THObjectRenderers.createEntityRenderers(new THObjectRendererProvider.Context());

    private static PostChain blendChain;
    private static void initBlend(){

    }

    static {
        RenderEvents.registerRenderLevelStageTask("test_effect_clear", RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS, (poseStack, partialTick) -> {
            //TEST_RENDER_TARGET.resize(THObjectContainerRenderer.MAIN_RENDER_TARGET.width , THObjectContainerRenderer.MAIN_RENDER_TARGET.height, false);
            if(shouldApplyEffect()){
            TEST_RENDER_TARGET.setClearColor(0.0f,0.0f,0.0f,0.0f);
            TEST_RENDER_TARGET.clear(Minecraft.ON_OSX);
            TEST_RENDER_TARGET.copyDepthFrom(MAIN_RENDER_TARGET);
            MAIN_RENDER_TARGET.bindWrite(false);
            }
        });
        RenderEvents.registerRenderLevelStageTask("test_effect_applier",
                RenderLevelStageEvent.Stage.AFTER_ENTITIES,
                (poseStack, partialTick) -> {
                    if (shouldApplyEffect()) {
                        THObjectContainerRenderer.applyEffect(poseStack, partialTick);
                    }
                });
    }

    public static RenderTarget getRenderTarget(){
        return TEST_RENDER_TARGET;
    }

    public static void render(EntityRenderDispatcher entityRenderDispatcher, Frustum frustum, THObjectContainer container, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedOverlay) {
        renderTHObjects(entityRenderDispatcher, frustum, container.getObjectManager().getTHObjectsForRender(), partialTicks, poseStack, bufferSource, combinedOverlay);
    }

    private static boolean useShaderTransparency(){
        return Minecraft.useShaderTransparency();
    }

    private static boolean shouldApplyEffect(){
        return false;
    };

    private static final BufferBuilder HIT_BOX_BUFFER = new BufferBuilder(896);
    private static final BufferBuilder DANMAKU_BUFFER = new BufferBuilder(897);
    public static void renderTHObjects(EntityRenderDispatcher entityRenderDispatcher, Frustum frustum, List<THObject> objectList, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedOverlay){
        final Vec3 cameraPosition = entityRenderDispatcher.camera.getPosition();
        final double camX = cameraPosition.x;
        final double camY = cameraPosition.y;
        final double camZ = cameraPosition.z;

        final RenderTarget mainRenderTarget = THObjectContainerRenderer.MAIN_RENDER_TARGET;
        final ShaderInstance shader = ShaderLoader.DANMAKU_DEPTH_OUTLINE_SHADER;

        if(shader != null) {
            THObjectContainerRenderer.DEPTH_BUFFER.copyDepthFrom(mainRenderTarget);
            shader.setSampler("DepthBuffer", THObjectContainerRenderer.DEPTH_BUFFER.getDepthTextureId());
            //shader.setSampler("ScreenBuffer", mainRenderTarget.getColorTextureId());
            mainRenderTarget.bindWrite(true);
        }

        if (shouldApplyEffect()) {
            mainRenderTarget.unbindWrite();
            THObjectContainerRenderer.TEST_RENDER_TARGET.copyDepthFrom(mainRenderTarget);
            THObjectContainerRenderer.TEST_RENDER_TARGET.bindWrite(true);
        }

        RenderSystem.enableBlend();
        if (!objectList.isEmpty()) {
            boolean shouldRenderHitBox = entityRenderDispatcher.shouldRenderHitBoxes();
            RenderType renderTypeLines = RenderType.lines();

            Map<RenderType,List<THObject>> map = new HashMap<>();
            for (THObject object : objectList) {
                RenderType renderType = THObjectContainerRenderer.getTHObjectRenderer(object).getRenderType(object);
                map.computeIfAbsent(renderType, (key) -> new ArrayList<>()).add(object);
            }
            for (Map.Entry<RenderType, List<THObject>> entry : map.entrySet()) {
                var renderType = entry.getKey();
                var list = entry.getValue();
                List<THObject> sortedList = layerObjects(list, camX, camY, camZ);
                BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
                builder.begin(renderType.mode(), renderType.format());
                if (shouldRenderHitBox){
                    HIT_BOX_BUFFER.begin(renderTypeLines.mode(), renderTypeLines.format());
                }
                for (THObject object : sortedList) {
                    if (object != null) {
                        AbstractTHObjectRenderer<THObject> renderer = THObjectContainerRenderer.getTHObjectRenderer(object);
                        if (renderer.shouldRender(object, frustum, camX, camY, camZ)) {
                            poseStack.pushPose();
                            Vec3 objectPos = object.getOffsetPosition(partialTicks);
                            poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
                            poseStack.pushPose();
                            renderer.render(object, objectPos, partialTicks, poseStack, builder, combinedOverlay);
                            poseStack.popPose();
                            if(shouldRenderHitBox) {
                                renderer.renderHitBox(object, objectPos, partialTicks, poseStack, HIT_BOX_BUFFER);
                            }
                            poseStack.popPose();
                        }
                    }
                }
                if (shouldRenderHitBox) {
                    renderTypeLines.setupRenderState();
                    BufferUploader.drawWithShader(HIT_BOX_BUFFER.end());
                    renderTypeLines.clearRenderState();
                }
                renderType.setupRenderState();
                BufferUploader.drawWithShader(builder.end());
                renderType.clearRenderState();
                /*renderType.setupRenderState();
                if (shouldApplyEffect()) {
                    THObjectContainerRenderer.TEST_RENDER_TARGET.bindWrite(true);
                }
                BufferUploader.drawWithShader(builder.end());*if (shouldApplyEffect()) {
                    THObjectContainerRenderer.TEST_RENDER_TARGET.unbindWrite();
                }
                renderType.clearRenderState();*/
            }
        }

        RenderSystem.blendEquation(32774);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        /*if(shouldApplyEffect()) {
            THObjectContainerRenderer.TEST_RENDER_TARGET.unbindWrite();
            mainRenderTarget.copyDepthFrom(THObjectContainerRenderer.TEST_RENDER_TARGET);
            mainRenderTarget.bindWrite(true);
        }*/
    }

    public static List<THObject> layerObjects(List<THObject> list, double camX, double camY, double camZ){
        if (list == null || list.size() <= 1) {
            return list;
        }

        try {
            list.sort((o1, o2) -> {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                return Double.compare(
                        o2.getPosition().distanceToSqr(camX,camY,camZ),
                        o1.getPosition().distanceToSqr(camX,camY,camZ)
                );
            });
        }catch (Exception e){
            THDanmakuCraftMod.LOGGER.error(e.toString());
        }
        return list;
    }

    public static void renderContainerBound(THObjectContainer container, PoseStack poseStack, VertexConsumer vertexConsumer) {
        AABB aabb = container.getContainerBound().move(container.getPosition().reverse());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    public static void applyEffect(PoseStack poseStack, float partialTick){
        ShaderInstance customShader = ShaderLoader.getShader(ResourceLocationUtil.mod("box_blur"));
        if (customShader != null) {
            RenderTarget inTarget = TEST_RENDER_TARGET;
            RenderTarget outTarget = MAIN_RENDER_TARGET;
            outTarget.copyDepthFrom(inTarget);

            int[] inSize = {inTarget.width, inTarget.height};
            int[] outSize = {outTarget.width , outTarget.height};

            outTarget.bindWrite(true);
            customShader.setSampler("DiffuseSampler", inTarget.getColorTextureId());
            Matrix4f projMat = new Matrix4f().setOrtho(0.0F, (float) outSize[0], 0.0F, (float) outSize[1], 0.1F, 1000.0F);
            customShader.safeGetUniform("ProjMat").set(projMat);
            customShader.safeGetUniform("InSize").set((float) inSize[0], (float) inSize[1]);
            customShader.safeGetUniform("OutSize").set((float) outSize[0], (float) outSize[1]);
            customShader.apply();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            drawOnScreen(outSize[0],outSize[1]);
            customShader.clear();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            outTarget.unbindWrite();

            MAIN_RENDER_TARGET.bindWrite(false);

        }
    }

    public static void drawOnScreen(float screenWidth, float screenHeight){
        BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(0.0, 0.0, 0.0).endVertex();
        bufferbuilder.vertex(screenWidth, 0.0, 0.0).endVertex();
        bufferbuilder.vertex(screenWidth, screenHeight, 0.0).endVertex();
        bufferbuilder.vertex(0.0, screenHeight, 0.0).endVertex();
        BufferUploader.draw(bufferbuilder.end());
    }

    public static <T extends THObject> AbstractTHObjectRenderer<T> getTHObjectRenderer(@NotNull T object) {
        return (AbstractTHObjectRenderer<T>) THOBJECT_RENDERERS.get(object.getType());
    }
}
