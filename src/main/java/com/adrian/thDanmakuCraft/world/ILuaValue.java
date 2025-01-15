package com.adrian.thDanmakuCraft.world;

import org.luaj.vm2.LuaValue;

public interface ILuaValue {

    default LuaValue ofLuaClass(){
        LuaValue library = LuaValue.tableOf();
        library.setmetatable(this.getMeta());
        return library;
    };

    LuaValue ofLuaValue();

    LuaValue getMeta();
}
