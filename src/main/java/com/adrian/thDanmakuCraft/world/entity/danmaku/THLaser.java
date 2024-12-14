package com.adrian.thDanmakuCraft.world.entity.danmaku;

import com.adrian.thDanmakuCraft.client.renderer.danmaku.AbstractTHObjectRenderer;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHObjectContainerRenderer;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class THLaser extends THObject {

    public float width;
    public float length;

    public THLaser(THObjectType<THLaser> type, EntityTHObjectContainer container) {
        super(type, container);
    }

    public THLaser(EntityTHObjectContainer container){
        this(THObjectInit.TH_LASER.get(),container);
    }


}
