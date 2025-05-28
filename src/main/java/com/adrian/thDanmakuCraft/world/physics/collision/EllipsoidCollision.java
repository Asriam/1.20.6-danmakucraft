package com.adrian.thDanmakuCraft.world.physics.collision;

import com.adrian.thDanmakuCraft.util.CollisionHelper;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class EllipsoidCollision extends Collision{
    public EllipsoidCollision(Vec3 center, Vector3f axis, Vector3f size) {
        super(center, axis, size);
    }

    @Override
    public CollisionResult intersects(Collision other) {
        return MISSED;
    }

    @Override
    public boolean intersectsLite(Collision other) {
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
