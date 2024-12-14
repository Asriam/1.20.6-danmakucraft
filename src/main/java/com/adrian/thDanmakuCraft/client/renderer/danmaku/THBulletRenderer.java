package com.adrian.thDanmakuCraft.client.renderer.danmaku;

import com.adrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(value = Dist.CLIENT)
public class THBulletRenderer extends AbstractTHObjectRenderer<THBullet> {

    public THBulletRenderer(THObjectRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(THBullet bullet, Vec3 bulletPos, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay) {
        if (bullet.color.a <= 0) {
            return;
        }
        poseStack.pushPose();
        this.renderTHBullet(bullet.getStyle(), bullet, partialTicks, poseStack, bufferSource, combinedOverlay);
        poseStack.popPose();
    }

    public void renderTHBullet(THBullet.BULLET_STYLE style, THBullet object, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int overlay) {
        poseStack.pushPose();
        THBulletRenderers.THBulletRendererFactory factory = THBulletRenderers.getRenderFactory(style);
        if (style.getIs3D() && factory != null) {
            if (style.getShouldFaceCamera()) {
                poseStack.mulPose(this.getRenderDispatcher().cameraOrientation());
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            } else {
                Vector3f rotation = object.getRotation();
                poseStack.mulPose(new Quaternionf().rotationYXZ(rotation.y, -rotation.x + Mth.DEG_TO_RAD * 90.0f, rotation.z));
            }
            THBulletRenderers.render3DBullet(this, object, factory, partialTicks, poseStack, bufferSource, overlay);

        } else {
            poseStack.mulPose(this.getRenderDispatcher().cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            THBulletRenderers.render2DBullet(this, object, partialTicks, poseStack, bufferSource, overlay);
        }
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public enum BULLET_QUALITY_LEVEL {
        VERY_VERY_CLOSE(12,12,false),
        VERY_CLOSE(10,10,false),
        CLOSE(8,8,false),
        MEDIUM(6,6,false),
        FAR(6,5,false),
        VERY_FAR(4,4,true);

        public final int edgeANum;
        public final int edgeBNum;
        public boolean is2D;

        BULLET_QUALITY_LEVEL(int edgeA, int edgeB, boolean is2D){
            this.edgeANum = edgeA;
            this.edgeBNum = edgeB;
            this.is2D = is2D;
        }

        public static BULLET_QUALITY_LEVEL getQualityLevel(THObject object, double camX, double camY, double camZ){
            Vec3 pos = object.getPosition();
            double d0 = pos.x - camX;
            double d1 = pos.y - camY;
            double d2 = pos.z - camZ;
            double distSquare = (d0 * d0 + d1 * d1 + d2 * d2);

            double d4 = object.getBoundingBoxForCulling().getSize() * 4.0D;
            if (Double.isNaN(d4)) {
                d4 = 4.0D;
            }
            d4 *= d4;

            double[] distOfLevel = {2.0D,4.0D,8.0D,16.0D,32.0D,48.0D,60.0D};

            if(distSquare < d4*distOfLevel[0]*distOfLevel[0]){
                return VERY_VERY_CLOSE;
            }else if(distSquare < d4*distOfLevel[1]*distOfLevel[1]){
                return VERY_CLOSE;
            }else if(distSquare < d4*distOfLevel[2]*distOfLevel[2]){
                return CLOSE;
            }else if(distSquare < d4*distOfLevel[3]*distOfLevel[3]){
                return MEDIUM;
            }else if(distSquare < d4*distOfLevel[4]*distOfLevel[4]){
                return FAR;
            }
            return VERY_FAR;
        }
    }
}
