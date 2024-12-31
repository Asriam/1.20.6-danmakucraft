package com.adrian.thDanmakuCraft.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface IDataStorage {

    void writeData(FriendlyByteBuf buffer);

    void readData(FriendlyByteBuf buffer);

    CompoundTag save(CompoundTag compoundTag);

    void load(CompoundTag compoundTag);
}
