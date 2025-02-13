package com.adrian.thDanmakuCraft.network.syncher;

import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MyEntityDataSerializers {

    public static final EntityDataSerializer<THObjectContainer> THOBJECT_CONTAINER = EntityDataSerializer.forValueType(new StreamCodec<FriendlyByteBuf, THObjectContainer>() {
        public void encode(@NotNull FriendlyByteBuf byteBuf, @NotNull THObjectContainer container) {
            container.encode(byteBuf);
        }
        public @NotNull THObjectContainer decode(@NotNull FriendlyByteBuf byteBuf) {
            THObjectContainer container = new THObjectContainer(null);
            container.decode(byteBuf);
            return container;
        }
    });

    public static final EntityDataSerializer<THObject> THOBJECT = EntityDataSerializer.forValueType(new StreamCodec<FriendlyByteBuf, THObject>() {
        public void encode(@NotNull FriendlyByteBuf byteBuf, @NotNull THObject object) {
            byteBuf.writeResourceLocation(object.getType().getKey());
            object.encode(byteBuf);
        }
        public @NotNull THObject decode(@NotNull FriendlyByteBuf byteBuf) {
            ResourceLocation key = byteBuf.readResourceLocation();
            THObject object = THObjectType.getValue(key).create(null);
            object.decode(byteBuf);
            return object;
        };
    });

    static {
        EntityDataSerializers.registerSerializer(THOBJECT_CONTAINER);
        EntityDataSerializers.registerSerializer(THOBJECT);
    }

}
