package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.Vec3;

public class PointCollision extends PositionCollision {

    public PointCollision(Vec3 center) {
        super(center);
    }

    @Override
    public CollisionResult intersects(BaseCollision other) {
        return null;
    }

    @Override
    public boolean intersectsLite(BaseCollision other) {
        return false;
    }
}
