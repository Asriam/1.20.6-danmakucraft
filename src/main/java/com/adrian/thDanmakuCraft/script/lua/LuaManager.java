package com.adrian.thDanmakuCraft.script.lua;

import com.adrian.thDanmakuCraft.script.ScriptManager;
import com.adrian.thDanmakuCraft.util.RunnableWithException;

public class LuaManager extends ScriptManager {

    LuaManager(){
        super();
    }

    @Override
    public Object invokeScript(String functionName, Object... args) throws Exception{
        return null;
    }

    @Override
    public Object invokeScript(String functionName, RunnableWithException whenException, Object... args){
        return null;
    }
}
