package com.adrian.thDanmakuCraft.script.lua;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.entity.danmaku.laser.THCurvedLaser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.luaj.vm2.script.LuaScriptEngine;

import javax.script.ScriptException;


public class LuaCore {

    public static LuaCore LUA = new LuaCore();

    public final LuaScriptEngine scriptEngine;
    //public final Globals GLOBALS;

    public LuaCore() {
        /*
        GLOBALS = JsePlatform.standardGlobals();
        LuaValue chunk = loadScript("main.lua");
        this.bindClass("core",              LuaCore.class);
        this.bindClass("Mth" ,              Mth.class);
        this.bindClass("Vec2" ,             Vec2.class);
        this.bindClass("Vec3" ,             Vec3.class);
        this.bindClass("THObject" ,         THObject.class);
        this.bindClass("THBullet" ,         THBullet.class);
        this.bindClass("THCurvedLaser" ,    THCurvedLaser.class);
        chunk.call();
        */

        this.scriptEngine = new LuaScriptEngine();
        this.putAPI();
    }

    public static void init(){
        LUA = new LuaCore();
        core.doFile("main.lua");
    }

    public void putAPI(){
        try {
            this.scriptEngine.eval("print('go fuck yourself!!!!!!!!!!!!!!!!!!!!!!!!!!')");
            this.bindClass("core",              LuaCore.core.class);
            this.bindClass("Mth" ,              Mth.class);
            this.bindClass("Vec2" ,             Vec2.class);
            this.bindClass("Vec3" ,             Vec3.class);
            this.bindClass("THObject" ,         THObject.class);
            this.bindClass("THBullet" ,         THBullet.class);
            this.bindClass("THCurvedLaser" ,    THCurvedLaser.class);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public void bindClass(String key, Class<?> Class) throws ScriptException {
        //GLOBALS.load(key + " = luajava.bindClass('"  + Class.getName()+"')\n").call();
        this.scriptEngine.eval(key + " = luajava.bindClass('"+Class.getName()+"');");
    }

    public static LuaCore getLUA(){
        return LUA;
    }

    /*
    public void loadAllScript(){
        LuaLoader.getResourceMap().forEach(((resourceLocation, script) -> {
            GLOBALS.load(script,resourceLocation.getPath());
        }));
    }*/

    public void loadScript(String path){
        try {
            String script = LuaLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MODID,"data/lua/"+path));
            this.scriptEngine.eval(script);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public static class core {
        public static void doFile(String path) {
            LUA.loadScript(path);
        }

        public static boolean isValid(Object object) {
            return object != null;
        }

        public static void info(String msg) {
            THDanmakuCraftCore.LOGGER.info(msg);
        }

        public static void warn(String msg) {
            THDanmakuCraftCore.LOGGER.warn(msg);
        }
    }
}
