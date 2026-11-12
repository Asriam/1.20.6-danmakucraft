package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.client.renderer.buffer.VertexBuilder;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class RayCastingHelper {
    /** 除錯用：把 renderEllipsoidForLaser 看到的矩陣印到 log（每秒最多一次）。 */
    public static boolean DEBUG_MATRIX = false;
    private static long lastMatrixLog = 0L;

    public enum Shape{
        Sphere((short) 1),Ellipsoid((short) 2), Cube((short) 3), Star((short) 4);

        public final short shapeId;

        Shape(short shapeId) {
            this.shapeId = shapeId;
        }
    }

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
        float half = 1.6f * Math.max(Math.abs(scale.x), Math.max(Math.abs(scale.y), Math.abs(scale.z)));
        vertex(builder, center, right, up, -half, -half, rotation, scale, color, coreColor, vector4f, Shape.Ellipsoid);
        vertex(builder, center, right, up, half, -half, rotation, scale, color, coreColor, vector4f, Shape.Ellipsoid);
        vertex(builder, center, right, up, half, half, rotation, scale, color, coreColor, vector4f, Shape.Ellipsoid);
        vertex(builder, center, right, up, -half, half, rotation, scale, color, coreColor, vector4f, Shape.Ellipsoid);
    }

    public static void renderShape(
            VertexBuilder builder,
            Vector3f center,
            Vector3f rotation,
            Vector3f scale,
            Color color, Color coreColor, Vector4f vector4f, Shape shape){
        // 四邊形必須面向相機：否則從某些角度看過去會變成一直線，
        // 光線就不會穿過 ellipsoid（遠處會縮小到看不見）。
        //Quaternionf camRot = getCamera().rotation();
        Vec2 rot = THObject.VectorAngleToRadAngle(new Vec3(center));
        Quaternionf camRot = new Quaternionf().rotateY(rot.y- Mth.PI).rotateX(rot.x);

        Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f).rotate(camRot);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f).rotate(camRot);
        float half = 1.6f * Math.max(Math.abs(scale.x), Math.max(Math.abs(scale.y), Math.abs(scale.z)));
        vertex(builder, center, right, up, -half, -half, rotation, scale, color, coreColor, vector4f, shape);
        vertex(builder, center, right, up, half, -half, rotation, scale, color, coreColor, vector4f, shape);
        vertex(builder, center, right, up, half, half, rotation, scale, color, coreColor, vector4f, shape);
        vertex(builder, center, right, up, -half, half, rotation, scale, color, coreColor, vector4f, shape);
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
        Vector2f[] quad = {
                new Vector2f(0.0f,0.0f),
                new Vector2f(1.0f,0.0f),
                new Vector2f(1.0f,1.0f),
                new Vector2f(0.0f,1.0f),
        };
        for (Vector2f qua: quad) {
            builder.vertex(qua.x, qua.y, -1.0f)
                    .vertex(center)
                    .vertex(rotation)
                    .vertex(scale)
                    .color(color)
                    .color(coreColor)
                    .vec4(vector4f.x, vector4f.y, vector4f.z, vector4f.w)
                    .endVertex();
        }
    }

    /** 以 view space 的 (x, y, -1) 為角落寫一個全螢幕四邊形頂點。 */
    private static void ellipsoidScreenCorner(VertexBuilder builder, Matrix4f invModelView,
                                              float x, float y, Vector3f center, Vector3f rotation,
                                              Vector3f scale, Color color, Color coreColor, Vector4f params) {
        Vector3f corner = invModelView.transformPosition(new Vector3f(x, y, -1.0f), new Vector3f());
        writeEllipsoidVertex(builder, corner, center, rotation, scale, color, coreColor, params);
    }

    public static void bobView(PoseStack p_109139_, float p_109140_) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCameraEntity() instanceof Player) {
            Player player = (Player)minecraft.getCameraEntity();
            float f = player.walkDist - player.walkDistO;
            float f1 = -(player.walkDist + f * p_109140_);
            float f2 = Mth.lerp(p_109140_, player.oBob, player.bob);
            p_109139_.translate(Mth.sin(f1 * (float) Math.PI) * f2 * 0.5F, -Math.abs(Mth.cos(f1 * (float) Math.PI) * f2), 0.0F);
            p_109139_.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * f2 * 3.0F));
            p_109139_.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F));
        }
    }

    /**
     * 給鐳射用：把「頭尾 + width」想成一條 3D capsule，投影到螢幕後用最小面積的 4 頂點梯形框住。
     *
     * <p>原理：相機看一顆半徑 r、深度 d 的球，投影到「正規化影像座標」
     * (x/(-z), y/(-z))（也就是 z = -1 平面上的座標）會是一個圓，半徑
     * rho = r / sqrt(d^2 - r^2)。整條 capsule 的投影就是兩端圓的凸包，
     * 最小的 4 邊形 = 兩端沿軸向各延伸 rho 的端蓋 + 兩側外切線：</p>
     * <pre>
     *   A - dir*rhoA +- n*rhoA        B + dir*rhoB +- n*rhoB
     * </pre>
     *
     * <p>和「8 個 OBB 角 → 凸包 → 旋轉卡尺」相比：CPU 只需 2 次投影 + 幾個向量運算，
     * 面積也更小（OBB 截面用對角 sqrt(2)*r，這裡用實際的 rho）。</p>
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
        float radius = Math.max(Math.max(Math.abs(scale.x), Math.abs(scale.y)), Math.max(width * 0.5f, 1.0e-4f));

        Matrix4f modelView = RenderSystem.getModelViewMatrix();

        Vector3f a = modelView.transformPosition(new Vector3f(head), new Vector3f());
        Vector3f b = modelView.transformPosition(new Vector3f(tail), new Vector3f());

        // 近平面裁切
        final float nearZ = -0.01f;
        boolean aIn = a.z <= nearZ;
        boolean bIn = b.z <= nearZ;
        if (!aIn && !bIn) {
            return;
        }
        if (aIn != bIn) {
            float t = (nearZ - a.z) / (b.z - a.z);
            Vector3f clipped = new Vector3f(a).lerp(b, t);
            if (aIn) {
                b.set(clipped);
            } else {
                a.set(clipped);
            }
        }

        // 沿線段取樣
        final int samples = 5;
        Vector2f[] centers = new Vector2f[samples];
        float[] radii = new float[samples];
        for (int i = 0; i < samples; i++) {
            float t = i / (float) (samples - 1);
            float x = a.x + (b.x - a.x) * t;
            float y = a.y + (b.y - a.y) * t;
            float z = a.z + (b.z - a.z) * t;
            float depth = Math.max(-z, radius * 1.001f);
            float rho = radius / (float) Math.sqrt(Math.max(depth * depth - radius * radius, 1.0e-4f));
            centers[i] = new Vector2f(x / -z, y / -z);
            radii[i] = Math.min(rho, 8.0f);
        }

        Vector2f a2 = centers[0];
        Vector2f b2 = centers[samples - 1];
        Vector2f dir = new Vector2f(b2).sub(a2);
        float length = dir.length();
        if (length < 1.0e-5f) {
            dir.set(1.0f, 0.0f);
        } else {
            dir.div(length);
        }
        Vector2f normal = new Vector2f(-dir.y, dir.x);

        float pad = 0.0025f;
        float minU = Float.MAX_VALUE, maxU = -Float.MAX_VALUE;
        float minV = Float.MAX_VALUE, maxV = -Float.MAX_VALUE;
        for (int i = 0; i < samples; i++) {
            float u = centers[i].x * dir.x + centers[i].y * dir.y;
            float v = centers[i].x * normal.x + centers[i].y * normal.y;
            float r = radii[i] + pad;
            minU = Math.min(minU, u - r);
            maxU = Math.max(maxU, u + r);
            minV = Math.min(minV, v - r);
            maxV = Math.max(maxV, v + r);
        }

        Vector2f c1 = new Vector2f(dir).mul(minU).add(new Vector2f(normal).mul(minV));
        Vector2f c2 = new Vector2f(dir).mul(maxU).add(new Vector2f(normal).mul(minV));
        Vector2f c3 = new Vector2f(dir).mul(maxU).add(new Vector2f(normal).mul(maxV));
        Vector2f c4 = new Vector2f(dir).mul(minU).add(new Vector2f(normal).mul(maxV));

        // 正規化影像座標 -> view space (z = -1) -> 相機相對世界座標
        Matrix4f invModelView = new Matrix4f(modelView).invert();
        Vector2f[] quad = {c1, c2, c3, c4};
        for (Vector2f point : quad) {
            /*Vector3f world = invModelView.transformPosition(
                    new Vector3f(point.x, point.y, -1.0f), new Vector3f());*/
            writeEllipsoidVertex(builder, new Vector3f(point.x, point.y, -1.0f), center, rotation, scale, color, coreColor, vector4f);
        }

        //THDanmakuCraftMod.LOGGER.info("Quad:{}", (Object) quad);
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

    /**
     * 用相機參數自行組出 world -> view 的旋轉矩陣。
     *
     * <p>不直接使用 {@code RenderSystem.getModelViewMatrix()}：那個矩陣可能被上一個 pass
     * （例如手持物品的 bobView）或其它模組污染，導致建 quad 與 shader 實際拿到的矩陣不一致。</p>
     */
    public static Matrix4f cameraModelView() {
        Quaternionf rotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        return new Matrix4f().rotation(rotation.conjugate(new Quaternionf()));
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
                .shape(Shape.Ellipsoid.shapeId)
                .endVertex();
    }

    /** 寫一個 billboard 頂點：center + right*x + up*y。 */
        public static void vertex(VertexBuilder builder, Vector3f center, Vector3f right, Vector3f up,
                                  float x, float y, Vector3f rotation, Vector3f scale, Color color, Color coreColor, Vector4f vector4f, Shape shape) {
            builder.vertex(center.x + right.x * x + up.x * y,
                            center.y + right.y * x + up.y * y,
                            center.z + right.z * x + up.z * y)
                    .vertex(center)
                    .vertex(rotation)
                    .vertex(scale)
                    .color(color)
                    .color(coreColor)
                    .vec4(vector4f.x,vector4f.y,vector4f.z,vector4f.w)
                    .shape(shape.shapeId)
                    .endVertex();
        }
}
