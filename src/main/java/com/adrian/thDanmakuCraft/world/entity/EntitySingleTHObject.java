package com.adrian.thDanmakuCraft.world.entity;

import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

import java.util.List;

public class EntitySingleTHObject extends Entity implements ITHObjectContainer , IEntityAdditionalSpawnData {

    protected final EntityTHObjectContainer.TargetUserManager targetUserManager;
    protected THObject object;

    public EntitySingleTHObject(EntityType<?> type, Level level) {
        super(type, level);
        this.targetUserManager = new EntityTHObjectContainer.TargetUserManager(level);
        this.object = null;
    }

    public EntitySingleTHObject(THObject object, Level level) {
        this(EntityInit.ENTITY_SINGLE_THOBJECT.get(),level);
        this.object = object;
    }

    public THObject getObject() {
        return object;
    }

    @Override
    public void tick(){
        this.object.onTick();
        this.setPos(this.object.getPosition());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_333664_) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {

    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {

    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {

    }

    @Override
    public Level getLevel() {
        return this.level();
    }

    @Override
    public RandomSource getRandomSource() {
        return this.random;
    }

    @Override
    public Vec3 getPosition() {
        return this.position();
    }

    @Override
    public THObjectManager getObjectManager() {
        return null;
    }

    @Override
    public AABB getAabb() {
        return null;
    }

    @Override
    public Entity getUser() {
        return this.targetUserManager.safeGetUser();
    }

    @Override
    public Entity getTarget() {
        return this.targetUserManager.safeGetTarget();
    }

    @Override
    public List<Entity> getEntitiesInBound() {
        return List.of();
    }

    @Override
    public Entity getThis() {
        return null;
    }
}
