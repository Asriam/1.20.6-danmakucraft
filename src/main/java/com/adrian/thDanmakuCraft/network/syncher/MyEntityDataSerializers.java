package com.adrian.thDanmakuCraft.network.syncher;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class MyEntityDataSerializers {

    /*private static final EntityDataSerializer<THObjectContainer> THOBJECT_CONTAINER = EntityDataSerializer.forValueType(new StreamCodec<FriendlyByteBuf, THObjectContainer>() {
        public void encode(@NotNull FriendlyByteBuf byteBuf, @NotNull THObjectContainer container) {
            container.encode(byteBuf);
        }
        public @NotNull THObjectContainer decode(@NotNull FriendlyByteBuf byteBuf) {
            THObjectContainer container = new THObjectContainer(null);
            container.decode(byteBuf);
            return container;
        }
    });*/

    private static final EntityDataSerializer<THObjectContainer> THOBJECT_CONTAINER = new EntityDataSerializer<THObjectContainer>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, THObjectContainer> codec() {
            return new StreamCodec<FriendlyByteBuf, THObjectContainer>() {
                public void encode(@NotNull FriendlyByteBuf byteBuf, @NotNull THObjectContainer container) {
                    container.encode(byteBuf);
                }
                public @NotNull THObjectContainer decode(@NotNull FriendlyByteBuf byteBuf) {
                    THObjectContainer container = new THObjectContainer(null);
                    container.decode(byteBuf);
                    return container;
                }
            };
        }

        @Override
        public THObjectContainer copy(THObjectContainer container) {
            return container;
        }
    };

    private static final EntityDataSerializer<THObject> THOBJECT = EntityDataSerializer.forValueType(new StreamCodec<FriendlyByteBuf, THObject>() {
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

    /*static {
        EntityDataSerializers.registerSerializer(THOBJECT_CONTAINER);
        EntityDataSerializers.registerSerializer(THOBJECT);
    }*/

    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, THDanmakuCraftMod.MOD_ID);

    // Register the serializer
    public static final RegistryObject<EntityDataSerializer<THObjectContainer>> THOBJECT_CONTAINER_SERIALIZER = ENTITY_DATA_SERIALIZERS.register("thobject_container_serializer", () -> THOBJECT_CONTAINER);

    public static EntityDataSerializer<THObjectContainer> getTHObjectContainerSerializer() {
        return THOBJECT_CONTAINER_SERIALIZER.get();
    }
}
