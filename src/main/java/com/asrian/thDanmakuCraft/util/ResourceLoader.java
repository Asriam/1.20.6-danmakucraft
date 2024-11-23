package com.asrian.thDanmakuCraft.util;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceLoader {

    private static final ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

    @Nullable
    public static JsonObject loadJson(ResourceLocation resourceLocation) {
        try {
            return JsonParser.parseReader(loadResource(resourceLocation).openAsReader()).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static String loadFileAsString(ResourceLocation resourceLocation) {
        try {
            return readRescource(loadResource(resourceLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readRescource(Resource resource) throws IOException {
        return new BufferedReader(resource.openAsReader()).lines().collect(Collectors.joining("\n"));
    }

    @Nullable
    public static Resource loadResource(ResourceLocation resourceLocation) throws RuntimeException{
            return resourceManager.getResource(resourceLocation).orElseThrow(() -> new RuntimeException("Resource not found: " + resourceLocation));
    }

    public static List<Resource> loadRescourceStack(ResourceLocation resourceLocation){
        return resourceManager.getResourceStack(resourceLocation);
    }

    public static Map<ResourceLocation,Resource> loadAllResourcesInFolder(ResourceLocation folderPath, String suffix){
        Map<ResourceLocation, Resource> resourceMap=  resourceManager.listResources(folderPath.getPath(), path -> path.toString().endsWith(suffix));
        Map<ResourceLocation, Resource> map1 = new HashMap<>();
        resourceMap.forEach((resourceLocation, resource) -> {
            try (InputStream inputstream = resource.open()) {
                byte[] abyte = inputstream.readAllBytes();
                map1.put(resourceLocation, new Resource(resource.source(), () -> {
                    return new ByteArrayInputStream(abyte);
                }));
            } catch (Exception exception) {
                THDanmakuCraftCore.LOGGER.warn("Failed to read resource {}", resourceLocation, exception);
            }

        });
        return map1;
    }
}
