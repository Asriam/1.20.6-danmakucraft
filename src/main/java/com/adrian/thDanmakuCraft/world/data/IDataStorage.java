package com.adrian.thDanmakuCraft.world.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface IDataStorage extends ISerializable{

    void encode(FriendlyByteBuf buffer);

    void decode(FriendlyByteBuf buffer);

    CompoundTag save(CompoundTag compoundTag);

    void load(CompoundTag compoundTag);
}
