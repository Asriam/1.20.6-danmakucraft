package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.VertexBuilder;
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

    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, float pow, Vector3f offsetPosition, Vector3f scale, final int edgeA, final int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, Color color, Color endColor, Color coreColor, boolean inverse, boolean isStraight) {
        Color startColor = color;
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


                Vector3f[] pos = new Vector3f[] {
                        new Vector3f(x1*sin1,cos1,z1*sin1),
                        new Vector3f(x2*sin1,cos1,z2*sin1),
                        new Vector3f(x2*sin2,cos2,z2*sin2),
                        new Vector3f(x1*sin2,cos2,z1*sin2),
                };
                Vector3f[] vertex = new Vector3f[] {
                        pos[0].mul(scale,new Vector3f()).add(offsetPosition),
                        pos[1].mul(scale,new Vector3f()).add(offsetPosition),
                        pos[2].mul(scale,new Vector3f()).add(offsetPosition),
                        pos[3].mul(scale,new Vector3f()).add(offsetPosition),
                };
                Vector3f[] normal;
                if (isStraight){
                    normal = new Vector3f[] {
                            new Vector3f(x1,0,z1),
                            new Vector3f(x2,0,z2),
                            new Vector3f(x2,0,z2),
                            new Vector3f(x1,0,z1),
                    };
                }else {
                    normal = pos;
                }
                Matrix4f pose$ = pose.pose();
                if (!inverse) {
                    VertexBuilder builder = new VertexBuilder(consumer);
                    builder.positionColorColorUvUvNormal(
                            pose$, vertex[0],
                            startColor,coreColor,
                            uvStart.x, uvStart.y,
                            uvEnd.x, uvEnd.y,
                            pose.normal(), normal[0]);
                    builder.positionColorColorUvUvNormal(
                            pose$, vertex[1],
                            startColor,coreColor,
                            uvStart.x, uvStart.y,
                            uvEnd.x, uvEnd.y,
                            pose.normal(), normal[1]);
                    builder.positionColorColorUvUvNormal(
                            pose$, vertex[2],
                            finalColor,coreColor,
                            uvStart.x, uvStart.y,
                            uvEnd.x, uvEnd.y,
                            pose.normal(), normal[2]);
                    builder.positionColorColorUvUvNormal(
                            pose$, vertex[3],
                            finalColor,coreColor,
                            uvStart.x, uvStart.y,
                            uvEnd.x, uvEnd.y,
                            pose.normal(), normal[3]);
                }else {
                    VertexBuilder builder = new VertexBuilder(consumer);
                    builder.positionColorColorUvUvNormal(
                            pose$, vertex[3],
                            finalColor,coreColor,
                            uvStart.x, uvStart.y,
                            uvEnd.x, uvEnd.y,
                            pose.normal(), normal[3]);
                    builder.positionColorColorUvUvNormal(
                            pose$, vertex[2],
                            finalColor,coreColor,
                            uvStart.x, uvStart.y,
                            uvEnd.x, uvEnd.y,
                            pose.normal(), normal[2]);
                    builder.positionColorColorUvUvNormal(
                            pose$, vertex[1],
                            startColor,coreColor,
                            uvStart.x, uvStart.y,
                            uvEnd.x, uvEnd.y,
                            pose.normal(), normal[1]);
                    builder.positionColorColorUvUvNormal(
                            pose$, vertex[0],
                            startColor,coreColor,
                            uvStart.x, uvStart.y,
                            uvEnd.x, uvEnd.y,
                            pose.normal(), normal[0]);
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
        renderSphere(consumer, pose, pow, offsetPosition, scale, edgeA, edgeB, isHalf, uvStart, uvEnd, color, endColor, coreColor,false,false);
    }
    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, float pow, Vector3f offsetPosition, Vector3f scale, int edgeA, int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, Color color, int alpha, Color coreColor) {
        renderSphere(consumer,pose,pow,offsetPosition,scale,edgeA,edgeB,isHalf,uvStart,uvEnd,color,THObject.Color(color.r,color.g,color.b,alpha),coreColor);
    }

    public static void renderCuboid(VertexConsumer consumer, PoseStack.Pose pose, Vec3 posOffset, Vec3 scale){

    }
}
