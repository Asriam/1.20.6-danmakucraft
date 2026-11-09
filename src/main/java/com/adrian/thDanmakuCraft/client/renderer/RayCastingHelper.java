package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.client.renderer.buffer.VertexBuilder;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class RayCastingHelper {
    public static void renderEllipsoid(
            VertexBuilder builder,
            Vector3f center,
            Vector3f rotation,
            Vector3f scale,
            Color color, Color coreColor, Vector4f vector4f){
        // 四邊形必須面向相機：否則從某些角度看過去會變成一直線，
        // 光線就不會穿過 ellipsoid（遠處會縮小到看不見）。
        //Quaternionf camRot = getCamera().rotation();
        Vec2 rot = THObject.VectorAngleToRadAngle(new Vec3(center));
        Quaternionf camRot = new Quaternionf().rotateY(rot.y- Mth.PI).rotateX(rot.x);

        Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f).rotate(camRot);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f).rotate(camRot);
        float half = 1.6f * Math.max(Math.abs(scale.x),
                Math.max(Math.abs(scale.y), Math.abs(scale.z)));
        ellipsoidVertex(builder, center, right, up, -half, -half, rotation, scale, color, coreColor, vector4f);
        ellipsoidVertex(builder, center, right, up, half, -half, rotation, scale, color, coreColor, vector4f);
        ellipsoidVertex(builder, center, right, up, half, half, rotation, scale, color, coreColor, vector4f);
        ellipsoidVertex(builder, center, right, up, -half, half, rotation, scale, color, coreColor, vector4f);
    }

    /**
     * 直接畫一個「鋪滿畫面」的四邊形（給射線行進的 ellipsoid 用）。
     *
     * <p>取 view space 中 z = -1 平面上的螢幕四角（NDC ±1），用 inverse(ModelViewMat)
     * 轉回相機相對世界座標；vertex shader 乘上 ModelViewMat 之後 gl_Position 就會蓋滿畫面。</p>
     */
    public static void renderEllipsoidOnScreen(
            VertexBuilder builder,
            Vector3f center,
            Vector3f rotation,
            Vector3f scale,
            Color color, Color coreColor, Vector4f vector4f) {
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        float tanX = 1.0f / projection.m00();
        float tanY = 1.0f / projection.m11();
        Matrix4f invModelView = new Matrix4f(RenderSystem.getModelViewMatrix()).invert();
        ellipsoidScreenCorner(builder, invModelView, -tanX, -tanY, center, rotation, scale, color, coreColor, vector4f);
        ellipsoidScreenCorner(builder, invModelView,  tanX, -tanY, center, rotation, scale, color, coreColor, vector4f);
        ellipsoidScreenCorner(builder, invModelView,  tanX,  tanY, center, rotation, scale, color, coreColor, vector4f);
        ellipsoidScreenCorner(builder, invModelView, -tanX,  tanY, center, rotation, scale, color, coreColor, vector4f);
    }

    /** 以 view space 的 (x, y, -1) 為角落寫一個全螢幕四邊形頂點。 */
    private static void ellipsoidScreenCorner(VertexBuilder builder, Matrix4f invModelView,
                                              float x, float y, Vector3f center, Vector3f rotation,
                                              Vector3f scale, Color color, Color coreColor, Vector4f params) {
        Vector3f corner = invModelView.transformPosition(new Vector3f(x, y, -1.0f), new Vector3f());
        writeEllipsoidVertex(builder, corner, center, rotation, scale, color, coreColor, params);
    }

    /**
     * 給鐳射用：用「頭尾 + width」模擬一個 cube，算出它在螢幕上的<b>最小面積包圍四邊形</b>，
     * 只畫那一塊區域（比全螢幕省非常多 fill rate）。
     *
     * <p>流程：8 個 cube 角落 → view space → 對近平面裁切 → 投影到 NDC →
     * 凸包 → 旋轉卡尺求最小面積矩形 → 把矩形四個 NDC 角轉回相機相對世界座標。</p>
     *
     * @param head  鐳射頭（相機相對世界座標）
     * @param tail  鐳射尾（相機相對世界座標）
     * @param width 鐳射粗細
     */
    public static void renderEllipsoidForLaser(
            VertexBuilder builder,
            Vector3f head,
            Vector3f tail,
            float width,
            Vector3f rotation,
            Vector3f scale,
            Color color, Color coreColor, Vector4f vector4f) {
        Vector3f center = new Vector3f(head).add(tail).mul(0.5f);

        // ---- 1. 頭尾 + width → cube 的 8 個角 ----
        Vector3f axis = new Vector3f(tail).sub(head);
        if (axis.lengthSquared() < 1.0e-8f) {
            axis.set(0.0f, 0.0f, 1.0f);
        } else {
            axis.normalize();
        }
        Vector3f helper = Math.abs(axis.y) > 0.99f
                ? new Vector3f(1.0f, 0.0f, 0.0f)
                : new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f right = new Vector3f(axis).cross(helper).normalize();
        Vector3f up = new Vector3f(right).cross(axis).normalize();
        float halfWidth = Math.max(width * 0.5f, 1.0e-4f);

        Vector3f[] corners = new Vector3f[8];
        for (int i = 0; i < 8; i++) {
            Vector3f base = (i < 4) ? head : tail;
            float sx = ((i & 1) == 0) ? 1.0f : -1.0f;
            float sy = ((i & 2) == 0) ? 1.0f : -1.0f;
            corners[i] = new Vector3f(base)
                    .add(new Vector3f(right).mul(halfWidth * sx))
                    .add(new Vector3f(up).mul(halfWidth * sy));
        }

        // ---- 2. 投影到 NDC（跨近平面時補切點）----
        Matrix4f modelView = RenderSystem.getModelViewMatrix();
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        List<Vector2f> projected = projectBoxOctants(corners, modelView, projection);
        if (projected.isEmpty()) {
            return;
        }

        // ---- 3. 凸包 + 最小面積矩形 ----
        List<Vector2f> hull = convexHull(projected);
        float[] rect = minAreaRect(hull);
        if (rect == null) {
            return;
        }
        float minU = rect[0], minV = rect[1], maxU = rect[2], maxV = rect[3];
        float edgeX = rect[4], edgeY = rect[5], normalX = rect[6], normalY = rect[7];

        // 留邊，避免橢圓邊緣被切掉
        float pad = 0.01f + 0.05f * Math.max(maxU - minU, maxV - minV);
        minU -= pad; minV -= pad; maxU += pad; maxV += pad;

        // ---- 4. 完全在畫面外就不畫 ----
        float screenMinX = Float.MAX_VALUE, screenMaxX = -Float.MAX_VALUE;
        float screenMinY = Float.MAX_VALUE, screenMaxY = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            float u = ((i == 0 || i == 3) ? minU : maxU);
            float v = ((i < 2) ? minV : maxV);
            float ndcX = edgeX * u + normalX * v;
            float ndcY = edgeY * u + normalY * v;
            screenMinX = Math.min(screenMinX, ndcX);
            screenMaxX = Math.max(screenMaxX, ndcX);
            screenMinY = Math.min(screenMinY, ndcY);
            screenMaxY = Math.max(screenMaxY, ndcY);
        }
        if (screenMaxX < -1.0f || screenMinX > 1.0f || screenMaxY < -1.0f || screenMinY > 1.0f) {
            return;
        }

        // ---- 5. NDC 四角 → view space (z = -1) → 相機相對世界 ----
        float tanX = 1.0f / projection.m00();
        float tanY = 1.0f / projection.m11();
        Matrix4f invModelView = new Matrix4f(modelView).invert();
        float[] us = {minU, maxU, maxU, minU};
        float[] vs = {minV, minV, maxV, maxV};
        for (int i = 0; i < 4; i++) {
            float ndcX = edgeX * us[i] + normalX * vs[i];
            float ndcY = edgeY * us[i] + normalY * vs[i];
            Vector3f world = invModelView.transformPosition(
                    new Vector3f(ndcX * tanX, ndcY * tanY, -1.0f), new Vector3f());
            writeEllipsoidVertex(builder, world, center, rotation, scale, color, coreColor, vector4f);
        }
    }

    /** 把 cube 的 8 個角投影成 NDC 點（跨近平面時補上切點）。 */
    private static List<Vector2f> projectBoxOctants(Vector3f[] corners, Matrix4f modelView, Matrix4f projection) {
        Vector3f[] view = new Vector3f[8];
        for (int i = 0; i < 8; i++) {
            view[i] = modelView.transformPosition(new Vector3f(corners[i]), new Vector3f());
        }
        final float nearZ = -0.01f;
        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };
        List<Vector2f> out = new ArrayList<>();
        for (int[] edge : edges) {
            Vector3f a = view[edge[0]];
            Vector3f b = view[edge[1]];
            boolean aIn = a.z <= nearZ;
            boolean bIn = b.z <= nearZ;
            if (aIn) {
                addProjected(out, a, projection);
            }
            if (bIn) {
                addProjected(out, b, projection);
            }
            if (aIn != bIn) {
                float t = (nearZ - a.z) / (b.z - a.z);
                addProjected(out, new Vector3f(a).lerp(b, t), projection);
            }
        }
        return out;
    }

    private static void addProjected(List<Vector2f> out, Vector3f viewPos, Matrix4f projection) {
        Vector4f clip = projection.transform(new Vector4f(viewPos, 1.0f));
        if (clip.w <= 1.0e-6f) {
            return;
        }
        out.add(new Vector2f(clip.x / clip.w, clip.y / clip.w));
    }

    /** Andrew monotone chain 凸包。 */
    private static List<Vector2f> convexHull(List<Vector2f> points) {
        List<Vector2f> sorted = new ArrayList<>(points);
        sorted.sort((a, b) -> a.x != b.x ? Float.compare(a.x, b.x) : Float.compare(a.y, b.y));
        List<Vector2f> hull = new ArrayList<>();
        for (Vector2f p : sorted) {
            while (hull.size() >= 2 && cross(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0.0f) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }
        int lower = hull.size() + 1;
        for (int i = sorted.size() - 2; i >= 0; i--) {
            Vector2f p = sorted.get(i);
            while (hull.size() >= lower && cross(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0.0f) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }
        if (!hull.isEmpty()) {
            hull.remove(hull.size() - 1);
        }
        return hull;
    }

    private static float cross(Vector2f o, Vector2f a, Vector2f b) {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x);
    }

    /**
     * 旋轉卡尺求最小面積包圍矩形。
     *
     * @return {minU, minV, maxU, maxV, edgeX, edgeY, normalX, normalY}；失敗回傳 null
     */
    private static float[] minAreaRect(List<Vector2f> hull) {
        if (hull.isEmpty()) {
            return null;
        }
        if (hull.size() < 3) {
            float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
            float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            for (Vector2f p : hull) {
                minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
                minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
            }
            if (maxX - minX < 0.02f) { minX -= 0.01f; maxX += 0.01f; }
            if (maxY - minY < 0.02f) { minY -= 0.01f; maxY += 0.01f; }
            return new float[]{minX, minY, maxX, maxY, 1.0f, 0.0f, 0.0f, 1.0f};
        }
        float bestArea = Float.MAX_VALUE;
        float[] best = null;
        for (int i = 0; i < hull.size(); i++) {
            Vector2f a = hull.get(i);
            Vector2f b = hull.get((i + 1) % hull.size());
            float ex = b.x - a.x;
            float ey = b.y - a.y;
            float len = (float) Math.sqrt(ex * ex + ey * ey);
            if (len < 1.0e-6f) {
                continue;
            }
            ex /= len; ey /= len;
            float nx = -ey;
            float ny = ex;
            float minU = Float.MAX_VALUE, maxU = -Float.MAX_VALUE;
            float minV = Float.MAX_VALUE, maxV = -Float.MAX_VALUE;
            for (Vector2f p : hull) {
                float u = p.x * ex + p.y * ey;
                float v = p.x * nx + p.y * ny;
                minU = Math.min(minU, u); maxU = Math.max(maxU, u);
                minV = Math.min(minV, v); maxV = Math.max(maxV, v);
            }
            float area = (maxU - minU) * (maxV - minV);
            if (area < bestArea) {
                bestArea = area;
                best = new float[]{minU, minV, maxU, maxV, ex, ey, nx, ny};
            }
        }
        return best;
    }

    /** 寫一個 ellipsoid 頂點（位置已算好）。 */
    private static void writeEllipsoidVertex(VertexBuilder builder, Vector3f position, Vector3f center,
                                             Vector3f rotation, Vector3f scale,
                                             Color color, Color coreColor, Vector4f params) {
        builder.vertex(position.x, position.y, position.z)
                .vertex(center)
                .vertex(rotation)
                .vertex(scale)
                .color(color)
                .color(coreColor)
                .vec4(params.x, params.y, params.z, params.w)
                .endVertex();
    }

    /** 寫一個 billboard 頂點：center + right*x + up*y。 */
        public static void ellipsoidVertex(VertexBuilder builder, Vector3f center, Vector3f right, Vector3f up,
                                            float x, float y, Vector3f rotation, Vector3f scale, Color color, Color coreColor, Vector4f vector4f) {
            builder.vertex(center.x + right.x * x + up.x * y,
                            center.y + right.y * x + up.y * y,
                            center.z + right.z * x + up.z * y)
                    .vertex(center)
                    .vertex(rotation)
                    .vertex(scale)
                    .color(color)
                    .color(coreColor)
                    .vec4(vector4f.x,vector4f.y,vector4f.z,vector4f.w)
                    .endVertex();
        }
}
