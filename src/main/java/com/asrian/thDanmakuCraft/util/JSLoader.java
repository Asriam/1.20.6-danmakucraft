package com.asrian.thDanmakuCraft.util;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JSLoader {

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

    @Nullable
    public Resource getResource(ResourceLocation resourceLocation){
        return this.resourceMap.get(resourceLocation);
    }

    @Nullable
    public String getResourceAsString(ResourceLocation resourceLocation){
        return this.resourceMap2.get(resourceLocation);
    }


}
