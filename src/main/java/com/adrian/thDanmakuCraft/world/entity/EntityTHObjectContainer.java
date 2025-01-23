package com.adrian.thDanmakuCraft.world.entity;

import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public class EntityTHObjectContainer extends Entity implements IEntityAdditionalSpawnData {
    //private static final EntityDataAccessor<THObjectContainer> DATA_THOBJECT_CONTAINER = SynchedEntityData.defineId(EntityTHObjectContainer.class, MyEntityDataSerializers.THOBJECT_CONTAINER);

    private final THObjectContainer container;
    public EntityTHObjectContainer(EntityType<?> type, Level level) {
        super(type, level);
        this.container = new THObjectContainer(this);
    }

    public EntityTHObjectContainer(Level level, Vec3 pos) {
        this(EntityInit.ENTITY_THOBJECT_CONTAINER.get(),level);
        this.setPos(pos);
    }

    public EntityTHObjectContainer(String luaClass, Level level, Vec3 pos) {
        super(EntityInit.ENTITY_THOBJECT_CONTAINER.get(),level);
        this.container = new THObjectContainer(this,luaClass);
        this.setPos(pos);
    }

    public THObjectContainer getContainer() {
        return this.container;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        //builder.define(DATA_THOBJECT_CONTAINER, new THObjectContainer(this));
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.container.onAddToWorld();
    }

    @Override
    public void tick(){
        this.container.tick();
        this.setBoundingBox(this.container.getAabb());

    }

    public EntityDimensions getDimensions(Pose p_19975_) {
        //return new EntityDimensions((float) this.container.aabb.getXsize(), (float) this.container.aabb.getYsize(), 0.0F, null, false);
        return null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.container.load(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.container.save(tag);
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
