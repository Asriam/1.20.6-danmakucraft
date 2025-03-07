package com.adrian.thDanmakuCraft.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.function.IntFunction;

public class FriendlyByteBufUtil {

    public static void writeVec3(FriendlyByteBuf byteBuf, Vec3 vec3){
        byteBuf.writeDouble(vec3.x);
        byteBuf.writeDouble(vec3.y);
        byteBuf.writeDouble(vec3.z);
    }

    public static Vec3 readVec3(FriendlyByteBuf byteBuf){
        return new Vec3(
                byteBuf.readDouble(),
                byteBuf.readDouble(),
                byteBuf.readDouble()
        );
    }

    public static void writeColor(FriendlyByteBuf byteBuf,Color color){
        byteBuf.writeInt(color.r);
        byteBuf.writeInt(color.g);
        byteBuf.writeInt(color.b);
        byteBuf.writeInt(color.a);
    }

    public static Color readColor(FriendlyByteBuf byteBuf){
        return new Color(
                byteBuf.readInt(),
                byteBuf.readInt(),
                byteBuf.readInt(),
                byteBuf.readInt()
        );
    }

    public static <T> void writeCollection(FriendlyByteBuf byteBuf, Collection<T> collection, StreamEncoder<? super FriendlyByteBuf, T> encoder){
        byteBuf.writeVarInt(collection.size());

        for (T t : collection) {
            encoder.encode(byteBuf, t);
        }
    }

    public static <T, C extends Collection<T>> C readCollection(FriendlyByteBuf byteBuf, IntFunction<C> intFunction, StreamDecoder<? super FriendlyByteBuf, T> decoder){
        int i = byteBuf.readVarInt();
        C c = (C)intFunction.apply(i);

        for (int j = 0; j < i; j++) {
            c.add(decoder.decode(byteBuf));
        }

        return c;
    }
}
