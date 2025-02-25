package com.adrian.thDanmakuCraft.client.renderer.entity.mount;

import com.adrian.thDanmakuCraft.world.entity.mount.BroomMount;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BroomMountRenderer extends EntityRenderer<BroomMount> {
    protected BroomMountRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public ResourceLocation getTextureLocation(BroomMount p_114482_) {
        return null;
    }
}
