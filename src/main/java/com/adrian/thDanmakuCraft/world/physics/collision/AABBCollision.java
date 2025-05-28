package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class AABBCollision extends Collision{
    public AABBCollision(Vec3 center, Vector3f size) {
        this.update(center, size);
    }

    public AABBCollision(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.update(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AABBCollision(AABB aabb) {
        this.update(aabb.minX,aabb.minY,aabb.minZ,aabb.maxX,aabb.maxY,aabb.maxZ);
    }

    @Override
    public CollisionResult intersects(Collision other) {
        if(other instanceof AABBCollision) {
            return new CollisionResult(intersects((AABBCollision) other),null,0.0f);
        }
        return MISSED;
    }

    @Override
    public boolean intersectsLite(Collision other) {
        if(other instanceof AABBCollision) {
            return intersects((AABBCollision) other);
        }
        return false;
    }

    public CollisionResult intersectsAABB(AABBCollision other) {
        boolean intersects = this.intersects(other.minX(), other.minY(), other.minZ(), other.maxX(), other.maxY(), other.maxZ());
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
        return this.center.x()-this.size.x();
    }

    public double minY(){
        return this.center.y()-this.size.y();
    }

    public double minZ(){
        return this.center.z()-this.size.z();
    }

    public double maxX(){
        return this.center.x()+this.size.x();
    }

    public double maxY(){
        return this.center.y()+this.size.y();
    }

    public double maxZ(){
        return this.center.z()+this.size.z();
    }

    public static AABBCollision formAABB(AABB aabb) {
        return new AABBCollision(aabb);
    }

    public AABB toAABB(){
        return new AABB(this.minX(), this.minY(), this.minZ(), this.maxX(), this.maxY(), this.maxZ());
    }

    @Override
    @Deprecated
    public void update(Vec3 center, Vector3f axis, Vector3f size){}

    public void update(Vec3 center, Vector3f size){
        this.center = center;
        this.size = size;
    }

    public void update(double minX, double minY, double minZ, double maxX, double maxY, double maxZ){
        this.size = new Vec3((maxX-minX)/2, (maxY-minY)/2, (maxZ-minZ)/2).toVector3f();
        this.center = new Vec3(minX+size.x, minY+size.y, minZ+size.z);
    }
}
