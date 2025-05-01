package com.adrian.thDanmakuCraft.world.physics.collision;

public class PointCollision extends Collision{
    @Override
    public CollisionResult intersects(Collision other) {
        return null;
    }

    @Override
    public boolean intersectsLite(Collision other) {
        return false;
    }
}
