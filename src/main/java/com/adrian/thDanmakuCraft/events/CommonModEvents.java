package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.lua.LuaCore;
import com.adrian.thDanmakuCraft.lua.LuaLoader;
import com.adrian.thDanmakuCraft.network.PacketHandler;
import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.registries.THDanmakuCraftRegistries;
import com.adrian.thDanmakuCraft.util.ResourceLoader;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event){
        //event.put(EntityInit.EXAMPLE_ENTITY.get(), EntityExample);
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event){

    }

    @SubscribeEvent
    public static void newRegistry(NewRegistryEvent event){
        event.create(makeRegistry(THDanmakuCraftRegistries.Keys.THOBJECT_TYPE, "object_0").legacyName("thobjects"));
    }

    private static <T> RegistryBuilder<T> makeRegistry(ResourceKey<? extends Registry<T>> key, String _default)
    {
        return new RegistryBuilder<T>().setName(key.location()).setMaxID(Integer.MAX_VALUE - 1).hasTags().setDefaultKey(ResourceLocationUtil.of(_default));
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event){
        event.enqueueWork(PacketHandler::register);
    }
}
