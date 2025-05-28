package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class RotatableCollision extends PositionCollision {

    protected Vector3f eulerAngles;
    protected Vector3f size; ///半长

    public RotatableCollision(Vec3 center, Vector3f axis, Vector3f size) {
        super(center);
        this.update(center, axis, size);
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

    public Vector3f getEulerAngles(){
        return eulerAngles;
    }

    public Vector3f getSize(){
        return size;
    }
}
