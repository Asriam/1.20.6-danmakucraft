package com.adrian.thDanmakuCraft.client.renderer.shape;

import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.VertexBuilder;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.ConstantUtil;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CylinderVertexHelper implements ShapeVertexHelper{

    private final Matrix4f pose;
    private final Matrix3f normal;
    private final Vector3f offsetPosition;
    private final float radius;
    private final float length;
    private final int edge;
    private final boolean isClosed;
    private final Color colorTop;
    private final Color colorBottom;
    private final boolean inverse;

    public CylinderVertexHelper(Matrix4f pose, Matrix3f normal, Vector3f offsetPosition, float radius, float length, int edge, boolean isClosed, Color colorTop, Color colorBottom, boolean inverse) {
        this.pose = pose;
        this.normal = normal;
        this.offsetPosition = offsetPosition;
        this.radius = radius;
        this.length = length;
        this.edge = edge;
        this.isClosed = isClosed;
        this.colorTop = colorTop;
        this.colorBottom = colorBottom;
        this.inverse = inverse;
    }

    @Override
    public void vertexs(VertexHelper helper1, VertexHelper helper2, VertexHelper helper3, VertexHelper helper4) {
        for(int i=0;i<edge;i++){
            float angle = (Mth.PI * 2) / edge;
            float x1 = Mth.cos(angle*i)*radius;
            float z1 = Mth.sin(angle*i)*radius;

            float x2 = Mth.cos(angle*(i+1))*radius;
            float z2 = Mth.sin(angle*(i+1))*radius;

            float lengthDiv2 = length;

            Vector3f[] pos = new Vector3f[]{
                    new Vector3f(x1, lengthDiv2, z1),
                    new Vector3f(x2, lengthDiv2, z2),
                    new Vector3f(x2, -lengthDiv2, z2),
                    new Vector3f(x1, -lengthDiv2, z1),
            };

            Vector3f[] real_position = new Vector3f[]{
                    pose.transformPosition(pos[0].add(offsetPosition, new Vector3f())),
                    pose.transformPosition(pos[1].add(offsetPosition, new Vector3f())),
                    pose.transformPosition(pos[2].add(offsetPosition, new Vector3f())),
                    pose.transformPosition(pos[3].add(offsetPosition, new Vector3f())),
            };

            Vector3f[] normal = pos;

            Vector3f camaraPos = ConstantUtil.VECTOR3F_ZERO;
            Vector3f QuadNormal = RenderUtil.calculateNormal(real_position[0], real_position[1], real_position[2], real_position[3]);

            if (inverse){
                QuadNormal.mul(-1.0f);
            }

            if(!RenderUtil.shouldCull() || RenderUtil.isVisible(QuadNormal, camaraPos, real_position[0], real_position[1], real_position[2], real_position[3])){
                if (inverse){
                    helper1.vertex(real_position[3], VertexBuilder.transformNormal(this.normal, normal[3]), colorBottom);
                    helper2.vertex(real_position[2], VertexBuilder.transformNormal(this.normal, normal[2]), colorBottom);
                    helper3.vertex(real_position[1], VertexBuilder.transformNormal(this.normal, normal[1]), colorTop);
                    helper4.vertex(real_position[0], VertexBuilder.transformNormal(this.normal, normal[0]), colorTop);
                }else {
                    helper1.vertex(real_position[0], VertexBuilder.transformNormal(this.normal, normal[0]), colorTop);
                    helper2.vertex(real_position[1], VertexBuilder.transformNormal(this.normal, normal[1]), colorTop);
                    helper3.vertex(real_position[2], VertexBuilder.transformNormal(this.normal, normal[2]), colorBottom);
                    helper4.vertex(real_position[3], VertexBuilder.transformNormal(this.normal, normal[3]), colorBottom);
                }
            }
        }
    }
}
