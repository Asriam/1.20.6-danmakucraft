package com.adrian.thDanmakuCraft;

import com.adrian.thDanmakuCraft.init.*;
import com.adrian.thDanmakuCraft.script.js.JSCore;
import com.adrian.thDanmakuCraft.script.js.JSLoader;
import com.adrian.thDanmakuCraft.script.lua.LuaCore;
import com.adrian.thDanmakuCraft.script.lua.LuaLoader;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(THDanmakuCraftCore.MOD_ID)
public class THDanmakuCraftCore
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "thdanmakucraft";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    //public static JSLoader JSLOADER;
    public static final ResourceManager RESOURCE_MANAGER = Minecraft.getInstance().getResourceManager();

    public THDanmakuCraftCore(FMLJavaModLoadingContext context) {
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
        JSLoader.init();
        JSCore.init();

        LuaLoader.init();
        LuaCore.init();
    }

    public static Map<String,Runnable> onServerStartingTask = new HashMap<>();

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event) {
        JSLoader.init();
        JSCore.init();

        LuaLoader.init();
        LuaCore.init();
        for(Runnable task:onServerStartingTask.values()){
            task.run();
        }
    }
}
