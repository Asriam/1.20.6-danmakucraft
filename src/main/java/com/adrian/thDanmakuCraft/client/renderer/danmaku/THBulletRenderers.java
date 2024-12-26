package com.adrian.thDanmakuCraft.client.renderer.danmaku;

import com.adrian.thDanmakuCraft.client.renderer.THObjectRenderHelper;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class THBulletRenderers {

    public static void render2DBullet(THBulletRenderer renderer, THBullet bullet, PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks, int combinedOverlay) {
        poseStack.scale(bullet.getScale().x, bullet.getScale().y,bullet.getScale().z);
        PoseStack.Pose posestack_pose = poseStack.last();
        //VertexConsumer vertexConsumer = bufferSource.getBuffer(THRenderType.BLEND_NONE.apply(bullet.getTexture()));
        int index = bullet.getBulletColor().getIndex();
        THObjectRenderHelper.renderTexture(vertexConsumer, posestack_pose, combinedOverlay, Vec3.ZERO, Vec2.ONE,
                new Vec2(0.0f,1.0f/16*(index-1)),
                new Vec2(1.0f,1.0f/16*(index)),
                bullet.getColor());
    }

    public static void render3DBullet(THBulletRenderer renderer, THBullet bullet, PoseStack poseStack, VertexConsumer vertexConsumer, THBulletRendererFactory factory, float partialTicks, int combinedOverlay) {
        THObject.Color indexColor = bullet.getBulletColor().getColor();
        THObject.Color color = THObject.Color(
                bullet.color.r * indexColor.r/255,
                bullet.color.g * indexColor.g/255,
                bullet.color.b * indexColor.b/255,
                (int) (bullet.color.a * 0.6)
        );
        THObject.Color coreColor = bullet.color;
        factory.render(renderer,bullet, vertexConsumer, poseStack,combinedOverlay,partialTicks,color,coreColor);
    }

    public static class Renderers {
        public static void arrow_big(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, int combinedOverlay,float partialTicks,THObject.Color color,THObject.Color coreColor) {
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.4f,0.8f,0.4f);
            Vec3 coreScale = scale.multiply(0.4f,0.4f,0.4f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
            THBulletRenderer.BULLET_QUALITY_LEVEL cull = THBulletRenderer.BULLET_QUALITY_LEVEL.getQualityLevel(bullet,camPos.x,camPos.y,camPos.z);
            int edgeA = cull.edgeANum;
            int edgeB = cull.edgeBNum;
            Vec3 offset = new Vec3(0.0f,-0.45f,0.0f);
            //VertexConsumer vertexconsumer = bufferSource.getBuffer(getRenderType(bullet.getBlend(),false));
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, combinedOverlay,1.2f,
                    offset,
                    scale,
                    edgeA,edgeB,true,
                    new Vec2(0.5f,0.6f),
                    Vec2.ONE,
                    color, 0,coreColor.multiply(0.4f));
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, combinedOverlay,2,
                    offset.add(0.0f,0.4f,0.0f),
                    coreScale,
                    edgeA,edgeB,false,
                    new Vec2(0.5f,0.4f),
                    Vec2.ONE,
                    coreColor.multiply(0.6f),0,THBullet.Color.WHITE().multiply(0.5f));
            /*
            RenderType renderType = getRenderType(bullet.getBlend(),true);
            BufferBuilder vertexconsumer = (BufferBuilder) bufferSource.getBuffer(renderType);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, combinedOverlay,1.2f,
                    offset,
                    scale,
                    edgeA,edgeB,true,
                    new Vec2(0.5f,0.6f),
                    Vec2.ONE,
                    color, 0,coreColor.multiply(0.4f));
            renderType.setupRenderState();
            BufferUploader.drawWithShader(vertexconsumer.end());
            vertexconsumer.begin(VertexFormat.Mode.QUADS,renderType.format());
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, combinedOverlay,2,
                    offset.add(0.0f,0.4f,0.0f),
                    coreScale,
                    edgeA,edgeB,false,
                    new Vec2(0.5f,0.4f),
                    Vec2.ONE,
                    coreColor.multiply(0.6f),0,THBullet.Color.WHITE().multiply(0.5f));
            BufferUploader.drawWithShader(vertexconsumer.end());
            renderType.clearRenderState();
            vertexconsumer.begin(VertexFormat.Mode.QUADS,renderType.format());
             */
        }

        public static void ball_mid(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor){
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.5f,0.5f,0.5f);
            //Vec3 coreScale = scale.multiply(0.6f,0.6f,0.6f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
            THBulletRenderer.BULLET_QUALITY_LEVEL cull = THBulletRenderer.BULLET_QUALITY_LEVEL.getQualityLevel(bullet,camPos.x,camPos.y,camPos.z);
            int edgeA = cull.edgeANum;
            int edgeB = cull.edgeBNum;
            /*
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.LIGHTNING);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,1,
                    Vec3.ZERO,
                    coreScale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    coreColor.multiply(0.8f),coreColor.multiply(0.8f));
             */
            //VertexConsumer vertexconsumer = bufferSource.getBuffer(getRenderType(bullet.getBlend(),true));
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,1,
                    Vec3.ZERO,
                    scale,
                    edgeA,edgeB,false,
                    new Vec2(0.5f,0.0f),
                    Vec2.ONE,
                    color,color,coreColor);
            //RenderSystem.depthFunc(515);
        }

        public static void ball_big(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor) {
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.8f, 0.8f, 0.8f);
            Vec3 coreScale = scale.multiply(0.8f, 0.8f, 0.8f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
            THBulletRenderer.BULLET_QUALITY_LEVEL cull = THBulletRenderer.BULLET_QUALITY_LEVEL.getQualityLevel(bullet, camPos.x, camPos.y, camPos.z);
            int edgeA = cull.edgeANum;
            int edgeB = cull.edgeBNum;
            /*
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.LIGHTNING);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,1,
                    Vec3.ZERO,
                    coreScale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    coreColor.multiply(0.8f),coreColor.multiply(0.8f));3
            */
            //VertexConsumer vertexconsumer = bufferSource.getBuffer(getRenderType(bullet.getBlend(),true));
            THObjectRenderHelper.renderSphere(vertexconsumer, posestack_pose, p_254296_, 1,
                    Vec3.ZERO,
                    scale,
                    edgeA, edgeB, false,
                    new Vec2(0.7f,-0.1f),
                    Vec2.ONE,
                    color, color, coreColor);
        }

        public static void ellipse(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor){
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.5f,1.0f,0.5f);
            Vec3 coreScale = scale.multiply(0.7f,0.7f,0.7f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
            THBulletRenderer.BULLET_QUALITY_LEVEL cull = THBulletRenderer.BULLET_QUALITY_LEVEL.getQualityLevel(bullet,camPos.x,camPos.y,camPos.z);
            int edgeA = cull.edgeANum;
            int edgeB = cull.edgeBNum;
            /*
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.LIGHTNING);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,1,
                    Vec3.ZERO,
                    coreScale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    coreColor.multiply(0.8f),0);
             */
            //VertexConsumer vertexconsumer = bufferSource.getBuffer(getRenderType(bullet.getBlend(),true));
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,1,
                    Vec3.ZERO,
                    scale,
                    edgeA,edgeB,false,
                    new Vec2(0.8f,0.1f),
                    Vec2.ONE,
                    color, color.multiply(0.8f), coreColor.multiply(0.9f));
        }

        public static void grain_a(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor){
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.2f,0.4f,0.2f);
            Vec3 coreScale = scale.multiply(0.6f,0.6f,0.6f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.getRenderDispatcher().camera.getPosition();
            THBulletRenderer.BULLET_QUALITY_LEVEL cull = THBulletRenderer.BULLET_QUALITY_LEVEL.getQualityLevel(bullet,camPos.x,camPos.y,camPos.z);
            int edgeA = cull.edgeANum;
            int edgeB = cull.edgeBNum;
            Vec3 offset = new Vec3(0.0f,-0.1f,0.0f);
            /*
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.LIGHTNING);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,2,
                    offset.add(0.0f,0.06f,0.0f),
                    coreScale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    coreColor.multiply(0.8f), 0);
             */
            //VertexConsumer vertexconsumer = bufferSource.getBuffer(getRenderType(bullet.getBlend(),true));
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,2,
                    offset,
                    scale,
                    edgeA,edgeB,false,
                    new Vec2(0.5f,0.0f),
                    Vec2.ONE,
                    color, color.multiply(0.5f), coreColor);
        }

        public static void grain_b(THBulletRenderer renderer, THBullet bullet, VertexConsumer vertexconsumer, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor){
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.25f,0.5f,0.25f);
            Vec3 coreScale = scale.multiply(0.6f,0.6f,0.6f);
            PoseStack.Pose posestack_pose = poseStack.last();
            int edgeA = 4;
            int edgeB = 4;
            /*
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,2,
                    Vec3.ZERO,
                    scale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color, 0, coreColor);

             */
            //VertexConsumer vertexconsumer = bufferSource.getBuffer(getRenderType(bullet.getBlend(),true));
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,2,
                    Vec3.ZERO,
                    scale,
                    edgeA,edgeB,false,
                    new Vec2(0.5f,0.1f),
                    Vec2.ONE,
                    color, color.multiply(0.5f), coreColor.multiply(0.4f));
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,2,
                    Vec3.ZERO,
                    coreScale,
                    edgeA,edgeB,false,
                    new Vec2(0.5f,0.0f),
                    Vec2.ONE,
                    coreColor, coreColor.multiply(0.5f), coreColor.multiply(0.5f));
        }
    }

    private static final Map<THBullet.BULLET_STYLE, THBulletRendererFactory> bulletRenderers = new EnumMap<>(THBullet.BULLET_STYLE.class);

    public static void register(THBullet.BULLET_STYLE bulletStyle, THBulletRendererFactory factory){
        bulletRenderers.put(bulletStyle,factory);
    }

    public static THBulletRendererFactory getRenderFactory(THBullet.BULLET_STYLE bulletStyle){
        return bulletRenderers.get(bulletStyle);
    }

    static {
        register(THBullet.BULLET_STYLE.arrow_big, Renderers::arrow_big);
        register(THBullet.BULLET_STYLE.ball_mid, Renderers::ball_mid);
        register(THBullet.BULLET_STYLE.ball_big, Renderers::ball_big);
        register(THBullet.BULLET_STYLE.grain_a, Renderers::grain_a);
        register(THBullet.BULLET_STYLE.grain_b, Renderers::grain_b);
        register(THBullet.BULLET_STYLE.ellipse, Renderers::ellipse);
    }

    public interface THBulletRendererFactory {
        void render(THBulletRenderer renderer, THBullet bullet, VertexConsumer consumer, PoseStack poseStack, int p_254296_, float partialTicks, THObject.Color color, THObject.Color coreColor);
    }
}
