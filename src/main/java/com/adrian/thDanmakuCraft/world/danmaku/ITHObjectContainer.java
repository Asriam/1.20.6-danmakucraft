package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.world.ILuaValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public interface ITHObjectContainer extends ILuaValue{
    Vec3 getPosition();

    void spawnTHObject(THObject thObject);

    THObjectManager getObjectManager();

    Level level();

    List<Entity> getEntitiesInBound();

    Entity getUser();

    Entity getHostEntity();

    AABB getContainerBound();

    <T extends THObject> T getObjectFromUUID(UUID uuid);
}
