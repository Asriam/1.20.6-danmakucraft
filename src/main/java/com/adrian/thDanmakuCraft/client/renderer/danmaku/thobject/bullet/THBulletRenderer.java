package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.bullet;

import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.AbstractTHObjectRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.THObjectRendererProvider;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(value = Dist.CLIENT)
public class THBulletRenderer extends AbstractTHObjectRenderer<THBullet> {

    public THBulletRenderer(THObjectRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(THBullet bullet, Vec3 bulletPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer, int combinedOverlay) {
        if (bullet.color.a <= 0) {
            return;
        }
        //poseStack.pushPose();
        this.renderTHBullet(bullet.getStyle(), bullet, bulletPos, partialTicks, poseStack, vertexConsumer, combinedOverlay);
        //poseStack.popPose();
    }

    public void renderTHBullet(THBullet.DefaultBulletStyle style, THBullet bullet, Vec3 bulletPos, float partialTicks, PoseStack poseStack, VertexConsumer vertexConsumer, int overlay) {
        poseStack.pushPose();
        THBulletRenderers.THBulletRendererFactory factory = THBulletRenderers.getRenderer(style);
        if (style.is3D() && factory != null) {
            int time = 6;
            float offSetTimer = bullet.getTimer() + partialTicks;
            boolean flag = bullet.spawnAnimation && offSetTimer < time-time*0.2f;
            if(flag){
                float scale = bullet.getScale().x * 1.6f * (float) Math.pow((double) (time - offSetTimer) / time,1.2f);
                poseStack.pushPose();
                poseStack.mulPose(this.getRenderDispatcher().cameraOrientation());
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

                Color bulletColor = bullet.getBulletColor();
                RenderUtil.renderSphere(vertexConsumer, poseStack.last(), 1,
                        Vec3.ZERO,
                        new Vec3(scale, scale, scale),
                        6, 10, true,
                        new Vec2(0.4f, 2.0f),
                        new Vec2(0.0f, 2.0f),
                        bulletColor,
                        bulletColor,
                        bullet.getColor());
                poseStack.popPose();
            }else {
                if (style.shouldFaceCamera()) {
                    poseStack.mulPose(this.getRenderDispatcher().cameraOrientation());
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                } else {
                    Vector3f rotation = bullet.getRotation();
                    poseStack.mulPose(new Quaternionf().rotationYXZ(rotation.y, -rotation.x + Mth.DEG_TO_RAD * 90.0f, rotation.z));
                }
                THBulletRenderers.render3DBullet(this, bullet, poseStack, vertexConsumer, factory, partialTicks, overlay);
            }

        } else {
            //poseStack.mulPose(this.getRenderDispatcher().cameraOrientation());
            Vec2 rotation = THObject.VectorAngleToRadAngle(this.getRenderDispatcher().camera.getPosition().vectorTo(bulletPos));
            //poseStack.mulPose(new Quaternionf().rotateYXZ(rotation.y,-rotation.x,0.0f));
            poseStack.mulPose(new Quaternionf().rotateY(rotation.y).rotateX(-rotation.x));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            THBulletRenderers.render2DBullet(this, bullet, poseStack, vertexConsumer, partialTicks, overlay);
        }
        poseStack.popPose();
    }

    @Override
    public RenderType getRenderType(THBullet bullet) {
        if(bullet.getStyle().is3D()){
            return THBulletRenderers.getRenderer(bullet.getStyle()).getRenderType(bullet);
        }else {
            return THBulletRenderers.getBullet2DRenderer().getRenderType(bullet);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public enum BULLET_LEVEL_OF_DETAIL {
        VERY_VERY_CLOSE(12,12,false),
        VERY_CLOSE(10,10,false),
        CLOSE(8,8,false),
        MEDIUM(6,6,false),
        FAR(5,6,false),
        VERY_FAR(4,4,true),
        TEST(30,30,false);

        public static final double[] distOfLevel = {4.0D,8.0D,16.0D,32.0D,48.0D,60.0D,80.0D};
        public final int edgeANum;
        public final int edgeBNum;
        public boolean is2D;

        BULLET_LEVEL_OF_DETAIL(int edgeA, int edgeB, boolean is2D){
            this.edgeANum = edgeA;
            this.edgeBNum = edgeB;
            this.is2D = is2D;
        }

        public static BULLET_LEVEL_OF_DETAIL getQualityLevel(THObject object, double camX, double camY, double camZ){
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
