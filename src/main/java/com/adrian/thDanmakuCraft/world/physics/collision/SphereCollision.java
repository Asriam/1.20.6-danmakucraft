package com.adrian.thDanmakuCraft.world.physics.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class SphereCollision extends Collision{
    protected float radius;
    
    public SphereCollision(Vec3 center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public CollisionResult intersects(Collision other) {
        return MISSED;
    }

    @Override
    public boolean intersectsLite(Collision other) {
        return false;
    }

    public CollisionResult intersectWithPoint(PointCollision other) {
        Vec3 center = other.center;
        double distanceSquared = this.center.distanceToSqr(center);
        if (distanceSquared <= radius * radius) {
            Vector3f collisionNormal = this.center.vectorTo(center).normalize().toVector3f();
            return new CollisionResult(true, collisionNormal, 0.0f);
        }
        return MISSED;
    }
    
    public CollisionResult intersectWithSphere(SphereCollision other) {
        Vec3 thisCenter = this.center;
        Vec3 otherCenter = other.center;
        double distance = thisCenter.distanceTo(otherCenter);
        double radiusSum = this.radius + other.radius;

        if (distance <= radiusSum) {
            double collisionDepth = radiusSum - distance;
            Vector3f collisionNormal = thisCenter.vectorTo(otherCenter).normalize().toVector3f();
            return new CollisionResult(true, collisionNormal, (float) collisionDepth);
        }

        return MISSED;
    }

    public CollisionResult intersectWithEllipsoid(EllipsoidCollision other) {
        // Extract ellipsoid properties
        Vec3 ellipsoidCenter = other.getCenter();
        Vector3f ellipsoidRadii = other.getSize();
        Vector3f eulerAngles = other.getEulerAngles();

        // Compute rotation matrix from Euler angles
        Matrix3f rotationMatrix = computeRotationMatrixFromEulerAngles(eulerAngles);

        // Transform sphere center into ellipsoid's local space
        Vec3 sphereCenterLocal = this.center.subtract(ellipsoidCenter);
        Vector3f transformedSphereCenter = rotationMatrix.transform(sphereCenterLocal.toVector3f());

        // Normalize the transformed sphere center by ellipsoid radii
        double x = transformedSphereCenter.x() / ellipsoidRadii.x();
        double y = transformedSphereCenter.y() / ellipsoidRadii.y();
        double z = transformedSphereCenter.z() / ellipsoidRadii.z();

        // Check if the point lies inside the ellipsoid
        double distanceSquared = x * x + y * y + z * z;
        if (distanceSquared <= 1.0) {
            Vector3f collisionNormal = transformedSphereCenter.normalize();
            return new CollisionResult(true, collisionNormal, 0.0f);
        }

        return MISSED;
    }
    
    public CollisionResult intersectWithOBB(OBBCollision other) {
        return MISSED;
    }

    public CollisionResult intersectWithAABB(AABBCollision other) {
        return MISSED;
    }

}
