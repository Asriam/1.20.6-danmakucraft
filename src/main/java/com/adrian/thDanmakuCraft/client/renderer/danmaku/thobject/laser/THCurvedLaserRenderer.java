package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.laser;

import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.THBlendMode;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.AbstractTHObjectRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.THObjectRendererProvider;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.ConstantUtil;
import com.adrian.thDanmakuCraft.util.VertexBuilder;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THLaser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.utils.Lists;
import org.joml.Vector3f;

import java.util.List;

@OnlyIn(value = Dist.CLIENT)
public class THCurvedLaserRenderer extends AbstractTHObjectRenderer<THCurvedLaser> {

    public THCurvedLaserRenderer(THObjectRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(THCurvedLaser laser, Vec3 laserPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer, int combinedOverlay) {
        if (laser.color.a <= 0) {
            return;
        }

        poseStack.pushPose();
        int edge = 4;
        Color indexColor = laser.laserColor;
        Color laserColor = THObject.Color(
                laser.color.r * indexColor.r / 255,
                laser.color.g * indexColor.g / 255,
                laser.color.b * indexColor.b / 255,
                (int) (laser.color.a * 0.7f)
        );
        Color coreColor = laser.color;

        //VertexConsumer vertexConsumer = bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE);

        var nodes0 = laser.nodeManager.getAllNodes();
        float width = laser.getWidthForRender(partialTicks);
        if(laser.spawnAnimation) {
            float width2 = width * 3.0f * (float) Math.pow((double) (nodes0.size() - (laser.getTimer() + partialTicks)) / nodes0.size(), 0.4f);
            if (width2 > 0) {
                poseStack.pushPose();
                Vec3 prePos = laserPos.vectorTo(nodes0.getLast().getOffsetPosition(partialTicks));
                poseStack.translate(prePos.x, prePos.y, prePos.z);
                //Vec2 faceCamRotation = THObject.VectorAngleToRadAngle(this.getRenderDispatcher().camera.getPosition().vectorTo(nodes0.getLast().getOffsetPosition(partialTicks)));
                //poseStack.mulPose(new Quaternionf().rotateY(faceCamRotation.y).rotateX(-faceCamRotation.x));
                poseStack.mulPose(this.getRenderDispatcher().cameraOrientation());
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                RenderUtil.renderSphere(vertexConsumer, poseStack.last(), 1,
                        ConstantUtil.VECTOR3F_ZERO,
                        new Vector3f(width2, width2, width2),
                        4, 12, true,
                        new Vec2(0.4f, 2.0f),
                        new Vec2(0.0f, 2.0f),
                        laserColor, laserColor, coreColor);
                poseStack.popPose();
            }
        }

        List<List<THCurvedLaser.NodeManager.LaserNode>> piecewisedNodeList = Lists.newArrayList();
        List<THCurvedLaser.NodeManager.LaserNode> nodeList = Lists.newArrayList();
        for (THCurvedLaser.NodeManager.LaserNode node : nodes0) {
            if (node.isValid()) {
                nodeList.add(node);
            } else {
                List<THCurvedLaser.NodeManager.LaserNode> nodes = Lists.newArrayList();
                nodes.addAll(nodeList);
                piecewisedNodeList.add(nodes);
                nodeList = Lists.newArrayList();
            }
        }
        piecewisedNodeList.add(nodeList);
        for (List<THCurvedLaser.NodeManager.LaserNode> NodeList : piecewisedNodeList) {
            renderCurvedLaser(laserPos, vertexConsumer, poseStack, NodeList, width, width * 0.5f, edge, laser.getRenderCull(), laserColor, coreColor, partialTicks, combinedOverlay, 1.0f, 0.95f);
        }
        //this.renderCurvedLaser(renderer,laserPos,bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE),poseStack,this.nodeManager.getNodes(),this.width,this.width*0.5f,edge, 1, color, coreColor,partialTicks,combinedOverlay,1.0f,0.95f);
        poseStack.popPose();
    }

    //曲線聚光渲染的一坨屎山
    @OnlyIn(value = Dist.CLIENT)
    public void renderCurvedLaser(Vec3 laserPos, VertexConsumer vertexConsumer, PoseStack poseStack, List<THCurvedLaser.NodeManager.LaserNode> nodeList, float width, float coreWidth, int edge, int cull, Color laserColor, Color coreColor, float partialTicks, int combinedOverlay, float laserLength, float coreLength) {
        if (nodeList.isEmpty() || nodeList.size() < 3) {
            return;
        }

        PoseStack.Pose pose = poseStack.last();

        final float perAngle = Mth.DEG_TO_RAD * 360.0f / edge;

        boolean renderCore = true;
        boolean renderExternal = false;

        for(int g = 0; g < 2; g++) {

            //external
            Vec3[] lastNodePos_1 = new Vec3[edge];
            Vec3[] lastNodePos_2 = new Vec3[edge];
            //core
            Vec3[] lastNodePos_3 = new Vec3[edge];
            Vec3[] lastNodePos_4 = new Vec3[edge];
            //normal
            Vec3[] lastNormal_1 = new Vec3[edge];
            Vec3[] lastNormal_2 = new Vec3[edge];
            Vec3[] lastNormal_3 = new Vec3[edge];
            Vec3[] lastNormal_4 = new Vec3[edge];

            Color coreColor2 = coreColor.multiply(0.5f);

            Vec3 _pos1;
            Vec3 _pos2;

            int index = 0;
            Color GRAY = Color.GRAY();
            Vec3 pos0 = new Vec3(1.0f, 0.0d, 0.0d);
            for (THCurvedLaser.NodeManager.LaserNode node : nodeList) {
                if (index + 1 >= nodeList.size()) {
                    break;
                }

                if (node.isValid() && (cull >= 1 && (cull == 1 || (index + cull) % cull == 0)) || index == nodeList.size() - 2) {
                    THCurvedLaser.NodeManager.LaserNode node2 = nodeList.get(index + 1);
                    if (shouldRenderNode(node, node2, this.getFrustum())) {
                        Vec3 pos1 = node.getOffsetPosition(partialTicks);
                        Vec3 pos2 = node2.getOffsetPosition(partialTicks);
                        THCurvedLaser.NodeManager.LaserNode node3 = !(index >= nodeList.size() - 2) ? nodeList.get(index + 2) : null;
                        Vec2 node1Angle = THObject.VectorAngleToRadAngle(pos1.vectorTo(pos2));
                        Vec2 node2Angle = node3 != null ? THObject.VectorAngleToRadAngle(pos2.vectorTo(node3.getOffsetPosition(partialTicks))) : node1Angle;

                        float right_angle = Mth.DEG_TO_RAD * 90.0f;
                        Vec2 angle1 = new Vec2(node1Angle.x, node1Angle.y);
                        Vec2 angle2 = new Vec2(node2Angle.x, node2Angle.y);

                        _pos1 = pos1.subtract(laserPos);
                        _pos2 = pos2.subtract(laserPos);

                        float nodeWidth1 = circle((float) (index) / (nodeList.size()), laserLength);
                        float nodeWidth2 = circle((float) (index + 1) / (nodeList.size()), laserLength);
                        float posA = nodeWidth1 * width;
                        float posB = !(index >= nodeList.size() - 2) ? nodeWidth2 * width : 0.0f;

                        float nodeWidth3 = circle((float) (index) / (nodeList.size()), coreLength);
                        float nodeWidth4 = circle((float) (index + 1) / (nodeList.size()), coreLength);
                        float posA1 = nodeWidth3 * coreWidth;
                        float posB2 = !(index >= nodeList.size() - 2) ? nodeWidth4 * coreWidth : 0.0f;

                        for (int i = 0; i < edge; i++) {
                            if (renderCore) {
                                //core render
                                Vec3 calculatedPos3_1 = lastNodePos_3[i] == null ? pos0.yRot(i * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y).scale(posA1).add(_pos1) : lastNodePos_3[i];
                                Vec3 calculatedPos3_2 = lastNodePos_4[i] == null ? pos0.yRot((i + 1) * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y).scale(posA1).add(_pos1) : lastNodePos_4[i];

                                Vec3 normal3_1 = lastNormal_3[i] == null ? pos0.yRot(i * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y) : lastNormal_3[i];
                                Vec3 normal3_2 = lastNormal_4[i] == null ? pos0.yRot((i + 1) * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y) : lastNormal_4[i];

                                Vec3 calculatedPos4_1 = pos0.yRot(i * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y).scale(posB2).add(_pos2);
                                Vec3 calculatedPos4_2 = pos0.yRot((i + 1) * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y).scale(posB2).add(_pos2);

                                Vec3 normal4_1 = pos0.yRot(i * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y);
                                Vec3 normal4_2 = pos0.yRot((i + 1) * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y);

                                lastNodePos_3[i] = calculatedPos4_1;
                                lastNodePos_4[i] = calculatedPos4_2;

                                lastNormal_3[i] = normal4_1;
                                lastNormal_4[i] = normal4_2;

                                VertexBuilder builder = new VertexBuilder(vertexConsumer);
                                builder.positionColorColorUvUvNormal(pose.pose(),calculatedPos3_1.toVector3f(), coreColor,GRAY, 0.5f,2.0f, 0.0f,2.0f, pose.normal(),normal3_1.toVector3f());
                                builder.positionColorColorUvUvNormal(pose.pose(),calculatedPos4_1.toVector3f(), coreColor,GRAY, 0.5f,2.0f, 0.0f,2.0f, pose.normal(),normal4_1.toVector3f());
                                builder.positionColorColorUvUvNormal(pose.pose(),calculatedPos4_2.toVector3f(), coreColor,GRAY, 0.5f,2.0f, 0.0f,2.0f, pose.normal(),normal4_2.toVector3f());
                                builder.positionColorColorUvUvNormal(pose.pose(),calculatedPos3_2.toVector3f(), coreColor,GRAY, 0.5f,2.0f, 0.0f,2.0f, pose.normal(),normal3_2.toVector3f());

                            }

                            if (renderExternal) {
                                //external render
                                Vec3 calculatedPos1_1 = lastNodePos_1[i] == null ? pos0.yRot(i * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y).scale(posA).add(_pos1) : lastNodePos_1[i];
                                Vec3 calculatedPos1_2 = lastNodePos_2[i] == null ? pos0.yRot((i + 1) * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y).scale(posA).add(_pos1) : lastNodePos_2[i];

                                Vec3 normal1_1 = lastNormal_1[i] == null ? pos0.yRot(i * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y) : lastNormal_1[i];
                                Vec3 normal1_2 = lastNormal_2[i] == null ? pos0.yRot((i + 1) * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y) : lastNormal_2[i];

                                Vec3 calculatedPos2_1 = pos0.yRot(i * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y).scale(posB).add(_pos2);
                                Vec3 calculatedPos2_2 = pos0.yRot((i + 1) * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y).scale(posB).add(_pos2);

                                Vec3 normal2_1 = pos0.yRot(i * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y);
                                Vec3 normal2_2 = pos0.yRot((i + 1) * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y);

                                lastNodePos_1[i] = calculatedPos2_1;
                                lastNodePos_2[i] = calculatedPos2_2;

                                lastNormal_1[i] = normal2_1;
                                lastNormal_2[i] = normal2_2;

                                VertexBuilder builder = new VertexBuilder(vertexConsumer);
                                builder.positionColorColorUvUvNormal(pose.pose(),calculatedPos1_1.toVector3f(), laserColor,coreColor2, 0.4f,2.0f, -0.1f,2.0f, pose.normal(),normal1_1.toVector3f());
                                builder.positionColorColorUvUvNormal(pose.pose(),calculatedPos2_1.toVector3f(), laserColor,coreColor2, 0.4f,2.0f, -0.1f,2.0f, pose.normal(),normal2_1.toVector3f());
                                builder.positionColorColorUvUvNormal(pose.pose(),calculatedPos2_2.toVector3f(), laserColor,coreColor2, 0.4f,2.0f, -0.1f,2.0f, pose.normal(),normal2_2.toVector3f());
                                builder.positionColorColorUvUvNormal(pose.pose(),calculatedPos1_2.toVector3f(), laserColor,coreColor2, 0.4f,2.0f, -0.1f,2.0f, pose.normal(),normal1_2.toVector3f());

                            }
                        }
                    }
                }
                index += 1;
            }
            renderCore = false;
            renderExternal = true;
        }
    }

    public static boolean shouldRenderNode(THCurvedLaser.NodeManager.LaserNode node1, THCurvedLaser.NodeManager.LaserNode node2, Frustum frustum) {
        AABB aabb1 = node1.getBoundingBoxForCulling().inflate(0.5D);
        AABB aabb2 = node2.getBoundingBoxForCulling().inflate(0.5D);
        if (aabb1.hasNaN() || aabb1.getSize() == 0.0D) {
            Vec3 position = node1.getPosition();
            aabb1 = new AABB(position.x - 2.0D, position.y - 2.0D, position.z - 2.0D, position.x + 2.0D, position.y + 2.0D, position.z + 2.0D);
        }
        if (aabb2.hasNaN() || aabb2.getSize() == 0.0D) {
            Vec3 position = node2.getPosition();
            aabb2 = new AABB(position.x - 2.0D, position.y - 2.0D, position.z - 2.0D, position.x + 2.0D, position.y + 2.0D, position.z + 2.0D);
        }

        return frustum.isVisible(aabb1) && frustum.isVisible(aabb2);
    }

    static float circle(float x, float width) {
        float num = (x / width * 2 - 1 / width);
        return Mth.sqrt(1 - num * num);
    }

    @Override
    public RenderType getRenderType(THCurvedLaser laser){
        return THRenderType.TEST_RENDER_TYPE_FUNCTION.apply(new THRenderType.TEST_RENDER_TYPE_FUNCTION_CONTEXT(THBlendMode.getBlendMode(laser.getBlend()), true));
    }

    @Override
    public boolean shouldRender(THCurvedLaser object, Frustum frustum, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void renderHitBox(THCurvedLaser laser, Vec3 objectPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer){
        renderTHCurvedLaserHitBoxes(laser, objectPos, poseStack, vertexConsumer, partialTicks, this.getFrustum());
    }

    static void renderTHCurvedLaserHitBoxes(THCurvedLaser laser, Vec3 laserPos, PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks, Frustum frustum){
        List<THCurvedLaser.NodeManager.LaserNode> nodes = laser.nodeManager.getAllNodes();
        for(THCurvedLaser.NodeManager.LaserNode node: nodes){
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
}
