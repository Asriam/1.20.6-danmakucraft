package com.adrian.thDanmakuCraft.client.renderer.danmaku.thobject.bullet;

import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.THBlendMode;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.client.renderer.VertexBuilder;
import com.adrian.thDanmakuCraft.util.ConstantUtil;
import com.adrian.thDanmakuCraft.util.IImage;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.Blend;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.EnumMap;

@OnlyIn(Dist.CLIENT)
public class THBulletRenderers {

    public static void render2DBullet(THBulletRenderer renderer, THBullet bullet, PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks) {
        poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
        PoseStack.Pose pose = poseStack.last();
        IImage.Image image = bullet.getImage();

        Color color = bullet.getColor();
        Color bulletColor = bullet.getBulletColor();
        Vec2 uvStart = image.getUVStart();
        Vec2 uvEnd =   image.getUVEnd();
        Vec2 scale = Vec2.ONE.scale(0.5f);

        VertexBuilder builder = new VertexBuilder(vertexConsumer);
        builder.positionColorColorUV(pose.pose(), -scale.x, -scale.y, 0.0f, color, bulletColor, uvStart.x, uvEnd.y);
        builder.positionColorColorUV(pose.pose(), scale.x, -scale.y, 0.0f, color, bulletColor, uvStart.x, uvStart.y);
        builder.positionColorColorUV(pose.pose(), scale.x, scale.y, 0.0f, color, bulletColor, uvEnd.x, uvStart.y);
        builder.positionColorColorUV(pose.pose(), -scale.x, scale.y, 0.0f, color, bulletColor, uvEnd.x, uvEnd.y);
    }

    public static void render3DBullet(THBulletRenderer renderer, THBullet bullet, PoseStack poseStack, VertexConsumer vertexConsumer, THBulletRendererFactory factory, float partialTicks) {
        //Color indexColor = bullet.getBulletIndexColor().getColor();
        Color indexColor = bullet.getBulletColor();
        Color color = THObject.Color(
                bullet.color.r * indexColor.r / 255,
                bullet.color.g * indexColor.g / 255,
                bullet.color.b * indexColor.b / 255,
                (int) (bullet.color.a * 0.6)
        );
        Color coreColor = bullet.color;
        factory.render(renderer, bullet, vertexConsumer, poseStack, partialTicks, color, coreColor);
    }

    public static abstract class BulletRenderer implements THBulletRendererFactory {
        public boolean shouldCull = true;
        protected static RenderType defaultRenderType = THRenderType.TEST_RENDER_TYPE_FUNCTION.apply(new THRenderType.TEST_RENDER_TYPE_FUNCTION_CONTEXT(THBlendMode.getBlendMode(Blend.add), true));

        public abstract void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, float partialTicks, Color color, Color coreColor);

        public RenderType getRenderType(THBullet bullet){
            return THRenderType.TEST_RENDER_TYPE_FUNCTION.apply(new THRenderType.TEST_RENDER_TYPE_FUNCTION_CONTEXT(THBlendMode.getBlendMode(bullet.getBlend()), this.shouldCull));
        }

        public static class bullet_2d extends BulletRenderer{
            bullet_2d(){
                this.shouldCull = true;
            }
            @Override
            public void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, float partialTicks, Color color, Color coreColor) {
                render2DBullet(renderer, bullet, poseStack, vertexconsumer, partialTicks);
            }

