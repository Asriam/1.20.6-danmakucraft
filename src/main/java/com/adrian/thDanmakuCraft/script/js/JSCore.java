package com.adrian.thDanmakuCraft.script.js;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.danmaku.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.laser.THCurvedLaser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@Deprecated
public class JSCore {
    private static JSCore JAVASCRIPT;

    public static final String ENGINE_NAME = new ResourceLocation(THDanmakuCraftCore.MOD_ID,"js").toString();
    private final ScriptEngine scriptEngine;

    public JSCore() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngineManager.registerEngineName(ENGINE_NAME,new NashornScriptEngineFactory());
        this.scriptEngine = scriptEngineManager.getEngineByName(ENGINE_NAME);
        this.putAPI();
    }

    public static void init(){
        JAVASCRIPT = new JSCore();
        core.doFile("main.js");
    }

    public static ScriptEngine getEngine(){
        return JAVASCRIPT.scriptEngine;
    }

    public void loadScript(String path){
        try {
            String script = JSLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"data/js/"+path));
            this.scriptEngine.eval(script);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public void bindClass(String key, Class<?> Class) throws Exception{
        this.scriptEngine.eval("var "+key + " = Java.type('"+Class.getName()+"');");
    }

    public void putAPI(){
        try {
            this.bindClass("core",          JSCore.core.class);
            this.bindClass("Mth",           Mth.class);
            this.bindClass("Vec2",          Vec2.class);
            this.bindClass("Vec3",          Vec3.class);
            this.bindClass("THObject",      THObject.class);
            this.bindClass("THBullet",      THBullet.class);
            this.bindClass("THCurvedLaser", THCurvedLaser.class);
            this.scriptEngine.put("Java", null);
            //this.scriptEngine.eval(JSLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MODID,"data/js/api.js")));
        } catch (Exception e) {
            THDanmakuCraftCore.LOGGER.warn("Failed reading api.",e);
            e.printStackTrace();
        }
    }


    public static class core {
        public static void doFile(String path) {
            JAVASCRIPT.loadScript(path);
        }

        public static void info(String msg) {
            THDanmakuCraftCore.LOGGER.info(msg);
        }

        public static void warn(String msg) {
            THDanmakuCraftCore.LOGGER.warn(msg);
        }
    }
}
