package com.adrian.thDanmakuCraft.network.packets;

import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class THObjectContainerPacket {

    private final THObjectContainer container;

    public THObjectContainerPacket(THObjectContainer container){
        this.container = container;
    }

    public static void encode(THObjectContainerPacket packet, FriendlyByteBuf byteBuf){
        //packet.container.encode(byteBuf);
        /*ByteBufOutputStream bos = new ByteBufOutputStream(byteBuf);
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.buffer());
            packet.container.encode(tempBuffer);
            gzip.write(tempBuffer.array());
        } catch (IOException e) {
            throw new RuntimeException("Compressing failed!", e);
        }*/
    }

    public static THObjectContainerPacket decode(FriendlyByteBuf byteBuf){
        THObjectContainer container = new THObjectContainer(null);
        //container.decode(byteBuf);
        /*ByteBufInputStream bis = new ByteBufInputStream(byteBuf);
        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(gzip.readAllBytes()));
            container.decode(tempBuffer);
        } catch (IOException e) {
            throw new RuntimeException("Decompressing failed!",e);
        }*/
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
