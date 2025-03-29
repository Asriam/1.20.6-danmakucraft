package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.laser;

import com.adrian.thDanmakuCraft.client.renderer.MyRenderTypes;
import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.THBlendMode;
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
import net.minecraft.world.phys.AABB;
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
    public void render(THLaser laser, Vec3 laserPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer) {
        renderLaser(laser, laserPos, partialTicks, poseStack, vertexConsumer);
    }

    public static void renderLaser(THLaser laser, Vec3 laserPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer){
        poseStack.pushPose();
        Vector3f rotation = laser.getOffsetRotation(partialTicks);
        Vec3 pos = laser.getOffsetLaserCenter(partialTicks);
        poseStack.translate(pos.x,pos.y,pos.z);
        poseStack.mulPose(new Quaternionf().rotationYXZ(-rotation.y, rotation.x + Mth.PI/2, rotation.z));
        float laserWidth = laser.getOffsetWidth(partialTicks)/2;
        float laserLength = laser.getOffsetLength(partialTicks)/2;
        Vector3f scale = new Vector3f(laserWidth,laserLength,laserWidth);
        Color laserCoreColor = laser.getColor();
        Color laserColor = laser.getLaserColor().multiply(laserCoreColor);
        int edgeA = 12;
        int edgeB = 8;
        RenderUtil.renderSphere(vertexConsumer, poseStack.last(), 2,
                ConstantUtil.VECTOR3F_ZERO,
                scale,
                edgeA, edgeB, false,
                new Vec2(0.5f, 3.0f),
                new Vec2(0.5f, 1.0f),
                laserColor.multiply(0.8f),
                laserColor.multiply(0.1f),
                laserCoreColor,false,false);
        /*RenderUtil.renderSphere(vertexConsumer, poseStack.last(), 2,
                ConstantUtil.VECTOR3F_ZERO,
                scale.mul(0.6f,0.8f,0.6f,new Vector3f()),
                edgeA, edgeB, false,
                new Vec2(0.0f, 1.0f),
                new Vec2(0.3f, 1.0f),
                laserCoreColor.multiply(0.8f),
                laserCoreColor.multiply(0.5f),
                laserCoreColor);
        RenderUtil.renderSphere(vertexConsumer, poseStack.last(), 2,
                ConstantUtil.VECTOR3F_ZERO,
                scale,
                edgeA, edgeB, false,
                new Vec2(0.0f, 1.0f),
                new Vec2(0.3f, 1.0f),
                laserColor.multiply(0.6f),
                laserColor.multiply(0.3f),
                laserCoreColor,false,true);*/
        poseStack.popPose();
    }

    @Override
    public RenderType getRenderType(THLaser laser){
        return MyRenderTypes.TEST_RENDER_TYPE_FUNCTION.apply(new MyRenderTypes.TEST_RENDER_TYPE_FUNCTION_CONTEXT(THBlendMode.getBlendMode(laser.getBlend()), true));
    }

    @Override
    public boolean shouldRender(THLaser laser, Frustum frustum, double camX, double camY, double camZ) {
        Vec3 closetPoint = laser.getClosetPoint(new Vec3(camX,camY,camZ));
        Vec3 startPos = laser.getPosition();
        Vec3 endPos = laser.getEndPos();
        float width = laser.getWidth();
        return frustum.isVisible(new AABB(
                closetPoint.x-width,closetPoint.y-width,closetPoint.z-width,
                closetPoint.x+width,closetPoint.y+width,closetPoint.z+width
        )) || frustum.isVisible(new AABB(
                startPos.x-width,startPos.y-width,startPos.z-width,
                startPos.x+width,startPos.y+width,startPos.z+width
        )) || frustum.isVisible(new AABB(
                endPos.x-width,endPos.y-width,endPos.z-width,
                endPos.x+width,endPos.y+width,endPos.z+width
        ));
    }

    @Override
    public void renderHitBox(THLaser laser, Vec3 objectPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer){
        poseStack.pushPose();
        Vector3f rotation = laser.getOffsetRotation(partialTicks);
        Vec3 pos = laser.getOffsetLaserCenter(partialTicks);
        poseStack.translate(pos.x,pos.y,pos.z);
        poseStack.mulPose(new Quaternionf().rotationYXZ(-rotation.y,rotation.x,rotation.z));
        Color color = new  Color(0,255,255,255);
        float width = laser.getOffsetWidth(partialTicks)/2;
        float length = laser.getOffsetLength(partialTicks)/2;
        RenderUtil.renderSphere(vertexConsumer,poseStack.last(),1,
                ConstantUtil.VECTOR3F_ZERO,
                new Vector3f(width*0.6f,width*0.6f,length),
                6,8,false,
                Vec2.ZERO,
                Vec2.ONE,
                color,color,color);
        poseStack.popPose();
    }
}
