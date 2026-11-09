package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.laser;

import com.adrian.thDanmakuCraft.client.renderer.RayCastingHelper;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.THObjectContainerRenderer;
import com.adrian.thDanmakuCraft.client.renderer.type.RenderTypes;
import com.adrian.thDanmakuCraft.client.renderer.type.THBlendMode;
import com.adrian.thDanmakuCraft.client.renderer.buffer.VertexBuilder;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.AbstractTHObjectRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.THObjectRendererProvider;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THLaser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
        /*poseStack.pushPose();
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
        int style = 2;
        VertexBuilder builder = new VertexBuilder(vertexConsumer);
        switch (style) {
            case 1: {
                RenderUtil.renderSphere(vertexConsumer, poseStack.last(), 2,
                        ConstantUtil.VECTOR3F_ZERO,
                        scale,
                        edgeA, edgeB, false,
                        new Vec2(0.5f, 3.0f),
                        new Vec2(0.5f, 1.0f),
                        laserColor.multiply(0.8f),
                        laserColor.multiply(0.1f),
                        laserCoreColor, false, false);
                break;
            }
            case 2: {
                RenderUtil.renderSphere(vertexConsumer, poseStack.last(), 2,
                        ConstantUtil.VECTOR3F_ZERO,
                        scale.mul(0.6f, 0.8f, 0.6f, new Vector3f()),
                        edgeA, edgeB, false,
                        new Vec2(0.0f, 1.0f),
                        new Vec2(0.3f, 1.0f),
                        laserCoreColor.multiply(0.9f),
                        laserCoreColor.multiply(0.6f),
                        laserCoreColor);
                RenderUtil.renderSphere(vertexConsumer, poseStack.last(), 2,
                        ConstantUtil.VECTOR3F_ZERO,
                        scale,
                        edgeA, edgeB, false,
                        new Vec2(0.0f, 1.0f),
                        new Vec2(0.3f, 1.0f),
                        laserColor.multiply(0.9f),
                        laserColor.multiply(0.5f),
                        laserCoreColor, false, false);
                break;
            }
            case 3: {
                new CylinderVertexHelper(poseStack.last().pose(),  poseStack.last().normal(),
                        ConstantUtil.VECTOR3F_ZERO, scale.x*0.6f, scale.y , edgeA, true,
                        laserCoreColor.multiply(0.6f), laserCoreColor, false)
                        .vertex((position, normal, color)->{
                            builder.vertex(position.x, position.y, position.z)
                                    .color(color.multiply(0.9f))
                                    .color(color.multiply(0.8f))
                                    .uv(0.6f, 1.0f)
                                    .uv(0.6f,1.0f)
                                    .normal(normal)
                                    .endVertex();
                        });
                new CylinderVertexHelper(poseStack.last().pose(),  poseStack.last().normal(),
                        ConstantUtil.VECTOR3F_ZERO, scale.x, scale.y , edgeA, true,
                        laserColor.multiply(0.8f), laserColor, false)
                        .vertex((position, normal, color)->{
                            builder.vertex(position.x, position.y, position.z)
                                    .color(color.multiply(0.8f))
                                    .color(color.multiply(0.6f))
                                    .uv(0.0f, 1.0f)
                                    .uv(0.6f,1.0f)
                                    .normal(normal)
                                    .endVertex();
                });
                break;
            }
        }
        poseStack.popPose();*/
        Color laserCoreColor = laser.getColor();
        Color laserColor = laser.getLaserColor().multiply(laserCoreColor);
        float laserWidth = laser.getOffsetWidth(partialTicks)/2;
        float laserLength = Math.max(laser.getOffsetLength(partialTicks)/2,0.01f);
        Vector3f rotation = laser.getOffsetRotation(partialTicks);
        Vector3f offsetRotation = new Vector3f(rotation.x,-rotation.y,-rotation.z);
        Vector3f scale = new Vector3f(laserWidth,laserWidth,laserLength);
        VertexBuilder builder = new VertexBuilder(THObjectContainerRenderer.ELLIPSOID_TEST_BUFFER);
        Vec3 camPos = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        Vec3 pos1 = laserPos.add(laser.getOffsetLaserCenter(partialTicks));
        Vector3f pos = pos1.subtract(camPos).toVector3f();

        // 用 ellipsoid 的長軸（local Z）反推頭尾，交給最小包圍四邊形的版本
        Matrix3f rot = new Matrix3f().rotationZ(offsetRotation.z)
                .mul(new Matrix3f().rotationY(offsetRotation.y))
                .mul(new Matrix3f().rotationX(offsetRotation.x));
        Vector3f axis = rot.getColumn(2, new Vector3f());
        Vector3f head = new Vector3f(pos).sub(new Vector3f(axis).mul(laserLength));
        Vector3f tail = new Vector3f(pos).add(new Vector3f(axis).mul(laserLength));
        //Vector3f head = laser.getOffsetPosition(partialTicks).subtract(camPos).toVector3f();
        //Vector3f tail = laser.getOffsetEndPos(partialTicks).subtract(camPos).toVector3f();
        float boxWidth = laserWidth * 2.0f;

        RayCastingHelper.renderEllipsoidForLaser(
                builder,
                head,
                tail,
                boxWidth,
                offsetRotation,
                scale.mul(0.6f, 0.6f, 0.8f, new Vector3f()),
                laserCoreColor.multiply(0.6f),
                laserCoreColor,
                new Vector4f(0.0f, 1.0f,0.3f, 1.0f)
        );
        RayCastingHelper.renderEllipsoidForLaser(
                builder,
                head,
                tail,
                boxWidth,
                offsetRotation,
                scale,
                laserColor.multiply(0.6f),
                laserCoreColor,
                new Vector4f(0.0f, 1.0f,0.3f, 1.0f)
        );

        /*RayCastingHelper.renderEllipsoid(
                builder,
                pos,
                offsetRotation,
                scale.mul(0.6f, 0.6f, 0.8f, new Vector3f()),
                laserCoreColor.multiply(0.6f),
                laserCoreColor,
                new Vector4f(0.0f, 1.0f,0.3f, 1.0f)
        );
        RayCastingHelper.renderEllipsoid(
                builder,
                pos,
                offsetRotation,
                scale,
                laserColor.multiply(0.6f),
                laserCoreColor,
                new Vector4f(0.0f, 1.0f,0.3f, 1.0f)
        );*/
    }

    @Override
    public RenderType getRenderType(THLaser laser){
        return RenderTypes.TEST_RENDER_TYPE_FUNCTION.apply(new RenderTypes.TEST_RENDER_TYPE_FUNCTION_CONTEXT(THBlendMode.getBlendMode(laser.getBlend()), true));
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
        /*RenderUtil.renderSphere(vertexConsumer,poseStack.last(),1,
                ConstantUtil.VECTOR3F_ZERO,
                new Vector3f(width*0.6f,width*0.6f,length),
                6,8,false,
                Vec2.ZERO,
                Vec2.ONE,
                color,color,color);*/
        /*VertexBuilder builder = new VertexBuilder(vertexConsumer);
        new SphereVertexHelper(
                poseStack.last().pose(), poseStack.last().normal(),
                ConstantUtil.VECTOR3F_ZERO,new Vector3f(width*0.6f,width*0.6f,length),
                6,8,1,color,color
        ).vertexs((pos1,normal1,color1)->{
            builder.vertex(pos1).color(color1).normal(normal1);
        },(pos1,normal1,color1)->{
            builder.vertex(pos1).color(color1).normal(normal1);
        },(pos1,normal1,color1)->{
            //builder.vertex(pos1).color(color1).normal(normal1);
        },(pos1,normal1,color1)->{
            //builder.vertex(pos1).color(color1).normal(normal1);
        });*/
        poseStack.popPose();
    }
}
