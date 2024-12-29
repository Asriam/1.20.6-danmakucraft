package com.adrian.thDanmakuCraft.world.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface ITHObjectContainer {

    Level getLevel  ();

    RandomSource getRandomSource();

    Vec3 getPosition();

    THObjectManager getObjectManager();

    AABB getAabb();

    Entity getUser();

    Entity getTarget();

    List<Entity> getEntitiesInBound();

    void remove(Entity.RemovalReason removalReason);

    Entity getThis();

    DamageSources damageSources();

    boolean isRemoved();
}
