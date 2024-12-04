package com.adrian.thDanmakuCraft.script.js;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.script.ScriptManager;
import com.adrian.thDanmakuCraft.util.RunnableWithException;

import javax.script.Invocable;
import javax.script.ScriptEngine;

public class JSManager extends ScriptManager {

    private final ScriptEngine engine = JSCore.getEngine();

    public JSManager(){
        super();
    }

    @Override
    public Object invokeScript(String functionName, Object... args) throws Exception{
        //engine.setContext(emptyContext);
        engine.eval(this.script);
        Invocable invocable = (Invocable) engine;
        return invocable.invokeFunction(functionName, args);
    }

    @Override
    public Object invokeScript(String functionName, RunnableWithException whenException, Object... args){
        if (!this.hasScript() && !this.shouldExecuteScript)
            return null;
        try {
            return invokeScript(functionName,args);
        }catch (Exception e) {
            THDanmakuCraftCore.LOGGER.warn("Faild to invoke script:\n {}", this.script, e);
            whenException.run(e);
            e.printStackTrace();
        }
        return null;
    }
}
