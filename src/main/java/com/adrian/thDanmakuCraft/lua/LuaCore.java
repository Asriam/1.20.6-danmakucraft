package com.adrian.thDanmakuCraft.lua;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.compress.utils.Lists;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static org.luaj.vm2.LuaValue.NIL;

public class LuaCore {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static LuaCore LUA = new LuaCore();
    private final Globals GLOBALS = JsePlatform.standardGlobals();
    private final Map<String,LuaValue> luaClassMap = Maps.newHashMap();
    //private final Map<String, LuaFunction> luaFunctionMap = Maps.newHashMap();
    private final Map<String,LuaValue> metaTableMap = Maps.newHashMap();
    public final List<String> spellCardClassKeys = Lists.newArrayList();

    public static LuaValue LuaUtilVec2;
    public static LuaValue LuaUtilVec3;
    public static LuaValue LuaUtilVec2Meta;
    public static LuaValue LuaUtilVec3Meta;

    private final LuaLoader luaLoader;

    public LuaCore() {
        //GLOBALS = JsePlatform.standardGlobals();
        this.putAPI();
        THObjectContainer.scriptEventCache.clear();
        luaLoader = LuaLoader.instance;
    }

    public static void init(){
        LUA = new LuaCore();
        String path = "main.lua";
        String script = LUA.luaLoader.getResourceAsString(getFileLocation(path));
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

    public static ResourceLocation getFileLocation(String path){
        return ResourceLocationUtil.thdanmakucraft("lua/"+path);
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
            String script = luaLoader.getResourceAsString(getFileLocation(path));
            return this.GLOBALS.load(script,path).call();
        } catch (Exception e) {
            THDanmakuCraftMod.LOGGER.error("Failed loading lua file! the file path is not valid!",e);
        }
        return NIL;
    }

    public void doFolder(String path){
        luaLoader.loadAllResourcesInFolderAsString(getFileLocation(path)).forEach((path1, resource) -> {
            this.GLOBALS.load(resource,path).call();
        });
    }

    public LuaValue defineClass(Varargs varargs){
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
            String className = "class$$"+(luaClassMap.size()+1);
            className = superClass.isnil() ? className : superClass.get("className").checkjstring() + "#" + className;
            clazz.set("className", className);
            setSuperClass(clazz,superClass);
            LUA.luaClassMap.put(className, clazz);
        }
        return clazz;
    }

    public LuaValue coreAPI(){
        LuaValue library = LuaValue.tableOf();
        /// Fields
        library.set("mod_id", THDanmakuCraftMod.MOD_ID);
        LuaTable thobjectTypes = LuaValue.tableOf();
        thobjectTypes.set("thobject", LuaValue.userdataOf(THObjectInit.TH_OBJECT.get()));
        thobjectTypes.set("bullet", LuaValue.userdataOf(THObjectInit.TH_BULLET.get()));
        thobjectTypes.set("laser", LuaValue.userdataOf(THObjectInit.TH_LASER.get()));
        thobjectTypes.set("curvy_laser", LuaValue.userdataOf(THObjectInit.TH_CURVY_LASER.get()));
        library.set("thobject_types", thobjectTypes);
        /// Functions
        library.set( "doFile", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                return core.doFile(luaValue.checkjstring());
            }
        });
        library.set( "doAllFilesInFolder", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                core.doFolder(luaValue.checkjstring());
                return LuaValue.NIL;
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
                    String className = "class$$"+(LUA.luaClassMap.size()+1);
                    className = superClass.isnil() ? className : superClass.get("className").checkjstring() + "#" + className;
                    clazz.set("className", className);
                    setSuperClass(clazz,superClass);
                    LUA.luaClassMap.put(className, clazz);
                }
                return clazz;
            }
        });
        library.set("defineSpellCardClass", new VarArgFunction() {
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
                    LUA.spellCardClassKeys.add(className);
                }else {
                    LuaValue superClass = checkLuaClass(varargs.arg(1));
                    String className = "class$$"+(LUA.luaClassMap.size()+1);
                    className = superClass.isnil() ? className : superClass.get("className").checkjstring() + "#" + className;
                    clazz.set("className", className);
                    setSuperClass(clazz,superClass);
                    LUA.luaClassMap.put(className, clazz);
                    LUA.spellCardClassKeys.add(className);
                }
                return clazz;
            }
        });
        library.set("registerMetaTable",new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs varargs) {
                LuaValue arg1 = varargs.arg(1);
                LuaValue arg2 = varargs.arg(2);
                if (arg1.isstring()){
                    String keyName = arg1.checkjstring();
                    LuaValue meta = arg2;
                    metaTableMap.put(keyName,meta);
                    meta.set("metatable_key", LuaValue.valueOf(keyName));
                }else if (arg1.istable()){
                    String keyName = "metatble$$"+metaTableMap.size()+1;
                    LuaValue meta = arg1;
                    metaTableMap.put(keyName,meta);
                    meta.set("metatable_key", LuaValue.valueOf(keyName));
                }
                return LuaValue.NIL;
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

        library.set("setupVec3Lib", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue vec3Lib, LuaValue metatable) {
                LuaCore.LuaUtilVec3 = vec3Lib;
                LuaCore.LuaUtilVec3Meta = metatable;
                return null;
            }
        });

        library.set("setupVec2Lib", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue vec2Lib, LuaValue metatable) {
                LuaCore.LuaUtilVec2 = vec2Lib;
                LuaCore.LuaUtilVec2Meta = metatable;
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
        //if (this.luaClassMap.containsKey(className)) {
        return this.luaClassMap.get(className);
        //}
        //throw new RuntimeException("There is no such class! class name:" + className);
    }

    public LuaValue getLuaClass(LuaValue luaClass){
        String className = luaClass.get("className").checkjstring();
        return this.luaClassMap.get(className);
    }

    public boolean containsMetaTable(LuaValue metaTable){
        return this.metaTableMap.containsValue(metaTable);
    }

    public boolean containsMetaTableKey(String key){
        return this.metaTableMap.containsKey(key);
    }

    public LuaValue getMetaTable(String key){
        return this.metaTableMap.get(key);
    }

    public static class core {
        public static LuaValue doFile(String path) {
            return LuaCore.getInstance().doFile(path);
        }

        public static void doFolder(String path) {
            LuaCore.getInstance().doFolder(path);
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
