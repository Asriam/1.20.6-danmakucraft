package com.adrian.thDanmakuCraft.client.renderer.shape;

import com.adrian.thDanmakuCraft.util.Color;
import org.joml.Vector3f;

/**
 * 形狀頂點產生器：把一個幾何面拆成四邊形，逐個頂點交給 {@link VertexHelper}。
 *
 * <p>實作端為了效能，會重複使用內部暫存的 {@link Vector3f}；因此
 * {@link VertexHelper#vertex} 必須「立即」消費傳入的座標／法線／顏色，
 * 不可在回呼之外保留參考。</p>
 */
public interface ShapeVertexHelper {

    /** 四個角落都用同一個 helper。 */
    default void vertex(VertexHelper helper) {
        this.vertexs(helper, helper, helper, helper);
    }

    void vertexs(VertexHelper helper1, VertexHelper helper2, VertexHelper helper3, VertexHelper helper4);

    /**
     * 四邊形是否朝向相機（相機在原點）。就地計算，不配置任何物件。
     *
     * <p>等價於舊的 {@code RenderUtil.calculateNormal(...).normalize()} 之後再對四個頂點做
     * {@code isAngleAcute}：因為正規化不改變正負號，所以這裡直接比較正負。</p>
     */
    static boolean frontFacing(float[] px, float[] py, float[] pz) {
        return frontFacing(px, py, pz, false);
    }

    /**
     * @param inverse 原本的實作在 inverse 時會把四邊形法線乘 -1 再做銳角測試
     */
    static boolean frontFacing(float[] px, float[] py, float[] pz, boolean inverse) {
        // 三角形 1：0-1-2
        float e1x = px[1] - px[0], e1y = py[1] - py[0], e1z = pz[1] - pz[0];
        float e2x = px[2] - px[0], e2y = py[2] - py[0], e2z = pz[2] - pz[0];
        float n1x = e1y * e2z - e1z * e2y;
        float n1y = e1z * e2x - e1x * e2z;
        float n1z = e1x * e2y - e1y * e2x;
        // 三角形 2：0-2-3
        float f1x = px[2] - px[0], f1y = py[2] - py[0], f1z = pz[2] - pz[0];
        float f2x = px[3] - px[0], f2y = py[3] - py[0], f2z = pz[3] - pz[0];
        float n2x = f1y * f2z - f1z * f2y;
        float n2y = f1z * f2x - f1x * f2z;
        float n2z = f1x * f2y - f1y * f2x;

        float nx = n1x + n2x, ny = n1y + n2y, nz = n1z + n2z;
        // 相機在原點：一般情況 normal·(cam - p) > 0 等價於 normal·p < 0；
        // inverse 時法線先被反向，因此條件反過來。
        if (inverse) {
            return nx * px[0] + ny * py[0] + nz * pz[0] > 0
                    && nx * px[1] + ny * py[1] + nz * pz[1] > 0
                    && nx * px[2] + ny * py[2] + nz * pz[2] > 0
                    && nx * px[3] + ny * py[3] + nz * pz[3] > 0;
        }
        return nx * px[0] + ny * py[0] + nz * pz[0] < 0
                && nx * px[1] + ny * py[1] + nz * pz[1] < 0
                && nx * px[2] + ny * py[2] + nz * pz[2] < 0
                && nx * px[3] + ny * py[3] + nz * pz[3] < 0;
    }

    @FunctionalInterface
    interface VertexHelper {
        void vertex(Vector3f vertexPos, Vector3f normal, Color color);
    }
}
