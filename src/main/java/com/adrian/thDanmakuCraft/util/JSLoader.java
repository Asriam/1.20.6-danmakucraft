package com.adrian.thDanmakuCraft.util;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JSLoader {

    private static JSLoader jsLoader;

    private final Map<ResourceLocation,Resource> resourceMap;
    private final Map<ResourceLocation,String>   resourceMap2 = new HashMap<>();

    public JSLoader(){
        this.resourceMap = ResourceLoader.loadAllResourcesInFolder(new ResourceLocation(THDanmakuCraftCore.MODID,"data/js"),"js");

        for (var resourceLocation:this.resourceMap.keySet()){
            try {
                var resource = resourceMap.get(resourceLocation);
                resourceMap2.put(resourceLocation,ResourceLoader.readRescource(resource));
            } catch (Exception e) {
                THDanmakuCraftCore.LOGGER.warn("Failed to read resource {}",resourceLocation,e);
            }
        }
    }

    public static void init(){
        jsLoader = new JSLoader();
    }

    @Nullable
    public static Resource getResource(ResourceLocation resourceLocation){
        return jsLoader.resourceMap.get(resourceLocation);
    }

    @Nullable
    public static String getResourceAsString(ResourceLocation resourceLocation){
        return jsLoader.resourceMap2.get(resourceLocation);
    }


}
