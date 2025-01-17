package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.network.syncher.MyEntityDataSerializers;
import com.adrian.thDanmakuCraft.world.EntityTHObjectContainerProvider;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEvents {

    @SubscribeEvent
    public static void onPlayerPostTick(TickEvent.PlayerTickEvent.Post event) {
        Player player = event.player;
        player.getAbilities().mayfly = true;
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if(entity instanceof Player player){
            if(!entity.getCapability(EntityTHObjectContainerProvider.PLAYER_THOBJECT_CONTAINER_CAPABILITY).isPresent()){
                event.addCapability(new ResourceLocation(THDanmakuCraftCore.MOD_ID, "thobject_container"), new EntityTHObjectContainerProvider());
            }
        }
    }

    /*
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
    }*/

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(THObjectContainer.class);
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
