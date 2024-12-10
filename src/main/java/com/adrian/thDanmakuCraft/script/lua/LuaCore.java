package com.adrian.thDanmakuCraft.script.lua;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.entity.danmaku.laser.THCurvedLaser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.script.LuaScriptEngine;
import org.slf4j.Logger;

import javax.script.ScriptException;

import static org.luaj.vm2.LuaValue.tableOf;

public class LuaCore {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static LuaCore LUA = new LuaCore();
    public final Globals GLOBALS;

    public LuaCore() {
        GLOBALS = JsePlatform.standardGlobals();
        this.putAPI();
    }

    public static void init(){
        LUA = new LuaCore();
        core.doFile("main.lua");
    }

    public void putAPI(){
        try {
            GLOBALS.set("core",coreAPI());
            //this.bindClass("core",              LuaCore.core.class);
            this.bindClass("Mth" ,              Mth.class);
            this.bindClass("Vec2" ,             Vec2.class);
            this.bindClass("Vec3" ,             Vec3.class);
            this.bindClass("THObject" ,         THObject.class);
            this.bindClass("THBullet" ,         THBullet.class);
            this.bindClass("THCurvedLaser" ,    THCurvedLaser.class);
        } catch (Exception e) {
            LOGGER.warn("Failed put api!", e);
        }
    }

    public void bindClass(String key, Class<?> Class) throws Exception {
        //this.GLOBALS.load(key + " = luajava.bindClass('"+Class.getName()+"');").call();
        this.GLOBALS.set(key, CoerceJavaToLua.coerce(Class));
    }

    public static LuaCore getInstance(){
        return LUA;
    }

    public void loadScript(String path){
        try {
            String script = LuaLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"data/lua/"+path));
            this.GLOBALS.load(script,path).call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LuaValue coreAPI(){
        LuaValue library = LuaValue.tableOf();
        library.set( "doFile", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                core.doFile(luaValue.checkjstring());
                return LuaValue.NIL;
            }
        });
        library.set( "isValid", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                return LuaValue.valueOf(core.isValid(luaValue.checkuserdata()));
            }
        });
        library.set( "info", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                core.info(luaValue.checkjstring());
                return LuaValue.NIL;
            }
        });
        library.set( "warn", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                core.warn(luaValue.checkjstring());
                return LuaValue.NIL;
            }
        });

        return library;
    }

    public static class core {
        public static void doFile(String path) {
            LuaCore.getInstance().loadScript(path);
        }

        public static boolean isValid(Object object) {
            return object != null;
        }

        public static void info(String msg) {
            LOGGER.info(msg);
        }

        public static void warn(String msg) {
            LOGGER.warn(msg);
        }
    }
}
