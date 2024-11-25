package com.adrian.thDanmakuCraft;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.util.JSLoader;
import net.minecraft.resources.ResourceLocation;
import org.luaj.vm2.script.LuaScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class LuaCore {
    private static LuaCore LUA;

    public static final String ENGINE_NAME = new ResourceLocation(THDanmakuCraftCore.MODID,"lua").toString();
    private final ScriptEngine scriptEngine;

    public LuaCore() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngineManager.registerEngineName(ENGINE_NAME,new LuaScriptEngineFactory());
        this.scriptEngine = scriptEngineManager.getEngineByName(ENGINE_NAME);
        this.putAPI();
    }

    public static void init(){
        LUA = new LuaCore();
    }

    public static final ScriptEngine getEngine(){
        return LUA.scriptEngine;
    }

    public final void putAPI(){
        try {
            this.scriptEngine.eval(JSLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MODID,"data/lua/api.lua")));
        } catch (Exception e) {
            THDanmakuCraftCore.LOGGER.warn("Failed reading api.",e);
            e.printStackTrace();
        }
    }
}
