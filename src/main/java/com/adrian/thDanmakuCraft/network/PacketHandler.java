package com.adrian.thDanmakuCraft.network;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.network.packets.THObjectContainerPacket;
import com.adrian.thDanmakuCraft.util.io.ResourceLocationUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler {

    private static final int PROTOCOL_VERSION = 1;
    private static int id = 1;
    public static final SimpleChannel SIMPLE_CHANNEL = ChannelBuilder
            .named(ResourceLocationUtil.location(THDanmakuCraftMod.MOD_ID,"main"))
            .serverAcceptedVersions((status,version) -> true)
            .clientAcceptedVersions((status,version) -> true)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .simpleChannel();

    public static int id(){
        return id++;
    }
    public static void register() {
        SIMPLE_CHANNEL.messageBuilder(THObjectContainerPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(THObjectContainerPacket::encode)
                .decoder(THObjectContainerPacket::decode)
                .consumerMainThread(THObjectContainerPacket::handle)
                .add();
    }

    public static void sendToServer(Object msg){
        SIMPLE_CHANNEL.send(msg, PacketDistributor.SERVER.noArg());
    }

    public static void sendToPlayer(Object msg, ServerPlayer player){
        SIMPLE_CHANNEL.send(msg, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToAllClient(Object msg){
        SIMPLE_CHANNEL.send(msg, PacketDistributor.ALL.noArg());
    }
}
