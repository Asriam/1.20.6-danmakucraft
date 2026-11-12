package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject;

import com.adrian.thDanmakuCraft.client.renderer.type.RenderTypes;
import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.type.THBlendMode;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(value = Dist.CLIENT)
public abstract class AbstractTHObjectRenderer<T extends THObject>{

    public static final EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

    public AbstractTHObjectRenderer(THObjectRendererProvider.Context context) {
    }

    public abstract void render(T object, Vec3 objectPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer);

    public RenderType getRenderType(T object){
        return RenderTypes.RENDER_TYPE_THOBJECT.apply(
                new RenderTypes.RENDER_TYPE_2D_DANMAKU_CONTEXT(
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
            AABB aabb = object.getBoundingBoxForCulling();
            if (aabb.hasNaN() || aabb.getSize() == 0.0D) {
                aabb = new AABB(object.getX() - 2.0D, object.getY() - 2.0D, object.getZ() - 2.0D, object.getX() + 2.0D, object.getY() + 2.0D, object.getZ() + 2.0D);
            }

            return frustum.isVisible(aabb);
        }
    }

    public void renderHitBox(T object, Vec3 objectPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer){
        renderTHObjectsHitBox(object, poseStack, vertexConsumer);
    }

    /**
     * 依碰撞型別畫出真正的碰撞範圍：
     * AABB / CUBOID 青色線框、SPHERE 綠色三軸圓、ELLIPSOID 黃色旋轉線框。
     */
    static void renderTHObjectsHitBox(@NotNull THObject object, PoseStack poseStack, VertexConsumer vertexConsumer) {
        // poseStack 已經平移到物件位置，這裡把世界座標的 bb 轉回物件本地座標
        AABB local = object.getBoundingBox().move(-object.getX(), -object.getY(), -object.getZ());
        switch (object.getCollisionType()) {
            case AABB, CUBOID ->
                    LevelRenderer.renderLineBox(poseStack, vertexConsumer, local, 0.0F, 1.0F, 1.0F, 1.0F);
            case SPHERE -> {
                // 球的碰撞半徑用的是 size.x（見 CollisionType.SPHERE）
                float radius = (float) object.getSize().x;
                renderWireSphere(poseStack, vertexConsumer, radius, 0.2F, 1.0F, 0.35F, 1.0F);
            }
            case ELLIPSOID -> {
                // 椭球的半軸就是 size，畫成跟著物件旋轉的線框
                poseStack.pushPose();
                Vector3f rotation = object.getRotation();
                poseStack.mulPose(new Quaternionf().rotationYXZ(rotation.y, -rotation.x, rotation.z));
                Vec3 size = object.getSize();
                AABB box = new AABB(-size.x, -size.y, -size.z, size.x, size.y, size.z);
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, box, 1.0F, 0.85F, 0.2F, 1.0F);
                poseStack.popPose();
            }
        }
    }

    /** 三個正交大圓組成的線框球（用 LINES 模式，每段 2 個頂點）。 */
    private static void renderWireSphere(PoseStack poseStack, VertexConsumer vertexConsumer, float radius,
                                         float red, float green, float blue, float alpha) {
        final int segments = 24;
        for (int axis = 0; axis < 3; axis++) {
            for (int i = 0; i < segments; i++) {
                double a0 = Mth.TWO_PI * i / segments;
                double a1 = Mth.TWO_PI * (i + 1) / segments;
                addCircleVertex(poseStack, vertexConsumer, axis, a0, radius, red, green, blue, alpha);
                addCircleVertex(poseStack, vertexConsumer, axis, a1, radius, red, green, blue, alpha);
            }
        }
    }

    private static void addCircleVertex(PoseStack poseStack, VertexConsumer vertexConsumer, int axis, double angle,
                                        float radius, float red, float green, float blue, float alpha) {
        float c = (float) Math.cos(angle) * radius;
        float s = (float) Math.sin(angle) * radius;
        float x, y, z;
        switch (axis) {
            case 0 -> { x = 0.0F; y = c; z = s; }
            case 1 -> { x = c; y = 0.0F; z = s; }
            default -> { x = c; y = s; z = 0.0F; }
        }
        vertexConsumer.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(red, green, blue, alpha)
                .setNormal(0.0F, 1.0F, 0.0F);
    }


    public EntityRenderDispatcher getRenderDispatcher(){
        return renderDispatcher;
    }

    public Frustum getFrustum(){
        return Minecraft.getInstance().levelRenderer.getFrustum();
    }
}
