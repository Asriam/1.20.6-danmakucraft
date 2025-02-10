package com.adrian.thDanmakuCraft.world;

import com.adrian.thDanmakuCraft.lua.LuaCore;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.OneArgFunction;

import java.util.Objects;

public class LuaValueHelper {
    public static class LuaEntityClass {
        public static Entity checkEntity(LuaValue luaValue){
            if (luaValue.get("source").checkuserdata() instanceof Entity entity){
                return entity;
            }
            throw new NullPointerException();
        }

        public static final LibFunction getPosition = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return Vec3ToLuaValue(checkEntity(luaValue0).position());
            }
        };

        public static final LibFunction getRotation = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                Entity entity = checkEntity(luaValue0);
                return Vec2ToLuaValue(new Vec2(entity.getXRot(), entity.getYRot()));
            }
        };

        private static LuaValue functions(){
            LuaTable library = LuaValue.tableOf();
            library.set("getPosition", LuaEntityClass.getPosition);
            library.set("getRotation", LuaEntityClass.getRotation);
            return library;
        }

        public static LuaValue meta = new LuaTable();
        static {
            meta.set("__index", functions());
        }
    }
    public static LuaValue EntityToLuaValue(Entity entity) {
        if (entity == null) return LuaValue.NIL;
        LuaValue library = LuaValue.tableOf();
        //Fields
        library.set("source",LuaValue.userdataOf(entity));
        //Functions
        library.set("getPosition", LuaEntityClass.getPosition);
        library.set("getRotation", LuaEntityClass.getRotation);
        return library;
    }

    public static LuaValue Vec3ToLuaValue(Vec3 vec3) {
        return Vec3ToLuaValue(vec3.x,vec3.y,vec3.z);
    }

    public static LuaValue Vec3ToLuaValue(double x, double y, double z) {
        LuaValue library = LuaValue.tableOf();
        library.set("x", x);
        library.set("y", y);
        library.set("z", z);
        if(LuaCore.LuaUtilVec3Meta != null);
        library.setmetatable(LuaCore.LuaUtilVec3Meta);
        return library;
    }

    public static LuaValue Vec2ToLuaValue(Vec2 vec2) {
        return Vec2ToLuaValue(vec2.x,vec2.y);
    }

    public static LuaValue Vec2ToLuaValue(double x, double y) {
        LuaValue library = LuaValue.tableOf();
        library.set("x", x);
        library.set("y", y);
        library.setmetatable(LuaCore.LuaUtilVec2Meta);
        return library;
    }

    public static LuaValue Vector3fToLuaValue(Vector3f vec3f) {
        return Vector3fToLuaValue(vec3f.x,vec3f.y,vec3f.z);
    }

    public static LuaValue Vector3fToLuaValue(float x, float y, float z) {
        /*LuaValue library = LuaValue.tableOf();
        library.set("x", x);
        library.set("y", y);
        library.set("z", z);
        return library;*/
        return Vec3ToLuaValue(x,y,z);
    }

    public static Vec3 LuaValueToVec3(LuaValue luaValue) {
        return new Vec3(
                luaValue.get("x").checkdouble(),
                luaValue.get("y").checkdouble(),
                luaValue.get("z").checkdouble()
        );
    }

    public static Vec2 LuaValueToVec2(LuaValue luaValue) {
        return new Vec2(
                luaValue.get("x").tofloat(),
                luaValue.get("y").tofloat()
        );
    }

    public static Vector3f LuaValueToVector3f(LuaValue luaValue) {
        return new Vector3f(
                luaValue.get("x").tofloat(),
                luaValue.get("y").tofloat(),
                luaValue.get("z").tofloat()
        );
    }

    public static LuaValue ColorToLuaValue(Color color) {
        LuaValue library = LuaValue.tableOf();
        library.set(1, LuaValue.valueOf(color.r));
        library.set(2, LuaValue.valueOf(color.g));
        library.set(3, LuaValue.valueOf(color.b));
        library.set(4, LuaValue.valueOf(color.a));
        return library;
    }

    public static Color LuaValueToColor(LuaValue luaValue){
        return new Color(
                luaValue.get(1).checkint(),
                luaValue.get(2).checkint(),
                luaValue.get(3).checkint(),
                luaValue.get(4).checkint()
        );
    }
}
