package com.adrian.thDanmakuCraft.script.js;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.util.ResourceLoader;
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
        this.resourceMap = ResourceLoader.loadAllResourcesInFolder(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"data/js"),"js");
        for (var resourceLocation:this.resourceMap.keySet()){
            try {
                THDanmakuCraftCore.LOGGER.info("Loading JS: {}",resourceLocation);
                Resource resource = resourceMap.get(resourceLocation);
                resourceMap2.put(resourceLocation,ResourceLoader.readResource(resource));
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
