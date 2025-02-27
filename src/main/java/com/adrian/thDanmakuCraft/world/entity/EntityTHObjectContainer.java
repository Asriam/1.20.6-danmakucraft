package com.adrian.thDanmakuCraft.world.entity;

import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.NotNull;

public class EntityTHObjectContainer extends Entity implements IEntityAdditionalSpawnData {
    //private static final EntityDataAccessor<THObjectContainer> DATA_THOBJECT_CONTAINER = SynchedEntityData.defineId(EntityTHObjectContainer.class, MyEntityDataSerializers.THOBJECT_CONTAINER);

    private final THObjectContainer container;
    public EntityTHObjectContainer(EntityType<?> type, Level level) {
        super(type, level);
        this.container = new THObjectContainer(this);
        this.setBoundingBox(this.getContainer().getContainerBound());
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
        //builder.define(DATA_THOBJECT_CONTAINER, new THObjectContainer(this));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {

    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.getContainer().onAddToWorld();
    }

    @Override
    public void tick(){
        this.getContainer().tick();
        //this.setBoundingBox(this.getContainer().getContainerBound());
    }

    @NotNull
    public Level getLevel() {
        return this.level();
    }
    /*
    @Override
    public final EntityDimensions getDimensions(Pose pose) {
        //return EntityType.SLIME.getDimensions().scale(100.0f);
        return super.getDimensions(pose);
    }*/

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.getContainer().load(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.getContainer().save(tag);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        this.container.encode(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.container.decode(buffer);
    }
}
