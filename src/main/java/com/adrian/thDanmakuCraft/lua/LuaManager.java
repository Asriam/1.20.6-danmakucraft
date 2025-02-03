package com.adrian.thDanmakuCraft.lua;

import com.adrian.thDanmakuCraft.script.ScriptManager;
import com.adrian.thDanmakuCraft.util.ResourceLoader;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.ArrayList;
import java.util.List;

public class LuaManager extends ScriptManager<LuaValue> {

    private static final Globals GLOBALS = LuaCore.getGlobals();
    private LuaValue luaValue;

    public LuaManager(){
        super();
    }

    @Override
    public void setScript(String script){
        if(script != null && !script.isEmpty()){
            this.luaValue = GLOBALS.load(script).call();
        }
        this.script = script;
    };

    @Override
    public Object invokeScript(String functionName, LuaValue... args) throws Exception{
        return this.luaValue.get(functionName).checkfunction().invoke(args);
    }

    @Override
    public Object invokeScript(String functionName, ResourceLoader.RunnableWithException whenException, LuaValue... args){
        try{
            return this.invokeScript(functionName,args);
        } catch (Exception e) {
            whenException.run(e);
        }
        return null;
    }

    @Override
    public ScriptType type() {
        return ScriptType.LUA;
    }
}
