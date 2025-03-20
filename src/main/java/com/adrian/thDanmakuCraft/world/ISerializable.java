package com.adrian.thDanmakuCraft.world;

import net.minecraft.network.FriendlyByteBuf;

public interface ISerializable{

    public void encode(FriendlyByteBuf buffer);
    public void decode(FriendlyByteBuf buffer);
}
