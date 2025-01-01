package com.adrian.thDanmakuCraft.script.lua;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.laser.THCurvedLaser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.luaj.vm2.script.LuaScriptEngine;

import javax.script.ScriptException;

@Deprecated
public class LuaCore2 {

    private static LuaCore2 LUA = new LuaCore2();
    public final LuaScriptEngine scriptEngine;

    public LuaCore2() {
        this.scriptEngine = new LuaScriptEngine();
        this.putAPI();
    }

    public static void init(){
        LUA = new LuaCore2();
        core.doFile("main.lua");
    }

    public void putAPI(){
        try {
            this.bindClass("core",              LuaCore2.core.class);
            this.bindClass("Mth" ,              Mth.class);
            this.bindClass("Vec2" ,             Vec2.class);
            this.bindClass("Vec3" ,             Vec3.class);
            this.bindClass("THObject" ,         THObject.class);
            this.bindClass("THBullet" ,         THBullet.class);
            this.bindClass("THCurvedLaser" ,    THCurvedLaser.class);
        } catch (ScriptException e) {
            THDanmakuCraftCore.LOGGER.warn("Failed put api!", e);
        }
    }

    public void bindClass(String key, Class<?> Class) throws ScriptException {
        this.scriptEngine.eval(key + " = luajava.bindClass('"+Class.getName()+"');");
    }

    public static LuaCore2 getInstance(){
        return LUA;
    }

    public void loadScript(String path){
        try {
            String script = LuaLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"data/lua/"+path));
            this.scriptEngine.eval(script);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public static class core {
        public static void doFile(String path) {
            LuaCore2.getInstance().loadScript(path);
        }

        public static boolean isValid(Object object) {
            return object != null;
        }

        public static void info(String msg) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                THDanmakuCraftCore.LOGGER.info(msg);
            });
        }

        public static void warn(String msg) {
            THDanmakuCraftCore.LOGGER.warn(msg);
        }
    }
}
