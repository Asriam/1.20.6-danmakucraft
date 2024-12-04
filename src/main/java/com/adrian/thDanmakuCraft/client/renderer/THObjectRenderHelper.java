package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class THObjectRenderHelper {
    public static Vector3f calculateNormal(Vector3f vertex1, Vector3f vertex2, Vector3f vertex3, Vector3f vertex4){
        Vector3f edge1_1 = new Vector3f(vertex2).sub(vertex1);
        Vector3f edge2_1 = new Vector3f(vertex3).sub(vertex1);
        Vector3f normal1 = edge1_1.cross(edge2_1);

        // Triangle 2: vertices v0, v2, v3
        Vector3f edge1_2 = new Vector3f(vertex3).sub(vertex1);
        Vector3f edge2_2 = new Vector3f(vertex4).sub(vertex1);
        Vector3f normal2 = edge1_2.cross(edge2_2);

        return new Vector3f(
                (normal1.x + normal2.x) / 2,
                (normal1.y + normal2.y) / 2,
                (normal1.z + normal2.z) / 2
        ).normalize();
    }

    public static void renderTexture(VertexConsumer consumer, PoseStack.Pose pose, int p_254296_,
                                     Vector3f vertex1, Vector2f uv1, THObject.Color color,
                                     Vector3f vertex2, Vector2f uv2, THObject.Color color2,
                                     Vector3f vertex3, Vector2f uv3, THObject.Color color3,
                                     Vector3f vertex4, Vector2f uv4, THObject.Color color4){
        Vector3f finalNormal = calculateNormal(vertex1,vertex2,vertex3,vertex4);
        vertex(consumer, pose, p_254296_, vertex1, finalNormal, uv1, color);
        vertex(consumer, pose, p_254296_, vertex2, finalNormal, uv2, color2);
        vertex(consumer, pose, p_254296_, vertex3, finalNormal, uv3, color3);
        vertex(consumer, pose, p_254296_, vertex4, finalNormal, uv4, color4);
    }

    public static void renderTexture(VertexConsumer consumer, PoseStack.Pose pose, int p_254296_,
                                     Vector3f vertex1, Vector2f uv1, THObject.Color color, THObject.Color coreColor,
                                     Vector3f vertex2, Vector2f uv2, THObject.Color color2,THObject.Color coreColor2,
                                     Vector3f vertex3, Vector2f uv3, THObject.Color color3,THObject.Color coreColor3,
                                     Vector3f vertex4, Vector2f uv4, THObject.Color color4,THObject.Color coreColor4){
        vertex(consumer, pose, p_254296_, vertex1, vertex1, uv1, color, coreColor);
        vertex(consumer, pose, p_254296_, vertex2, vertex2, uv2, color2,coreColor2);
        vertex(consumer, pose, p_254296_, vertex3, vertex3, uv3, color3,coreColor3);
        vertex(consumer, pose, p_254296_, vertex4, vertex4, uv4, color4,coreColor4);
    }

    public static void renderTexture(VertexConsumer consumer, PoseStack.Pose pose, int p_254296_, Vector3f vertex1, Vector2f uv1, Vector3f vertex2, Vector2f uv2, Vector3f vertex3, Vector2f uv3, Vector3f vertex4, Vector2f uv4, THObject.Color color) {
        renderTexture(consumer,pose,p_254296_, vertex1,uv1,color, vertex2,uv2,color, vertex3,uv3,color, vertex4,uv4,color);
    }

    public static void renderTexture(VertexConsumer consumer, PoseStack.Pose pose, int p_254296_,
                                     Vec3 offSetPos, Vec2 scale, Vec2 uvStart, Vec2 uvEnd, THObject.Color color){
        Vector3f pos = offSetPos.toVector3f();
        renderTexture(consumer, pose, p_254296_,
                new Vector3f(-0.5f*scale.x+pos.x, -0.5f*scale.y+pos.y, pos.z),   new Vector2f(uvStart.x, uvStart.y),color,
                new Vector3f(0.5f*scale.x+pos.x, -0.5f*scale.y+pos.y, pos.z),    new Vector2f(uvEnd.x, uvStart.y),  color,
                new Vector3f(0.5f*scale.x+pos.x, 0.5f*scale.y+pos.y, pos.z),     new Vector2f(uvEnd.x, uvEnd.y),    color,
                new Vector3f(-0.5f*scale.x+pos.x, 0.5f*scale.y+pos.y, pos.z),    new Vector2f(uvStart.x, uvEnd.y),  color);
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int uv2, Vector3f vertex, Vector2f uv, THObject.Color color) {
        vertex(consumer,pose,uv2,vertex.x, vertex.y, vertex.z,uv.x, uv.y,color);
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int uv2,
                                  float x, float y, float z,
                                  float u, float v, THObject.Color color) {
        consumer.vertex(pose.pose(), x, y, z)
                .color(color.r, color.g, color.b, color.a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uv2)
                .normal(pose, x, y, z)
                .endVertex();
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int uv2, Vector3f vertex, Vector3f normal, Vector2f uv, THObject.Color color) {
        consumer.vertex(pose.pose(), vertex.x, vertex.y, vertex.z)
                .color(color.r, color.g, color.b, color.a)
                .color(0.5f,0.5f,0.5f,0.5f)
                .uv(uv.x, uv.y)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uv2)
                .normal(pose, normal.x, normal.y, normal.z)
                .endVertex();
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int uv2, Vector3f vertex, Vector3f normal, Vector2f uv, THObject.Color color, THObject.Color coreColor) {
        consumer.vertex(pose.pose(), vertex.x, vertex.y, vertex.z)
                .color(color.r, color.g, color.b, color.a)
                .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                .uv(uv.x, uv.y)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uv2)
                .normal(pose, normal.x, normal.y, normal.z)
                .endVertex();
    }

    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, int overlay, float pow, Vec3 offsetPosition, Vec3 scale, final int edgeA, final int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, THObject.Color color, THObject.Color endColor, THObject.Color coreColor) {
        //THDanmakuCraftCore.LOGGER.info(THObjectRenderHelper.class.getName());

        THObject.Color startColor = color;
        THObject.Color deColor = THObject.Color.VOID();
        int edgeADiv2 = Mth.floor(edgeA / 2.0f);
        float angle1;
        float angle2;

        if (isHalf){
            angle1 = Mth.DEG_TO_RAD * (90.0f/edgeADiv2);
        }else {
            angle1 = Mth.DEG_TO_RAD * (180.0f/edgeADiv2);
        }

        angle2 = Mth.DEG_TO_RAD * (360.0f/edgeB);
        int edge3 = edgeADiv2-1;
        if (startColor.r != endColor.r){
            deColor.r = (startColor.r - endColor.r)/ edge3;
        }
        if (startColor.g != endColor.g){
            deColor.g = (startColor.g - endColor.g)/ edge3;
        }
        if (startColor.b != endColor.b){
            deColor.b = (startColor.b - endColor.b)/ edge3;
        }
        if (startColor.a != endColor.a){
            deColor.a = (startColor.a - endColor.a)/ edge3;
        }

        float currentX1 = -100.0f;
        float currentZ1 = -100.0f;
        for (int j = 0; j < edgeB; j++) {
            if (currentX1 <= -100.0f) {
                currentX1 = Mth.cos((angle2 * j));
                currentZ1 = Mth.sin((angle2 * j));
            }
            /*
            float x1 = Mth.cos((angle2 * j));
            float z1 = Mth.sin((angle2 * j));
             */
            float x1 = currentX1;
            float z1 = currentZ1;

            float x2 = Mth.cos((angle2 * (j + 1)));
            float z2 = Mth.sin((angle2 * (j + 1)));

            currentX1 = x2;
            currentZ1 = z2;

            startColor = color;
            float currentSin01 = -100.0f;
            float currentCos1  = -100.0f;
            for (int i = 0; i < edgeADiv2; i++) {
                if (currentSin01 <= -100.0f) {
                    currentSin01 = Mth.sin(i*angle1);
                    currentCos1  = Mth.cos(i*angle1);
                }

                //float sin01 = Mth.sin(i*angle1);
                //float cos1 = Mth.cos(i*angle1);
                float sin01 = currentSin01;
                float cos1 = currentCos1;
                float sin02 = Mth.sin((i+1)*angle1);
                float cos2 = Mth.cos((i+1)*angle1);

                currentSin01 = sin02;
                currentCos1  = cos2;

                float sin1,sin2;
                if (pow == 1.0){
                    sin1 = sin01;
                    sin2 = sin02;
                }else {
                    sin1 = (float) Math.pow(sin01,pow);
                    sin2 = (float) Math.pow(sin02,pow);
                }

                THObject.Color finalColor = color.subtract(deColor.multiply(i));

                Vector3f scaleF          = scale.toVector3f();
                Vector3f offsetPositionF = offsetPosition.toVector3f();

                /*
                Vector3f vertex = new Vector3f(x1*sin1,cos1,z1*sin1).mul(scaleF).add(offsetPositionF);
                consumer.vertex(pose.pose(),vertex.x,vertex.y,vertex.z)
                        .color(startColor.r, startColor.g, startColor.b, startColor.a)
                        .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                        .uv(0.0f, 0.0f)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(overlay)
                        .normal(pose, vertex.x, vertex.y, vertex.z)
                        .endVertex();

                vertex = new Vector3f(x2*sin1,cos1,z2*sin1).mul(scaleF).add(offsetPositionF);
                consumer.vertex(pose.pose(),vertex.x,vertex.y,vertex.z)
                        .color(startColor.r, startColor.g, startColor.b, startColor.a)
                        .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                        .uv(0.0f, 0.0f)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(overlay)
                        .normal(pose, vertex.x, vertex.y, vertex.z)
                        .endVertex();

                vertex = new Vector3f(x2*sin2,cos2,z2*sin2).mul(scaleF).add(offsetPositionF);
                consumer.vertex(pose.pose(),vertex.x,vertex.y,vertex.z)
                        .color(finalColor.r, finalColor.g, finalColor.b, finalColor.a)
                        .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                        .uv(0.0f, 0.0f)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(overlay)
                        .normal(pose, vertex.x, vertex.y, vertex.z)
                        .endVertex();

                vertex = new Vector3f(x1*sin2,cos2,z1*sin2).mul(scaleF).add(offsetPositionF);
                consumer.vertex(pose.pose(),vertex.x,vertex.y,vertex.z)
                        .color(finalColor.r, finalColor.g, finalColor.b, finalColor.a)
                        .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                        .uv(0.0f, 0.0f)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(overlay)
                        .normal(pose, vertex.x, vertex.y, vertex.z)
                        .endVertex();

                /*
                renderTexture(consumer, pose, p_254296_,
                        new Vector3f(x1*sin1,cos1,z1*sin1).mul(scaleF).add(offsetPositionF),
                        new Vector2f(uvStart.x, uvStart.y),startColor,coreColor,

                        new Vector3f(x2*sin1,cos1,z2*sin1).mul(scaleF).add(offsetPositionF),
                        new Vector2f(uvEnd.x,   uvStart.y),startColor,coreColor,

                        new Vector3f(x2*sin2,cos2,z2*sin2).mul(scaleF).add(offsetPositionF),
                        new Vector2f(uvEnd.x,   uvEnd.y),  finalColor,coreColor,

                        new Vector3f(x1*sin2,cos2,z1*sin2).mul(scaleF).add(offsetPositionF),
                        new Vector2f(uvStart.x, uvEnd.y),  finalColor,coreColor
                );*/



                /*
                renderTexture(consumer, pose, p_254296_,
                        new Vector3f((float) (x1*sin1*scale.x+offsetPosition.x),
                                     (float) (cos1*scale.y+offsetPosition.y),
                                     (float) (z1*sin1*scale.z+offsetPosition.z)),
                        new Vector2f(uvStart.x, uvStart.y),startColor,

                        new Vector3f((float) (x2*sin1*scale.x+offsetPosition.x),
                                     (float) (cos1*scale.y+offsetPosition.y),
                                     (float) (z2*sin1*scale.z+offsetPosition.z)),
                        new Vector2f(uvEnd.x,   uvStart.y),startColor,

                        new Vector3f((float) (x2*sin2*scale.x+offsetPosition.x),
                                     (float) (cos2*scale.y+offsetPosition.y),
                                     (float) (z2*sin2*scale.z+offsetPosition.z)),
                        new Vector2f(uvEnd.x,   uvEnd.y),  color1,

                        new Vector3f((float) (x1*sin2*scale.x+offsetPosition.x),
                                     (float) (cos2*scale.y+offsetPosition.y),
                                     (float) (z1*sin2*scale.z+offsetPosition.z)),
                        new Vector2f(uvStart.x, uvEnd.y),  color1
                );*/

                startColor = finalColor;
            }
        }
    }

    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, int p_254296_, float pow, Vec3 offsetPosition, Vec3 scale, int edgeA, int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, THObject.Color color, int alpha, THObject.Color coreColor) {
        renderSphere(consumer,pose,p_254296_,pow,offsetPosition,scale,edgeA,edgeB,isHalf,uvStart,uvEnd,color,THObject.Color(color.r,color.g,color.b,alpha),coreColor);
    }
}
