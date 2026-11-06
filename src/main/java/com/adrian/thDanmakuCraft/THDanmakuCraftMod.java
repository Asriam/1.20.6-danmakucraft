package com.adrian.thDanmakuCraft;

import com.adrian.thDanmakuCraft.events.GuiEvents;
import com.adrian.thDanmakuCraft.init.*;
import com.adrian.thDanmakuCraft.lua.LuaCore;
import com.adrian.thDanmakuCraft.lua.LuaLoader;
import com.adrian.thDanmakuCraft.network.PacketHandler;
import com.adrian.thDanmakuCraft.network.syncher.MyEntityDataSerializers;
import com.adrian.thDanmakuCraft.util.io.ResourceLoader;
import com.adrian.thDanmakuCraft.util.io.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(THDanmakuCraftMod.MOD_ID)
public class THDanmakuCraftMod
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "thdanmakucraft";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    //public static JSLoader JSLOADER;
    //public static ResourceManager RESOURCE_MANAGER;

    public THDanmakuCraftMod(@NotNull IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        BlockInit.BLOCKS.register(modEventBus);
        ItemInit.ITEMS.register(modEventBus);
        EntityInit.ENTITIES.register(modEventBus);
        MyEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(modEventBus);
        CreativeModeTabInit.CREATIVE_MODE_TABS.register(modEventBus);
        THObjectInit.TH_OBJECTS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLCommonSetupEvent event) {
        GuiEvents.addLayers();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        //LuaLoader.init();
        //LuaCore.init();
        //PacketHandler.register();
    }

    @SubscribeEvent
    public void onServerStartingEvent(final ServerStartingEvent event) {
        ResourceLoader.init(event.getServer().getResourceManager());
        LuaLoader.init();
        LuaCore.init();
    }
}
