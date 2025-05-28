package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.Vec3;

public abstract class PositionCollision extends BaseCollision{

    protected Vec3 center;

    public PositionCollision(Vec3 center){
        this.center = center;
    }

    public void setCenter(Vec3 center){
        this.center = center;
    }

    public void move(Vec3 move) {
        this.center.add(move);
    }

    public Vec3 getCenter(){
        return center;
    }
}
