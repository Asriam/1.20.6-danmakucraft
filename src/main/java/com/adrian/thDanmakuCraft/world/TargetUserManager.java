package com.adrian.thDanmakuCraft.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class TargetUserManager implements IDataStorage{
    private @Nullable Entity user,    target;
    private @Nullable UUID userUUID,targetUUID;
    private final Level level;

    public TargetUserManager(Level level, Entity user, Entity target){
        this.level = level;
        this.user = user;
        this.target = target;
        this.userUUID = user.getUUID();
        this.targetUUID = target.getUUID();
    }

    public TargetUserManager(Level level){
        this.level = level;
    }

    public TargetUserManager(THObjectContainer container) {
        this(container.level());
    }

    public void setUser(Entity user){
        if(user == null){
            this.user = null;
            return;
        }
        this.user = user;
        this.userUUID = user.getUUID();
    }

    public void setTarget(Entity target){
        if(target == null){
            this.target = null;
            return;
        }
        this.target = target;
        this.targetUUID = target.getUUID();
    }

    public Entity getEntityFromUUID(UUID uuid){
        ServerLevel serverLevel = (ServerLevel) this.level;
        return serverLevel.getEntity(uuid);
    }

    @Nullable
    public Entity safeGetUser(){
        if(this.user == null && this.userUUID != null){
            ServerLevel serverLevel = (ServerLevel) this.level;
            this.user = serverLevel.getEntity(this.userUUID);
            return this.user;
        }
        return this.user;
    }

    @Nullable
    public Entity safeGetTarget(){
        if(this.target == null && this.targetUUID != null){
            ServerLevel serverLevel = (ServerLevel) this.level;
            this.target = serverLevel.getEntity(this.targetUUID);
            return this.target;
        }
        return this.target;
    }

    public Entity setUserFromUUID(UUID uuid){
        this.user = this.getEntityFromUUID(uuid);
        return this.user;
    }

    public Entity setTargetFromUUID(UUID uuid){
        this.target = this.getEntityFromUUID(uuid);
        return this.target;
    }

    @Nullable
    public Entity unsafeGetUser(){
        return this.user;
    }

    @Nullable
    public Entity unsafeGetTarget(){
        return this.target;
    }

    public void writeData(FriendlyByteBuf buffer){
        buffer.writeVarInt(this.user   != null ? user.getId() : 0);
        buffer.writeVarInt(this.target != null ? target.getId() : 0);
    }

    public void readData(FriendlyByteBuf buffer){
        this.setUser(this.level.getEntity(buffer.readVarInt()));
        this.setTarget(this.level.getEntity(buffer.readVarInt()));
    }

    public CompoundTag save(CompoundTag compoundTag){
        String user = this.user != null ? this.user.getUUID().toString() : "";
        String target = this.target != null ? this.target.getUUID().toString() : "";
        compoundTag.putString("UserUUID",user);
        compoundTag.putString("TargetUUID",target);
        return compoundTag;
    }

    public void load(CompoundTag compoundTag){
        String userUUID = compoundTag.getString("UserUUID");
        String targetUUID = compoundTag.getString("TargetUUID");
        this.userUUID = userUUID.isEmpty() ? null : UUID.fromString(userUUID);
        this.targetUUID = targetUUID.isEmpty() ? null : UUID.fromString(targetUUID);
    }

    public void loadUserAndTarget(Level level){
        if(this.user == null && this.userUUID != null) {
            ServerLevel serverLevel = (ServerLevel) level;
            this.user = serverLevel.getEntity(this.userUUID);
        }

        if(this.target == null && this.targetUUID != null) {
            ServerLevel serverLevel = (ServerLevel) level;
            this.target = serverLevel.getEntity(this.targetUUID);
        }
    }


}
