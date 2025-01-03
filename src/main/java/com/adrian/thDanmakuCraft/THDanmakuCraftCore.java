package com.adrian.thDanmakuCraft;

import com.adrian.thDanmakuCraft.init.*;
import com.adrian.thDanmakuCraft.script.lua.LuaCore;
import com.adrian.thDanmakuCraft.script.lua.LuaLoader;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(THDanmakuCraftCore.MOD_ID)
public class THDanmakuCraftCore
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "thdanmakucraft";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    //public static JSLoader JSLOADER;
    public static ResourceManager RESOURCE_MANAGER;

    public THDanmakuCraftCore(@NotNull FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BlockInit.BLOCKS.register(modEventBus);
        ItemInit.ITEMS.register(modEventBus);
        EntityInit.ENTITIES.register(modEventBus);
        CreativeModeTabInit.CREATIVE_MODE_TABS.register(modEventBus);
        THObjectInit.TH_OBJECTS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        //JSLoader.init();
        //JSCore.init();
        LuaLoader.init();
        LuaCore.init();
    }

    public static Map<String,Runnable> onServerStartingTask = new HashMap<>();

    @SubscribeEvent
    public void onServerStarting(final @NotNull ServerStartingEvent event) {
        //THDanmakuCraftCore.LOGGER.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
        //JSLoader.init();
        //JSCore.init();
        LuaLoader.init();
        LuaCore.init();

        for(Runnable task:onServerStartingTask.values()){
            task.run();
        }
    }

    @Nullable
    @Deprecated
    public static ResourceManager getServerResourceManager() {
        // Get the Minecraft server instance
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            // Access the resource manager
            return server.getResourceManager();
        }
        return null; // Server might not be initialized yet
    }
}
