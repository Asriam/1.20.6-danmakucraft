package com.adrian.thDanmakuCraft.world;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LuaValueHelper {
    public static LuaValue EntityToLuaValue(Entity entity) {
        if (entity == null) return LuaValue.NIL;
        LuaValue library = LuaValue.tableOf();
        library.set("getPosition", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return Vec3ToLuaValue(entity.position());
            }
        });
        library.set("getRotation", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return Vec2ToLuaValue(new Vec2(entity.getXRot(), entity.getYRot()));
            }
        });
        return library;
    }

    public static LuaValue Vec3ToLuaValue(Vec3 vec3) {
        LuaValue library = LuaValue.tableOf();
        library.set("x", vec3.x);
        library.set("y", vec3.y);
        library.set("z", vec3.z);
        return library;
        //return CoerceJavaToLua.coerce(vec3);
    }

    public static LuaValue Vec2ToLuaValue(Vec2 vec2) {
        LuaValue library = LuaValue.tableOf();
        library.set("x", vec2.x);
        library.set("y", vec2.y);
        return library;
        //return CoerceJavaToLua.coerce(vec2);
    }

    public static LuaValue Vector3fToLuaValue(Vector3f vec3f) {
        LuaValue library = LuaValue.tableOf();
        library.set("x", vec3f.x);
        library.set("y", vec3f.y);
        library.set("z", vec3f.z);
        return library;
        //return CoerceJavaToLua.coerce(vec3f);
    }

    public static Vec3 LuaValueToVec3(LuaValue luaValue) {
        if (luaValue.isuserdata() && luaValue.checkuserdata() instanceof Vec3 vec3){
            return vec3;
        }else if (luaValue.istable()){
            return new Vec3(
                    luaValue.get("x").checkdouble(),
                    luaValue.get("y").checkdouble(),
                    luaValue.get("z").checkdouble()
            );
        }

        return null;
    }

    public static Vec2 LuaValueToVec2(LuaValue luaValue) {
        if (luaValue.isuserdata() && luaValue.checkuserdata() instanceof Vec2 vec2){
            return vec2;
        }

        if (luaValue.istable()){
            return new Vec2(
                    luaValue.get("x").tofloat(),
                    luaValue.get("y").tofloat()
            );
        }

        return null;
    }

    public static Vector3f LuaValueToVector3f(LuaValue luaValue) {
        if (luaValue.isuserdata() && luaValue.checkuserdata() instanceof Vector3f vector3f){
            return vector3f;
        }else if (luaValue.istable()){
            return new Vector3f(
                    luaValue.get("x").tofloat(),
                    luaValue.get("y").tofloat(),
                    luaValue.get("z").tofloat()
            );
        }

        return null;
    }
}
