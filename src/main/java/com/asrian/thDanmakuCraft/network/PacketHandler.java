package com.asrian.thDanmakuCraft.network;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler {
    private static final int PROTOCOL_VERSION = 1;
    public static final SimpleChannel INSTANCE = ChannelBuilder.named(
            new ResourceLocation(THDanmakuCraftCore.MODID, "main"))
            .serverAcceptedVersions((status, version) -> true)
            .clientAcceptedVersions((status, version) -> true)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        /*
        INSTANCE.messageBuilder(ServerboundUpdateTHObjectsPacket.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundUpdateTHObjectsPacket::encode)
                .decoder(ServerboundUpdateTHObjectsPacket::new)
                .consumerMainThread(ServerboundUpdateTHObjectsPacket::handle)
                .add();
        INSTANCE.messageBuilder(ClientboundUpdateTHObjectsPacket.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundUpdateTHObjectsPacket::encode)
                .decoder(ClientboundUpdateTHObjectsPacket::new)
                .consumerMainThread(ClientboundUpdateTHObjectsPacket::handle)
                .add();

         */
    }

    public static void sendToServer(Object msg){
        INSTANCE.send(msg, PacketDistributor.SERVER.noArg());
    }

    public static void sendToPlayer(Object msg, ServerPlayer player){
        INSTANCE.send(msg, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToAllClient(Object msg){
        INSTANCE.send(msg, PacketDistributor.ALL.noArg());
    }


}
