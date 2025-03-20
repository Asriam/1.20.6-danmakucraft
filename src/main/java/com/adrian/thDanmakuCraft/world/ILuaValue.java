package com.adrian.thDanmakuCraft.world;

import org.luaj.vm2.LuaValue;

public interface ILuaValue {

     static LuaValue setMeta(LuaValue luaValue){
        LuaValue meta = LuaValue.tableOf();
        meta.set("__index", luaValue);
        return meta;
    }

    static LuaValue setConstantMeta(LuaValue luaValue){
        LuaValue meta = setMeta(luaValue);
        meta.set("__index", luaValue);
        return meta;
    }

    static String getLuaClassName(LuaValue luaValue){
        if(luaValue.isstring()){
            return luaValue.checkjstring();
        }else if(luaValue.istable()){
            return luaValue.get("className").checkjstring();
        }
        return "";
    }

    default LuaValue ofLuaClass(){
        LuaValue library = LuaValue.tableOf();
        library.setmetatable(this.getMeta());
        return library;
    };

    LuaValue ofLuaValue();

    LuaValue getMeta();
}
