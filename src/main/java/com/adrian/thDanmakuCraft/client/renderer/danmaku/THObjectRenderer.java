package com.adrian.thDanmakuCraft.client.renderer.danmaku;

import com.adrian.thDanmakuCraft.client.renderer.THObjectRenderHelper;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

@OnlyIn(value = Dist.CLIENT)
public class THObjectRenderer extends AbstractTHObjectRenderer<THObject> {

    public THObjectRenderer(THObjectRendererProvider.Context context) {
        super(context);
    }

    public void render(THObject object, Vec3 objectPos, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay) {
        if (object.color.a <= 0) {
            return;
        }

        poseStack.pushPose();
        if (object.getShouldFaceCamera()) {
            poseStack.mulPose(this.getRenderDispatcher().cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        } else {
            Vector3f rotation = object.getRotation();
            poseStack.mulPose(new Quaternionf().rotationYXZ(rotation.y, rotation.x, rotation.z));
        }
        Vector3f scale = object.getScale();
        poseStack.scale(scale.x, scale.y, scale.z);
        PoseStack.Pose posestack$pose = poseStack.last();
        VertexConsumer vertexconsumer = bufferSource.getBuffer(object.blend.renderType.apply(object.getTexture()));

        THObjectRenderHelper.renderTexture(vertexconsumer, posestack$pose, combinedOverlay,
                new Vector3f(-0.5f, -0.5f, 0.0f), new Vector2f(0.0f, 1.0f),
                new Vector3f(0.5f, -0.5f, 0.0f), new Vector2f(1.0f, 1.0f),
                new Vector3f(0.5f, 0.5f, 0.0f), new Vector2f(1.0f, 0.0f),
                new Vector3f(-0.5f, 0.5f, 0.0f), new Vector2f(0.0f, 0.0f),
                object.color);
        poseStack.popPose();
    }
}
