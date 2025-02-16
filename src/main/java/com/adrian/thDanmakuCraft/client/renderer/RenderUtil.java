package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.utils.Lists;
import org.joml.*;

import java.lang.Math;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {
    public static List<Quad> quadList = Lists.newArrayList();

    public record Quad(Vertex vertex1,
                       Vertex vertex2,
                       Vertex vertex3,
                       Vertex vertex4) {

        public record Vertex(Matrix4f pose, Vector3f pos){

        }
    }

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

    public static void renderTexture(VertexConsumer consumer, PoseStack.Pose pose, int combinedOverlay,
                                     Vector3f vertex1, Vector2f uv1, Color color,
                                     Vector3f vertex2, Vector2f uv2, Color color2,
                                     Vector3f vertex3, Vector2f uv3, Color color3,
                                     Vector3f vertex4, Vector2f uv4, Color color4){
        Vector3f finalNormal = calculateNormal(vertex1,vertex2,vertex3,vertex4);
        vertex(consumer, pose, combinedOverlay, vertex1, finalNormal, uv1, color);
        vertex(consumer, pose, combinedOverlay, vertex2, finalNormal, uv2, color2);
        vertex(consumer, pose, combinedOverlay, vertex3, finalNormal, uv3, color3);
        vertex(consumer, pose, combinedOverlay, vertex4, finalNormal, uv4, color4);
    }

    public static void renderTexture(VertexConsumer consumer, PoseStack.Pose pose, int combinedOverlay,
                                     Vector3f vertex1, Vector2f uv1, Color color, Color coreColor,
                                     Vector3f vertex2, Vector2f uv2, Color color2, Color coreColor2,
                                     Vector3f vertex3, Vector2f uv3, Color color3, Color coreColor3,
                                     Vector3f vertex4, Vector2f uv4, Color color4, Color coreColor4){
        vertex(consumer, pose, combinedOverlay, vertex1, vertex1, uv1, color, coreColor);
        vertex(consumer, pose, combinedOverlay, vertex2, vertex2, uv2, color2,coreColor2);
        vertex(consumer, pose, combinedOverlay, vertex3, vertex3, uv3, color3,coreColor3);
        vertex(consumer, pose, combinedOverlay, vertex4, vertex4, uv4, color4,coreColor4);
    }

    public static void renderTexture(VertexConsumer consumer, PoseStack.Pose pose, int combinedOverlay, Vector3f vertex1, Vector2f uv1, Vector3f vertex2, Vector2f uv2, Vector3f vertex3, Vector2f uv3, Vector3f vertex4, Vector2f uv4, Color color) {
        renderTexture(consumer,pose,combinedOverlay, vertex1,uv1,color, vertex2,uv2,color, vertex3,uv3,color, vertex4,uv4,color);
    }

    public static void renderTexture(VertexConsumer consumer, PoseStack.Pose pose, int combinedOverlay,
                                     Vector3f pos, Vec2 scale, Vec2 uvStart, Vec2 uvEnd, Color color){
        renderTexture(consumer, pose, combinedOverlay,
                new Vector3f(-0.5f*scale.x+pos.x, -0.5f*scale.y+pos.y, pos.z),   new Vector2f(uvStart.x, uvStart.y),color,
                new Vector3f(0.5f*scale.x+pos.x, -0.5f*scale.y+pos.y, pos.z),    new Vector2f(uvEnd.x, uvStart.y),  color,
                new Vector3f(0.5f*scale.x+pos.x, 0.5f*scale.y+pos.y, pos.z),     new Vector2f(uvEnd.x, uvEnd.y),    color,
                new Vector3f(-0.5f*scale.x+pos.x, 0.5f*scale.y+pos.y, pos.z),    new Vector2f(uvStart.x, uvEnd.y),  color);
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int uv2, Vector3f vertex, Vector2f uv, Color color) {
        vertex(consumer,pose,uv2,vertex.x, vertex.y, vertex.z,uv.x, uv.y,color);
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int uv2,
                                  float x, float y, float z,
                                  float u, float v, Color color) {
        consumer.vertex(pose.pose(), x, y, z)
                .color(color.r, color.g, color.b, color.a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uv2)
                .normal(pose, x, y, z)
                .endVertex();
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int uv2, Vector3f vertex, Vector3f normal, Vector2f uv, Color color) {
        consumer.vertex(pose.pose(), vertex.x, vertex.y, vertex.z)
                .color(color.r, color.g, color.b, color.a)
                .color(0.5f,0.5f,0.5f,0.5f)
                .uv(uv.x, uv.y)
                //.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uv2)
                //.normal(pose, normal.x, normal.y, normal.z)
                .endVertex();
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int uv2, Vector3f vertex, Vector3f normal, Vector2f uv, Color color, Color coreColor) {
        consumer.vertex(pose.pose(), vertex.x, vertex.y, vertex.z)
                .color(color.r, color.g, color.b, color.a)
                .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                .uv(uv.x, 2.0f)
                .uv(uv.y, 2.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uv2)
                .normal(pose, normal.x, normal.y, normal.z)
                .endVertex();
    }

    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, float pow, Vector3f offsetPosition, Vector3f scale, final int edgeA, final int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, Color color, Color endColor, Color coreColor, boolean inverse) {
        Color startColor = color;
        //Color deColor = Color.VOID();
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

        Color deColor = Color.of(
                (startColor.r - endColor.r)/ edge3,
                (startColor.g - endColor.g)/ edge3,
                (startColor.b - endColor.b)/ edge3,
                (startColor.a - endColor.a)/ edge3);

        /*
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
        }*/

        for (int j = 0; j < edgeB; j++) {
            float x1 = Mth.cos((angle2 * j));
            float z1 = Mth.sin((angle2 * j));

            float x2 = Mth.cos((angle2 * (j + 1)));
            float z2 = Mth.sin((angle2 * (j + 1)));

            startColor = color;
            for (int i = 0; i < edgeADiv2; i++) {
                float sin01 = Mth.sin(i*angle1);
                float cos1 = Mth.cos(i*angle1);
                float sin02 = Mth.sin((i+1)*angle1);
                float cos2 = Mth.cos((i+1)*angle1);

                float sin1,sin2;
                if (pow == 1.0f){
                    sin1 = sin01;
                    sin2 = sin02;
                }else {
                    sin1 = (float) Math.pow(sin01,pow);
                    sin2 = (float) Math.pow(sin02,pow);
                }

                Color finalColor = color.subtract(deColor.multiply(i));

                Vector3f scaleF          = scale;
                Vector3f offsetPositionF = offsetPosition;

                Vector3f[] vertex = new Vector3f[] {
                        new Vector3f(x1*sin1,cos1,z1*sin1).mul(scaleF).add(offsetPositionF),
                        new Vector3f(x2*sin1,cos1,z2*sin1).mul(scaleF).add(offsetPositionF),
                        new Vector3f(x2*sin2,cos2,z2*sin2).mul(scaleF).add(offsetPositionF),
                        new Vector3f(x1*sin2,cos2,z1*sin2).mul(scaleF).add(offsetPositionF),
                };
                Vector3f[] normal = vertex;
                Matrix4f pose$ = pose.pose();

                boolean flag = true;

                if (flag) {
                    quadList.add(new Quad(
                            new Quad.Vertex(pose$, vertex[0]),
                            new Quad.Vertex(pose$, vertex[1]),
                            new Quad.Vertex(pose$, vertex[2]),
                            new Quad.Vertex(pose$, vertex[3])
                    ));
                    if (!inverse) {
                        consumer.vertex(pose$, vertex[0].x, vertex[0].y, vertex[0].z)
                                .color(startColor.r, startColor.g, startColor.b, startColor.a)
                                .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                                //.uv(0.0f, 0.0f)
                                .uv(uvStart.x, uvStart.y)
                                .uv(uvEnd.x, uvEnd.y)
                                .normal(pose, normal[0].x, normal[0].y, normal[0].z)
                                .endVertex();

                        consumer.vertex(pose$, vertex[1].x, vertex[1].y, vertex[1].z)
                                .color(startColor.r, startColor.g, startColor.b, startColor.a)
                                .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                                //.uv(0.0f, 0.0f)
                                .uv(uvStart.x, uvStart.y)
                                .uv(uvEnd.x, uvEnd.y)
                                .normal(pose, normal[1].x, normal[1].y, normal[1].z)
                                .endVertex();

                        consumer.vertex(pose$, vertex[2].x, vertex[2].y, vertex[2].z)
                                .color(finalColor.r, finalColor.g, finalColor.b, finalColor.a)
                                .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                                //.uv(0.0f, 0.0f)
                                .uv(uvStart.x, uvStart.y)
                                .uv(uvEnd.x, uvEnd.y)
                                .normal(pose, normal[2].x, normal[2].y, normal[2].z)
                                .endVertex();

                        consumer.vertex(pose$, vertex[3].x, vertex[3].y, vertex[3].z)
                                .color(finalColor.r, finalColor.g, finalColor.b, finalColor.a)
                                .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                                //.uv(0.0f, 0.0f)
                                .uv(uvStart.x, uvStart.y)
                                .uv(uvEnd.x, uvEnd.y)
                                .normal(pose, normal[3].x, normal[3].y, normal[3].z)
                                .endVertex();
                    }else {
                        //inverted vertex
                        consumer.vertex(pose$, vertex[3].x, vertex[3].y, vertex[3].z)
                                .color(finalColor.r, finalColor.g, finalColor.b, finalColor.a)
                                .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                                //.uv(0.0f, 0.0f)
                                .uv(uvStart.x, uvStart.y)
                                .uv(uvEnd.x, uvEnd.y)
                                .normal(pose, -normal[3].x, -normal[3].y, -normal[3].z)
                                .endVertex();

                        consumer.vertex(pose$, vertex[2].x, vertex[2].y, vertex[2].z)
                                .color(finalColor.r, finalColor.g, finalColor.b, finalColor.a)
                                .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                                //.uv(0.0f, 0.0f)
                                .uv(uvStart.x, uvStart.y)
                                .uv(uvEnd.x, uvEnd.y)
                                .normal(pose, -normal[2].x, -normal[2].y, -normal[2].z)
                                .endVertex();

                        consumer.vertex(pose$, vertex[1].x, vertex[1].y, vertex[1].z)
                                .color(startColor.r, startColor.g, startColor.b, startColor.a)
                                .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                                //.uv(0.0f, 0.0f)
                                .uv(uvStart.x, uvStart.y)
                                .uv(uvEnd.x, uvEnd.y)
                                .normal(pose, -normal[1].x, -normal[1].y, -normal[1].z)
                                .endVertex();

                        consumer.vertex(pose$, vertex[0].x, vertex[0].y, vertex[0].z)
                                .color(startColor.r, startColor.g, startColor.b, startColor.a)
                                .color(coreColor.r, coreColor.g, coreColor.b, coreColor.a)
                                //.uv(0.0f, 0.0f)
                                .uv(uvStart.x, uvStart.y)
                                .uv(uvEnd.x, uvEnd.y)
                                .normal(pose, -normal[0].x, -normal[0].y, -normal[0].z)
                                .endVertex();
                    }
                }
                startColor = finalColor;
            }
        }
    }


    public static Vector3f multiply(Matrix3f mat3, Vector3f vec3) {
        float x = vec3.x * mat3.m00 + vec3.y * mat3.m01 + vec3.z * mat3.m02;
        float y = vec3.x * mat3.m10 + vec3.y * mat3.m11 + vec3.z * mat3.m12;
        float z = vec3.x * mat3.m20 + vec3.y * mat3.m21 + vec3.z * mat3.m22;
        return new Vector3f(x, y, z);
    }

    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, float pow, Vector3f offsetPosition, Vector3f scale, final int edgeA, final int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, Color color, Color endColor, Color coreColor) {
        renderSphere(consumer, pose, pow, offsetPosition, scale, edgeA, edgeB, isHalf, uvStart, uvEnd, color, endColor, coreColor,false);
    }
    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, float pow, Vector3f offsetPosition, Vector3f scale, int edgeA, int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, Color color, int alpha, Color coreColor) {
        renderSphere(consumer,pose,pow,offsetPosition,scale,edgeA,edgeB,isHalf,uvStart,uvEnd,color,THObject.Color(color.r,color.g,color.b,alpha),coreColor);
    }

    public static void renderCuboid(VertexConsumer consumer, PoseStack.Pose pose, Vec3 posOffset, Vec3 scale){

    }
}
