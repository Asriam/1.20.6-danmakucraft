package com.adrian.thDanmakuCraft.client.renderer.danmaku;

import com.adrian.thDanmakuCraft.world.entity.danmaku.THLaser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public class THLaserRenderer extends AbstractTHObjectRenderer<THLaser> {

    public THLaserRenderer(THObjectRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(THLaser laser, Vec3 objectPos, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay) {
    }
}