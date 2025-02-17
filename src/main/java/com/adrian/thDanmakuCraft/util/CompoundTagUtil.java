package com.adrian.thDanmakuCraft.util;

import net.minecraft.nbt.*;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CompoundTagUtil {
    public static ListTag newDoubleList(double... value) {
        ListTag listtag = new ListTag();

        for (double d0 : value) {
            listtag.add(DoubleTag.valueOf(d0));
        }

        return listtag;
    }

    protected static ListTag newFloatList(float... value) {
        ListTag listtag = new ListTag();

        for (float f : value) {
            listtag.add(FloatTag.valueOf(f));
        }

        return listtag;
    }

    public static ListTag newIntList(int... value) {
        ListTag listtag = new ListTag();

        for (int i : value) {
            listtag.add(IntTag.valueOf(i));
        }

        return listtag;
    }

    protected static ListTag newStringList(String... value) {
        ListTag listtag = new ListTag();
        for (String i : value) {
            listtag.add(StringTag.valueOf(i));
        }
        return listtag;
    }

    protected static ListTag newVec2(Vec2 vec2) {
        return newFloatList(vec2.x, vec2.y);
    }

    public static ListTag newVec3(Vec3 vec3) {
        return newDoubleList(vec3.x, vec3.y, vec3.z);
    }

    public static ListTag newVector3f(Vector3f vec3) {
        return newFloatList(vec3.x, vec3.y, vec3.z);
    }
}
