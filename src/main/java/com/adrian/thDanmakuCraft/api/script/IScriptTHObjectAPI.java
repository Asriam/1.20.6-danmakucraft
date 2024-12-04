package com.adrian.thDanmakuCraft.api.script;

import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public interface IScriptTHObjectAPI {

    void spawn();
    void setPosition(Vec3 vec3);
    void setPosition(double x, double y, double z);
    Vec3 getPosition();

    void setScale(Vector3f scale);
    void setSize(Vec3 size);

    void setVelocity(Vec3 velocity, boolean setRotation);
    void setVelocity(float speed, Vec3 direction, boolean setRotation);
    void setVelocity(float speed, Vec2 rotation, boolean isDeg, boolean setRotation);

    public void setAcceleration(Vec3 acceleration);
    public void setAcceleration(float acceleration, Vec3 direction);

    void setColor(int r, int g, int b, int a);
    THObject.Color getColor();

    int getTimer();
}
