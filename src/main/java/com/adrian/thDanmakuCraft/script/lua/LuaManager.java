package com.adrian.thDanmakuCraft.script.lua;

import com.adrian.thDanmakuCraft.script.ScriptManager;
import com.adrian.thDanmakuCraft.util.RunnableWithException;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

public class LuaManager extends ScriptManager {

    private final Globals GLOBALS = LuaCore.getGlobals();

    LuaManager(){
        super();
    }

    @Override
    public Object invokeScript(String functionName, Object... args) throws Exception{
        if (!this.hasScript() || !this.shouldExecuteScript){
            return null;
        }

        LuaValue[] values = new LuaValue[args.length];
        int index = 0;
        for(Object arg:args){
            values[index] = CoerceJavaToLua.coerce(arg);
            index++;
        }
        GLOBALS.load(this.script).get(functionName).checkfunction().invoke(values);
        return null;
    }

    @Override
    public Object invokeScript(String functionName, RunnableWithException whenException, Object... args){
        return null;
    }
}
