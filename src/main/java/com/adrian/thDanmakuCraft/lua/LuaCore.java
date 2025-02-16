package com.adrian.thDanmakuCraft.lua;

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

import static org.luaj.vm2.LuaValue.NIL;

public class LuaCore {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static LuaCore LUA = new LuaCore();
    private final Globals GLOBALS = JsePlatform.standardGlobals();
    private final Map<String,LuaValue> luaClassMap = Maps.newHashMap();

    public static LuaValue LuaUtilVec2;
    public static LuaValue LuaUtilVec3;
    public static LuaValue LuaUtilVec2Meta;
    public static LuaValue LuaUtilVec3Meta;

    private final LuaLoader luaLoader;

    public LuaCore() {
        //GLOBALS = JsePlatform.standardGlobals();
        this.putAPI();
        luaLoader = LuaLoader.instance;
    }

    public static void init(){
        LUA = new LuaCore();
        String path = "main.lua";
        String script = LUA.luaLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"lua/"+path));
        LuaCore luaCore = LuaCore.getInstance();
        luaCore.GLOBALS.load(script,path).call();
    }

    public void putAPI(){
        try {
            GLOBALS.set("core",coreAPI());
            GLOBALS.set("Mth", mthAPI());
            //this.bindClass("Mth" ,              Mth.class);
            //this.bindClass("Vec2" ,             Vec2.class);
            //this.bindClass("Vec3" ,             Vec3.class);
            //this.bindClass("THObject" ,         THObject.class);
            //this.bindClass("THBullet" ,         THBullet.class);
            //this.bindClass("THCurvedLaser" ,    THCurvedLaser.class);
            //GLOBALS.set("THObjectContainer", THObjectContainer.meta.get("__index"));
            //GLOBALS.set("THObject", THObject.meta.get("__index"));
            //GLOBALS.set("THBullet", THBullet.meta.get("__index"));

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

    public LuaValue doFile(String path){
        try {
            String script = luaLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"lua/"+path));
            return this.GLOBALS.load(script,path).call();
        } catch (Exception e) {
            //e.printStackTrace();
            THDanmakuCraftCore.LOGGER.error("Failed doing lua file!",e);
        }
        return NIL;
    }

    public LuaValue coreAPI(){
        LuaValue library = LuaValue.tableOf();
        library.set("mod_id", THDanmakuCraftCore.MOD_ID);
        library.set( "doFile", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                return core.doFile(luaValue.checkjstring());
            }
        });
        library.set( "isValid", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                return LuaValue.valueOf(!luaValue.isnil() && (luaValue.isuserdata() && core.isValid(luaValue.checkuserdata())));
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

        library.set("vec3", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs varargs) {
                LuaValue arg1 = varargs.arg(1);
                return CoerceJavaToLua.coerce(new Vec3(
                        arg1.checkdouble(),
                        varargs.arg(2).checkdouble(),
                        varargs.arg(3).checkdouble()));
            }
        });

        library.set("vec2", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs varargs) {
                return CoerceJavaToLua.coerce(new Vec2(
                        varargs.arg(1).tofloat(),
                        varargs.arg(2).tofloat()));
            }
        });

        library.set("defineClass", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs varargs) {
                LuaValue clazz = LuaValue.tableOf();
                LuaValue arg1 = varargs.arg(1);
                if (arg1.isstring()) {
                    LuaValue superClass = checkLuaClass(varargs.arg(2));
                    String className = arg1.checkjstring();
                    className = superClass.isnil() ? className : superClass.get("className").checkjstring() + "#" + className;
                    clazz.set("className", className);
                    setSuperClass(clazz,superClass);
                    LUA.luaClassMap.put(className, clazz);
                }else {
                    LuaValue superClass = checkLuaClass(varargs.arg(1));
                    String className = "class$"+(luaClassMap.size()+1);
                    className = superClass.isnil() ? className : superClass.get("className").checkjstring() + "#" + className;
                    clazz.set("className", className);
                    setSuperClass(clazz,superClass);
                    LUA.luaClassMap.put(className, clazz);
                    System.out.print(className+"\n");
                }
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

        library.set("setupVec3Lib", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LuaCore.LuaUtilVec3 = luaValue;
                LuaValue meta = LuaValue.tableOf();
                meta.set("__index", luaValue);
                LuaCore.LuaUtilVec3Meta = meta;
                return null;
            }
        });

        library.set("setupVec2Lib", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LuaCore.LuaUtilVec2 = luaValue;
                LuaValue meta = LuaValue.tableOf();
                meta.set("__index", luaValue);
                LuaCore.LuaUtilVec2Meta = meta;
                return null;
            }
        });

        return library;
    }

    public static void setSuperClass(LuaValue clazz,LuaValue superClass){
        if (superClass != LuaValue.NIL) {
            LuaValue meta = LuaValue.tableOf();
            meta.set("__index",superClass);
            clazz.setmetatable(meta);
            clazz.set("super", superClass);
        }
    }

    public LuaValue mthAPI(){
        LuaValue library = LuaValue.tableOf();
        library.set("sin", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                return LuaValue.valueOf(Mth.sin(luaValue.tofloat()));
            }
        });
        library.set("cos", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                return LuaValue.valueOf(Mth.cos(luaValue.tofloat()));
            }
        });
        library.set("sqrt", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                return LuaValue.valueOf(Mth.sqrt(luaValue.tofloat()));
            }
        });
        library.set("lerp", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                return LuaValue.valueOf(Mth.lerp(
                        varargs.arg(1).checkdouble(),
                        varargs.arg(2).checkdouble(),
                        varargs.arg(3).checkdouble()
                ));
            }
        });
        library.set("DEG_TO_RAD",Mth.DEG_TO_RAD);
        library.set("RAD_TO_DEG",Mth.RAD_TO_DEG);
        library.set("PI",Mth.PI);
        return library;
    }
    public static LuaValue checkLuaClass(LuaValue value){
        if (isLuaClass(value)) {
            return value;
        } else if (value.isstring()) {
            return LuaCore.getInstance().getLuaClass(value.checkjstring());
        }
        return NIL;
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
            return LuaCore.getInstance().doFile(path);
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
