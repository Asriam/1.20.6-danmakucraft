package com.adrian.thDanmakuCraft.client.renderer.danmaku;

import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHObjectContainerRenderer;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public interface THObjectRendererProvider<T extends THObject> {

    AbstractTHObjectRenderer<T> create(THObjectRendererProvider.Context context);

    @OnlyIn(Dist.CLIENT)
    record Context(EntityTHObjectContainerRenderer mainRenderer) {

    }
}
