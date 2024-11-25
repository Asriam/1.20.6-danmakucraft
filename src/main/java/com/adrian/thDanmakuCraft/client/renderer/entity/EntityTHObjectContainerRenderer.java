package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.adrian.thDanmakuCraft.world.entity.danmaku.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EntityTHObjectContainerRenderer extends EntityRenderer<EntityTHObjectContainer> {
    public final EntityRenderDispatcher dispatcher;
    public Frustum frustum;

    public EntityTHObjectContainerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.dispatcher = super.entityRenderDispatcher;
    }

    @Override
    public void render(EntityTHObjectContainer entity, float rotationX, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay){
        //super.render(entity, rotationX, partialTicks, poseStack, bufferSource, combinedOverlay);
        renderContainerBound(entity, poseStack, bufferSource.getBuffer(RenderType.lines()));

        poseStack.popPose();
        poseStack.pushPose();

        Vec3 cameraPosition = this.dispatcher.camera.getPosition();
        double camX = cameraPosition.x;
        double camY = cameraPosition.y;
        double camZ = cameraPosition.z;

        entity.layerObjects(camX,camY,camZ);

        for(THObject object:entity.getObjectManager().getTHObjects()){
            if (object != null && (object instanceof THCurvedLaser || this.shouldRenderTHObject(object, this.frustum, camX, camY, camZ))) {
                poseStack.pushPose();
                Vec3 offsetPos = object.getOffsetPosition(partialTicks);
                poseStack.translate(offsetPos.x()-camX, offsetPos.y()-camY, offsetPos.z()-camZ);
                object.onRender(this, offsetPos, partialTicks, poseStack, bufferSource, combinedOverlay);
                if (this.dispatcher.shouldRenderHitBoxes() && object.colli) {
                    if(object instanceof THCurvedLaser laser){
                        renderTHCurvedLaserHitBoxes(laser, offsetPos, poseStack, bufferSource.getBuffer(RenderType.lines()),partialTicks,this.frustum,cameraPosition);
                    }else {
                        renderTHObjectsHitBox(object, poseStack, bufferSource.getBuffer(RenderType.lines()));
                    }
                }
                poseStack.popPose();
            }
        }
        poseStack.popPose();
        poseStack.pushPose();
        Vec3 entityPos = entity.getPosition(partialTicks);
        poseStack.translate(entityPos.x-camX, entityPos.y-camY, entityPos.z-camZ);
    }

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
