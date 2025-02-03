package com.adrian.thDanmakuCraft.world;

import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.OneArgFunction;

public class LuaValueHelper {
    public static class LuaEntityClass {
        private static Entity checkEntity(LuaValue luaValue){
            if (luaValue.get("source").checkuserdata() instanceof Entity entity){
                return entity;
            }
            throw new NullPointerException();
        }

        private static final LibFunction getPosition = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return Vec3ToLuaValue(checkEntity(luaValue0).position());
            }
        };

        private static final LibFunction getRotation = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                Entity entity = checkEntity(luaValue0);
                return Vec2ToLuaValue(new Vec2(entity.getXRot(), entity.getYRot()));
            }
        };

    }

    public static LuaValue Vec3MemberFunctions(){
        LuaValue luaValue = LuaValue.tableOf();

        return luaValue;
    };
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
        return library;
    }

    public static LuaValue Vec2ToLuaValue(Vec2 vec2) {
        return Vec2ToLuaValue(vec2.x,vec2.y);
    }

    public static LuaValue Vec2ToLuaValue(double x, double y) {
        LuaValue library = LuaValue.tableOf();
        library.set("x", x);
        library.set("y", y);
        return library;
    }

    public static LuaValue Vector3fToLuaValue(Vector3f vec3f) {
        return Vector3fToLuaValue(vec3f.x,vec3f.y,vec3f.z);
    }

    public static LuaValue Vector3fToLuaValue(float x, float y, float z) {
        LuaValue library = LuaValue.tableOf();
        library.set("x", x);
        library.set("y", y);
        library.set("z", z);
        return library;
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
}
