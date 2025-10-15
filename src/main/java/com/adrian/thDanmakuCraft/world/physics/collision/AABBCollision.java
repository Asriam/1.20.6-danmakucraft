package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class AABBCollision extends BaseCollision {
    protected double maxX, maxY, maxZ, minX, minY, minZ;

    public AABBCollision(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.update(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AABBCollision(AABB aabb) {
        this(aabb.minX,aabb.minY,aabb.minZ,aabb.maxX,aabb.maxY,aabb.maxZ);
    }

    @Override
    public CollisionResult intersects(BaseCollision other) {
        if(other instanceof AABBCollision) {
            return new CollisionResult(intersects((AABBCollision) other),null,0.0f);
        }
        return MISSED;
    }

    @Override
    public boolean intersectsLite(BaseCollision other) {
        if(other instanceof AABBCollision) {
            return intersects((AABBCollision) other);
        }
        return false;
    }

    public CollisionResult intersectsWithAABB(AABBCollision other) {
        
        return MISSED;
    }

    public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.minX() < maxX
                && this.maxX() > minX
                && this.minY() < maxY
                && this.maxY() > minY
                && this.minZ() < maxZ
                && this.maxZ() > minZ;
    }

    public boolean intersects(AABBCollision other){
        return this.intersects(other.minX(), other.minY(), other.minZ(), other.maxX(), other.maxY(), other.maxZ());
    }

    public double minX(){
        return this.minX;
    }

    public double minY(){
        return this.minY;
    }

    public double minZ(){
        return this.minZ;
    }

    public double maxX(){
        return this.maxX;
    }

    public double maxY(){
        return this.maxY;
    }

    public double maxZ(){
        return this.maxZ;
    }

    public Vec3 getMax(){
        return new Vec3(this.maxX(),this.maxY(),this.maxZ());
    }

    public Vec3 getMin(){
        return new Vec3(this.minX(),this.minY(),this.minZ());
    }

    public static AABBCollision formAABB(AABB aabb) {
        return new AABBCollision(aabb);
    }

    public AABB toAABB(){
        return new AABB(this.minX(), this.minY(), this.minZ(), this.maxX(), this.maxY(), this.maxZ());
    }

    public void update(double minX, double minY, double minZ, double maxX, double maxY, double maxZ){
        /*this.size = new Vec3((maxX-minX)/2, (maxY-minY)/2, (maxZ-minZ)/2).toVector3f();
        this.center = new Vec3(minX+size.x, minY+size.y, minZ+size.z);*/
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }
}
