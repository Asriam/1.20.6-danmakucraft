package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.laser;

import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.THBlendMode;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.AbstractTHObjectRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.THObjectRendererProvider;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.ConstantUtil;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THLaser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(value = Dist.CLIENT)
public class THLaserRenderer extends AbstractTHObjectRenderer<THLaser> {

    public THLaserRenderer(THObjectRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(THLaser laser, Vec3 laserPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer, int combinedOverlay) {
        renderLaser(laser, laserPos, partialTicks, poseStack, vertexConsumer);
    }

    public static void renderLaser(THLaser laser, Vec3 laserPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer){
        poseStack.pushPose();
        Vector3f rotation = laser.getOffsetRotation(partialTicks);
        Vec3 pos = laser.getLaserCenterForRender(partialTicks);
        poseStack.translate(pos.x,pos.y,pos.z);
        poseStack.mulPose(new Quaternionf().rotationYXZ(-rotation.y, rotation.x - Mth.PI/2, rotation.z));
        float laserWidth = laser.getWidthForRender(partialTicks);
        float laserLength = laser.getLengthForRender(partialTicks);
        Vector3f scale = new Vector3f(laserWidth,laserLength,laserWidth);
        Color laserCoreColor = laser.getColor();
        Color laserColor = laser.getLaserColor().multiply(laserCoreColor);
        int edgeA = 16;
        int edgeB = 8;
        /*RenderUtil.renderSphere(vertexConsumer, poseStack.last(), 2,
                ConstantUtil.VECTOR3F_ZERO,
                scale.mul(0.7f,new Vector3f()),
                edgeA, edgeB, false,
                new Vec2(0.0f, 1.0f),
                new Vec2(1.0f, 1.0f),
                laserCoreColor,
                laserCoreColor.multiply(0.5f),
                laserCoreColor);*/
        RenderUtil.renderSphere(vertexConsumer, poseStack.last(), 2,
                ConstantUtil.VECTOR3F_ZERO,
                scale,
                edgeA, edgeB, false,
                new Vec2(0.0f, 1.0f),
                new Vec2(0.4f, 1.0f),
                laserColor,
                laserColor.multiply(0.1f),
                laserCoreColor);
        poseStack.popPose();
    }

    @Override
    public RenderType getRenderType(THLaser laser){
        return THRenderType.TEST_RENDER_TYPE_FUNCTION.apply(new THRenderType.TEST_RENDER_TYPE_FUNCTION_CONTEXT(THBlendMode.getBlendMode(laser.getBlend()), true));
    }

    @Override
    public boolean shouldRender(THLaser object, Frustum frustum, double camX, double camY, double camZ) {
        return true;
    }
}
