package com.adrian.thDanmakuCraft.network.packets;

import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public class THObjectContainerPacket {

    private final THObjectContainer container;

    public THObjectContainerPacket(THObjectContainer container){
        this.container = container;
    }

    public static void encode(THObjectContainerPacket packet, FriendlyByteBuf byteBuf){
        packet.container.encode(byteBuf);
    }

    public static THObjectContainerPacket decode(FriendlyByteBuf byteBuf){
        THObjectContainer container = new THObjectContainer(null);
        container.decode(byteBuf);
        return new THObjectContainerPacket(container);
    }

    public void handle(CustomPayloadEvent.Context context){
        context.enqueueWork(()->{
            DistExecutor.unsafeCallWhenOn(Dist.CLIENT, ()-> ()-> {
                return null;
            });
        });
        context.setPacketHandled(true);
    }
}
