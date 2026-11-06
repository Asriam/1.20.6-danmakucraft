package com.adrian.thDanmakuCraft.network.packets;

import com.adrian.thDanmakuCraft.util.io.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 把整個 {@link THObjectContainer} 壓縮後同步給客戶端。
 */
public record THObjectContainerPacket(THObjectContainer container) implements CustomPacketPayload {

    public static final Type<THObjectContainerPacket> TYPE =
            new Type<>(ResourceLocationUtil.thdanmakucraft("thobject_container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, THObjectContainerPacket> STREAM_CODEC =
            StreamCodec.of(THObjectContainerPacket::write, THObjectContainerPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, THObjectContainerPacket packet) {
        ByteBufOutputStream bos = new ByteBufOutputStream(buf);
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.buffer());
            packet.container().encode(tempBuffer);
            gzip.write(tempBuffer.array());
        } catch (IOException e) {
            throw new RuntimeException("Compressing failed!", e);
        }
    }

    private static THObjectContainerPacket read(RegistryFriendlyByteBuf buf) {
        THObjectContainer container = new THObjectContainer(null);
        ByteBufInputStream bis = new ByteBufInputStream(buf);
        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(gzip.readAllBytes()));
            container.decode(tempBuffer);
        } catch (IOException e) {
            throw new RuntimeException("Decompressing failed!", e);
        }
        return new THObjectContainerPacket(container);
    }

    public static void handle(THObjectContainerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 目前客戶端在這個封包上不需要額外處理
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
