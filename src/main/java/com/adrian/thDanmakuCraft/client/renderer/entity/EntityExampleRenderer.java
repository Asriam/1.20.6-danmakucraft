package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.client.renderer.THObjectRenderHelper;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.world.entity.EntityExample;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityExampleRenderer extends EntityRenderer<EntityExample> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(THDanmakuCraftCore.MOD_ID,"textures/danmaku/ball_mid.png");
    private static final ResourceLocation TEXTURE_LOCATION2 = new ResourceLocation(THDanmakuCraftCore.MOD_ID,"textures/danmaku/arrow_big.png");
    //private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);

    public EntityExampleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityExample entity, float p_114706_, float p_114707_, PoseStack poseStack, MultiBufferSource bufferSource, int p_114710_){
        poseStack.pushPose();
        poseStack.scale(0.5f,0.5f,0.5f);

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0f,0.0f,0.0f);
        PoseStack.Pose posestack$pose = poseStack.last();
        /*
        Matrix4f pose = posestack$pose.pose();
        Matrix3f normal = posestack$pose.normal();
         */

        int index = entity.getIndex();
        VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.RENDER_TYPE_2D_DANMAKU.apply(new THRenderType.RENDER_TYPE_2D_DANMAKU_CONTEXT(TEXTURE_LOCATION2,THObjectRenderHelper.parseBlend(THObject.Blend.add))));
        vertex(vertexconsumer, posestack$pose, p_114710_, 0.0F, 0, 0, 1.0f/16*index);
        vertex(vertexconsumer, posestack$pose, p_114710_, 1.0F, 0, 1, 1.0f/16*index);
        vertex(vertexconsumer, posestack$pose, p_114710_, 1.0F, 1, 1, 1.0f/16*(index-1));
        vertex(vertexconsumer, posestack$pose, p_114710_, 0.0F, 1, 0, 1.0f/16*(index-1));

        /*
        vertex(vertexconsumer, matrix4f, matrix3f, p_114710_, 0.0F, 0, 0, 1.0f/16);
        vertex(vertexconsumer, matrix4f, matrix3f, p_114710_, 1.0F, 0, 2, 1.0f/16);
        vertex(vertexconsumer, matrix4f, matrix3f, p_114710_, 1.0F, 2, 2, 0);
        vertex(vertexconsumer, matrix4f, matrix3f, p_114710_, 0.0F, 2, 0, 0);
         */
        poseStack.popPose();
        super.render(entity, p_114706_, p_114707_, poseStack, bufferSource, p_114710_);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose poseStack, int p_254296_, float p_253632_, int p_254132_, float u, float v) {
        consumer.vertex(poseStack.pose(), p_253632_ - 0.5F, (float)p_254132_ - 0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_254296_)
                .normal(poseStack, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityExample p_114482_) {
        return TEXTURE_LOCATION;
    }
}
