package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class RotatableCollision extends PositionCollision {

    protected Matrix3f rotateMatrix;
    protected Vector3f size; ///半长

    public RotatableCollision(Vec3 center, Vector3f axis, Vector3f size) {
        super(center);
        this.update(center, axis, size);
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

    public Vector3f getSize(){
        return size;
    }

    public void rorate(Vector3f angles){
        this.rotateMatrix.rotateYXZ(angles.y, angles.x, angles.z);
    }

    public void setRotation(Vector3f angles){
        this.rotateMatrix = new Matrix3f().rotateYXZ(angles.y, angles.x, angles.z);
    }

    public void setRotateMatrix(Matrix3f rotateMatrix){
        this.rotateMatrix = rotateMatrix;
    }

    public Matrix3f getRotateMatrix(){
        return this.rotateMatrix;
    }

    public void update(Vec3 center, Vector3f angles, Vector3f size){
        this.center = center;
        this.setRotation(angles);
        this.size = size;
    }

    public void update(Vec3 center, Matrix3f rotateMatrix, Vector3f size){
        this.center = center;
        this.rotateMatrix = rotateMatrix;
        this.size = size;
    }
}
