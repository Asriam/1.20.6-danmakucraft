package com.adrian.thDanmakuCraft.util;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public class SerializeUtil {

    public byte[] serialize(Serializable serializable) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        serializable.encode(buffer);
        return buffer.array();
    }

    public <T extends Serializable> T deserialize(T newInstance, byte[] data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeBytes(data);
        newInstance.decode(buffer);
        return newInstance;
    }

    public static interface Serializable {
        public void encode(FriendlyByteBuf buffer);
        public void decode(FriendlyByteBuf buffer);
    }
}
