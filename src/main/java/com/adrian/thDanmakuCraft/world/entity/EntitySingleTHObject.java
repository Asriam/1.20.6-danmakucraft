package com.adrian.thDanmakuCraft.world.entity;

import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.network.syncher.MyEntityDataSerializers;
import com.adrian.thDanmakuCraft.world.danmaku.ITHObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectManager;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.luaj.vm2.LuaValue;

import java.util.List;
import java.util.UUID;

public class EntitySingleTHObject extends Entity implements IEntityAdditionalSpawnData, ITHObjectContainer {

    //private static final EntityDataAccessor<THObject> DATA_THOBJECT_CONTAINER = SynchedEntityData.defineId(EntitySingleTHObject.class, MyEntityDataSerializers.getTHObjectContainerSerializer());
    protected THObject object;

    public EntitySingleTHObject(EntityType<?> type, Level level) {
        super(type, level);
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
        buffer.writeResourceLocation(this.object.getType().getKey());
        this.object.encode(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.object = THObjectType.getValue(buffer.readResourceLocation()).create(this);
        this.object.decode(buffer);
    }

    @Override
    public Vec3 getPosition() {
        return null;
    }

    @Override
    public void spawnTHObject(THObject thObject) {

    }

    @Override
    public THObjectManager getObjectManager() {
        return null;
    }

    @Override
    public List<Entity> getEntitiesInBound() {
        return List.of();
    }

    @Override
    public Entity getUser() {
        return null;
    }

    @Override
    public Entity getHostEntity() {
        return this;
    }

    @Override
    public AABB getContainerBound() {
        return null;
    }

    @Override
    public <T extends THObject> T getObjectFromUUID(UUID uuid) {
        return null;
    }

    @Override
    public LuaValue ofLuaValue() {
        return null;
    }

    @Override
    public LuaValue getMeta() {
        return null;
    }
}
