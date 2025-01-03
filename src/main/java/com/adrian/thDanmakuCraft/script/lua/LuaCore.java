package com.adrian.thDanmakuCraft.script.lua;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.Logger;

import static org.luaj.vm2.LuaValue.NIL;

public class LuaCore {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static LuaCore LUA = new LuaCore();
    private final Globals GLOBALS;

    public LuaCore() {
        GLOBALS = JsePlatform.standardGlobals();
        this.putAPI();
    }

    public static void init(){
        LUA = new LuaCore();
        String path = "main.lua";
        String script = LuaLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"data/lua/"+path));
        LuaCore.getInstance().GLOBALS.load(script,path).call();
    }

    public void putAPI(){
        try {
            GLOBALS.set("core",coreAPI());
            //this.bindClass("core",              LuaCore.core.class);
            this.bindClass("Mth" ,              Mth.class);
            //this.bindClass("Vec2" ,             Vec2.class);
            //this.bindClass("Vec3" ,             Vec3.class);
            //this.bindClass("THObject" ,         THObject.class);
            //this.bindClass("THBullet" ,         THBullet.class);
            //this.bindClass("THCurvedLaser" ,    THCurvedLaser.class);
            GLOBALS.set("luajava", NIL);
        } catch (Exception e) {
            LOGGER.warn("Failed put api!", e);
        }
    }

    public void bindClass(String key, Class<?> Class) throws Exception {
        //this.GLOBALS.load(key + " = luajava.bindClass('"+Class.getName()+"');").call();
        this.GLOBALS.set(key, CoerceJavaToLua.coerce(Class));
    }

    public static Globals getGlobals(){
        return LUA.GLOBALS;
    }

    public static LuaCore getInstance(){
        return LUA;
    }

    public LuaValue doScript(String path){
        try {
            String script = LuaLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"data/lua/"+path));
            return this.GLOBALS.load(script,path).call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NIL;
    }

    public LuaValue coreAPI(){
        LuaValue library = LuaValue.tableOf();
        library.set( "doFile", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                return core.doFile(luaValue.checkjstring());
            }
        });
        library.set( "isValid", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                return LuaValue.valueOf(!luaValue.isnil() || (luaValue.isuserdata() && core.isValid(luaValue.checkuserdata())));
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

        library.set("newVec3", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs varargs) {
                return CoerceJavaToLua.coerce(new Vec3(
                        varargs.arg(1).checkdouble(),
                        varargs.arg(2).checkdouble(),
                        varargs.arg(3).checkdouble()));
            }
        });

        library.set("newVec2", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs varargs) {
                return CoerceJavaToLua.coerce(new Vec2(
                        varargs.arg(1).tofloat(),
                        varargs.arg(2).tofloat()));
            }
        });

        return library;
    }

    public static class core {
        public static LuaValue doFile(String path) {
            return LuaCore.getInstance().doScript(path);
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
