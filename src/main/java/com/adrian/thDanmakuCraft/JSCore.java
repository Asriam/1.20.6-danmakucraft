package com.adrian.thDanmakuCraft;

import com.adrian.thDanmakuCraft.util.JSLoader;
import net.minecraft.resources.ResourceLocation;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JSCore {
    private static JSCore JAVASCRIPT;

    public static final String ENGINE_NAME = new ResourceLocation(THDanmakuCraftCore.MODID,"js").toString();
    private final ScriptEngine scriptEngine;

    public JSCore() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngineManager.registerEngineName(ENGINE_NAME,new NashornScriptEngineFactory());
        this.scriptEngine = scriptEngineManager.getEngineByName(ENGINE_NAME);
        this.putAPI();
    }

    public static void init(){
        JAVASCRIPT = new JSCore();
    }

    public static final ScriptEngine getEngine(){
        return JAVASCRIPT.scriptEngine;
    }

    public final void putAPI(){
        try {
            this.scriptEngine.eval(JSLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MODID,"data/js/api.js")));
        } catch (Exception e) {
            THDanmakuCraftCore.LOGGER.warn("Failed reading api.",e);
            e.printStackTrace();
        }
    }
}
