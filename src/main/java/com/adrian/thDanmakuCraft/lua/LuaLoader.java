package com.adrian.thDanmakuCraft.lua;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.util.ResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LuaLoader {

    private static LuaLoader luaLoader;

    private final Map<ResourceLocation,Resource> resourceMap;
    private final Map<ResourceLocation,String>   resourceMap2 = new HashMap<>();

    public LuaLoader(){
        this.resourceMap = ResourceLoader.loadAllResourcesInFolder(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"data/lua"),"lua");
        this.resourceMap.forEach(((resourceLocation, resource) -> {
            try {
                THDanmakuCraftCore.LOGGER.info("Loading resource {}",resourceLocation);
                resourceMap2.put(resourceLocation,ResourceLoader.readResource(resource));
            } catch (Exception e) {
                THDanmakuCraftCore.LOGGER.warn("Failed to load resource {}",resourceLocation,e);
            }
        }));
    }

    public static void init(){
        luaLoader = new LuaLoader();
    }

    @Nullable
    public static Resource getResource(ResourceLocation resourceLocation){
        return luaLoader.resourceMap.get(resourceLocation);
    }

    @Nullable
    public static String getResourceAsString(ResourceLocation resourceLocation){
        return luaLoader.resourceMap2.get(resourceLocation);
    }

    @Nullable
    public static Map<ResourceLocation,String> getResourceMap(){
        return luaLoader.resourceMap2;
    }
}
