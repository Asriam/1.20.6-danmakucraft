package com.adrian.thDanmakuCraft.world;

import org.luaj.vm2.LuaValue;

public interface ILuaValue {

     static LuaValue setMeta(LuaValue luaValue){
        LuaValue meta = LuaValue.tableOf();
        meta.set("__index", luaValue);
        return meta;
    }

    default LuaValue ofLuaClass(){
        LuaValue library = LuaValue.tableOf();
        library.setmetatable(this.getMeta());
        return library;
    };

    LuaValue ofLuaValue();

    LuaValue getMeta();
}
