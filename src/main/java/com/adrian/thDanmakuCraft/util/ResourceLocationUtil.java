package com.adrian.thDanmakuCraft.util;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationUtil {

    public static ResourceLocation minecraft(String path) {
        return ResourceLocation.parse(path);
    }
    public static ResourceLocation mod(String path) {
        return ResourceLocation.fromNamespaceAndPath(THDanmakuCraftMod.MOD_ID, path);
    }

    public static ResourceLocation location(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation of(String path){
        return ResourceLocation.parse(path);
    }
}
