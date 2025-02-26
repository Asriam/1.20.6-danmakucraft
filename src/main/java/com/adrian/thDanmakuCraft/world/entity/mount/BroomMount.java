package com.adrian.thDanmakuCraft.world.entity.mount;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BroomMount extends Entity {
    public BroomMount(EntityType<? extends BroomMount> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_333664_) {

    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_20139_) {

    }
}
