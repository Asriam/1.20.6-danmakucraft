package com.adrian.thDanmakuCraft.client.renderer.shape;

import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.util.Color;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 圓柱側面頂點產生器。
 *
 * <p>效能重點：</p>
 * <ul>
 *   <li>圓環的 sin／cos 依 {@code edge} 快取，內圈不再呼叫三角函數</li>
 *   <li>座標、法線寫進固定暫存物件，內圈零配置</li>
 *   <li>背面剔除就地計算，不再經過 {@code RenderUtil.calculateNormal}／{@code isVisible}</li>
 * </ul>
 */
public class CylinderVertexHelper implements ShapeVertexHelper {

    private static final Map<Integer, float[][]> RING_CACHE = new LinkedHashMap<>(8, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, float[][]> eldest) {
            return size() > 8;
        }
    };

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

    private final Vector3f posScratch = new Vector3f();
    private final Vector3f normalScratch = new Vector3f();
    private final float[] px = new float[4];
    private final float[] py = new float[4];
    private final float[] pz = new float[4];

    public CylinderVertexHelper(Matrix4f pose, Matrix3f normal, Vector3f offsetPosition, float radius,
                                float length, int edge, boolean isClosed, Color colorTop, Color colorBottom,
                                boolean inverse) {
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

    /** @return {@code [cos, sin]}，長度 {@code edge + 1} */
    private static float[][] ring(int edge) {
        synchronized (RING_CACHE) {
            float[][] cached = RING_CACHE.get(edge);
            if (cached != null) {
                return cached;
            }
            float step = (Mth.PI * 2.0f) / edge;
            float[] cos = new float[edge + 1];
            float[] sin = new float[edge + 1];
            for (int i = 0; i <= edge; i++) {
                float angle = step * i;
                cos[i] = Mth.cos(angle);
                sin[i] = Mth.sin(angle);
            }
            float[][] value = { cos, sin };
            RING_CACHE.put(edge, value);
            return value;
        }
    }

    @Override
    public void vertexs(VertexHelper helper1, VertexHelper helper2, VertexHelper helper3, VertexHelper helper4) {
        if (this.edge <= 0) {
            return;
        }
        float[][] tables = ring(this.edge);
        float[] cos = tables[0];
        float[] sin = tables[1];

        final Matrix4f pose = this.pose;
        final Matrix3f normalMatrix = this.normal;
        final float radius = this.radius;
        final float lengthValue = this.length;
        final float ox = this.offsetPosition.x, oy = this.offsetPosition.y, oz = this.offsetPosition.z;
        final boolean cull = RenderUtil.shouldCull();
        final Color top = this.colorTop;
        final Color bottom = this.colorBottom;
        final Vector3f pos = this.posScratch;
        final Vector3f normal = this.normalScratch;
        final float[] px = this.px, py = this.py, pz = this.pz;

        for (int i = 0; i < this.edge; i++) {
            final float c1 = cos[i], s1 = sin[i];
            final float c2 = cos[i + 1], s2 = sin[i + 1];

            // 上緣 y=+length、下緣 y=-length（保留原本的行為：不做 /2）
            pose.transformPosition(c1 * radius + ox, lengthValue + oy, s1 * radius + oz, pos);
            px[0] = pos.x; py[0] = pos.y; pz[0] = pos.z;
            pose.transformPosition(c2 * radius + ox, lengthValue + oy, s2 * radius + oz, pos);
            px[1] = pos.x; py[1] = pos.y; pz[1] = pos.z;
            pose.transformPosition(c2 * radius + ox, -lengthValue + oy, s2 * radius + oz, pos);
            px[2] = pos.x; py[2] = pos.y; pz[2] = pos.z;
            pose.transformPosition(c1 * radius + ox, -lengthValue + oy, s1 * radius + oz, pos);
            px[3] = pos.x; py[3] = pos.y; pz[3] = pos.z;

            if (cull && !ShapeVertexHelper.frontFacing(px, py, pz, this.inverse)) {
                continue;
            }

            // 法線沿用原本的行為：normal = pos（含 ±length 的 y 分量）
            final float nx0 = c1 * radius, nz0 = s1 * radius;
            final float nx1 = c2 * radius, nz1 = s2 * radius;

            if (this.inverse) {
                emit(helper1, normalMatrix, px, py, pz, 3, nx0, -lengthValue, nz0, bottom, pos, normal);
                emit(helper2, normalMatrix, px, py, pz, 2, nx1, -lengthValue, nz1, bottom, pos, normal);
                emit(helper3, normalMatrix, px, py, pz, 1, nx1, lengthValue, nz1, top, pos, normal);
                emit(helper4, normalMatrix, px, py, pz, 0, nx0, lengthValue, nz0, top, pos, normal);
            } else {
                emit(helper1, normalMatrix, px, py, pz, 0, nx0, lengthValue, nz0, top, pos, normal);
                emit(helper2, normalMatrix, px, py, pz, 1, nx1, lengthValue, nz1, top, pos, normal);
                emit(helper3, normalMatrix, px, py, pz, 2, nx1, -lengthValue, nz1, bottom, pos, normal);
                emit(helper4, normalMatrix, px, py, pz, 3, nx0, -lengthValue, nz0, bottom, pos, normal);
            }
        }
    }

    private static void emit(VertexHelper helper, Matrix3f normalMatrix,
                             float[] px, float[] py, float[] pz, int index,
                             float nx, float ny, float nz, Color color,
                             Vector3f pos, Vector3f normal) {
        pos.x = px[index];
        pos.y = py[index];
        pos.z = pz[index];
        normal.x = nx * normalMatrix.m00 + ny * normalMatrix.m10 + nz * normalMatrix.m20;
        normal.y = nx * normalMatrix.m01 + ny * normalMatrix.m11 + nz * normalMatrix.m21;
        normal.z = nx * normalMatrix.m02 + ny * normalMatrix.m12 + nz * normalMatrix.m22;
        helper.vertex(pos, normal, color);
    }
}
