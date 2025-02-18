package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject;

import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.IImage;
import com.adrian.thDanmakuCraft.util.VertexBuilder;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(value = Dist.CLIENT)
public class THObjectRenderer extends AbstractTHObjectRenderer<THObject> {

    public THObjectRenderer(THObjectRendererProvider.Context context) {
        super(context);
    }

    public void render(THObject object, Vec3 objectPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer, int combinedOverlay) {
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

        IImage.Image image = object.getImage();

        Color color = object.getColor();
        Vec2 uvStart = image.getUVStart();
        Vec2 uvEnd =   image.getUVEnd();
        Vec2 _scale = Vec2.ONE.scale(0.5f);

        VertexBuilder builder = new VertexBuilder(vertexConsumer);
        builder.positionColorUV(posestack$pose.pose(), -_scale.x, -_scale.y, 0.0f, color, uvStart.x, uvEnd.y);
        builder.positionColorUV(posestack$pose.pose(), _scale.x, -_scale.y, 0.0f, color, uvStart.x, uvStart.y);
        builder.positionColorUV(posestack$pose.pose(), _scale.x, _scale.y, 0.0f, color, uvEnd.x, uvStart.y);
        builder.positionColorUV(posestack$pose.pose(), -_scale.x, _scale.y, 0.0f, color, uvEnd.x, uvEnd.y);
        poseStack.popPose();
    }
}
