package com.adrian.thDanmakuCraft.world.entity;

import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.world.TargetUserManager;
import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public class EntitySingleTHObject extends Entity implements IEntityAdditionalSpawnData {

    protected final TargetUserManager targetUserManager;
    protected THObject object;

    public EntitySingleTHObject(EntityType<?> type, Level level) {
        super(type, level);
        this.targetUserManager = new TargetUserManager(level);
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.object.save(tag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.object.load(tag);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        this.object.writeData(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.object.readData(additionalData);
    }
}
