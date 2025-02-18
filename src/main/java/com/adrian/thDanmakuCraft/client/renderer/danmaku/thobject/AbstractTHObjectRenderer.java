package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject;

import com.adrian.thDanmakuCraft.client.renderer.THBlendMode;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public abstract class AbstractTHObjectRenderer<T extends THObject>{

    private final EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

    public AbstractTHObjectRenderer(THObjectRendererProvider.Context context) {
    }

    public abstract void render(T object, Vec3 objectPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer, int combinedOverlay);

    public RenderType getRenderType(T object){
        return THRenderType.RENDER_TYPE_THOBJECT.apply(
                new THRenderType.RENDER_TYPE_2D_DANMAKU_CONTEXT(
                object.getImage().getTextureLocation(),
                THBlendMode.getBlendMode(object.getBlend()))
        );
    }

    public EntityRenderDispatcher getRenderDispatcher(){
        return renderDispatcher;
    }

    public Frustum getFrustum(){
        return Minecraft.getInstance().levelRenderer.getFrustum();
    }
}
