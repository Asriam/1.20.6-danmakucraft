package com.adrian.thDanmakuCraft.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class MathUtil {

    public static Vec3 getClosestPointOnSegment(Vec3 pos1, Vec3 pos2, Vec3 camaraPos) {
        // 計算線段方向向量
        Vec3 D = pos2.subtract(pos1);

        // 計算攝像機到線段起點的向量
        Vec3 V = camaraPos.subtract(pos1);

        // 計算 t
        double t = V.dot(D) / D.dot(D);

        // 限制 t 在 [0, 1] 範圍內
        t = Math.max(0, Math.min(1, t));

        // 計算最近點
        return pos1.add(D.scale(t));
    }
}
