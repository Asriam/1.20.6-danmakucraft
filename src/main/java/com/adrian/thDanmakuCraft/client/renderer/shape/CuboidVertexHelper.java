package com.adrian.thDanmakuCraft.client.renderer.shape;

import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.util.Color;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 立方體（六個面）頂點產生器，{@code size} 為各軸的半長。
 *
 * <p>原本這個類別是空實作，這裡補上六面，並沿用其他 helper 的零配置寫法。</p>
 */
public class CuboidVertexHelper implements ShapeVertexHelper {

    private final Matrix4f pose;
    private final Matrix3f normal;
    private final Vector3f offsetPosition;
    private final Vector3f size;
    private final Color color;

    private final Vector3f posScratch = new Vector3f();
    private final Vector3f normalScratch = new Vector3f();
    private final float[] px = new float[4];
    private final float[] py = new float[4];
    private final float[] pz = new float[4];

    public CuboidVertexHelper(Matrix4f pose, Matrix3f normal, Vector3f offsetPosition, Vector3f size) {
        this(pose, normal, offsetPosition, size, Color.WHITE());
    }

    public CuboidVertexHelper(Matrix4f pose, Matrix3f normal, Vector3f offsetPosition, Vector3f size, Color color) {
        this.pose = pose;
        this.normal = normal;
        this.offsetPosition = offsetPosition;
        this.size = size;
        this.color = color;
    }

    @Override
    public void vertexs(VertexHelper helper1, VertexHelper helper2, VertexHelper helper3, VertexHelper helper4) {
        final float sx = this.size.x, sy = this.size.y, sz = this.size.z;
        final float ox = this.offsetPosition.x, oy = this.offsetPosition.y, oz = this.offsetPosition.z;

        // +X
        face(helper1, helper2, helper3, helper4, 1, 0, 0,
                ox + sx, oy - sy, oz + sz,
                ox + sx, oy - sy, oz - sz,
                ox + sx, oy + sy, oz - sz,
                ox + sx, oy + sy, oz + sz);
        // -X
        face(helper1, helper2, helper3, helper4, -1, 0, 0,
                ox - sx, oy - sy, oz - sz,
                ox - sx, oy - sy, oz + sz,
                ox - sx, oy + sy, oz + sz,
                ox - sx, oy + sy, oz - sz);
        // +Y
        face(helper1, helper2, helper3, helper4, 0, 1, 0,
                ox - sx, oy + sy, oz + sz,
                ox + sx, oy + sy, oz + sz,
                ox + sx, oy + sy, oz - sz,
                ox - sx, oy + sy, oz - sz);
        // -Y
        face(helper1, helper2, helper3, helper4, 0, -1, 0,
                ox - sx, oy - sy, oz - sz,
                ox + sx, oy - sy, oz - sz,
                ox + sx, oy - sy, oz + sz,
                ox - sx, oy - sy, oz + sz);
        // +Z
        face(helper1, helper2, helper3, helper4, 0, 0, 1,
                ox - sx, oy - sy, oz + sz,
                ox + sx, oy - sy, oz + sz,
                ox + sx, oy + sy, oz + sz,
                ox - sx, oy + sy, oz + sz);
        // -Z
        face(helper1, helper2, helper3, helper4, 0, 0, -1,
                ox + sx, oy - sy, oz - sz,
                ox - sx, oy - sy, oz - sz,
                ox - sx, oy + sy, oz - sz,
                ox + sx, oy + sy, oz - sz);
    }

    private void face(VertexHelper helper1, VertexHelper helper2, VertexHelper helper3, VertexHelper helper4,
                      float nx, float ny, float nz,
                      float x0, float y0, float z0,
                      float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3) {
        final Matrix4f pose = this.pose;
        final Vector3f pos = this.posScratch;

        pose.transformPosition(x0, y0, z0, pos);
        this.px[0] = pos.x; this.py[0] = pos.y; this.pz[0] = pos.z;
        pose.transformPosition(x1, y1, z1, pos);
        this.px[1] = pos.x; this.py[1] = pos.y; this.pz[1] = pos.z;
        pose.transformPosition(x2, y2, z2, pos);
        this.px[2] = pos.x; this.py[2] = pos.y; this.pz[2] = pos.z;
        pose.transformPosition(x3, y3, z3, pos);
        this.px[3] = pos.x; this.py[3] = pos.y; this.pz[3] = pos.z;

        if (RenderUtil.shouldCull() && !ShapeVertexHelper.frontFacing(this.px, this.py, this.pz)) {
            return;
        }

        emit(helper1, nx, ny, nz, 0);
        emit(helper2, nx, ny, nz, 1);
        emit(helper3, nx, ny, nz, 2);
        emit(helper4, nx, ny, nz, 3);
    }

    private void emit(VertexHelper helper, float nx, float ny, float nz, int index) {
        final Matrix3f normalMatrix = this.normal;
        final Vector3f pos = this.posScratch;
        final Vector3f normal = this.normalScratch;
        pos.x = this.px[index];
        pos.y = this.py[index];
        pos.z = this.pz[index];
        normal.x = nx * normalMatrix.m00 + ny * normalMatrix.m10 + nz * normalMatrix.m20;
        normal.y = nx * normalMatrix.m01 + ny * normalMatrix.m11 + nz * normalMatrix.m21;
        normal.z = nx * normalMatrix.m02 + ny * normalMatrix.m12 + nz * normalMatrix.m22;
        helper.vertex(pos, normal, this.color);
    }
}
