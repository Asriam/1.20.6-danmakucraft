package com.adrian.thDanmakuCraft.network;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.network.packets.THObjectContainerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge payload 註冊（取代 Forge 的 SimpleChannel）。
 */
@EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID/*, bus = EventBusSubscriber.Bus.MOD*/)
public final class PacketHandler {

    private PacketHandler() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(THObjectContainerPacket.TYPE, THObjectContainerPacket.STREAM_CODEC,
                THObjectContainerPacket::handle);
    }

    public static void sendToServer(THObjectContainerPacket packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToPlayer(THObjectContainerPacket packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToAllClient(THObjectContainerPacket packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }
}
