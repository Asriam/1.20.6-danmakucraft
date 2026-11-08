package com.adrian.thDanmakuCraft.client.renderer.danmaku;

import com.adrian.thDanmakuCraft.client.renderer.shader.ShaderLoader;
import com.adrian.thDanmakuCraft.client.renderer.type.ModVertexFormats;
import com.adrian.thDanmakuCraft.client.renderer.type.RenderTypes;
import com.adrian.thDanmakuCraft.client.renderer.compat.LegacyBufferBuilder;
import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.AbstractTHObjectRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.THObjectRendererProvider;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.THObjectRenderers;
import com.adrian.thDanmakuCraft.util.io.ResourceLocationUtil;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.*;

public class THObjectContainerRenderer {

    private static final RenderTarget MAIN_RENDER_TARGET = Minecraft.getInstance().getMainRenderTarget();
    private static final RenderTarget TEST_RENDER_TARGET = new TextureTarget(1000,1000,true,true);
    public static final RenderTarget DEPTH_BUFFER = new TextureTarget(1000,1000,true,true);
    //private static final RenderTarget DEPTH_BUFFER = new TextureTarget(MAIN_RENDER_TARGET.width,MAIN_RENDER_TARGET.height,true,true);
    private static final Map<THObjectType<?>, AbstractTHObjectRenderer<?>> THOBJECT_RENDERERS = THObjectRenderers.createEntityRenderers(new THObjectRendererProvider.Context());

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

    private static final Map<RenderType,LegacyBufferBuilder> buffers = new HashMap<>();
    /** 每幀重用的批次表（key = RenderType，value = 該批次的物件清單）。 */
    private static final Map<RenderType, List<THObject>> BATCHES = new HashMap<>();
    private static final LegacyBufferBuilder HIT_BOX_BUFFER = new LegacyBufferBuilder(4096);
    public static final LegacyBufferBuilder BUFFER_2 = new LegacyBufferBuilder(4096);
    public static final LegacyBufferBuilder ELLIPSOID_TEST_BUFFER = new LegacyBufferBuilder(4096);

