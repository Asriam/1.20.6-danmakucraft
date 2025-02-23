package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject;

import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.THBlendMode;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.ConstantUtil;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.CollisionType;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(value = Dist.CLIENT)
public abstract class AbstractTHObjectRenderer<T extends THObject>{

    private final EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

    public AbstractTHObjectRenderer(THObjectRendererProvider.Context context) {
    }

    public abstract void render(T object, Vec3 objectPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer);

    public RenderType getRenderType(T object){
        return THRenderType.RENDER_TYPE_THOBJECT.apply(
                new THRenderType.RENDER_TYPE_2D_DANMAKU_CONTEXT(
                object.getImage().getTextureLocation(),
                THBlendMode.getBlendMode(object.getBlend()))
        );
    }

    public boolean shouldRender(T object, Frustum frustum, double camX, double camY, double camZ) {
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

    public void renderHitBox(T object, Vec3 objectPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer){
        renderTHObjectsHitBox(object, poseStack, vertexConsumer);
    }

    static void renderTHObjectsHitBox(@NotNull THObject object, PoseStack poseStack, VertexConsumer vertexConsumer) {
        AABB aabb = object.getBoundingBox().move(-object.getX(), -object.getY(), -object.getZ());
        if(object.getCollisionType() == CollisionType.AABB) {
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, 0.0F, 1.0F, 1.0F, 1.0F);
        }else if(object.getCollisionType() == CollisionType.SPHERE){
            Color color = THObject.Color(0,255,255,255);
            float size = (float) object.getSize().x;
            RenderUtil.renderSphere(vertexConsumer,poseStack.last(),1,
                    ConstantUtil.VECTOR3F_ZERO,
                    new Vector3f(size,size,size),
                    6,6,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color,color,color);
        }else if(object.getCollisionType() == CollisionType.ELLIPSOID){
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


    public EntityRenderDispatcher getRenderDispatcher(){
        return renderDispatcher;
    }

    public Frustum getFrustum(){
        return Minecraft.getInstance().levelRenderer.getFrustum();
    }
}
