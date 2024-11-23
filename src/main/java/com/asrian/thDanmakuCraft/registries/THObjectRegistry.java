package com.asrian.thDanmakuCraft.registries;

import com.asrian.thDanmakuCraft.world.entity.danmaku.THObject;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class THObjectRegistry {
    //public static Map<String, String> objectName = new HashMap<String, String>();
    public static Map<String, Class<? extends THObject>> objectClass = new HashMap();

    public static String registerTHObject(Class<? extends THObject> object_class, String name){
        objectClass.put(name, object_class);
        return name;
    }

    public static String registerTHObject(Class<? extends THObject> object_class, ResourceLocation name){
        return registerTHObject(object_class, name.toString());
    }

    public static Class<? extends THObject> getObjectClass(String name)
    {
        return objectClass.get(name);
    }



    public static Class<? extends THObject> getObjectClass(ResourceLocation name)
    {
        return getObjectClass(name.toString());
    }
}
