package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.lua.LuaCore;
import com.adrian.thDanmakuCraft.lua.LuaLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Events {

    public static void onEnttiyTick(EntityEvent.EntityConstructing event) {

    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        CommandBuildContext context = event.getBuildContext();
        event.getDispatcher().register(Commands.literal("refresh_lua_lib").executes(arguments -> {
            LuaLoader.init();
            LuaCore.init();
            return 15;
        }));
    }
}
