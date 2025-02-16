package com.adrian.thDanmakuCraft.client.renderer.danmaku;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.client.renderer.*;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.AbstractTHObjectRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.THObjectRendererProvider;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.THObjectRenderers;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.bullet.THBulletRenderers;
import com.adrian.thDanmakuCraft.events.RenderEvents;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.ConstantUtil;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THLaser;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class THObjectContainerRenderer {

    public static final RenderTarget TEST_RENDER_TARGET = new TextureTarget(1000,1000,true,true);
    public static final RenderTarget MAIN_RENDER_TARGET = Minecraft.getInstance().getMainRenderTarget();
    public static final RenderTarget DEPTH_BUFFER = new TextureTarget(800,800,true,true);
    //private static final RenderTarget DEPTH_BUFFER = new TextureTarget(MAIN_RENDER_TARGET.width,MAIN_RENDER_TARGET.height,true,true);
    private static final Map<THObjectType<?>, AbstractTHObjectRenderer<?>> THOBJECT_RENDERERS = THObjectRenderers.createEntityRenderers(new THObjectRendererProvider.Context());

    static {
        RenderEvents.registerRenderLevelStageTask("test_effect_clear", RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS, (poseStack, partialTick) -> {
            THObjectContainerRenderer.TEST_RENDER_TARGET.resize(THObjectContainerRenderer.MAIN_RENDER_TARGET.width , THObjectContainerRenderer.MAIN_RENDER_TARGET.height, false);
            THObjectContainerRenderer.TEST_RENDER_TARGET.setClearColor(0.0f,0.0f,0.0f,0.0f);
            THObjectContainerRenderer.TEST_RENDER_TARGET.clear(true);
        });
        RenderEvents.registerRenderLevelStageTask("test_effect_applier",
                RenderLevelStageEvent.Stage.AFTER_WEATHER,
                THObjectContainerRenderer::applyEffect);
    }

    public static void render(EntityRenderDispatcher entityRenderDispatcher, Frustum frustum, THObjectContainer container, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedOverlay) {
        renderContainers(entityRenderDispatcher, frustum, List.of(new THObjectContainer[]{container}), partialTicks, poseStack, bufferSource, combinedOverlay);
        //renderContainer(entityRenderDispatcher, frustum, container, partialTicks, poseStack, bufferSource, combinedOverlay);
    }

    private static final boolean shouldApplyEffect = false;

    /*
    public static void renderContainer(EntityRenderDispatcher entityRenderDispatcher, Frustum frustum, THObjectContainer container, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedOverlay){
        if (entityRenderDispatcher.shouldRenderHitBoxes()) {
            THObjectContainerRenderer.renderContainerBound(container, poseStack, bufferSource.getBuffer(RenderType.lines()));
        }
        PoseStack.Pose poseStack1$$ = poseStack.last();

        poseStack.popPose();
        poseStack.pushPose();

        final Vec3 cameraPosition = entityRenderDispatcher.camera.getPosition();
        final double camX = cameraPosition.x;
        final double camY = cameraPosition.y;
        final double camZ = cameraPosition.z;

        final RenderTarget mainRenderTarget = THObjectContainerRenderer.MAIN_RENDER_TARGET;
        final ShaderInstance shader = ShaderLoader.DANMAKU_DEPTH_OUTLINE_SHADER;

        if(shader != null) {
            THObjectContainerRenderer.DEPTH_BUFFER.copyDepthFrom(mainRenderTarget);
            shader.setSampler("DepthBuffer", THObjectContainerRenderer.DEPTH_BUFFER.getDepthTextureId());
            mainRenderTarget.bindWrite(true);
        }

        final boolean shouldApplyEffect = true;

        if (shouldApplyEffect) {
            mainRenderTarget.unbindWrite();
            THObjectContainerRenderer.TEST_RENDER_TARGET.copyDepthFrom(mainRenderTarget);
            THObjectContainerRenderer.TEST_RENDER_TARGET.bindWrite(true);
        }

        final List<THObject> objectList = container.getObjectManager().getTHObjectsForRender();
        RenderSystem.enableBlend();
        if (!objectList.isEmpty()) {
            if (entityRenderDispatcher.shouldRenderHitBoxes()) {
                BufferBuilder vertexConsumer = (BufferBuilder) bufferSource.getBuffer(RenderType.lines());
                for (THObject object:objectList) {
                    if (object != null && (object instanceof THCurvedLaser || THObjectContainerRenderer.shouldRenderTHObject(object, frustum, camX, camY, camZ))) {
                        poseStack.pushPose();
                        Vec3 objectPos = object.getOffsetPosition(partialTicks);
                        poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
                        if (object.collision) {
                            if (object instanceof THCurvedLaser laser) {
                                THObjectContainerRenderer.renderTHCurvedLaserHitBoxes(laser, objectPos, poseStack, vertexConsumer, partialTicks, frustum);
                            } else {
                                THObjectContainerRenderer.renderTHObjectsHitBox(object, poseStack, vertexConsumer);
                            }
                        }
                        poseStack.popPose();

                    }
                }
            }

            Map<RenderType,List<THObject>> map = new HashMap<>();
            for (THObject object : objectList) {
                RenderType renderType = THObjectContainerRenderer.getRenderType(object);
                map.computeIfAbsent(renderType, (key) -> new ArrayList<>()).add(object);
            }

            map.forEach((renderType, list) -> {
                List<THObject> sortedList = layerObjects(list, camX, camY, camZ);
                BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
                builder.begin(renderType.mode(), renderType.format());

                for (THObject object : sortedList) {
                    if (object != null && (object instanceof THCurvedLaser || THObjectContainerRenderer.shouldRenderTHObject(object, frustum, camX, camY, camZ))) {
                        poseStack.pushPose();
                        Vec3 objectPos = object.getOffsetPosition(partialTicks);
                        poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
                        THObjectContainerRenderer.getTHObjectRenderer(object).render(object, objectPos, partialTicks, poseStack, builder, combinedOverlay);
                        poseStack.popPose();
                    }
                }

                renderType.setupRenderState();
                if (shouldApplyEffect) {
                    THObjectContainerRenderer.TEST_RENDER_TARGET.bindWrite(true);
                }
                BufferUploader.drawWithShader(builder.end());
                if (shouldApplyEffect) {
                    THObjectContainerRenderer.TEST_RENDER_TARGET.unbindWrite();
                }
                renderType.clearRenderState();
            });
        }

        RenderSystem.blendEquation(32774);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        if(shouldApplyEffect) {
            THObjectContainerRenderer.TEST_RENDER_TARGET.unbindWrite();
            mainRenderTarget.copyDepthFrom(THObjectContainerRenderer.TEST_RENDER_TARGET);
            mainRenderTarget.bindWrite(true);
        }

        poseStack.popPose();
        poseStack.pushPose();

        poseStack.mulPose(poseStack1$$.pose());
    }
    */

    public static Frustum frustum = Minecraft.getInstance().levelRenderer.getFrustum();
    public static void renderContainers(EntityRenderDispatcher entityRenderDispatcher, Frustum frustum, List<THObjectContainer> containers, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedOverlay){
        /*
        if (entityRenderDispatcher.shouldRenderHitBoxes()) {
            for(THObjectContainer container:containers){
            THObjectContainerRenderer.renderContainerBound(container, poseStack, bufferSource.getBuffer(RenderType.lines()));
            }
        }*/

        /*PoseStack.Pose poseStack1$$ = poseStack.last();

        poseStack.popPose();
        poseStack.pushPose();*/

        final Vec3 cameraPosition = entityRenderDispatcher.camera.getPosition();
        final double camX = cameraPosition.x;
        final double camY = cameraPosition.y;
        final double camZ = cameraPosition.z;

        //poseStack.translate(-camX,-camY,-camZ);

        final RenderTarget mainRenderTarget = THObjectContainerRenderer.MAIN_RENDER_TARGET;
        final ShaderInstance shader = ShaderLoader.DANMAKU_DEPTH_OUTLINE_SHADER;

        if(shader != null) {
            THObjectContainerRenderer.DEPTH_BUFFER.copyDepthFrom(mainRenderTarget);
            shader.setSampler("DepthBuffer", THObjectContainerRenderer.DEPTH_BUFFER.getDepthTextureId());
            mainRenderTarget.bindWrite(true);
        }

        if (shouldApplyEffect) {
            mainRenderTarget.unbindWrite();
            THObjectContainerRenderer.TEST_RENDER_TARGET.copyDepthFrom(mainRenderTarget);
            THObjectContainerRenderer.TEST_RENDER_TARGET.bindWrite(true);
        }

        final List<THObject> objectList = Lists.newArrayList();
        for (THObjectContainer container:containers) {
            objectList.addAll(container.getObjectManager().getTHObjectsForRender());
        }
        RenderSystem.enableBlend();
        if (!objectList.isEmpty()) {
            if (entityRenderDispatcher.shouldRenderHitBoxes()) {
                BufferBuilder vertexConsumer = (BufferBuilder) bufferSource.getBuffer(RenderType.lines());
                for (THObject object:objectList) {
                    if (object != null && (object instanceof THCurvedLaser || THObjectContainerRenderer.shouldRenderTHObject(object, frustum, camX, camY, camZ))) {
                        poseStack.pushPose();
                        Vec3 objectPos = object.getOffsetPosition(partialTicks);
                        poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
                        if (object.collision) {
                            if (object instanceof THCurvedLaser laser) {
                                THObjectContainerRenderer.renderTHCurvedLaserHitBoxes(laser, objectPos, poseStack, vertexConsumer, partialTicks, frustum);
                            } else {
                                THObjectContainerRenderer.renderTHObjectsHitBox(object, poseStack, vertexConsumer);
                            }
                        }
                        poseStack.popPose();

                    }
                }
            }

            Map<RenderType,List<THObject>> map = new HashMap<>();
            for (THObject object : objectList) {
                //RenderType renderType = THObjectContainerRenderer.getRenderType(object);
                RenderType renderType = THObjectContainerRenderer.getTHObjectRenderer(object).getRenderType(object);
                map.computeIfAbsent(renderType, (key) -> new ArrayList<>()).add(object);
            }

            map.forEach((renderType, list) -> {
                List<THObject> sortedList = layerObjects(list, camX, camY, camZ);
                BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
                builder.begin(renderType.mode(), renderType.format());

                for (THObject object : sortedList) {
                    if (object != null && (object instanceof THCurvedLaser || THObjectContainerRenderer.shouldRenderTHObject(object, frustum, camX, camY, camZ))) {
                        poseStack.pushPose();
                        Vec3 objectPos = object.getOffsetPosition(partialTicks);
                        poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
                        THObjectContainerRenderer.getTHObjectRenderer(object).render(object, objectPos, partialTicks, poseStack, builder, combinedOverlay);
                        poseStack.popPose();
                    }
                }

                renderType.setupRenderState();
                if (shouldApplyEffect) {
                    THObjectContainerRenderer.TEST_RENDER_TARGET.bindWrite(true);
                }
                BufferUploader.drawWithShader(builder.end());
                if (shouldApplyEffect) {
                    THObjectContainerRenderer.TEST_RENDER_TARGET.unbindWrite();
                }
                renderType.clearRenderState();
            });
        }

        RenderSystem.blendEquation(32774);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        if(shouldApplyEffect) {
            THObjectContainerRenderer.TEST_RENDER_TARGET.unbindWrite();
            mainRenderTarget.copyDepthFrom(THObjectContainerRenderer.TEST_RENDER_TARGET);
            //mainRenderTarget.clear(true);
            mainRenderTarget.bindWrite(true);
        }

        /*poseStack.popPose();
        poseStack.pushPose();

        poseStack.mulPose(poseStack1$$.pose())*/;
    }

    public static RenderType getRenderType(THObject object) {
        RenderType renderType;
        if(object instanceof THBullet bullet) {
            if(bullet.getStyle().is3D()){
                renderType = THBulletRenderers.getRenderer(bullet.getStyle()).getRenderType(bullet);
            }else {
                renderType = THBulletRenderers.getBullet2DRenderer().getRenderType(bullet);
            }
        }else if(object instanceof THCurvedLaser || object instanceof THLaser){
            renderType = THRenderType.TEST_RENDER_TYPE_FUNCTION.apply(new THRenderType.TEST_RENDER_TYPE_FUNCTION_CONTEXT(THBlendMode.getBlendMode(object.getBlend()), true));
        }else {
            renderType = THRenderType.RENDER_TYPE_THOBJECT.apply(new THRenderType.RENDER_TYPE_2D_DANMAKU_CONTEXT(
                    object.getImage().getTextureLocation(),
                    THBlendMode.getBlendMode(object.getBlend()))
            );
        }
        return renderType;
    }

    static void renderTHObjectsHitBox(@NotNull THObject object, PoseStack poseStack, VertexConsumer vertexConsumer) {
        AABB aabb = object.getBoundingBox().move(-object.getX(), -object.getY(), -object.getZ());
        if(object.getCollisionType() == THObject.CollisionType.AABB) {
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, 0.0F, 1.0F, 1.0F, 1.0F);
        }else if(object.getCollisionType() == THObject.CollisionType.SPHERE){
            Color color = THObject.Color(0,255,255,255);
            float size = (float) object.getSize().x;
            RenderUtil.renderSphere(vertexConsumer,poseStack.last(),1,
                    ConstantUtil.VECTOR3F_ZERO,
                    new Vector3f(size,size,size),
                    6,6,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color,color,color);
        }else if(object.getCollisionType() == THObject.CollisionType.ELLIPSOID){
            poseStack.pushPose();
            Vector3f rotation = object.getRotation();
            poseStack.mulPose(new Quaternionf().rotationYXZ(rotation.y,-rotation.x,rotation.z));
            Color color = THObject.Color(0,255,255,255);
            RenderUtil.renderSphere(vertexConsumer,poseStack.last(),1,
                    ConstantUtil.VECTOR3F_ZERO,
                    object.getSize().toVector3f(),
                    6,6,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color,color,color);
            poseStack.popPose();


        }

        Vec3 vec31 = object.getMotionDirection();
        Matrix4f matrix4f = poseStack.last().pose();
        //Matrix3f matrix3f = poseStack.last().normal();
        vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F).color(0, 0, 255, 255).normal(poseStack.last(), (float)vec31.x, (float)vec31.y, (float)vec31.z).endVertex();
        vertexConsumer.vertex(matrix4f, (float)(vec31.x * 2.0D), (float)((vec31.y * 2.0D)), (float)(vec31.z * 2.0D)).color(0, 0, 255, 255).normal(poseStack.last(), (float)vec31.x, (float)vec31.y, (float)vec31.z).endVertex();
    }

    static void renderTHCurvedLaserHitBoxes(THCurvedLaser laser, Vec3 laserPos, PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks, Frustum frustum){
        List<THCurvedLaser.LaserNode> nodes = laser.nodeManager.getAllNodes();
        for(THCurvedLaser.LaserNode node: nodes){
            Vec3 pos = node.getPosition();
            AABB aabb = node.getBoundingBoxForCulling().inflate(0.5D);
            if (aabb.hasNaN() || aabb.getSize() == 0.0D) {
                aabb = new AABB(pos.x() - 2.0D, pos.y() - 2.0D, pos.z() - 2.0D, pos.x() + 2.0D, pos.y() + 2.0D, pos.z() + 2.0D);
            }

            if (node.isValid() && frustum.isVisible(aabb)) {
                poseStack.pushPose();
                AABB aabb2 = node.getBoundingBox().move(-pos.x(), -pos.y(), -pos.z());
                Vec3 offsetPos = laserPos.vectorTo(node.getOffsetPosition(partialTicks));
                poseStack.translate(offsetPos.x(), offsetPos.y(), offsetPos.z());
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb2, 0.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        }
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
            THDanmakuCraftCore.LOGGER.error(e.toString());
        }
        return list;
    }

    static void renderContainerBound(THObjectContainer container, PoseStack poseStack, VertexConsumer vertexConsumer) {
        AABB aabb = container.getContainerBound().move(container.getPosition().reverse());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    public static boolean shouldRenderTHObject(THObject object, Frustum frustum, double camX, double camY, double camZ) {
        if (!object.shouldRender(camX,camY,camZ)) {
            return false;
        } else if (object.noCulling) {
            return true;
        } else {
            AABB aabb = object.getBoundingBoxForCulling().inflate(0.5D);
            if (aabb.hasNaN() || aabb.getSize() == 0.0D) {
                aabb = new AABB(object.getX() - 2.0D, object.getY() - 2.0D, object.getZ() - 2.0D, object.getX() + 2.0D, object.getY() + 2.0D, object.getZ() + 2.0D);
            }

            return frustum.isVisible(aabb);
        }
    }

    public static void applyEffect(PoseStack poseStack, float partialTick){
        ShaderInstance customShader = ShaderLoader.getShader(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"box_blur"));
        if (customShader != null) {
            RenderTarget inTarget = TEST_RENDER_TARGET;
            RenderTarget outTarget = MAIN_RENDER_TARGET;
            MAIN_RENDER_TARGET.unbindWrite();

            inTarget.copyDepthFrom(MAIN_RENDER_TARGET);

            int[] inSize = {inTarget.width, inTarget.height};
            int[] outSize = {outTarget.width , outTarget.height};

            outTarget.bindWrite(true);
            customShader.setSampler("DiffuseSampler", inTarget.getColorTextureId());
            Matrix4f projMat = new Matrix4f().setOrtho(0.0F, (float) outSize[0], 0.0F, (float) outSize[1], 0.1F, 1000.0F);
            customShader.safeGetUniform("ProjMat").set(projMat);
            customShader.safeGetUniform("InSize").set((float) inSize[0], (float) inSize[1]);
            customShader.safeGetUniform("OutSize").set((float) outSize[0], (float) outSize[1]);
            //customShader.safeGetUniform("BlurDir").set(1.0f,1.0f);
            //customShader.safeGetUniform("Radius").set(0.0f);
            //customShader.safeGetUniform("RadiusMultiplier").set(1.0f);
            customShader.apply();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            //RenderSystem.depthFunc(515);
            //RenderSystem.depthMask(true);
            //RenderSystem.enableDepthTest();
            drawOnScreen(outSize[0],outSize[1]);

            customShader.clear();
            //RenderSystem.depthFunc(515);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            outTarget.unbindWrite();

            MAIN_RENDER_TARGET.bindWrite(true);

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
