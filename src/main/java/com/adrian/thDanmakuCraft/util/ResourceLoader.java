package com.adrian.thDanmakuCraft.util;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceLoader {

    private static ResourceManager resourceManager;

    public static void init(ResourceManager rm){
        resourceManager = rm;
    }

    @Nullable
    public static String loadFileAsString(ResourceLocation resourceLocation) {
        try {
            return readResource(loadResource(resourceLocation));
        } catch (IOException e) {
            THDanmakuCraftCore.LOGGER.warn("Failed to load resource: {}", resourceLocation, e);
        }
        return null;
    }

    public static String readResource(Resource resource) throws IOException {
        return new BufferedReader(resource.openAsReader()).lines().collect(Collectors.joining("\n"));
    }

    public static Resource loadResource(ResourceLocation resourceLocation) throws RuntimeException{
            return resourceManager.getResource(resourceLocation).orElseThrow(() -> new RuntimeException("Resource not found: " + resourceLocation));
    }

    public static Map<ResourceLocation,Resource> loadAllResourcesInFolder(ResourceLocation folderPath, String suffix){
        return resourceManager.listResources(folderPath.getPath(), path -> path.toString().endsWith(suffix));
    }

    public interface RunnableWithException{

        void run(Exception exception);
    }
}
