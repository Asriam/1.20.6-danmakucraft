package com.adrian.thDanmakuCraft.world.player;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.THObjectContainerProvider;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.concurrent.atomic.AtomicReference;

public class MyPlayer {

    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event){
        if(!event.getObject().getCapability(THObjectContainerProvider.PLAYER_THOBJECT_CONTAINER_CAPABILITY).isPresent()){
            event.addCapability(new ResourceLocation(THDanmakuCraftCore.MOD_ID, "thobject_container"), new THObjectContainerProvider());
        }
    }
    public static void onPreTick(TickEvent.PlayerTickEvent.Pre event){
        Player player = event.player;
        player.getAbilities().mayfly = true;
    }

    public static void onPostTick(TickEvent.PlayerTickEvent.Post event){
        Player player = event.player;
        /*
        if(event.side == LogicalSide.SERVER) {
            tickTHObjectContainer(player);
        }*/
        //tickTHObjectContainer(player);
    }

    public static void onPlayerClone(PlayerEvent.Clone event){
        event.getOriginal().getCapability(THObjectContainerProvider.PLAYER_THOBJECT_CONTAINER_CAPABILITY).ifPresent(oldContainer -> {
            event.getEntity().getCapability(THObjectContainerProvider.PLAYER_THOBJECT_CONTAINER_CAPABILITY).ifPresent(newContainer -> {
                final FriendlyByteBuf spawnDataBuffer = new FriendlyByteBuf(Unpooled.buffer());
                oldContainer.writeSpawnData(spawnDataBuffer);

                newContainer.readSpawnData(spawnDataBuffer);
            });
        });
    }

    public static void tickTHObjectContainer(Entity entity){
        entity.getCapability(THObjectContainerProvider.PLAYER_THOBJECT_CONTAINER_CAPABILITY).ifPresent(container -> {
            container.initHostEntity(entity);
            container.tick();
        });
    }
    public static THObjectContainer getEntityTHObjectContainer(Entity entity){
        final AtomicReference<THObjectContainer> container = new AtomicReference<>();
        entity.getCapability(THObjectContainerProvider.PLAYER_THOBJECT_CONTAINER_CAPABILITY).ifPresent(container2 -> {
            container2.initHostEntity(entity);
            container.set(container2);
        });
        return container.get();
    }
}
