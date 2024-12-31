package com.adrian.thDanmakuCraft.script.lua;

import com.adrian.thDanmakuCraft.script.ScriptManager;
import com.adrian.thDanmakuCraft.util.ResourceLoader;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import javax.swing.plaf.PanelUI;
import java.util.ArrayList;
import java.util.List;

public class LuaManager extends ScriptManager {

    private final Globals GLOBALS = LuaCore.getGlobals();

    public LuaManager(){
        super();
    }

    @Override
    public Object invokeScript(String functionName, Object... args) throws Exception{
        if (!this.hasScript() || !this.shouldExecuteScript){
            return null;
        }

        List<LuaValue> values = new ArrayList<>();
        for(Object arg:args){
            if(arg instanceof LuaValue luaValue){
                values.add(luaValue);
            }else {
                values.add(CoerceJavaToLua.coerce(arg));
            }
        }
        LuaFunction function = GLOBALS.load(this.script).call().get(functionName).checkfunction();
        return function.invoke(values.toArray(new LuaValue[0]));
        //return function.invoke(CoerceJavaToLua.coerce(args[0]));
    }

    @Override
    public Object invokeScript(String functionName, ResourceLoader.RunnableWithException whenException, Object... args){
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
