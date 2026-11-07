package com.adrian.thDanmakuCraft.lua;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.util.io.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.danmaku.LuaEventCache;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;
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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.luaj.vm2.LuaValue.NIL;

public class LuaCore {

    private static final Logger LOGGER = LogUtils.getLogger();
    /** 使用者腳本的路徑前綴（避免與內建 lua 資源同名衝突）。 */
    public static final String USER_SCRIPT_PREFIX = "scripts/";
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
        // 常數機制要在任何腳本執行前安裝（__index / __newindex）
        LuaConstants.install(this.GLOBALS);
        this.putAPI();
        LuaEventCache.scriptEventCache.clear();
        luaLoader = LuaLoader.instance;
    }

    public static void init(){
        LUA = new LuaCore();
        LuaCore luaCore = LuaCore.getInstance();
        String MainPath = "main.lua";
        String AfterLoadingPath = "after_loading_libs.lua";
        luaCore.loadScript(LUA.luaLoader.getResourceAsString(getFileLocation(MainPath)), MainPath).call();
        luaCore.loadScript(LUA.luaLoader.getResourceAsString(getFileLocation(AfterLoadingPath)), AfterLoadingPath).call();
        luaCore.runUserMainScript();
    }

    /** 編輯器腳本根目錄：{@code config/thdanmakucraft/scripts}。 */
    public static Path userScriptsDir() {
        return FMLPaths.CONFIGDIR.get().resolve(THDanmakuCraftMod.MOD_ID).resolve("scripts");
    }

    /**
     * 執行編輯器根目錄的 {@code main.lua}（使用者可編輯）。
     *
     * <p>檔案不存在就跳過；腳本內容有錯只記錄，不會讓遊戲崩潰。</p>
     */
    public void runUserMainScript() {
        Path file = userScriptsDir().resolve("main.lua");
        if (!Files.isRegularFile(file)) {
            LOGGER.info("No user main script at {}", file);
            return;
        }
        try {
            String source = Files.readString(file, StandardCharsets.UTF_8);
            this.loadScript(source, file.toString()).call();
            LOGGER.info("Ran user main script {}", file);
        } catch (Throwable error) {
            LOGGER.error("Failed to run user main script {}", file, error);
        }
    }

    public void putAPI(){
        try {
            GLOBALS.set("core",coreAPI());
            GLOBALS.set("Mth", mthAPI());
            // constantize：constantize(name, value) / constantize(table) / constantize(value)
            GLOBALS.set("constantize", new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    return LuaConstants.constantize(args);
                }
            });
            GLOBALS.set("isConstant", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue name) {
                    return LuaValue.valueOf(LuaConstants.isConstant(name.checkjstring()));
                }
            });
            GLOBALS.set("constantizeGlobal", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue name) {
                    LuaConstants.lockGlobal(name.checkjstring());
                    return LuaValue.TRUE;
                }
            });
            // 以下兩個由 LuaConstantGuard 改寫後的程式碼呼叫，一般不直接使用
            GLOBALS.set("__lua_const_guard__", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue name) {
                    LuaConstants.guard(name.checkjstring());
                    return LuaValue.NIL;
                }
            });
            GLOBALS.set("__lua_const_register__", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue name) {
                    LuaConstants.registerSourceConstant(name.checkjstring());
                    return LuaValue.NIL;
                }
            });
            GLOBALS.set("luajava", NIL);
        } catch (Exception e) {
            LOGGER.warn("Failed put api!", e);
        }
    }

    /** 載入腳本前先套用常數改寫（constantize）。 */
    public LuaValue loadScript(String source, String chunkName) {
        return this.GLOBALS.load(LuaConstantGuard.transform(source), chunkName);
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
            String script = null;
            if (path.startsWith(USER_SCRIPT_PREFIX)) {
                // scripts/xxx：只找使用者腳本目錄，不會碰到內建 lua 資源
                String relative = path.substring(USER_SCRIPT_PREFIX.length());
                Path userFile = userScriptsDir().resolve(relative);
                if (Files.isRegularFile(userFile)) {
                    script = Files.readString(userFile, StandardCharsets.UTF_8);
                    LOGGER.info("Loading user lua script {}", userFile);
                }
            } else {
                script = luaLoader.getResourceAsString(getFileLocation(path));
                if (script == null) {
                    // 相容舊寫法：沒有前綴時，內建找不到才回退到使用者腳本目錄
                    Path userFile = userScriptsDir().resolve(path);
                    if (Files.isRegularFile(userFile)) {
                        script = Files.readString(userFile, StandardCharsets.UTF_8);
                        LOGGER.info("Loading user lua script {} (考慮加上 scripts/ 前綴)", userFile);
                    }
                }
            }
            if (script == null) {
                THDanmakuCraftMod.LOGGER.error("Lua file not found: {}", path);
                return NIL;
            }
            return this.loadScript(script, path).call();
        } catch (Exception e) {
            THDanmakuCraftMod.LOGGER.error("Failed loading lua file! the file path is not valid!",e);
        }
        return NIL;
    }

    public void doFolder(String path){
        boolean userFolder = path.startsWith(USER_SCRIPT_PREFIX);
        if (!userFolder) {
            luaLoader.loadAllResourcesInFolderAsString(getFileLocation(path)).forEach((path1, resource) -> {
                this.loadScript(resource, path).call();
            });
        }
        String relative = userFolder ? path.substring(USER_SCRIPT_PREFIX.length()) : path;
        // 執行編輯器腳本目錄下的 .lua（僅限這個資料夾，不再遞迴）
        Path userDir = userScriptsDir().resolve(relative);
        if (Files.isDirectory(userDir)) {
            try (var stream = Files.list(userDir)) {
                stream.filter(Files::isRegularFile)
                        .filter(file -> file.getFileName().toString().endsWith(".lua"))
                        .sorted()
                        .forEach(file -> {
                            try {
                                this.loadScript(Files.readString(file, StandardCharsets.UTF_8),
                                        file.toString()).call();
                            } catch (Throwable error) {
                                THDanmakuCraftMod.LOGGER.error("Failed loading lua file {}", file, error);
                            }
                        });
            } catch (Exception error) {
                THDanmakuCraftMod.LOGGER.error("Failed listing lua folder {}", userDir, error);
            }
        }
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

        // 全域函式：constantize("NAME", value) / constantize("NAME") / constantize(table)
        library.set("constantize", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return LuaConstants.constantize(args);
            }
        });
        library.set("isConstant", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue name) {
                return LuaValue.valueOf(LuaConstants.isConstant(name.checkjstring()));
            }
        });
        LuaConstants.constantize(library);
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
        LuaConstants.constantize(library);
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
