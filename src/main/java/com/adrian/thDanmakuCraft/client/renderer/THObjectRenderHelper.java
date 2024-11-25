package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
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
    public static void renderTexture(VertexConsumer consumer, PoseStack.Pose pose, int p_254296_,
                                     Vector3f vertex1, Vector2f uv1, THObject.Color color,
                                     Vector3f vertex2, Vector2f uv2, THObject.Color color2,
                                     Vector3f vertex3, Vector2f uv3, THObject.Color color3,
                                     Vector3f vertex4, Vector2f uv4, THObject.Color color4
                                     ) {
        vertex(consumer, pose, p_254296_, vertex1, uv1, color);
        vertex(consumer, pose, p_254296_, vertex2, uv2, color2);
        vertex(consumer, pose, p_254296_, vertex3, uv3, color3);
        vertex(consumer, pose, p_254296_, vertex4, uv4, color4);
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

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int p_254296_, Vector3f vertex, Vector2f uv, THObject.Color color) {
        vertex(consumer,pose,p_254296_,vertex.x, vertex.y, vertex.z,uv.x, uv.y,color);
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int p_254296_,
                                  float x, float y, float z,
                                  float u, float v, THObject.Color color) {
        consumer.vertex(pose.pose(), x, y, z)
                .color(color.r, color.g, color.b, color.a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_254296_)
                .normal(pose, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, int p_254296_, float pow, Vec3 offsetPosition, Vec3 scale, final int edgeA, final int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, THObject.Color color, THObject.Color endColor) {
        THObject.Color startColor = color;
        THObject.Color deColor = THObject.Color(0,0,0,0);
        int edgeADiv2 = Mth.floor(edgeA / 2.0f);
        float angle1;
        float angle2;

        if (isHalf){
            angle1 = Mth.DEG_TO_RAD * (90.0f/edgeADiv2);
        }else {
            angle1 = Mth.DEG_TO_RAD * (180.0f/edgeADiv2);
        }

        angle2 = Mth.DEG_TO_RAD * (360.0f/edgeB);

        if (startColor.r != endColor.r){
            deColor.r = (startColor.r - endColor.r)/(edgeADiv2);
        }
        if (startColor.g != endColor.g){
            deColor.g = (startColor.g - endColor.g)/(edgeADiv2);
        }
        if (startColor.b != endColor.b){
            deColor.b = (startColor.b - endColor.b)/(edgeADiv2);
        }
        if (startColor.a != endColor.a){
            deColor.a = (startColor.a - endColor.a)/(edgeADiv2);
        }

        for (int j = 0; j < edgeB; j++) {
            float x1 = Mth.cos((angle2 * j));
            float x2 = Mth.cos((angle2 * (j + 1)));

            float z1 = Mth.sin((angle2 * j));
            float z2 = Mth.sin((angle2 * (j + 1)));

            startColor = color;
            for (int i = 0; i < edgeADiv2; i++) {
                float sin01 = Mth.sin(i*angle1);
                float sin02 = Mth.sin((i+1)*angle1);

                float sin1,sin2;
                if (pow == 1){
                    sin1 = sin01;
                    sin2 = sin02;
                }else {
                    sin1 = (float) Math.pow(sin01,pow);
                    sin2 = (float) Math.pow(sin02,pow);
                }

                float cos1 = Mth.cos(i*angle1);
                float cos2 = Mth.cos((i+1)*angle1);
                THObject.Color finalColor = color.minus(deColor.multiply(i));
                //THObject.Color finalColor = THObject.Color(color.r-deColor.r*i,color.g-deColor.g*i,color.b-deColor.b*i,color.a-deColor.a*i);

                Vector3f scaleF = scale.toVector3f();
                Vector3f offsetPositionF = offsetPosition.toVector3f();

                renderTexture(consumer, pose, p_254296_,
                        new Vector3f(x1*sin1,cos1,z1*sin1).mul(scaleF).add(offsetPositionF),
                        new Vector2f(uvStart.x, uvStart.y),startColor,

                        new Vector3f(x2*sin1,cos1,z2*sin1).mul(scaleF).add(offsetPositionF),
                        new Vector2f(uvEnd.x,   uvStart.y),startColor,

                        new Vector3f(x2*sin2,cos2,z2*sin2).mul(scaleF).add(offsetPositionF),
                        new Vector2f(uvEnd.x,   uvEnd.y),  finalColor,

                        new Vector3f(x1*sin2,cos2,z1*sin2).mul(scaleF).add(offsetPositionF),
                        new Vector2f(uvStart.x, uvEnd.y),  finalColor
                );

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

    public static void renderSphere(VertexConsumer consumer, PoseStack.Pose pose, int p_254296_, float pow, Vec3 offsetPosition, Vec3 scale, int edgeA, int edgeB, boolean isHalf, Vec2 uvStart, Vec2 uvEnd, THObject.Color color, int alpha) {
        renderSphere(consumer,pose,p_254296_,pow,offsetPosition,scale,edgeA,edgeB,isHalf,uvStart,uvEnd,color,THObject.Color(color.r,color.g,color.b,alpha));
    }
}
