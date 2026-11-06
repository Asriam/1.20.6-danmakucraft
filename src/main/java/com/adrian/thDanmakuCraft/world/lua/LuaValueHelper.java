package com.adrian.thDanmakuCraft.world.lua;

import com.adrian.thDanmakuCraft.lua.LuaCore;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
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
            return LuaBridge.target(luaValue, Entity.class);
        }

        public static final LibFunction getPosition = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                Entity entity = checkEntity(luaValue0);
                //AABB bb = entity.getBoundingBox();
                //Vec3 position = entity.position().add(bb.getXsize()/2, bb.getYsize()/2, bb.getZsize()/2);
                return Vec3ToLuaValue(entity.getBoundingBox().getCenter());
            }
        };

        public static final LibFunction getLastPosition = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                Entity entity = checkEntity(luaValue0);
                AABB bb = entity.getBoundingBox();
                Vec3 position = new Vec3(entity.xo+bb.getXsize(), entity.yo+bb.getYsize(), entity.zo+bb.getZsize());
                return Vec3ToLuaValue(position);
            }
        };

        public static final LibFunction getRotation = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                Entity entity = checkEntity(luaValue0);
                return Vec2ToLuaValue(new Vec2(entity.getXRot(), entity.getYRot()));
            }
        };

        public static final LibFunction getName = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                Entity entity = checkEntity(luaValue0);
                if (entity instanceof Player player){
                    LuaValue.valueOf(player.getDisplayName().getString());
                }
                return LuaValue.valueOf(entity.getName().getString());
            }
        };

        private static LuaValue functions(){
            return LuaFunctions.create()
                    .add("getPosition", LuaEntityClass.getPosition)
                    .add("getLastPosition", LuaEntityClass.getLastPosition)
                    .add("getRotation", LuaEntityClass.getRotation)
                    .add("getName", LuaEntityClass.getName)
                    .build();
        }

        public static LuaValue meta = new LuaTable();
        static {
            meta.set("__index", functions());
        }
    }

    public static LuaValue PlayerToLuaValue(Player player){
        if (player == null) return LuaValue.NIL;
        LuaValue library = LuaValue.tableOf();
        //Fields
        library.set("source",LuaValue.userdataOf(player));
        //Functions
        library.set("getPosition", LuaEntityClass.getPosition);
        library.set("getRotation", LuaEntityClass.getRotation);
        return library;
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
        LuaValue vec3Lua = LuaValue.tableOf(0, 3);
        vec3Lua.set("x", x);
        vec3Lua.set("y", y);
        vec3Lua.set("z", z);
        LuaValue vec3Lib = LuaCore.LuaUtilVec3;
        if (!vec3Lib.isnil()){
            return vec3Lib.get("of").call(vec3Lua);
        }
        return vec3Lua;
    }

    public static LuaValue Vec2ToLuaValue(Vec2 vec2) {
        return Vec2ToLuaValue(vec2.x,vec2.y);
    }

    public static LuaValue Vec2ToLuaValue(double x, double y) {
        LuaValue vec2Lua = LuaValue.tableOf(0, 3);
        vec2Lua.set("x", x);
        vec2Lua.set("y", y);
        LuaValue vec2Lib = LuaCore.LuaUtilVec2;
        if (!vec2Lib.isnil()){
            return vec2Lib.get("of").call(vec2Lua);
        }
        return vec2Lua;
    }

    public static LuaValue Vector3fToLuaValue(Vector3f vec3f) {
        return Vector3fToLuaValue(vec3f.x,vec3f.y,vec3f.z);
    }

    public static LuaValue Vector3fToLuaValue(float x, float y, float z) {
        return Vec3ToLuaValue(x,y,z);
    }

    public static Vec3 LuaValueToVec3(LuaValue luaValue) {
        return new Vec3(
                luaValue.get(LuaKeys.X).checkdouble(),
                luaValue.get(LuaKeys.Y).checkdouble(),
                luaValue.get(LuaKeys.Z).checkdouble()
        );
    }

    public static Vec2 LuaValueToVec2(LuaValue luaValue) {
        return new Vec2(
                luaValue.get(LuaKeys.X).tofloat(),
                luaValue.get(LuaKeys.Y).tofloat()
        );
    }

    public static Vector3f LuaValueToVector3f(LuaValue luaValue) {
        return new Vector3f(
                luaValue.get(LuaKeys.X).tofloat(),
                luaValue.get(LuaKeys.Y).tofloat(),
                luaValue.get(LuaKeys.Z).tofloat()
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

    /** 原本在 util.lua.LuaValueUtil，已合併過來。 */
    public static boolean isValid(LuaValue value) {
        return value != null && !value.isnil();
    }

    public static boolean isNotValid(LuaValue value) {
        return value == null || value.isnil();
    }
}
