package com.adrian.thDanmakuCraft.world;

import org.luaj.vm2.LuaValue;

public interface ILuaValue {

    LuaValue ofLuaClass();

    LuaValue ofLuaValue();
}
