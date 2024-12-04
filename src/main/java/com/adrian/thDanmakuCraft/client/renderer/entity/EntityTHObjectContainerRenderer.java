package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.events.RenderEvents;
import com.adrian.thDanmakuCraft.client.renderer.ShaderLoader;
import com.adrian.thDanmakuCraft.world.entity.danmaku.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class EntityTHObjectContainerRenderer extends EntityRenderer<EntityTHObjectContainer> {
    public Frustum frustum;
    public EntityRenderDispatcher dispatcher;

    private static final RenderTarget testRenderTarget = new TextureTarget(1000,1000,true,true);
    private static final RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
    //public static final RenderTarget DEPTH_BUFFER = new TextureTarget(1000,1000,true,true);

    public EntityTHObjectContainerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.dispatcher = this.entityRenderDispatcher;

        RenderEvents.registryRenderLevelStageTask("test_shader_clear", RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS, (poseStack, partialTick) -> {
            testRenderTarget.setClearColor(0.0f,0.0f,0.0f,0.0f);
            testRenderTarget.clear(true);
            testRenderTarget.resize(mainRenderTarget.width , mainRenderTarget.height, false);
        });

        RenderEvents.registryRenderLevelStageTask("test_shader_applier", RenderLevelStageEvent.Stage.AFTER_ENTITIES, (poseStack, partialTick) -> {
            this.applyShader();
        });
    }

    public void applyShader(){
        ShaderInstance customShader = ShaderLoader.getShader(new ResourceLocation(THDanmakuCraftCore.MODID,"box_blur"));
        if (customShader != null) {
            RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
            RenderTarget inTarget = testRenderTarget;
            RenderTarget outTarget = mainRenderTarget;
            mainRenderTarget.unbindWrite();

            int[] inSize = {inTarget.width, inTarget.height};
            int[] outSize = {outTarget.width , outTarget.height};

            outTarget.bindWrite(true);
            customShader.setSampler("DiffuseSampler", inTarget.getColorTextureId());
            Matrix4f projMat = new Matrix4f().setOrtho(0.0F, (float) outSize[0], 0.0F, (float) outSize[1], 0.1F, 1000.0F);
            customShader.safeGetUniform("ProjMat").set(projMat);
            //customShader.safeGetUniform("ModelViewMat").set(new Matrix4f().translation(0.0F, 0.0F, -2000.0F));
            customShader.safeGetUniform("InSize").set((float) inSize[0], (float) inSize[1]);
            customShader.safeGetUniform("OutSize").set((float) outSize[0], (float) outSize[1]);
            customShader.safeGetUniform("BlurDir").set(1.0f,1.0f);
            customShader.safeGetUniform("Radius").set(1.0f);
            customShader.safeGetUniform("RadiusMultiplier").set(1.0f);
            customShader.apply();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );

            RenderSystem.depthFunc(519);

            BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferbuilder.vertex(0.0, 0.0, 0.0).endVertex();
            bufferbuilder.vertex((double)outSize[0], 0.0, 0.0).endVertex();
            bufferbuilder.vertex((double)outSize[0], (double)outSize[1], 0.0).endVertex();
            bufferbuilder.vertex(0.0, (double)outSize[1], 0.0).endVertex();
            BufferUploader.draw(bufferbuilder.end());

            customShader.clear();
            RenderSystem.depthFunc(515);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            outTarget.unbindWrite();

            mainRenderTarget.bindWrite(true);

        }
    }

    @Override
    public void render(EntityTHObjectContainer entity, float rotationX, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay){
        //super.render(entity, rotationX, partialTicks, poseStack, bufferSource, combinedOverlay);
        if (this.entityRenderDispatcher.shouldRenderHitBoxes()) {
            renderContainerBound(entity, poseStack, bufferSource.getBuffer(RenderType.lines()));
        }

        poseStack.popPose();
        poseStack.pushPose();


        Vec3 cameraPosition = this.entityRenderDispatcher.camera.getPosition();
        double camX = cameraPosition.x;
        double camY = cameraPosition.y;
        double camZ = cameraPosition.z;

        //ProfilerFiller profiler = InactiveProfiler.INSTANCE;
        //final List<THObject> objectList = layerObjects(entity.getObjectManager().getTHObjectsForRender(),camX,camY,camZ);
        final List<THObject> objectList = entity.getObjectManager().getTHObjectsForRender();

        Predicate<THObject> predicate = (object) -> (
                object != null && (object instanceof THCurvedLaser || this.shouldRenderTHObject(object, this.frustum, camX, camY, camZ))
        );

        RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        //ShaderInstance shader = ShaderLoader.getShader(new ResourceLocation(THDanmakuCraftCore.MODID,"depth_outline"));
        ShaderInstance shader = ShaderLoader.DANMAKU_DEPTH_OUTLINE_SHADER;
        if(shader != null) {
            //DEPTH_BUFFER.copyDepthFrom(mainRenderTarget);
            shader.setSampler("DepthBuffer", mainRenderTarget.getDepthTextureId());
            mainRenderTarget.bindWrite(true);
        }

        boolean flag = true;
        if (flag) {
            mainRenderTarget.unbindWrite();
            testRenderTarget.copyDepthFrom(mainRenderTarget);
            testRenderTarget.bindWrite(true);
        }
        //profiler.push("danmaku");

        RenderSystem.enableBlend();
        if (!objectList.isEmpty()) {
            //layerObjects(objectList,camX,camY,camZ);
            if (this.entityRenderDispatcher.shouldRenderHitBoxes()) {
                for (THObject object:objectList) {
                    if (predicate.test(object)) {
                        poseStack.pushPose();
                        Vec3 objectPos = object.getOffsetPosition(partialTicks);
                        poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
                        if (object.colli) {
                            if (object instanceof THCurvedLaser laser) {
                                renderTHCurvedLaserHitBoxes(laser, objectPos, poseStack, bufferSource.getBuffer(RenderType.lines()), partialTicks, this.frustum, cameraPosition);
                            } else {
                                renderTHObjectsHitBox(object, poseStack, bufferSource.getBuffer(RenderType.lines()));
                            }
                        }
                        poseStack.popPose();
                    }
                }
            }

            for (THObject object:objectList) {
                if (predicate.test(object)) {
                    poseStack.pushPose();
                    Vec3 objectPos = object.getOffsetPosition(partialTicks);
                    poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
                    object.onRender(this, objectPos, partialTicks, poseStack, bufferSource, combinedOverlay);
                    poseStack.popPose();
                }
            }

            bufferSource.getBuffer(RenderType.lines());
        }
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        //profiler.pop();

        if(flag) {
            testRenderTarget.unbindWrite();
            mainRenderTarget.copyDepthFrom(testRenderTarget);
            mainRenderTarget.bindWrite(true);
            //testRenderTarget.blitToScreen(mainRenderTarget.width,mainRenderTarget.height,true);
        }

        poseStack.popPose();
        poseStack.pushPose();
        Vec3 entityPos = entity.getPosition(partialTicks);
        poseStack.translate(entityPos.x-camX, entityPos.y-camY, entityPos.z-camZ);
    }

    /*
    public void renderObject(THObject object,float partialTicks,PoseStack poseStack,MultiBufferSource bufferSource,double camX,double camY,double camZ,int combinedOverlay){
        poseStack.pushPose();
        Vec3 objectPos = object.getOffsetPosition(partialTicks);
        poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
        object.onRender(this, objectPos, partialTicks, poseStack, bufferSource, combinedOverlay);
        poseStack.popPose();
    }*/


    private static void renderTHObjectsHitBox(THObject object, PoseStack poseStack, VertexConsumer vertexConsumer) {
        AABB aabb = object.getBoundingBox().move(-object.getX(), -object.getY(), -object.getZ());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, 0.0F, 1.0F, 1.0F, 1.0F);

        Vec3 vec31 = object.getMotionDirection();
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F).color(0, 0, 255, 255).normal(poseStack.last(), (float)vec31.x, (float)vec31.y, (float)vec31.z).endVertex();
        vertexConsumer.vertex(matrix4f, (float)(vec31.x * 2.0D), (float)((vec31.y * 2.0D)), (float)(vec31.z * 2.0D)).color(0, 0, 255, 255).normal(poseStack.last(), (float)vec31.x, (float)vec31.y, (float)vec31.z).endVertex();
    }

    private static void renderTHCurvedLaserHitBoxes(THCurvedLaser laser, Vec3 laserPos,PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks, Frustum frustum, Vec3 cameraPosition){
        List<THCurvedLaser.LaserNode> nodes = laser.nodeManager.getNodes();
        for(THCurvedLaser.LaserNode node: nodes){
            Vec3 pos = node.getPosition();
            AABB aabb = node.getBoundingBoxForCulling().inflate(0.5D);
            if (aabb.hasNaN() || aabb.getSize() == 0.0D) {
                aabb = new AABB(pos.x() - 2.0D, pos.y() - 2.0D, pos.z() - 2.0D, pos.x() + 2.0D, pos.y() + 2.0D, pos.z() + 2.0D);
            }

            if (frustum.isVisible(aabb)) {
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
        try {
            list.sort((o1, o2) -> {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                Vec3 pos1 = o1.getPosition();
                Vec3 pos2 = o2.getPosition();
                double d1x = pos1.x - camX;
                double d1y = pos1.y - camY;
                double d1z = pos1.z - camZ;
                double dist1Square = (d1x * d1x + d1y * d1y + d1z * d1z);
                double d2x = pos2.x - camX;
                double d2y = pos2.y - camY;
                double d2z = pos2.z - camZ;
                double dist2Square = (d2x * d2x + d2y * d2y + d2z * d2z);
                if (dist1Square < dist2Square) {
                    return 1;
                } else {
                    return -1;
                }
            });
        }catch (Exception e){

        }
        return list;
    }


    private static void renderContainerBound(EntityTHObjectContainer entity, PoseStack poseStack, VertexConsumer vertexConsumer) {
        AABB aabb = entity.getAabb().move(-entity.getX(), -entity.getY(), -entity.getZ());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean shouldRender(EntityTHObjectContainer entity, Frustum frustum, double camX, double camY, double camZ) {
        this.frustum = frustum;
        return true;
    }

    public boolean shouldRenderTHObject(THObject object, Frustum frustum, double camX, double camY, double camZ) {
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

    @Override
    public ResourceLocation getTextureLocation(EntityTHObjectContainer container) {
        return null;
    }
}
