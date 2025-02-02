package com.adrian.thDanmakuCraft.api.script;

import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public interface IScriptTHObjectAPI {

    <T extends THObject> T spawn();
    void setPosition(Vec3 vec3);
    void setPosition(double x, double y, double z);
    Vec3 getPosition();

    void setScale(Vector3f scale);
    void setSize(Vec3 size);

    void setVelocity(Vec3 velocity, boolean setRotation);
    void setVelocityFromDirection(float speed, Vec3 direction, boolean setRotation);
    void setVelocityFromRotation(float speed, Vec2 rotation, boolean isDeg, boolean setRotation);

    void setAcceleration(Vec3 acceleration);
    void setAccelerationFromDirection(float acceleration, Vec3 direction);
    void setAccelerationFromRotation(float acceleration, Vec2 rotation, boolean isDeg);

    void setColor(int r, int g, int b, int a);
    Color getColor();

    int getTimer();
}
