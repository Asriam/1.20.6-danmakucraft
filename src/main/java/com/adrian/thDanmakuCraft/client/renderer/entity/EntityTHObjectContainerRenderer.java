package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.adrian.thDanmakuCraft.client.renderer.*;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class EntityTHObjectContainerRenderer extends EntityRenderer<EntityTHObjectContainer> {

    public EntityTHObjectContainerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityTHObjectContainer entity, float rotationX, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedOverlay) {
        //super.render(entity, rotationX, partialTicks, poseStack, bufferSource, combinedOverlay);
        THObjectContainerRenderer.render(this.getRenderDispatcher(), this.getFrustum(), entity.getContainer(), partialTicks, poseStack, bufferSource, combinedOverlay);
    }

    @Override
    public boolean shouldRender(@NotNull EntityTHObjectContainer entity, @NotNull Frustum frustum, double camX, double camY, double camZ) {
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
