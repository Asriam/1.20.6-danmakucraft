package com.adrian.thDanmakuCraft.script.lua;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.google.common.collect.Maps;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.luaj.vm2.LuaValue.NIL;

public class LuaCore {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static LuaCore LUA = new LuaCore();
    private final Globals GLOBALS;
    private final Map<String,LuaValue> luaClassMap = Maps.newHashMap();

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

        library.set("registerClass", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs varargs) {
                LuaValue clazz = LuaValue.tableOf();
                String className = varargs.arg(1).checkjstring();
                clazz.set("className", className);

                LuaValue parentClass = varargs.arg(2);
                if(isLuaClass(parentClass)){
                    clazz.set("super", parentClass);
                }else if(parentClass.isstring()){
                    clazz.set("super", LuaCore.this.getLuaClass(parentClass.checkjstring()));
                }
                LUA.luaClassMap.put(className, clazz);
                return clazz;
            }
        });

        library.set("getClass", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LuaValue clazz = LuaCore.this.getLuaClass(luaValue.checkjstring());
                if(clazz != null){
                    return clazz;
                }
                return LuaValue.NIL;
            }
        });

        return library;
    }

    public static boolean isLuaClass(LuaValue luaClass){
        if (luaClass.isnil()) {
            return false;
        }
        if (luaClass.istable()) {
            LuaValue className = luaClass.get("className");
            if (className.isnil()){
                return false;
            }else return className.isstring();
        }
        return false;
    }

    public LuaValue getLuaClass(String className){
        return this.luaClassMap.get(className);
    }

    public LuaValue getLuaClass(LuaValue luaClass){
        String className = luaClass.get("className").checkjstring();
        return this.luaClassMap.get(className);
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
