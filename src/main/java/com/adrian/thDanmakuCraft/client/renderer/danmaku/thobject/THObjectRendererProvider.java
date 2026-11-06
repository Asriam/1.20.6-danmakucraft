package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject;

import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public interface THObjectRendererProvider<T extends THObject> {

    AbstractTHObjectRenderer<T> create(THObjectRendererProvider.Context context);

    @OnlyIn(Dist.CLIENT)
    record Context() {

    }
}
