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
 * 球體（或依 pow 變形的球體）頂點產生器。
 *
 * <p>效能重點：</p>
 * <ul>
 *   <li>經線／緯線的 sin／cos（以及 sin^pow）只算一次並快取，內圈不再呼叫三角函數</li>
 *   <li>每個頂點的座標、法線都寫進固定的暫存物件，內圈不產生任何垃圾</li>
 *   <li>背面剔除改成就地計算四邊形法線，省掉 {@code RenderUtil.calculateNormal} 的配置</li>
 *   <li>每列顏色只算一次（與緯度 j 無關），且保留原本的整數色階行為</li>
 * </ul>
 */
public class SphereVertexHelper implements ShapeVertexHelper {

    /** 三角函數快取（key 由 edgeA/edgeB/isHalf/pow 組成）。 */
    private static final Map<Long, float[][]> TRIG_CACHE = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, float[][]> eldest) {
            return size() > 8;
        }
    };

    private final Matrix4f pose;
    private final Matrix3f normal;
    private final Vector3f offsetPosition;
    private final Vector3f radius;
    private final float pow;
    private final boolean inverse;
    private final boolean isStraight;
    private final boolean isHalf;
    private final int edgeA;
    private final int edgeB;

    /** 每一列（i）的顏色，與緯度 j 無關，只算一次。 */
    private final Color[] rowColors;

    // 內圈暫存（避免每個頂點都 new）
    private final Vector3f posScratch = new Vector3f();
    private final Vector3f normalScratch = new Vector3f();
    private final float[] px = new float[4];
    private final float[] py = new float[4];
    private final float[] pz = new float[4];

    public SphereVertexHelper(Matrix4f pose, Matrix3f normal, Vector3f offsetPosition, Vector3f radius,
                              int edgeA, int edgeB, float pow, boolean isHalf, Color startColor, Color endColor,
                              boolean inverse, boolean isStraight) {
        this.pose = pose;
        this.normal = normal;
        this.offsetPosition = offsetPosition;
        this.radius = radius;
        this.pow = pow;
        this.edgeA = edgeA;
        this.edgeB = edgeB;
        this.isHalf = isHalf;
        this.inverse = inverse;
        this.isStraight = isStraight;
        this.rowColors = buildRowColors(edgeA, startColor, endColor);
    }

    public SphereVertexHelper(Matrix4f pose, Matrix3f normal, Vector3f offsetPosition, Vector3f radius,
                              int edgeA, int edgeB, float pow, Color startColor, Color endColor) {
        this(pose, normal, offsetPosition, radius, edgeA, edgeB, pow, false, startColor, endColor, false, false);
    }

    /** 保留原本的整數色階（start - (int)(step * i)），並避免 edgeA 太小造成除以零。 */
    private static Color[] buildRowColors(int edgeA, Color startColor, Color endColor) {
        int rows = Mth.floor(edgeA / 2.0f);
        if (rows <= 0) {
            return new Color[0];
        }
        int steps = Math.max(1, rows - 1);
        int dr = (startColor.r - endColor.r) / steps;
        int dg = (startColor.g - endColor.g) / steps;
        int db = (startColor.b - endColor.b) / steps;
        int da = (startColor.a - endColor.a) / steps;
        Color[] colors = new Color[rows];
        for (int i = 0; i < rows; i++) {
            colors[i] = new Color(
                    startColor.r - (int) (dr * (float) i),
                    startColor.g - (int) (dg * (float) i),
                    startColor.b - (int) (db * (float) i),
                    startColor.a - (int) (da * (float) i));
        }
        return colors;
    }

    /**
     * @return {@code [cosM, sinM, cosP, sinP]}；sinM 已套用 pow
     */
    private static float[][] trig(int rows, int cols, boolean isHalf, float pow) {
        long key = (rows & 0xFFFFL)
                | ((cols & 0xFFFFL) << 16)
                | (isHalf ? 1L << 32 : 0L)
                | ((long) Float.floatToIntBits(pow) << 33);
        synchronized (TRIG_CACHE) {
            float[][] cached = TRIG_CACHE.get(key);
            if (cached != null) {
                return cached;
            }
            float angle1 = Mth.DEG_TO_RAD * ((isHalf ? 90.0f : 180.0f) / rows);
            float angle2 = Mth.DEG_TO_RAD * (360.0f / cols);

            float[] cosM = new float[rows + 1];
            float[] sinM = new float[rows + 1];
            for (int i = 0; i <= rows; i++) {
                float angle = i * angle1;
                cosM[i] = Mth.cos(angle);
                float sin = Mth.sin(angle);
                sinM[i] = pow == 1.0f ? sin : (float) Math.pow(sin, pow);
            }
            float[] cosP = new float[cols + 1];
            float[] sinP = new float[cols + 1];
            for (int j = 0; j <= cols; j++) {
                float angle = j * angle2;
                cosP[j] = Mth.cos(angle);
                sinP[j] = Mth.sin(angle);
            }
            float[][] value = { cosM, sinM, cosP, sinP };
            TRIG_CACHE.put(key, value);
            return value;
        }
    }

    @Override
    public void vertexs(VertexHelper helper1, VertexHelper helper2, VertexHelper helper3, VertexHelper helper4) {
        int rows = Mth.floor(this.edgeA / 2.0f);
        int cols = this.edgeB;
        if (rows <= 0 || cols <= 0 || this.rowColors.length < rows) {
            return;
        }

        float[][] tables = trig(rows, cols, this.isHalf, this.pow);
        float[] cosM = tables[0], sinM = tables[1], cosP = tables[2], sinP = tables[3];

        final Matrix4f pose = this.pose;
        final Matrix3f normalMatrix = this.normal;
        final float rx = this.radius.x, ry = this.radius.y, rz = this.radius.z;
        final float ox = this.offsetPosition.x, oy = this.offsetPosition.y, oz = this.offsetPosition.z;
        final float sign = this.inverse ? -1.0f : 1.0f;
        final boolean cull = RenderUtil.shouldCull();
        final boolean straight = this.isStraight;
        final Color[] rowColors = this.rowColors;
        final Vector3f pos = this.posScratch;
        final Vector3f normal = this.normalScratch;
        final float[] px = this.px, py = this.py, pz = this.pz;
        final Color startColor = rowColors[0];

        for (int j = 0; j < cols; j++) {
            final float x1 = cosP[j], z1 = sinP[j];
            final float x2 = cosP[j + 1], z2 = sinP[j + 1];

            for (int i = 0; i < rows; i++) {
                final float s1 = sinM[i], s2 = sinM[i + 1];
                final float c1 = cosM[i], c2 = cosM[i + 1];

                // 單位球上的四個角
                final float u0x = x1 * s1, u0y = c1, u0z = z1 * s1;
                final float u1x = x2 * s1, u1y = c1, u1z = z2 * s1;
                final float u2x = x2 * s2, u2y = c2, u2z = z2 * s2;
                final float u3x = x1 * s2, u3y = c2, u3z = z1 * s2;

                // 半徑縮放 + 位移 + 相機變換
                pose.transformPosition(u0x * rx + ox, u0y * ry + oy, u0z * rz + oz, pos);
                px[0] = pos.x; py[0] = pos.y; pz[0] = pos.z;
                pose.transformPosition(u1x * rx + ox, u1y * ry + oy, u1z * rz + oz, pos);
                px[1] = pos.x; py[1] = pos.y; pz[1] = pos.z;
                pose.transformPosition(u2x * rx + ox, u2y * ry + oy, u2z * rz + oz, pos);
                px[2] = pos.x; py[2] = pos.y; pz[2] = pos.z;
                pose.transformPosition(u3x * rx + ox, u3y * ry + oy, u3z * rz + oz, pos);
                px[3] = pos.x; py[3] = pos.y; pz[3] = pos.z;

                if (cull && !ShapeVertexHelper.frontFacing(px, py, pz, this.inverse)) {
                    continue;
                }

                // 原版：送出時用的是「上一列」的顏色（i == 0 時就是起始色）
                final Color prevColor = i == 0 ? startColor : rowColors[i - 1];
                final Color rowColor = rowColors[i];
                final float nx0 = (straight ? x1 : u0x) * sign;
                final float ny0 = (straight ? 0.0f : u0y) * sign;
                final float nz0 = (straight ? z1 : u0z) * sign;
                final float nx1 = (straight ? x2 : u1x) * sign;
                final float ny1 = (straight ? 0.0f : u1y) * sign;
                final float nz1 = (straight ? z2 : u1z) * sign;
                final float nx2 = (straight ? x2 : u2x) * sign;
                final float ny2 = (straight ? 0.0f : u2y) * sign;
                final float nz2 = (straight ? z2 : u2z) * sign;
                final float nx3 = (straight ? x1 : u3x) * sign;
                final float ny3 = (straight ? 0.0f : u3y) * sign;
                final float nz3 = (straight ? z1 : u3z) * sign;

                if (this.inverse) {
                    emit(helper1, normalMatrix, px, py, pz, 3, nx3, ny3, nz3, rowColor, pos, normal);
                    emit(helper2, normalMatrix, px, py, pz, 2, nx2, ny2, nz2, rowColor, pos, normal);
                    emit(helper3, normalMatrix, px, py, pz, 1, nx1, ny1, nz1, prevColor, pos, normal);
                    emit(helper4, normalMatrix, px, py, pz, 0, nx0, ny0, nz0, prevColor, pos, normal);
                } else {
                    emit(helper1, normalMatrix, px, py, pz, 0, nx0, ny0, nz0, prevColor, pos, normal);
                    emit(helper2, normalMatrix, px, py, pz, 1, nx1, ny1, nz1, prevColor, pos, normal);
                    emit(helper3, normalMatrix, px, py, pz, 2, nx2, ny2, nz2, rowColor, pos, normal);
                    emit(helper4, normalMatrix, px, py, pz, 3, nx3, ny3, nz3, rowColor, pos, normal);
                }
            }
        }
    }

    /** 把第 {@code index} 個角交給 helper（重用 pos／normal 暫存物件，不產生垃圾）。 */
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
