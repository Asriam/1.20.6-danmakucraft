package com.adrian.thDanmakuCraft.client.renderer.shape;

import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.VertexBuilder;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.ConstantUtil;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SphereVertexHelper extends ShapeVertexHelper{

    //private final int edgeA;
    private final int edgeB;
    private final Vector3f radius;
    private final float pow;
    //private final boolean isHalf;
    private final Color startColor;
    //private final Color endColor;
    private final boolean inverse;
    private final boolean isStraight;
    private final Vector3f offsetPosition;
    private final int edgeADiv2;
    private final Color deColor;
    private final float angle1;
    private final float angle2;
    private final Matrix4f pose;
    private final Matrix3f pose_normal;


    public SphereVertexHelper(Matrix4f pose, Matrix3f pose_normal, Vector3f offsetPosition, Vector3f radius, int edgeA, int edgeB, float pow, boolean isHalf, Color startColor, Color endColor, boolean inverse, boolean isStraight) {
        //this.edgeA = edgeA;
        this.pose = pose;
        this.pose_normal = pose_normal;
        this.edgeB = edgeB;
        this.offsetPosition = offsetPosition;
        this.radius = radius;
        this.pow = pow;
        //this.isHalf = isHalf;
        this.startColor = startColor;
        //this.endColor = endColor;
        this.inverse = inverse;
        this.isStraight = isStraight;
        this.edgeADiv2 = Mth.floor(edgeA / 2.0f);
        int edge3 = edgeADiv2-1;
        if (isHalf){
            angle1 = Mth.DEG_TO_RAD * (90.0f/edgeADiv2);
        }else {
            angle1 = Mth.DEG_TO_RAD * (180.0f/edgeADiv2);
        }
        angle2 = Mth.DEG_TO_RAD * (360.0f/edgeB);
        this.deColor = Color.of(
                (startColor.r - endColor.r)/ edge3,
                (startColor.g - endColor.g)/ edge3,
                (startColor.b - endColor.b)/ edge3,
                (startColor.a - endColor.a)/ edge3);
    }

    public SphereVertexHelper(Matrix4f pose, Matrix3f pose_normal, Vector3f offsetPosition, Vector3f radius, int edgeA, int edgeB, float pow, Color startColor, Color endColor){
        this(pose,pose_normal,offsetPosition,radius,edgeA,edgeB,pow,false,startColor,endColor,false,false);
    }

    public void vertex(ShapeVertexHelper.VertexHelper helper){
        /*for (int j = 0; j < edgeB; j++) {
            float x1 = Mth.cos((angle2 * j));
            float z1 = Mth.sin((angle2 * j));

            float x2 = Mth.cos((angle2 * (j + 1)));
            float z2 = Mth.sin((angle2 * (j + 1)));

            Color startColor = this.startColor;
            for (int i = 0; i < edgeADiv2; i++) {
                float sin01 = Mth.sin(i * angle1);
                float cos1 = Mth.cos(i * angle1);
                float sin02 = Mth.sin((i + 1) * angle1);
                float cos2 = Mth.cos((i + 1) * angle1);

                float sin1 = pow == 1.0f ? sin01 : (float) Math.pow(sin01, pow);
                float sin2 = pow == 1.0f ? sin02 : (float) Math.pow(sin02, pow);

                Color finalColor = this.startColor.subtract(deColor.multiply(i));
                Vector3f[] pos = new Vector3f[]{
                        new Vector3f(x1 * sin1, cos1, z1 * sin1),
                        new Vector3f(x2 * sin1, cos1, z2 * sin1),
                        new Vector3f(x2 * sin2, cos2, z2 * sin2),
                        new Vector3f(x1 * sin2, cos2, z1 * sin2),
                };
                Vector3f[] real_position = new Vector3f[]{
                        pose.transformPosition(pos[0].mul(radius, new Vector3f()).add(offsetPosition)),
                        pose.transformPosition(pos[1].mul(radius, new Vector3f()).add(offsetPosition)),
                        pose.transformPosition(pos[2].mul(radius, new Vector3f()).add(offsetPosition)),
                        pose.transformPosition(pos[3].mul(radius, new Vector3f()).add(offsetPosition)),
                };
                Vector3f[] normal;
                if (isStraight) {
                    normal = new Vector3f[]{
                            new Vector3f(x1, 0, z1),
                            new Vector3f(x2, 0, z2),
                            new Vector3f(x2, 0, z2),
                            new Vector3f(x1, 0, z1),
                    };
                } else {
                    normal = pos;
                }
                Vector3f camaraPos = ConstantUtil.VECTOR3F_ZERO;
                Vector3f QuadNormal = RenderUtil.calculateNormal(real_position[0], real_position[1], real_position[2], real_position[3]);
                if (inverse){
                    QuadNormal.mul(-1.0f);
                }
                if(!RenderUtil.shouldCull() || (
                        RenderUtil.isAngleAcute(real_position[0],QuadNormal,camaraPos) &&
                        RenderUtil.isAngleAcute(real_position[1],QuadNormal,camaraPos) &&
                        RenderUtil.isAngleAcute(real_position[2],QuadNormal,camaraPos) &&
                        RenderUtil.isAngleAcute(real_position[3],QuadNormal,camaraPos)
                )) {
                    if (!inverse) {
                        helper.vertex(real_position[0], VertexBuilder.transformNormal(pose_normal, normal[0]), startColor);
                        helper.vertex(real_position[1], VertexBuilder.transformNormal(pose_normal, normal[1]), startColor);
                        helper.vertex(real_position[2], VertexBuilder.transformNormal(pose_normal, normal[2]), finalColor);
                        helper.vertex(real_position[3], VertexBuilder.transformNormal(pose_normal, normal[3]), finalColor);
                    }else{
                        helper.vertex(real_position[3], VertexBuilder.transformNormal(pose_normal, normal[3]), finalColor);
                        helper.vertex(real_position[2], VertexBuilder.transformNormal(pose_normal, normal[2]), finalColor);
                        helper.vertex(real_position[1], VertexBuilder.transformNormal(pose_normal, normal[1]), startColor);
                        helper.vertex(real_position[0], VertexBuilder.transformNormal(pose_normal, normal[0]), startColor);
                    }
                }
                startColor = finalColor;
            }
        }*/
        this.vertexs(helper,helper,helper,helper);
    }

    public void vertexs(
            ShapeVertexHelper.VertexHelper helper1,
            ShapeVertexHelper.VertexHelper helper2,
            ShapeVertexHelper.VertexHelper helper3,
            ShapeVertexHelper.VertexHelper helper4){
        for (int j = 0; j < edgeB; j++) {
            float x1 = Mth.cos((angle2 * j));
            float z1 = Mth.sin((angle2 * j));

            float x2 = Mth.cos((angle2 * (j + 1)));
            float z2 = Mth.sin((angle2 * (j + 1)));

            Color startColor = this.startColor;
            for (int i = 0; i < edgeADiv2; i++) {
                float sin01 = Mth.sin(i * angle1);
                float cos1 = Mth.cos(i * angle1);
                float sin02 = Mth.sin((i + 1) * angle1);
                float cos2 = Mth.cos((i + 1) * angle1);

                float sin1 = pow == 1.0f ? sin01 : (float) Math.pow(sin01, pow);
                float sin2 = pow == 1.0f ? sin02 : (float) Math.pow(sin02, pow);

                Color finalColor = this.startColor.subtract(deColor.multiply(i));
                Vector3f[] pos = new Vector3f[]{
                        new Vector3f(x1 * sin1, cos1, z1 * sin1),
                        new Vector3f(x2 * sin1, cos1, z2 * sin1),
                        new Vector3f(x2 * sin2, cos2, z2 * sin2),
                        new Vector3f(x1 * sin2, cos2, z1 * sin2),
                };
                Vector3f[] real_position = new Vector3f[]{
                        pose.transformPosition(pos[0].mul(radius, new Vector3f()).add(offsetPosition)),
                        pose.transformPosition(pos[1].mul(radius, new Vector3f()).add(offsetPosition)),
                        pose.transformPosition(pos[2].mul(radius, new Vector3f()).add(offsetPosition)),
                        pose.transformPosition(pos[3].mul(radius, new Vector3f()).add(offsetPosition)),
                };
                Vector3f[] normal;
                if (isStraight) {
                    normal = new Vector3f[]{
                            new Vector3f(x1, 0, z1),
                            new Vector3f(x2, 0, z2),
                            new Vector3f(x2, 0, z2),
                            new Vector3f(x1, 0, z1),
                    };
                } else {
                    normal = pos;
                }
                Vector3f camaraPos = ConstantUtil.VECTOR3F_ZERO;
                Vector3f QuadNormal = RenderUtil.calculateNormal(real_position[0], real_position[1], real_position[2], real_position[3]);
                if (inverse){
                    QuadNormal.mul(-1.0f);
                }
                if(!RenderUtil.shouldCull() || (
                        RenderUtil.isAngleAcute(real_position[0],QuadNormal,camaraPos) &&
                                RenderUtil.isAngleAcute(real_position[1],QuadNormal,camaraPos) &&
                                RenderUtil.isAngleAcute(real_position[2],QuadNormal,camaraPos) &&
                                RenderUtil.isAngleAcute(real_position[3],QuadNormal,camaraPos)
                )) {
                    if (!inverse) {
                        helper1.vertex(real_position[0], VertexBuilder.transformNormal(pose_normal, normal[0]), startColor);
                        helper2.vertex(real_position[1], VertexBuilder.transformNormal(pose_normal, normal[1]), startColor);
                        helper3.vertex(real_position[2], VertexBuilder.transformNormal(pose_normal, normal[2]), finalColor);
                        helper4.vertex(real_position[3], VertexBuilder.transformNormal(pose_normal, normal[3]), finalColor);
                    }else{
                        helper1.vertex(real_position[3], VertexBuilder.transformNormal(pose_normal, normal[3]), finalColor);
                        helper2.vertex(real_position[2], VertexBuilder.transformNormal(pose_normal, normal[2]), finalColor);
                        helper3.vertex(real_position[1], VertexBuilder.transformNormal(pose_normal, normal[1]), startColor);
                        helper4.vertex(real_position[0], VertexBuilder.transformNormal(pose_normal, normal[0]), startColor);
                    }
                }
                startColor = finalColor;
            }
        }
    }

}
