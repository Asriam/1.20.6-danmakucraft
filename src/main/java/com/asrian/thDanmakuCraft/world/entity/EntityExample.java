package com.asrian.thDanmakuCraft.world.entity;

import com.asrian.thDanmakuCraft.init.EntityInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityExample extends Entity{
    public static final EntityDataAccessor<Integer> INDEX = SynchedEntityData.defineId(EntityExample.class, EntityDataSerializers.INT);

    public EntityExample(EntityType<EntityExample> type, Level level) {
        super(type,level);
    }

    public EntityExample(Level level, Vec3 pos){
        this(EntityInit.EXAMPLE_ENTITY.get(),level);
        this.setPos(pos);
    }

    public int getIndex(){
        return this.entityData.get(INDEX);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        //super.addAdditionalSaveData();
        compoundTag.putInt("Index",this.getIndex());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(INDEX, 1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
       // entityData.set(INDEX, compoundTag.getInt("Index") != 0 ? compoundTag.getInt("Index"):this.entityData.get(INDEX));
        entityData.set(INDEX, compoundTag.getInt("Index"));
        //THDanmakuCraftCore.LOGGER.info("LOAD!!!!!!!!!!!!!!!!!!!!!");
        //entityData.set(INDEX, 2);
    }
}
