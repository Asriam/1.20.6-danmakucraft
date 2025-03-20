package com.adrian.thDanmakuCraft.registries;

import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.*;

public class THDanmakuCraftRegistries {

    public static final IForgeRegistry<THObjectType> THOBJECT_TYPE = active(Keys.THOBJECT_TYPE);
    public static class Keys {
        /**
         * com.adrian.thDanmakuCraft.event.newRegistry;
         */
        public static final ResourceKey<Registry<THObjectType>> THOBJECT_TYPE = key("th_object");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(ResourceLocationUtil.thdanmakucraft(name));
        }
    }

    private static <T> IForgeRegistry<T> active(ResourceKey<Registry<T>> key) {
        return RegistryManager.ACTIVE.getRegistry(key);
    }

}
