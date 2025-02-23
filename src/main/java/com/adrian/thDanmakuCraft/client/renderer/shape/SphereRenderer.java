package com.adrian.thDanmakuCraft.client.renderer.shape;

import com.adrian.thDanmakuCraft.util.Color;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class SphereRenderer {

    //private final int edgeA;
    private final int edgeB;
    private final Vector3f radius;
    private final float pow;
    //private final boolean isHalf;
    private final Color startColor;
    //private final Color endColor;
    private final boolean isStraight;
    private final Vector3f offsetPosition;
    private final int edgeADiv2;
    private final Color deColor;
    private final float angle1;
    private final float angle2;

    public SphereRenderer(Vector3f offsetPosition, Vector3f radius, int edgeA, int edgeB, float pow, boolean isHalf, Color startColor, Color endColor, boolean isStraight) {
        //this.edgeA = edgeA;
        this.edgeB = edgeB;
        this.offsetPosition = offsetPosition;
        this.radius = radius;
        this.pow = pow;
        //this.isHalf = isHalf;
        this.startColor = startColor;
        //this.endColor = endColor;
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

    public SphereRenderer(Vector3f offsetPosition, Vector3f radius, int edgeA, int edgeB, float pow, Color startColor, Color endColor){
        this(offsetPosition,radius,edgeA,edgeB,pow,false,startColor,endColor,false);
    }

    public void render(VertexHelper helper){
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
                Vector3f[] vertex = new Vector3f[]{
                        pos[0].mul(radius, new Vector3f()).add(offsetPosition),
                        pos[1].mul(radius, new Vector3f()).add(offsetPosition),
                        pos[2].mul(radius, new Vector3f()).add(offsetPosition),
                        pos[3].mul(radius, new Vector3f()).add(offsetPosition),
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
                helper.vertex(vertex[0], normal[0], startColor);
                helper.vertex(vertex[1], normal[1], startColor);
                helper.vertex(vertex[2], normal[2], finalColor);
                helper.vertex(vertex[3], normal[3], finalColor);
                startColor = finalColor;
            }
        }
    }

    public interface VertexHelper{
        void vertex(Vector3f vertexPos, Vector3f normal, Color color);
    }
}
