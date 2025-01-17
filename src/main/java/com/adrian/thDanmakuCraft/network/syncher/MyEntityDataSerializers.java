package com.adrian.thDanmakuCraft.network.syncher;

import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MyEntityDataSerializers {

    public static final EntityDataSerializer<THObjectContainer> THOBJECT_CONTAINER = EntityDataSerializer.forValueType(new StreamCodec<ByteBuf, THObjectContainer>() {
        public void encode(@NotNull ByteBuf byteBuf, @NotNull THObjectContainer container) {
            container.writeSpawnData((FriendlyByteBuf) byteBuf);
        }
        public @NotNull THObjectContainer decode(@NotNull ByteBuf byteBuf) {
            THObjectContainer container = new THObjectContainer(null);
            container.readSpawnData((FriendlyByteBuf) byteBuf);
            return container;
        }
    });

    public static final EntityDataSerializer<THObject> THOBJECT = EntityDataSerializer.forValueType(new StreamCodec<ByteBuf, THObject>() {
        public void encode(@NotNull ByteBuf byteBuf, @NotNull THObject object) {
            FriendlyByteBuf friendlyByteBuf = (FriendlyByteBuf) byteBuf;
            friendlyByteBuf.writeResourceLocation(object.getType().getKey());
            object.writeData(friendlyByteBuf);
        }
        public @NotNull THObject decode(@NotNull ByteBuf byteBuf) {
            FriendlyByteBuf friendlyByteBuf = (FriendlyByteBuf) byteBuf;
            ResourceLocation key = friendlyByteBuf.readResourceLocation();
            THObject object = THObjectType.getValue(key).create(null);
            object.readData(friendlyByteBuf);
            return object;
        };
    });

    static {
        EntityDataSerializers.registerSerializer(THOBJECT_CONTAINER);
        EntityDataSerializers.registerSerializer(THOBJECT);
    }

}
