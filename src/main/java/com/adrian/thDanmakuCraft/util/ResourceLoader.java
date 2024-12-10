package com.adrian.thDanmakuCraft.util;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceLoader {

    private static final ResourceManager resourceManager = THDanmakuCraftCore.RESOURCE_MANAGER;

    @Nullable
    public static String loadFileAsString(ResourceLocation resourceLocation) {
        try {
            return readResource(loadResource(resourceLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readResource(Resource resource) throws IOException {
        return new BufferedReader(resource.openAsReader()).lines().collect(Collectors.joining("\n"));
    }

    public static Resource loadResource(ResourceLocation resourceLocation) throws RuntimeException{
            return resourceManager.getResource(resourceLocation).orElseThrow(() -> new RuntimeException("Resource not found: " + resourceLocation));
    }

    public static List<Resource> loadRescourceStack(ResourceLocation resourceLocation){
        return resourceManager.getResourceStack(resourceLocation);
    }

    public static Map<ResourceLocation,Resource> loadAllResourcesInFolder(ResourceLocation folderPath, String suffix){
        return resourceManager.listResources(folderPath.getPath(), path -> path.toString().endsWith(suffix));
    }
}
