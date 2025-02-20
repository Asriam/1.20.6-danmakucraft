package com.adrian.thDanmakuCraft.world.danmaku.thobject;

import com.adrian.thDanmakuCraft.util.CollisionHelper;
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
