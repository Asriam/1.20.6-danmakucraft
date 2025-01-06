package com.adrian.thDanmakuCraft.world;

import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
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
            //return null;
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
            LuaValue x = luaValue.get("x");
            if(!x.isnil()) {
                return new Vec3(
                        x.checkdouble(),
                        luaValue.get("y").checkdouble(),
                        luaValue.get("z").checkdouble()
                );
            }else {
                return new Vec3(
                        luaValue.get(1).checkdouble(),
                        luaValue.get(2).checkdouble(),
                        luaValue.get(3).checkdouble()
                );
            }
        }

        return Vec3.ZERO;
    }

    public static Vec2 LuaValueToVec2(LuaValue luaValue) {
        if (luaValue.isuserdata() && luaValue.checkuserdata() instanceof Vec2 vec2){
            return vec2;
        }

        if (luaValue.istable()){
            LuaValue x = luaValue.get("x");
            if(!x.isnil()) {
                return new Vec2(
                        x.tofloat(),
                        luaValue.get("y").tofloat()
                );
            }else {
                return new Vec2(
                        luaValue.get(1).tofloat(),
                        luaValue.get(2).tofloat()
                );
            }
        }

        return Vec2.ZERO;
    }

    public static Vector3f LuaValueToVector3f(LuaValue luaValue) {
        if (luaValue.isuserdata() && luaValue.checkuserdata() instanceof Vector3f vector3f){
            return vector3f;
        }else if (luaValue.istable()){
            LuaValue x = luaValue.get("x");
            if(!x.isnil()) {
                return new Vector3f(
                        x.tofloat(),
                        luaValue.get("y").tofloat(),
                        luaValue.get("z").tofloat()
                );
            }else {
                return new Vector3f(
                        luaValue.get(1).tofloat(),
                        luaValue.get(2).tofloat(),
                        luaValue.get(3).tofloat()
                );
            }
        }

        return new Vector3f(0.0f,0.0f,0.0f);
    }
}
