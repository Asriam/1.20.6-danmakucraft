package com.adrian.thDanmakuCraft.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

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
}
