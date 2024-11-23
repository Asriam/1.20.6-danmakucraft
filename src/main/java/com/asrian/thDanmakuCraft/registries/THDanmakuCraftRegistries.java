package com.asrian.thDanmakuCraft.registries;

import com.asrian.thDanmakuCraft.world.entity.danmaku.THObjectType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.*;

public class THDanmakuCraftRegistries {

    public static final IForgeRegistry<THObjectType> THOBJECT_TYPE = active(Keys.THOBJECT_TYPE);
    public class Keys {
        /**
         * com.asrian.thDanmakuCraft.event.newRegistry;
         */
        public static final ResourceKey<Registry<THObjectType>> THOBJECT_TYPE = key("th_object");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(new ResourceLocation(name));
        }
    }

    private static <T> IForgeRegistry<T> active(ResourceKey<Registry<T>> key) {
        return RegistryManager.ACTIVE.getRegistry(key);
    }

}
