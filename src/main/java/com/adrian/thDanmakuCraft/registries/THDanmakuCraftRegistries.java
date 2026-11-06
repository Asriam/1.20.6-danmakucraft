package com.adrian.thDanmakuCraft.registries;

import com.adrian.thDanmakuCraft.util.io.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * 自訂註冊表。NeoForge 用 {@code NewRegistryEvent#create(RegistryBuilder)} 建立，
 * 建立後的 {@link Registry} 實例存在這裡供 {@link THObjectType} 查詢。
 */
public final class THDanmakuCraftRegistries {

    public static class Keys {
        public static final ResourceKey<Registry<THObjectType>> THOBJECT_TYPE =
                ResourceKey.createRegistryKey(ResourceLocationUtil.thdanmakucraft("th_object"));
    }

    /** 由 {@code CommonModEvents#newRegistry} 填入。 */
    public static volatile Registry<THObjectType> THOBJECT_TYPE;

    private THDanmakuCraftRegistries() {
    }
}
