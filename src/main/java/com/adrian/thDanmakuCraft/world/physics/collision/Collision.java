package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class Collision {

    protected Vec3 center;
    protected Vector3f eulerAngles;
    protected Vector3f size; ///半长

    protected Collision(){}

    public Collision(Vec3 center, Vector3f axis, Vector3f size) {
        this.update(center, axis, size);
    }

    public void setCenter(Vec3 center){
        this.center = center;
    }

    public void move(Vec3 move){
        this.center.add(move);
    }

    public void rotate(Vector3f rotate){
        this.eulerAngles.add(rotate);
    }

    public void setRotation(Vector3f rotate){
        this.eulerAngles = rotate;
    }

    public void scale(float scale){
        this.size.mul(scale);
    }

    public void scale(Vector3f scale){
        this.size.mul(scale);
    }

    public void setSize(Vector3f size){
        this.size = size;
    }

    public void update(Vec3 center, Vector3f axis, Vector3f size){
        this.center = center;
        this.eulerAngles = axis;
        this.size = size;
    }

    public Matrix4f getTransformationMatrix() {
        Matrix4f transformationMatrix = new Matrix4f();
        transformationMatrix.rotate(new Quaternionf().rotateYXZ(eulerAngles.y, eulerAngles.x, eulerAngles.z)).scale(size);
        return transformationMatrix;
    }

    public abstract CollisionResult intersects(Collision other);

    public abstract boolean intersectsLite(Collision other);

    public static final CollisionResult MISSED = new CollisionResult(false, new Vector3f(),0.0f);

    /// @param isColliding 是否碰撞
    /// @param collisionNormal 碰撞的方向 从A朝向B
    /// @param depth 碰撞点与表面的垂直距离
    public record CollisionResult(boolean isColliding, Vector3f collisionNormal, float depth){

        public boolean isColliding() {
            return isColliding;
        }
    }

    public Vec3 getCenter(){
        return center;
    }

    public Vector3f getEulerAngles(){
        return eulerAngles;
    }

    public Vector3f getSize(){
        return size;
    }

    public static Matrix3f computeRotationMatrixFromEulerAngles(Vector3f eulerAngles){
        return new Matrix3f().rotateYXZ(eulerAngles.y, eulerAngles.x, eulerAngles.z);
    }
}
