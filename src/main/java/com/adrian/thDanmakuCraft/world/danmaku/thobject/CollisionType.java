package com.adrian.thDanmakuCraft.world.danmaku.thobject;

import com.adrian.thDanmakuCraft.util.math.CollisionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public enum CollisionType {
    AABB(CollisionType::AABB),
    SPHERE(CollisionType::SPHERE),
    ELLIPSOID(CollisionType::Ellipsoid),
    CUBOID(CollisionType::CUBOID),
    ;

    private final CollisionFactory factory;

    CollisionType(CollisionFactory factory) {
        this.factory = factory;
    }

    public void collisionEntity(THObject object, Entity entity, Runnable whenColling) {
        if (this.factory.collision(object.getPosition(), object.getSize(), object.getRotation(), entity.getBoundingBox())) {
            whenColling.run();
        }
    }

    public boolean collision(THObject object, net.minecraft.world.phys.AABB aabb) {
        return this.factory.collision(object.getPosition(), object.getSize(), object.getRotation(), aabb);
    }

    public boolean collisionEntity(THObject object, Entity entity) {
        return this.factory.collision(object.getPosition(), object.getSize(), object.getRotation(), entity.getBoundingBox());
    }

    public boolean collisionBlock(THObject object, BlockPos pos) {
        return this.factory.collision(object.getPosition(), object.getSize(), object.getRotation(),
                new AABB(pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
    }

    /**
     * 不建立 Vec3 / Vector3f / AABB 的命中判定（彈幕每 tick、每個實體都會呼叫）。
     *
     * <p>語意與 {@link #collision(THObject, AABB)} 一致，但：
     * <ul>
     *   <li>中心與尺寸以純量傳入，呼叫端可每 tick 只取一次</li>
     *   <li>AABB / CUBOID 用「中心 ± 尺寸/2」判定（原本 {@code new AABB(center, size)} 會把
     *       size 當成第二個角點，實際上是反向的退化盒，這裡一併修正）</li>
     * </ul>
     */
    public boolean collision(double cx, double cy, double cz,
                             double sx, double sy, double sz,
                             float rx, float ry, float rz,
                             double minX, double minY, double minZ,
                             double maxX, double maxY, double maxZ) {
        switch (this) {
            case AABB:
            case CUBOID:
                return CollisionHelper.isCollidingAABB(cx, cy, cz, sx * 0.5, sy * 0.5, sz * 0.5,
                        minX, minY, minZ, maxX, maxY, maxZ);
            case SPHERE:
                return CollisionHelper.isCollidingSphereBox(cx, cy, cz, sx,
                        minX, minY, minZ, maxX, maxY, maxZ);
            case ELLIPSOID:
            default:
                return CollisionHelper.isCollidingOrientedEllipsoidBox(cx, cy, cz, sx, sy, sz,
                        rx, ry, rz, minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    /** {@link #collisionBlock(THObject, BlockPos)} 的無配置版本。 */
    public boolean collisionBlock(double cx, double cy, double cz,
                                  double sx, double sy, double sz,
                                  float rx, float ry, float rz,
                                  int bx, int by, int bz) {
        return collision(cx, cy, cz, sx, sy, sz, rx, ry, rz,
                bx, by, bz, bx + 1.0, by + 1.0, bz + 1.0);
    }

    public static boolean AABB(Vec3 center, Vec3 size, Vector3f rotation, AABB aabb) {
        return CollisionHelper.isCollidingAABB(center, size, aabb);
    }

    public static boolean SPHERE(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb) {
        return CollisionHelper.isCollidingSphereBox(center, scale.x, aabb);
    }

    public static boolean Ellipsoid(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb) {
        return CollisionHelper.isCollidingOrientedEllipsoidBox(center, scale, rotation, aabb);
    }

    public static boolean CUBOID(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb) {
        return CollisionHelper.isCollidingAABB(center, scale, aabb);
    }

    public
    interface CollisionFactory {
        boolean collision(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb);
    }
}
