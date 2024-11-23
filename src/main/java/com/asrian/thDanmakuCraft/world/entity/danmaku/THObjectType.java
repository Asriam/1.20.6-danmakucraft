package com.asrian.thDanmakuCraft.world.entity.danmaku;

import com.asrian.thDanmakuCraft.registries.THDanmakuCraftRegistries;
import com.asrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public record THObjectType<T extends THObject>(THObjectFactory<T> factory) {

    public static ResourceLocation getKey(THObjectType<? extends THObject> object) {
        return THDanmakuCraftRegistries.THOBJECT_TYPE.getKey(object);
    }

    public ResourceLocation getKey() {
        return THDanmakuCraftRegistries.THOBJECT_TYPE.getKey(this);
    }

    public Class<? extends THObject> getBaseClass() {
        return THObject.class;
    }

    @Nullable
    public static THObjectType<? extends THObject> getValue(ResourceLocation key) {
        return THDanmakuCraftRegistries.THOBJECT_TYPE.getValue(key);
    }

    public T create(EntityTHObjectContainer container) {
        return this.factory.create(this, container);
    }

    public record Builder<T extends THObject>(THObjectFactory<T> factory) {

        public static <T extends THObject> Builder<T> of(THObjectType.THObjectFactory<T> factory) {
            return new THObjectType.Builder<>(factory);
        }

        public THObjectType<T> build() {
            return new THObjectType<>(this.factory);
        }
    }

    public interface THObjectFactory<T extends THObject> {
        T create(THObjectType<T> type, EntityTHObjectContainer container);
    }
}
