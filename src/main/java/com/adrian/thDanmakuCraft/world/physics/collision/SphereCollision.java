package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class SphereCollision extends Collision{
    public SphereCollision(Vec3 center, Vector3f axis, Vector3f size) {
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

    public CollisionResult intersectOBB(OBBCollision other) {
        return MISSED;
    }

    public CollisionResult intersectAABB(AABBCollision other) {
        return MISSED;
    }

}
