package com.adrian.thDanmakuCraft.world.entity;

import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public class EntitySingleTHObject extends Entity implements IEntityAdditionalSpawnData {

    private Entity user;
    private Entity target;
    private final THObject object;

    public EntitySingleTHObject(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.object = null;
    }

    public EntitySingleTHObject(THObject object, Level world) {
        super(EntityInit.ENTITY_SINGLE_THOBJECT.get(),world);
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
}
