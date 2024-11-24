package com.asrian.thDanmakuCraft.util.script;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;
import com.asrian.thDanmakuCraft.util.RunnableWithException;

import javax.script.Invocable;
import javax.script.ScriptEngine;

public class JavaScriptManager extends ScriptManager {

    private final ScriptEngine engine = THDanmakuCraftCore.getEngine();
    //private final ScriptEngine engine = new JavaScript().getEngine();

    public JavaScriptManager(){
        super();
    }

    @Override
    public Object invokeScript(String functionName, Object... args) throws Exception{
        engine.eval(this.script);
        Invocable invocable = (Invocable) engine;
        return invocable.invokeFunction(functionName, args);
    }

    @Override
    public Object invokeScript(String functionName, RunnableWithException whenException, Object... args){
        if (!this.hasScript() && !this.shouldExecuteScript)
            return null;
        try {
            THDanmakuCraftCore.LOGGER.info(engine.getContext().toString());
            return invokeScript(functionName,args);
        }catch (Exception e) {
            THDanmakuCraftCore.LOGGER.warn("Faild to invoke script:\n {}", this.script, e);
            whenException.run(e);
        }
        return null;
    }
}
