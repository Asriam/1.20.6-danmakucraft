package com.adrian.thDanmakuCraft.util;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GZIPUtil {

    public static void compress(FriendlyByteBuf buffer, Encoder encoder){
        ByteBufOutputStream bos = new ByteBufOutputStream(buffer);
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.buffer());
            encoder.encode(tempBuffer);
            gzip.write(tempBuffer.array());
        } catch (IOException e) {
            throw new RuntimeException("Compressing failed!", e);
        }
    }

    interface Encoder{
        void encode(FriendlyByteBuf buf);
    }

    interface Decoder{
        void decode(FriendlyByteBuf buf);
    }
}
