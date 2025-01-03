package com.adrian.thDanmakuCraft.world.entity;

import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.world.THObjectContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public class EntityTHObjectContainer extends Entity implements IEntityAdditionalSpawnData {
    private final THObjectContainer container;
    public EntityTHObjectContainer(EntityType<?> type, Level level) {
        super(type, level);
        this.container = new THObjectContainer(this);
    }

    public EntityTHObjectContainer(Level level, Vec3 pos) {
        this(EntityInit.ENTITY_THOBJECT_CONTAINER.get(),level);
        this.setPos(pos);
    }

    public THObjectContainer getContainer() {
        return this.container;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.container.onAddToWorld();
    }

    @Override
    public void tick(){
        this.container.tick();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.container.readAdditionalSaveData(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.container.addAdditionalSaveData(tag);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        this.container.writeSpawnData(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.container.readSpawnData(buffer);
    }
}
