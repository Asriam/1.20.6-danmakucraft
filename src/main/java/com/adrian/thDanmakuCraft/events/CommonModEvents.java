package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.lua.LuaCore;
import com.adrian.thDanmakuCraft.lua.LuaLoader;
import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.registries.THDanmakuCraftRegistries;
import com.adrian.thDanmakuCraft.util.io.ResourceLoader;
import com.adrian.thDanmakuCraft.util.io.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.danmaku.LuaEventCache;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event){
        //event.put(EntityInit.EXAMPLE_ENTITY.get(), EntityExample);
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event){

    }

    @SubscribeEvent
    public static void newRegistry(NewRegistryEvent event){
        THDanmakuCraftRegistries.THOBJECT_TYPE = event.create(
                new RegistryBuilder<THObjectType>(THDanmakuCraftRegistries.Keys.THOBJECT_TYPE)
                        .maxId(Integer.MAX_VALUE - 1)
                        .defaultKey(ResourceLocationUtil.thdanmakucraft("object_0")));
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event){
        // 網路 payload 改由 PacketHandler 的 RegisterPayloadHandlersEvent 註冊
    }
}
