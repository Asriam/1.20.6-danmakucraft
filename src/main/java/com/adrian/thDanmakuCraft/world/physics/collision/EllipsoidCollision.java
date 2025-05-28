package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class EllipsoidCollision extends RotatableCollision {
    public EllipsoidCollision(Vec3 center, Vector3f axis, Vector3f size) {
        super(center, axis, size);
    }

    @Override
    public CollisionResult intersects(BaseCollision other) {
        return MISSED;
    }

    @Override
    public boolean intersectsLite(BaseCollision other) {
        return false;
    }

    public CollisionResult intersectWithEllipsoid(EllipsoidCollision other){
        return MISSED;
    }

    public CollisionResult intersectWithOBB(OBBCollision other) {
        return MISSED;
    }

    public CollisionResult intersectWithAABB(AABBCollision other) {
        return MISSED;
    }
}
