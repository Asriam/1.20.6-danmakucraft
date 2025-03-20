package com.adrian.thDanmakuCraft.client.renderer.entity.mount;

import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.entity.mount.BroomMount;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BroomMountRenderer extends EntityRenderer<BroomMount> {

    ResourceLocation TEXTURE = ResourceLocationUtil.thdanmakucraft("textures/entity/mount/broom_mount.png");
    final Model model = null;
    public BroomMountRenderer(EntityRendererProvider.Context context) {
        super(context);
        //this.model = new BroomMountModel<>(context.bakeLayer(BroomMountModel.LAYER_LOCATION));
    }

    @Override
   public void render(BroomMount entity, float xRot, float partailTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay) {
       super.render(entity, xRot, partailTicks, poseStack, bufferSource, combinedOverlay);
       RenderType renderType = this.model.renderType(this.getTextureLocation(entity));
       VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);
       this.model.renderToBuffer(poseStack,vertexconsumer,0,combinedOverlay,1.0f,1.0f,1.0f,1.0f);
   }

    @Override
    public ResourceLocation getTextureLocation(BroomMount entity) {
        return TEXTURE;
    }
}
