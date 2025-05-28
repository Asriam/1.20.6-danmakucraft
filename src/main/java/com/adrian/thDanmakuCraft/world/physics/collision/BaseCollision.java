package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class BaseCollision {

    public abstract CollisionResult intersects(BaseCollision other);

    public abstract boolean intersectsLite(BaseCollision other);

    public static final CollisionResult MISSED = new CollisionResult(false, new Vector3f(),0.0f);

    /// @param isColliding 是否碰撞
    /// @param collisionNormal 碰撞的方向 从A朝向B
    /// @param depth 碰撞点与表面的垂直距离
    public record CollisionResult(boolean isColliding, Vector3f collisionNormal, float depth){

        public boolean isColliding() {
            return isColliding;
        }
    }

    public static Matrix3f computeRotationMatrixFromEulerAngles(Vector3f eulerAngles){
        return new Matrix3f().rotate(new Quaternionf().rotateYXZ(eulerAngles.y, eulerAngles.x, eulerAngles.z));
    }
}
