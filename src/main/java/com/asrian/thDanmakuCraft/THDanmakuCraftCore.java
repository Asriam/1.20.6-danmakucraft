package com.asrian.thDanmakuCraft;

import com.asrian.thDanmakuCraft.init.*;
import com.asrian.thDanmakuCraft.script.JavaScript;
import com.asrian.thDanmakuCraft.util.JSLoader;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import javax.script.ScriptEngine;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(THDanmakuCraftCore.MODID)
public class THDanmakuCraftCore
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "thdanmakucraft";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final JavaScript JAVASCRIPT = new JavaScript();
    public static JSLoader JSLOADER;

    public static final ScriptEngine getEngine() {
        return JAVASCRIPT.getEngine();
    }

    public THDanmakuCraftCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BlockInit.BLOCKS.register(modEventBus);
        ItemInit.ITEMS.register(modEventBus);
        EntityInit.ENTITIES.register(modEventBus);
        CreativeModeTabInit.CREATIVE_MODE_TABS.register(modEventBus);
        THObjectInit.TH_OBJECTS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        JSLOADER = new JSLoader();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}
