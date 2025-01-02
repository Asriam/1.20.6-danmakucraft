package com.adrian.thDanmakuCraft.client.renderer.danmaku;

import com.adrian.thDanmakuCraft.client.renderer.THObjectRenderHelper;
import com.adrian.thDanmakuCraft.world.danmaku.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.utils.Lists;
import org.joml.Vector2f;

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
        THObject.Color indexColor = laser.laserColor.getColor();
        THObject.Color laserColor = THObject.Color(
                laser.color.r * indexColor.r / 255,
                laser.color.g * indexColor.g / 255,
                laser.color.b * indexColor.b / 255,
                (int) (laser.color.a * 0.7f)
        );
        THObject.Color coreColor = laser.color;

        //VertexConsumer vertexConsumer = bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE);

        var nodes0 = laser.nodeManager.getAllNodes();
        float width2 = laser.width * 3.0f * (float) Math.pow((double) (nodes0.size() - laser.getTimer()) / nodes0.size(),0.4f);
        if (width2 > 0) {
            poseStack.pushPose();
            Vec3 prePos = laserPos.vectorTo(nodes0.getLast().getOffsetPosition(partialTicks));
            poseStack.translate(prePos.x, prePos.y, prePos.z);
            THObjectRenderHelper.renderSphere(vertexConsumer, poseStack.last(), combinedOverlay, 1,
                    Vec3.ZERO,
                    new Vec3(width2, width2, width2),
                    10, 10, false,
                    new Vec2(0.5f, -0.3f),
                    Vec2.ONE,
                    laserColor, laserColor, coreColor);
            poseStack.popPose();
        }

        List<List<THCurvedLaser.LaserNode>> piecewisedNodeList = Lists.newArrayList();
        List<THCurvedLaser.LaserNode> nodeList = Lists.newArrayList();
        for (THCurvedLaser.LaserNode node : nodes0) {
            if (node.isValid()) {
                nodeList.add(node);
            } else {
                List<THCurvedLaser.LaserNode> nodes = Lists.newArrayList();
                nodes.addAll(nodeList);
                piecewisedNodeList.add(nodes);
                nodeList = Lists.newArrayList();
            }
        }
        piecewisedNodeList.add(nodeList);
        for (List<THCurvedLaser.LaserNode> NodeList : piecewisedNodeList) {
            renderCurvedLaser(this, laserPos, vertexConsumer, poseStack, NodeList, laser.width, laser.width * 0.5f, edge, 2, laserColor, coreColor, partialTicks, combinedOverlay, 1.0f, 0.95f);
        }
        //this.renderCurvedLaser(renderer,laserPos,bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE),poseStack,this.nodeManager.getNodes(),this.width,this.width*0.5f,edge, 1, color, coreColor,partialTicks,combinedOverlay,1.0f,0.95f);
        poseStack.popPose();
    }

    //曲線聚光渲染的一坨屎山
    @OnlyIn(value = Dist.CLIENT)
    public void renderCurvedLaser(AbstractTHObjectRenderer renderer, Vec3 laserPos, VertexConsumer vertexConsumer, PoseStack poseStack, List<THCurvedLaser.LaserNode> nodeList, float width, float coreWidth, int edge, int cull, THObject.Color laserColor, THObject.Color coreColor, float partialTicks, int combinedOverlay, float laserLength, float coreLength) {
        if (nodeList.isEmpty() || nodeList.size() < 3) {
            return;
        }
        //THDanmakuCraftCore.LOGGER.info("sadasxxxxxxxxxxx");

        PoseStack.Pose pose = poseStack.last();

        final float perAngle = Mth.DEG_TO_RAD * 360.0f / edge;

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

        THObject.Color coreColor2 = coreColor.multiply(0.5f);

        Vec3 _pos1;
        Vec3 _pos2;

        int index = 0;
        THObject.Color GRAY = THObject.Color.GRAY();
        for (THCurvedLaser.LaserNode node : nodeList) {
            if (index + 1 >= nodeList.size()) {
                break;
            }

            if (node.isValid() && (cull == 1 || (index + cull) % cull == 0)) {
                THCurvedLaser.LaserNode node2 = nodeList.get(index + 1);
                Vec3 pos1 = node.getOffsetPosition(partialTicks);
                Vec3 pos2 = node2.getOffsetPosition(partialTicks);
                THCurvedLaser.LaserNode node3 = !(index >= nodeList.size() - 2) ? nodeList.get(index + 2) : null;
                Vec2 node1Angle = THObject.VectorAngleToRadAngle(pos1.vectorTo(pos2));
                Vec2 node2Angle = node3 != null ? THObject.VectorAngleToRadAngle(pos2.vectorTo(node3.getOffsetPosition(partialTicks))) : node1Angle;

                float right_angle = Mth.DEG_TO_RAD * 90.0f;
                Vec2 angle1 = new Vec2(node1Angle.x, node1Angle.y);
                Vec2 angle2 = new Vec2(node2Angle.x, node2Angle.y);

                _pos1 = pos1.subtract(laserPos);
                _pos2 = pos2.subtract(laserPos);

                if (shouldRenderNode(node, node2, this.getFrustum())) {
                    float nodeWidth1 = circle((float) (index) / (nodeList.size() - 1), laserLength);
                    float nodeWidth2 = circle((float) (index + 1) / (nodeList.size() - 1), laserLength);

                    float nodeWidth3 = circle((float) (index) / (nodeList.size() - 1), coreLength);
                    float nodeWidth4 = circle((float) (index + 1) / (nodeList.size() - 1), coreLength);

                    for (int i = 0; i < edge; i++) {
                        //external render
                        Vec3 posA = new Vec3(nodeWidth1 * width, 0.0d, 0.0d);
                        Vec3 posB = new Vec3(!(index >= nodeList.size() - 2) ? nodeWidth2 * width : 0.0f, 0.0d, 0.0d);

                        Vec3 calculatedPos1_1 = lastNodePos_1[i] == null ? posA.yRot(i * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y).add(_pos1) : lastNodePos_1[i];
                        Vec3 calculatedPos1_2 = lastNodePos_2[i] == null ? posA.yRot((i + 1) * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y).add(_pos1) : lastNodePos_2[i];

                        Vec3 normal1_1 = lastNormal_1[i] == null ? calculatedPos1_1.subtract(_pos1) : lastNormal_1[i];
                        Vec3 normal1_2 = lastNormal_2[i] == null ? calculatedPos1_2.subtract(_pos1) : lastNormal_2[i];

                        Vec3 calculatedPos2_1 = posB.yRot(i * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y).add(_pos2);
                        Vec3 calculatedPos2_2 = posB.yRot((i + 1) * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y).add(_pos2);

                        Vec3 normal2_1 = calculatedPos2_1.subtract(_pos2);
                        Vec3 normal2_2 = calculatedPos2_2.subtract(_pos2);

                        lastNodePos_1[i] = calculatedPos2_1;
                        lastNodePos_2[i] = calculatedPos2_2;

                        lastNormal_1[i] = normal2_1;
                        lastNormal_2[i] = normal2_2;

                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos1_1.toVector3f(), normal1_1.toVector3f(), new Vector2f(0.5f, 0.0f), laserColor, coreColor2);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos2_1.toVector3f(), normal2_1.toVector3f(), new Vector2f(0.5f, 0.0f), laserColor, coreColor2);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos2_2.toVector3f(), normal2_2.toVector3f(), new Vector2f(0.5f, 0.0f), laserColor, coreColor2);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos1_2.toVector3f(), normal1_2.toVector3f(), new Vector2f(0.5f, 0.0f), laserColor, coreColor2);

                        //core render
                        Vec3 posA1 = new Vec3(nodeWidth3 * coreWidth, 0.0d, 0.0d);
                        Vec3 posB2 = new Vec3(!(index >= nodeList.size() - 2) ? nodeWidth4 * coreWidth : 0.0f, 0.0d, 0.0d);

                        Vec3 calculatedPos3_1 = lastNodePos_3[i] == null ? posA1.yRot(i * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y).add(_pos1) : lastNodePos_3[i];
                        Vec3 calculatedPos3_2 = lastNodePos_4[i] == null ? posA1.yRot((i + 1) * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y).add(_pos1) : lastNodePos_4[i];

                        Vec3 normal3_1 = lastNormal_3[i] == null ? calculatedPos3_1.subtract(_pos1) : lastNormal_3[i];
                        Vec3 normal3_2 = lastNormal_4[i] == null ? calculatedPos3_2.subtract(_pos1) : lastNormal_4[i];

                        Vec3 calculatedPos4_1 = posB2.yRot(i * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y).add(_pos2);
                        Vec3 calculatedPos4_2 = posB2.yRot((i + 1) * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y).add(_pos2);

                        Vec3 normal4_1 = calculatedPos4_1.subtract(_pos2);
                        Vec3 normal4_2 = calculatedPos4_2.subtract(_pos2);

                        lastNodePos_3[i] = calculatedPos4_1;
                        lastNodePos_4[i] = calculatedPos4_2;

                        lastNormal_3[i] = normal4_1;
                        lastNormal_4[i] = normal4_2;

                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos3_1.toVector3f(), normal3_1.toVector3f(), new Vector2f(0.5f, 0.0f), coreColor, GRAY);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos4_1.toVector3f(), normal4_1.toVector3f(), new Vector2f(0.5f, 0.0f), coreColor, GRAY);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos4_2.toVector3f(), normal4_2.toVector3f(), new Vector2f(0.5f, 0.0f), coreColor, GRAY);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos3_2.toVector3f(), normal3_2.toVector3f(), new Vector2f(0.5f, 0.0f), coreColor, GRAY);
                    }
                }
            }
            index += 1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean shouldRenderNode(THCurvedLaser.LaserNode node1, THCurvedLaser.LaserNode node2, Frustum frustum) {
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
}
