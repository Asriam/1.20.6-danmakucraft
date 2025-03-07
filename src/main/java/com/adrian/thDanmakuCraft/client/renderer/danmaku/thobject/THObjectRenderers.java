package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject;


import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.bullet.THBulletRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.laser.THCurvyLaserRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.laser.THLaserRenderer;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

@OnlyIn(value = Dist.CLIENT)
public class THObjectRenderers {
    private static final Map<THObjectType<? extends THObject>, THObjectRendererProvider<? extends THObject>> PROVIDERS = new Object2ObjectOpenHashMap<>();

    public static <T extends THObject> void register(THObjectType<T> type, THObjectRendererProvider<T> provider) {
        PROVIDERS.put(type, provider);
    }

    public static Map<THObjectType<? extends THObject>, AbstractTHObjectRenderer<? extends THObject>> createEntityRenderers(THObjectRendererProvider.Context context) {
        ImmutableMap.Builder<THObjectType<? extends THObject>, AbstractTHObjectRenderer<? extends THObject>> builder = ImmutableMap.builder();
        PROVIDERS.forEach((type, provider) -> builder.put(type, provider.create(context)));
        return builder.build();
    }

    static {
        register(THObjectInit.TH_OBJECT.get(),       THObjectRenderer::new);
        register(THObjectInit.TH_BULLET.get(),       THBulletRenderer::new);
        register(THObjectInit.TH_LASER.get(),        THLaserRenderer::new);
        register(THObjectInit.TH_CURVY_LASER.get(), THCurvyLaserRenderer::new);
    }
}
