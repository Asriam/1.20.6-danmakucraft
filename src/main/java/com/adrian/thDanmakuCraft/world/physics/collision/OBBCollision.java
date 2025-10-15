package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class OBBCollision extends RotatableCollision {
    
    Matrix3f rotateMatrix;

    public OBBCollision(Vec3 center, Vector3f eulerAngles, Vector3f size) {
        super(center, eulerAngles, size);
        this.rotateMatrix = computeRotationMatrixFromEulerAngles(eulerAngles);
    }

    @Override
    public CollisionResult intersects(BaseCollision other) {
        if(other instanceof OBBCollision){

        }
        return MISSED;
    }

    @Override
    public boolean intersectsLite(BaseCollision other) {
        return false;
    }

    public CollisionResult intersectOBB(OBBCollision other) {
        // TODO
        return MISSED;
    }

    public CollisionResult intersectAABB(AABBCollision other) {
        // TODO
        return MISSED;
    }
}
