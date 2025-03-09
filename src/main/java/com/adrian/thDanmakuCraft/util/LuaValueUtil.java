package com.adrian.thDanmakuCraft.util;

import net.minecraft.network.FriendlyByteBuf;
import org.luaj.vm2.LuaValue;

public class LuaValueUtil {

    public static boolean isValid(LuaValue value){
        return value != null && !value.isnil();
    }

    public static boolean isNotValid(LuaValue value){
        return value == null || value.isnil();
    }
}
