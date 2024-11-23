package com.asrian.thDanmakuCraft.util.script;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;

import javax.script.Invocable;
import javax.script.ScriptEngine;

public class JavaScriptManager extends AbstractScriptManager {

    private final ScriptEngine engine = THDanmakuCraftCore.getEngine();

    public JavaScriptManager(){

    }

    @Override
    public Object invokeScript(String functionName, Object... args) throws Exception{
        if (!this.hasScript())
            return null;
        engine.eval(this.script);
        Invocable invocable = (Invocable) engine;
        return invocable.invokeFunction(functionName, args);
    }

    public Object invokeScript(String functionName, Runnable whenException, Object... args){
        if (!this.hasScript())
            return null;
        try {
            return invokeScript(functionName,args);
        }catch (Exception e){
            THDanmakuCraftCore.LOGGER.warn("Faild to invoke script:\n {}",this.script,e);
            whenException.run();
        }
        return null;
    }
}
