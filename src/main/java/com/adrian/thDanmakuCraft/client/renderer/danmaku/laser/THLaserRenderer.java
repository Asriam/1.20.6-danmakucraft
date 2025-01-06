package com.adrian.thDanmakuCraft.client.renderer.danmaku.laser;

import com.adrian.thDanmakuCraft.client.renderer.danmaku.AbstractTHObjectRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.THObjectRendererProvider;
import com.adrian.thDanmakuCraft.world.danmaku.laser.THLaser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public class THLaserRenderer extends AbstractTHObjectRenderer<THLaser> {

    public THLaserRenderer(THObjectRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(THLaser laser, Vec3 objectPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer, int combinedOverlay) {
    }


}
