package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.adrian.thDanmakuCraft.client.renderer.danmaku.THObjectContainerRenderer;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class EntityTHObjectContainerRenderer<T extends EntityTHObjectContainer> extends EntityRenderer<T> {

    public EntityTHObjectContainerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float rotationX, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedOverlay) {
        //super.render(entity, rotationX, partialTicks, poseStack, bufferSource, combinedOverlay);
        THObjectContainerRenderer.render(this.getRenderDispatcher(), this.getFrustum(), entity.getContainer(), partialTicks, poseStack, bufferSource, combinedOverlay);
    }

    @Override
    public boolean shouldRender(@NotNull T entity, @NotNull Frustum frustum, double camX, double camY, double camZ) {
        //this.frustum = frustum;
        return true;
    }

    public EntityRenderDispatcher getRenderDispatcher(){
        return this.entityRenderDispatcher;
    }

    public Frustum getFrustum(){
        return Minecraft.getInstance().levelRenderer.getFrustum();
    }

    @Override
    @NotNull
    public ResourceLocation getTextureLocation(@NotNull EntityTHObjectContainer container) {
        return null;
    }
}
