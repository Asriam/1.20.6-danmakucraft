package com.adrian.thDanmakuCraft.lua;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.util.ResourceLoader;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LuaLoader {

    public static LuaLoader instance;

    private final Map<ResourceLocation,Resource> resourceMap;
    private final Map<ResourceLocation,String>   resourceMap2 = new HashMap<>();

    public LuaLoader(){
        this.resourceMap = ResourceLoader.loadAllResourcesInFolder(ResourceLocationUtil.thdanmakucraft("lua"),"lua");
        this.resourceMap.forEach(((resourceLocation, resource) -> {
            try {
                THDanmakuCraftMod.LOGGER.info("Loading resource {}",resourceLocation);
                resourceMap2.put(resourceLocation,ResourceLoader.readResource(resource));
            } catch (Exception e) {
                THDanmakuCraftMod.LOGGER.warn("Failed to load resource {}",resourceLocation,e);
            }
        }));
    }

    public static void init(){
        instance = new LuaLoader();
    }

    @Nullable
    public Resource getResource(ResourceLocation resourceLocation){
        return this.resourceMap.get(resourceLocation);
    }

    @Nullable
    public String getResourceAsString(ResourceLocation resourceLocation){
        return this.resourceMap2.get(resourceLocation);
    }

    public Map<ResourceLocation,Resource> loadAllResourcesInFolder(ResourceLocation path){
        return ResourceLoader.loadAllResourcesInFolder(path,"lua");
    }

    public Map<ResourceLocation,String> loadAllResourcesInFolderAsString(ResourceLocation path){
        final Map<ResourceLocation,Resource> map = ResourceLoader.loadAllResourcesInFolder(path,"lua");
        final Map<ResourceLocation,String> asString = Maps.newHashMap();
        map.forEach(((resourceLocation, resource) -> {
            try {
                asString.put(resourceLocation,ResourceLoader.readResource(resource));
            } catch (Exception e) {
                THDanmakuCraftMod.LOGGER.warn("Failed to load resource {}",resourceLocation,e);
            }
        }));
        return asString;
    }

    @Nullable
    public Map<ResourceLocation,String> getResourceMap(){
        return this.resourceMap2;
    }
}
