package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEvents {
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player){
            //MyPlayer.onAttachCapabilities(event);
        }
    }

    @SubscribeEvent
    public static void onPlayerPreTick(TickEvent.PlayerTickEvent.Pre event) {
        //MyPlayer.onPreTick(event);
    }
    @SubscribeEvent
    public static void onPlayerPostTick(TickEvent.PlayerTickEvent.Post event) {
        //MyPlayer.onPostTick(event);
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        //MyPlayer.onPlayerClone(event);
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        //event.register(THObjectContainer.class);
    }

    //private static final EntityDataAccessor<THObjectContainer> PLAYER_DATA_THOBJECT_CONTAINER = SynchedEntityData.defineId(Player.class, MyEntityDataSerializers.THOBJECT_CONTAINER);
    /*
    public static void onPlayerDefineSynchedData(TickEvent.PlayerTickEvent event){
        Player player = event.player;
        THObjectContainer container = player.getEntityData().get(PLAYER_DATA_THOBJECT_CONTAINER);
        container.tick();
    };

    public static THObjectContainer getPlayerTHObjectContainer(Player player){
        THObjectContainer container = player.getEntityData().get(PLAYER_DATA_THOBJECT_CONTAINER);
        container.initHostEntity(player);
        return container;
    };*/
}