            public RenderType getRenderType(THBullet bullet){
                return THRenderType.RENDER_TYPE_2D_DANMAKU.apply(
                        new THRenderType.RENDER_TYPE_2D_DANMAKU_CONTEXT(
                        bullet.getImage().getTextureLocation(),
                        THBlendMode.getBlendMode(bullet.getBlend()))
                );
            }
        }

        public static class arrow_big extends BulletRenderer {
            arrow_big() {
                this.shouldCull = true;
            }

            public void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, float partialTicks, Color color, Color coreColor) {
                this.shouldCull = true;
                poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
                Vector3f scale = new Vector3f(0.4f, 0.8f, 0.4f);
                Vector3f coreScale = scale.mul(0.4f, 0.4f, 0.4f, new Vector3f());
                PoseStack.Pose posestack_pose = poseStack.last();
                Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
                THBulletRenderer.BULLET_LEVEL_OF_DETAIL cull = THBulletRenderer.BULLET_LEVEL_OF_DETAIL.getQualityLevel(bullet, camPos.x, camPos.y, camPos.z);
                int edgeA = cull.edgeANum;
                int edgeB = cull.edgeBNum;
                this.shouldCull = true;
                Vector3f offset = new Vector3f(0.0f, -0.45f, 0.0f);
                RenderUtil.renderSphere(vertexconsumer, posestack_pose, 2,
                        offset.add(0.0f, 0.4f, 0.0f, new Vector3f()),
                        coreScale,
                        edgeA, edgeB, false,
                        new Vec2(0.8f, 1.0f),
                        new Vec2(0.2f, 3.0f),
                        color, 20, coreColor);
                RenderUtil.renderSphere(vertexconsumer, posestack_pose, 1.2f,
                        offset,
                        scale,
                        edgeA, edgeB, true,
                        new Vec2(0.4f, 2.0f),
                        new Vec2(0.4f, 2.0f),
                        color, color.setAlpha(0), coreColor.multiply(0.3f), true, false);
                /*
                this.shouldCull = true;
                VertexBuilder builder1 = new VertexBuilder(THObjectContainerRenderer.BUFFER_2);
                VertexBuilder builder2 = new VertexBuilder(vertexconsumer);
                new SphereRenderer(
                        posestack_pose.pose(), posestack_pose.normal(),
                        offset.add(0.0f, 0.4f, 0.0f, new Vector3f()), coreScale,
                        edgeA, edgeB,
                        1.0f, false,
                        color.multiply(0.6f), color.multiply(0.3f), false
                ).render((pos, normal, vertexColor) -> {
                    builder1.vertex(pos).color(vertexColor).color(coreColor).uv(0.8f, 1.0f).uv(0.2f, 3.0f).normal(normal).endVertex();
                });
                this.shouldCull = false;
                new SphereRenderer(
                        posestack_pose.pose(), posestack_pose.normal(),
                        offset, scale,
                        edgeA, edgeB,
                        1.2f, true,
                        color, color.setAlpha(0), false
                ).render((pos, normal, vertexColor) -> {
                    builder2.vertex(pos).color(vertexColor).color(coreColor.multiply(0.3f)).uv(0.4f, 2.0f).uv(0.4f, 2.0f).normal(normal).endVertex();
                });*/
            }
        }

        public static class ball_small extends BulletRenderer {
            public void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, float partialTicks, Color color, Color coreColor) {
                poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
                Vector3f scale = new Vector3f(0.2f, 0.2f, 0.2f);
                //Vec3 coreScale = scale.multiply(0.6f,0.6f,0.6f);
                PoseStack.Pose posestack_pose = poseStack.last();
                Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
                THBulletRenderer.BULLET_LEVEL_OF_DETAIL cull = THBulletRenderer.BULLET_LEVEL_OF_DETAIL.getQualityLevel(bullet, camPos.x, camPos.y, camPos.z);
                int edgeA = cull.edgeANum;
                int edgeB = cull.edgeBNum;
                RenderUtil.renderSphere(vertexconsumer, posestack_pose, 1,
                        ConstantUtil.VECTOR3F_ZERO,
                        scale,
                        edgeA, edgeB, false,
                        new Vec2(0.33f, 6.0f),
                        new Vec2(-0.1f, 1.0f),
                        color, color, coreColor);
            }
        }

        public static class ball_mid extends BulletRenderer {
            public void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, float partialTicks, Color color, Color coreColor) {
                poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
                Vector3f scale = new Vector3f(0.5f, 0.5f, 0.5f);
                //Vec3 coreScale = scale.multiply(0.6f,0.6f,0.6f);
                PoseStack.Pose posestack_pose = poseStack.last();
                Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
                THBulletRenderer.BULLET_LEVEL_OF_DETAIL cull = THBulletRenderer.BULLET_LEVEL_OF_DETAIL.getQualityLevel(bullet, camPos.x, camPos.y, camPos.z);
                int edgeA = cull.edgeANum;
                int edgeB = cull.edgeBNum;
                RenderUtil.renderSphere(vertexconsumer, posestack_pose, 1,
                        ConstantUtil.VECTOR3F_ZERO,
                        scale,
                        edgeA, edgeB, false,
                        new Vec2(0.5f, 2.0f),
                        new Vec2(0.0f, 2.0f),
                        color, color, coreColor);
            }
        }

        public static class ball_big extends BulletRenderer {
            public void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, float partialTicks, Color color, Color coreColor) {
                poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
                Vector3f scale = new Vector3f(0.8f, 0.8f, 0.8f);
                //Vec3 coreScale = scale.multiply(0.8f, 0.8f, 0.8f);
                PoseStack.Pose posestack_pose = poseStack.last();
                Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
                THBulletRenderer.BULLET_LEVEL_OF_DETAIL cull = THBulletRenderer.BULLET_LEVEL_OF_DETAIL.getQualityLevel(bullet, camPos.x, camPos.y, camPos.z);
                int edgeA = cull.edgeANum;
                int edgeB = cull.edgeBNum;
                RenderUtil.renderSphere(vertexconsumer, posestack_pose, 1,
                        ConstantUtil.VECTOR3F_ZERO,
                        scale,
                        edgeA, edgeB, false,
                        new Vec2(0.7f, 2.0f),
                        new Vec2(-0.1f, 2.0f),
                        color, color, coreColor);
            }
        }

        public static class ellipse extends BulletRenderer {
            public void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, float partialTicks, Color color, Color coreColor) {
                poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
                Vector3f scale = new Vector3f(0.5f, 1.0f, 0.5f);
                //Vec3 coreScale = scale.multiply(0.7f, 0.7f, 0.7f);
                PoseStack.Pose posestack_pose = poseStack.last();
                Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
                THBulletRenderer.BULLET_LEVEL_OF_DETAIL cull = THBulletRenderer.BULLET_LEVEL_OF_DETAIL.getQualityLevel(bullet, camPos.x, camPos.y, camPos.z);
                int edgeA = cull.edgeANum;
                int edgeB = cull.edgeBNum;
                RenderUtil.renderSphere(vertexconsumer, posestack_pose, 1,
                        ConstantUtil.VECTOR3F_ZERO,
                        scale,
                        edgeA, edgeB, false,
                        new Vec2(0.5f, 3.0f),
                        new Vec2(0.0f, 2.0f),
                        color, color.multiply(0.8f), coreColor.multiply(0.9f));
            }
        }

        public static class grain_a extends BulletRenderer {
            public void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, float partialTicks, Color color, Color coreColor) {
                poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
                Vector3f scale = new Vector3f(0.2f, 0.4f, 0.2f);
                //Vec3 coreScale = scale.multiply(0.6f, 0.6f, 0.6f);
                PoseStack.Pose posestack_pose = poseStack.last();
                Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
                THBulletRenderer.BULLET_LEVEL_OF_DETAIL cull = THBulletRenderer.BULLET_LEVEL_OF_DETAIL.getQualityLevel(bullet, camPos.x, camPos.y, camPos.z);
                int edgeA = cull.edgeANum;
                int edgeB = cull.edgeBNum;
                Vector3f offset = new Vector3f(0.0f, -0.1f, 0.0f);
                RenderUtil.renderSphere(vertexconsumer, posestack_pose, 2,
                        offset,
                        scale,
                        edgeA, edgeB, false,
                        new Vec2(0.4f, 3.0f),
                        new Vec2(0.1f, 2.0f),
                        color, color.multiply(0.5f), coreColor);
            }
        }

        public static class grain_b extends BulletRenderer {
            public void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, float partialTicks, Color color, Color coreColor) {
                poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
                Vector3f scale = new Vector3f(0.25f, 0.5f, 0.25f);
                Vector3f coreScale = scale.mul(0.6f, 0.6f, 0.6f, new Vector3f());
                PoseStack.Pose posestack_pose = poseStack.last();
                int edgeA = 4;
                int edgeB = 4;
                RenderUtil.renderSphere(vertexconsumer, posestack_pose, 2,
                        ConstantUtil.VECTOR3F_ZERO,
                        coreScale,
                        edgeA, edgeB, false,
                        new Vec2(0.8f, 2.0f),
                        new Vec2(0.0f, 2.0f),
                        color, color.multiply(0.5f), coreColor);
                RenderUtil.renderSphere(vertexconsumer, posestack_pose, 2,
                        ConstantUtil.VECTOR3F_ZERO,
                        scale,
                        edgeA, edgeB, false,
                        new Vec2(0.5f, 2.0f),
                        new Vec2(-0.1f, 3.0f),
                        color, color.multiply(0.5f), coreColor.multiply(0.4f));
            }
        }
    }

    private static final EnumMap<THBullet.DefaultBulletStyle, BulletRenderer> DefaultBulletRenderers = new EnumMap<>(THBullet.DefaultBulletStyle.class);
    private static final BulletRenderer Bullet2DRenderer = new THBulletRenderers.BulletRenderer.bullet_2d();
    public static void register(THBullet.DefaultBulletStyle bulletStyle, BulletRenderer renderer) {
        DefaultBulletRenderers.put(bulletStyle, renderer);
    }

    public static BulletRenderer getRenderer(THBullet.DefaultBulletStyle bulletStyle) {
        if(bulletStyle.isDefaultBulletStyle()) {
            if (DefaultBulletRenderers.containsKey(bulletStyle)) {
                return DefaultBulletRenderers.get(bulletStyle);
            }
        }
        return Bullet2DRenderer;
    }

    public static BulletRenderer getBullet2DRenderer(){
        return Bullet2DRenderer;
    }

    static {
        register(THBullet.DefaultBulletStyle.arrow_big, new BulletRenderer.arrow_big());
        register(THBullet.DefaultBulletStyle.ball_small, new BulletRenderer.ball_small());
        register(THBullet.DefaultBulletStyle.ball_mid, new BulletRenderer.ball_mid());
        register(THBullet.DefaultBulletStyle.ball_big, new BulletRenderer.ball_big());
        register(THBullet.DefaultBulletStyle.grain_a, new BulletRenderer.grain_a());
        register(THBullet.DefaultBulletStyle.grain_b, new BulletRenderer.grain_b());
        register(THBullet.DefaultBulletStyle.ellipse, new BulletRenderer.ellipse());
    }

    public interface THBulletRendererFactory {
        void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer consumer, PoseStack poseStack, float partialTicks, Color color, Color coreColor);
    }
}
