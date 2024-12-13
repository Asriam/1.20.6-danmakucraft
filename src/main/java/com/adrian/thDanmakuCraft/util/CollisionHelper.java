package com.adrian.thDanmakuCraft.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3d;
import org.joml.Vector3f;

public class CollisionHelper {

    // 檢查球與盒子的碰撞
    public static boolean isCollidingSphereBox(Vec3 sphereCenter, double sphereRadius, AABB box) {
        // 獲取球到盒子的最近點
        double nearestX = Mth.clamp(sphereCenter.x, box.minX, box.maxX);
        double nearestY = Mth.clamp(sphereCenter.y, box.minY, box.maxY);
        double nearestZ = Mth.clamp(sphereCenter.z, box.minZ, box.maxZ);

        // 計算球中心與最近點之間的距離平方
        double distanceSquared = distanceSquared(sphereCenter.x, sphereCenter.y, sphereCenter.z, nearestX, nearestY, nearestZ);
        // 比較距離平方 (避免使用 Math.sqrt, 提升性能)
        return distanceSquared <= (sphereRadius * sphereRadius);
    }

    public static boolean isCollidingEllipsoidBox(Vec3 ellipsoidCenter, Vec3 radii, AABB box) {
        // 縮放盒子內的每個維度到椭球的單位球，計算歸一化空間內的最近點
        double nearestX = Mth.clamp((box.minX - ellipsoidCenter.x) / radii.x, -1, 1) * radii.x + ellipsoidCenter.x;
        double nearestY = Mth.clamp((box.minY - ellipsoidCenter.y) / radii.y, -1, 1) * radii.y + ellipsoidCenter.y;
        double nearestZ = Mth.clamp((box.minZ - ellipsoidCenter.z) / radii.z, -1, 1) * radii.z + ellipsoidCenter.z;

        // 計算椭球中心與最近點之間的距離平方
        double distanceSquared = distanceSquared(ellipsoidCenter.x, ellipsoidCenter.y, ellipsoidCenter.z,
                nearestX, nearestY, nearestZ);
        // 當距離小於等於椭球的 1（縮放單位），則視為碰撞
        return distanceSquared <= 1.0;
    }

    public static boolean isCollidingOrientedEllipsoidBox(Vec3 ellipsoidCenter, Vec3 radii, Vector3f rotationAngles, AABB box) {
        // 创建旋转矩阵
        Matrix3x3d rotationMatrix = createRotationMatrix(rotationAngles);
        /*
        // 遍历盒子的所有顶点，检测是否与椭球碰撞
        Vec3[] boxVertices = getVertices(box);
        for (Vec3 vertex : boxVertices) {
            // 全局点转换为椭球局部点
            Vec3 localPoint = transformToLocal(vertex, ellipsoidCenter, rotationMatrix);

            // 检查局部空间中的点是否在椭球内
            if (isPointInUnitEllipsoid(localPoint, radii)) {
                return true; // 碰撞
            }
        }
         */
        // 如果没有顶点在椭球内，检查椭球中心到盒子最近点
        return checkClosestPointOnBoxToEllipsoid(ellipsoidCenter, radii, rotationMatrix, box);
    }

    // 创建旋转矩阵 (基于 x, y, z 的欧拉角)
    private static Matrix3x3d createRotationMatrix(Vector3f angles) {
        float cosX = Mth.cos(angles.x), sinX = Mth.sin(angles.x);
        float cosY = Mth.cos(angles.y), sinY = Mth.sin(angles.y);
        float cosZ = Mth.cos(angles.z), sinZ = Mth.sin(angles.z);

        Matrix3x3d rx = new Matrix3x3d(
                1, 0, 0 ,
                0, cosX, -sinX ,
                0, sinX, cosX
        );

        Matrix3x3d ry = new Matrix3x3d(
                 cosY, 0, sinY ,
                 0, 1, 0 ,
                 -sinY, 0, cosY
        );
        Matrix3x3d rz = new Matrix3x3d(
                 cosZ, -sinZ, 0 ,
                 sinZ, cosZ, 0 ,
                 0, 0, 1 );

        return rz.multiply(rx).multiply(ry); // Rz * Rx * Ry
    }

    // 将全局点转换为椭球局部空间
    private static Vec3 transformToLocal(Vec3 point, Vec3 center, Matrix3x3d rotationMatrix) {
        Vec3 globalToLocal = point.subtract(center); // 转换到以椭球中心为原点
        return rotationMatrix.multiply(globalToLocal); // 应用旋转矩阵的转置
    }

    // 检查点是否在单位椭球内
    private static boolean isPointInUnitEllipsoid(Vec3 localPoint, Vec3 radii) {
        double x = localPoint.x / radii.x;
        double y = localPoint.y / radii.y;
        double z = localPoint.z / radii.z;
        return (x * x + y * y + z * z) <= 1.0;
    }

    // 检查盒子上最接近椭球的点是否在椭球内部
    private static boolean checkClosestPointOnBoxToEllipsoid(Vec3 ellipsoidCenter, Vec3 radii, Matrix3x3d rotationMatrix, AABB box) {
        Vec3 closestPoint = getClosestPoint(box,ellipsoidCenter); // 获取盒子上最近点
        Vec3 localClosestPoint = transformToLocal(closestPoint, ellipsoidCenter, rotationMatrix);
        return isPointInUnitEllipsoid(localClosestPoint, radii);
    }

    private static double distanceSquared(double x1, double y1, double z1,
                                          double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return dx * dx + dy * dy + dz * dz;
    }

    public static Vec3[] getVertices(AABB aabb) {
        return new Vec3[] {
                new Vec3(aabb.minX, aabb.minY, aabb.minZ), new Vec3(aabb.maxX, aabb.minY, aabb.minZ),
                new Vec3(aabb.minX, aabb.maxY, aabb.minZ), new Vec3(aabb.maxX, aabb.maxY, aabb.minZ),
                new Vec3(aabb.minX, aabb.minY, aabb.maxZ), new Vec3(aabb.maxX, aabb.minY, aabb.maxZ),
                new Vec3(aabb.minX, aabb.maxY, aabb.maxZ), new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ)
        };
    }

    public static Vec3 getClosestPoint(AABB aabb,Vec3 center) {
        double closestX = Mth.clamp(center.x, aabb.minX, aabb.maxX);
        double closestY = Mth.clamp(center.y, aabb.minY, aabb.maxY);
        double closestZ = Mth.clamp(center.z, aabb.minZ, aabb.maxZ);
        return new Vec3(closestX, closestY, closestZ);
    }
}