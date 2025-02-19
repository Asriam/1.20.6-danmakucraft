package com.adrian.thDanmakuCraft.util;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationUtil {

    public static ResourceLocation minecraft(String path) {
        return new ResourceLocation(path);
    }
    public static ResourceLocation mod(String path) {
        return new ResourceLocation(THDanmakuCraftMod.MOD_ID, path);
    }

    public static ResourceLocation location(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation of(String path){
        return new ResourceLocation(path);
    }
}
