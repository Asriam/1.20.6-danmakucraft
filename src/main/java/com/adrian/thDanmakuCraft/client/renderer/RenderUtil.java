package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.ConstantUtil;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
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

    private static boolean shouldCull = true;
    public static void enableCull(){
        RenderSystem.assertOnRenderThread();
        shouldCull = true;
    }

    public static void disableCull(){
        RenderSystem.assertOnRenderThread();
        shouldCull = false;
    }

    public static boolean shouldCull(){
        return shouldCull;
    }

    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, float pow, Vector3f offsetPosition, Vector3f radius, final int edgeA, final int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, Color color, Color endColor, Color coreColor, boolean inverse, boolean isStraight) {
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
                Matrix4f pose$ = pose.pose();

                Vector3f[] pos = new Vector3f[] {
                        new Vector3f(x1*sin1,cos1,z1*sin1),
                        new Vector3f(x2*sin1,cos1,z2*sin1),
                        new Vector3f(x2*sin2,cos2,z2*sin2),
                        new Vector3f(x1*sin2,cos2,z1*sin2),
                };
                Vector3f[] real_position = new Vector3f[]{
                        pose$.transformPosition(pos[0].mul(radius,new Vector3f()).add(offsetPosition),new Vector3f()),
                        pose$.transformPosition(pos[1].mul(radius,new Vector3f()).add(offsetPosition),new Vector3f()),
                        pose$.transformPosition(pos[2].mul(radius,new Vector3f()).add(offsetPosition),new Vector3f()),
                        pose$.transformPosition(pos[3].mul(radius,new Vector3f()).add(offsetPosition),new Vector3f())
                };
                Matrix3f pose$normal = pose.normal();
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
                //Vector3f camaraPos = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition().subtract(Minecraft.getInstance().player.getEyePosition(Minecraft.getInstance().getPartialTick())).reverse().toVector3f();
                Vector3f camaraPos = ConstantUtil.VECTOR3F_ZERO;
                //Vector3f QuadNormal = pose$normal.transform(calculateNormal(vertex[0], vertex[1], vertex[2], vertex[3]));
                Vector3f QuadNormal = calculateNormal(real_position[0], real_position[1], real_position[2], real_position[3]);
                if (inverse){
                    QuadNormal.mul(-1.0f);
                }
                if(!shouldCull || (
                        isAngleAcute(real_position[0],QuadNormal,camaraPos) &&
                        isAngleAcute(real_position[1],QuadNormal,camaraPos) &&
                        isAngleAcute(real_position[2],QuadNormal,camaraPos) &&
                        isAngleAcute(real_position[3],QuadNormal,camaraPos)
                )) {
                    VertexBuilder builder = new VertexBuilder(consumer);
                    if (!inverse) {
                        builder.vertex(real_position[0]).color(startColor).color(coreColor).uv(uvStart.x, uvStart.y).uv(uvEnd.x, uvEnd.y).normal(pose$normal, normal[0]).endVertex();
                        builder.vertex(real_position[1]).color(startColor).color(coreColor).uv(uvStart.x, uvStart.y).uv(uvEnd.x, uvEnd.y).normal(pose$normal, normal[1]).endVertex();
                        builder.vertex(real_position[2]).color(finalColor).color(coreColor).uv(uvStart.x, uvStart.y).uv(uvEnd.x, uvEnd.y).normal(pose$normal, normal[2]).endVertex();
                        builder.vertex(real_position[3]).color(finalColor).color(coreColor).uv(uvStart.x, uvStart.y).uv(uvEnd.x, uvEnd.y).normal(pose$normal, normal[3]).endVertex();
                    } else {
                        builder.vertex(real_position[3]).color(finalColor).color(coreColor).uv(uvStart.x, uvStart.y).uv(uvEnd.x, uvEnd.y).normal(pose$normal, normal[3]).endVertex();
                        builder.vertex(real_position[2]).color(finalColor).color(coreColor).uv(uvStart.x, uvStart.y).uv(uvEnd.x, uvEnd.y).normal(pose$normal, normal[2]).endVertex();
                        builder.vertex(real_position[1]).color(startColor).color(coreColor).uv(uvStart.x, uvStart.y).uv(uvEnd.x, uvEnd.y).normal(pose$normal, normal[1]).endVertex();
                        builder.vertex(real_position[0]).color(startColor).color(coreColor).uv(uvStart.x, uvStart.y).uv(uvEnd.x, uvEnd.y).normal(pose$normal, normal[0]).endVertex();
                    }
                }
                startColor = finalColor;
            }
        }
    }

    public static boolean isAngleAcute(Vector3f point, Vector3f normal, Vector3f cameraPosition) {
        // 計算攝像機的視線方向
        Vector3f viewDirection = new Vector3f(cameraPosition).sub(point);

        // 計算點積
        float dotProduct = normal.dot(viewDirection);

        // 判斷夾角是否是銳角
        return dotProduct > 0.0f;
    }
    public static Vector3f multiply(Matrix3f mat3, Vector3f vec3) {
        float x = vec3.x * mat3.m00 + vec3.y * mat3.m10 + vec3.z * mat3.m20;
        float y = vec3.x * mat3.m01 + vec3.y * mat3.m11 + vec3.z * mat3.m21;
        float z = vec3.x * mat3.m02 + vec3.y * mat3.m12 + vec3.z * mat3.m22;
        return new Vector3f(x, y, z);
    }

    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, float pow, Vector3f offsetPosition, Vector3f radius, final int edgeA, final int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, Color color, Color endColor, Color coreColor) {
        renderSphere(consumer, pose, pow, offsetPosition, radius, edgeA, edgeB, isHalf, uvStart, uvEnd, color, endColor, coreColor,false,false);
    }
    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, float pow, Vector3f offsetPosition, Vector3f radius, int edgeA, int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, Color color, int alpha, Color coreColor) {
        renderSphere(consumer,pose,pow,offsetPosition,radius,edgeA,edgeB,isHalf,uvStart,uvEnd,color,THObject.Color(color.r,color.g,color.b,alpha),coreColor);
    }

    public static void renderCuboid(VertexConsumer consumer, PoseStack.Pose pose, Vec3 posOffset, Vec3 scale){

    }
}
