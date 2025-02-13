package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public class EntityTHSpellCardRenderer extends EntityTHObjectContainerRenderer<EntityTHSpellCard>{
    public EntityTHSpellCardRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityTHSpellCard entity, float rotationX, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedOverlay) {
        super.render(entity, rotationX, partialTicks, poseStack, bufferSource, combinedOverlay);
    }
}