    private static LegacyBufferBuilder getBuffer(RenderType renderType){
        if (buffers.containsKey(renderType)){
            return buffers.get(renderType);
        }
        return buffers.computeIfAbsent(renderType, (key) -> {
            LegacyBufferBuilder builder = new LegacyBufferBuilder(899+buffers.size());
            builder.begin(renderType.mode(), renderType.format());
            return builder;
        });
    }
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
            ShaderLoader.DANMAKU_DEPTH_OUTLINE_SHADER.setSampler("DepthBuffer", THObjectContainerRenderer.DEPTH_BUFFER.getDepthTextureId());
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
            ELLIPSOID_TEST_BUFFER.begin(VertexFormat.Mode.QUADS, ModVertexFormats.TEST_ELLIPSOID_VERTEXTFORMAT);
            boolean shouldRenderHitBox = entityRenderDispatcher.shouldRenderHitBoxes();
            RenderType renderTypeLines = RenderType.lines();
            ///Batching：重用同一個 map / 每個 RenderType 的 list（每幀只 clear）
            for (List<THObject> cached : BATCHES.values()) {
                cached.clear();
            }
            final Map<RenderType, List<THObject>> map = BATCHES;
            for (THObject object : objectList) {
                RenderType renderType = THObjectContainerRenderer.getTHObjectRenderer(object).getRenderType(object);
                List<THObject> bucket = map.get(renderType);
                if (bucket == null) {
                    bucket = new ArrayList<>();
                    map.put(renderType, bucket);
                }
                bucket.add(object);
            }
            for (Map.Entry<RenderType, List<THObject>> entry : map.entrySet()) {
                var renderType = entry.getKey();
                var list = entry.getValue();
                List<THObject> sortedList = sortObjects(list, cameraPosition);
                LegacyBufferBuilder builder = LegacyBufferBuilder.threadBuilder();
                builder.begin(renderType.mode(), renderType.format());
                if (shouldRenderHitBox) {
                    HIT_BOX_BUFFER.begin(renderTypeLines.mode(), renderTypeLines.format());
                }
                BUFFER_2.begin(renderType.mode(), renderType.format());
                for (THObject object : sortedList) {
                    if (object != null) {
                        AbstractTHObjectRenderer<THObject> renderer = THObjectContainerRenderer.getTHObjectRenderer(object);
                        if (renderer.shouldRender(object, frustum, camX, camY, camZ)) {
                            poseStack.pushPose();
                            Vec3 objectPos = object.getOffsetPosition(partialTicks);
                            poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
                            //渲染THObject
                            renderer.render(object, objectPos, partialTicks, poseStack, builder);
                            //渲染碰撞箱
                            if (shouldRenderHitBox && object.collision) {
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
                if (shouldApplyEffect()) {
                    THObjectContainerRenderer.TEST_RENDER_TARGET.bindWrite(true);
                }
                // 空的批次（整批被剔除、或 BUFFER_2 沒人寫）就不必建立 MeshData / 發 draw call
                boolean hasBuffer2 = BUFFER_2.vertexCount() > 0;
                boolean hasMain = builder.vertexCount() > 0;
                if (hasBuffer2 || hasMain) {
                    renderType.setupRenderState();
                    if (hasBuffer2) {
                        BufferUploader.drawWithShader(BUFFER_2.end());
                    }
                    if (hasMain) {
                        BufferUploader.drawWithShader(builder.end());
                    }
                    renderType.clearRenderState();
                } else {
                    // 仍要重置緩衝，否則下一批會沿用舊資料
                    BUFFER_2.end();
                    builder.end();
                }
                if (shouldApplyEffect()) {
                    THObjectContainerRenderer.TEST_RENDER_TARGET.unbindWrite();
                }
            }
            RenderTypes.TEST_ELLIPSOID.setupRenderState();
            BufferUploader.drawWithShader(ELLIPSOID_TEST_BUFFER.end());
            RenderTypes.TEST_ELLIPSOID.clearRenderState();
            // 不清 map：list 留給下一幀重用（上一層已 clear）

        }
        if(shouldApplyEffect()) {
            THObjectContainerRenderer.TEST_RENDER_TARGET.unbindWrite();
            mainRenderTarget.copyDepthFrom(THObjectContainerRenderer.TEST_RENDER_TARGET);
            mainRenderTarget.bindWrite(true);
        }

        RenderSystem.blendEquation(32774);
        //RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static List<THObject> sortObjects(List<THObject> list, Vec3 cam){
        if (list == null || list.size() <= 1) {
            return list;
        }

        try {
            // 原本比較器每次比較都呼叫 getPosition()（各配置一個 Vec3），
            // 5000 顆彈幕一趟排序會產生十幾萬個短命 Vec3。這裡直接算平方距離。
            list.sort((o1, o2) -> {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                return Double.compare(distanceSqr(o2, cam), distanceSqr(o1, cam));
            });
        }catch (Exception e){
            THDanmakuCraftMod.LOGGER.error(e.toString());
        }
        return list;
    }

    private static double distanceSqr(THObject object, Vec3 cam) {
        double dx = object.getX() - cam.x;
        double dy = object.getY() - cam.y;
        double dz = object.getZ() - cam.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static void renderContainerBound(THObjectContainer container, PoseStack poseStack, VertexConsumer vertexConsumer) {
        AABB aabb = container.getContainerBound().move(container.getPosition().reverse());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    public static void applyEffect(){
        ShaderInstance customShader = ShaderLoader.getShader(ResourceLocationUtil.thdanmakucraft("box_blur"));
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
        LegacyBufferBuilder bufferbuilder = LegacyBufferBuilder.threadBuilder();
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
