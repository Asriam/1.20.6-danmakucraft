package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHObjectContainerRenderer;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class THBulletRenderer {
    public static void render2DBullet(EntityTHObjectContainerRenderer renderer, THBullet bullet, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_114710_) {
        //poseStack.mulPose(renderer.dispatcher.cameraOrientation());
        //poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(bullet.getScale().x, bullet.getScale().y,bullet.getScale().z);
        PoseStack.Pose posestack_pose = poseStack.last();
        VertexConsumer vertexconsumer = bufferSource.getBuffer(bullet.blend.renderType.apply(bullet.getTexture()));
        int index = bullet.getBulletColor().getIndex();
        THObjectRenderHelper.renderTexture(vertexconsumer, posestack_pose, p_114710_, Vec3.ZERO, Vec2.ONE,
                new Vec2(0.0f,1.0f/16*(index-1)),
                new Vec2(1.0f,1.0f/16*(index)),
                bullet.getColor());
    }

    public static void render3DBullet(EntityTHObjectContainerRenderer renderer, THBullet bullet, THBulletRenderer.THBulletRenderFactory factory, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_114710_) {
        THObject.Color indexColor = bullet.getBulletColor().getColor();
        THObject.Color color = THObject.Color(
                bullet.color.r * indexColor.r/255,
                bullet.color.g * indexColor.g/255,
                bullet.color.b * indexColor.b/255,
                (int) (bullet.color.a * 0.6)
        );
        THObject.Color coreColor = bullet.color;
        factory.render(renderer,bullet, bufferSource,poseStack,p_114710_,partialTicks,color,coreColor);
    }

    public static class BulletRenderers {
        public static void arrow_big(EntityTHObjectContainerRenderer renderer, THBullet bullet, MultiBufferSource bufferSource, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor) {
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.4f,0.8f,0.4f);
            Vec3 coreScale = scale.multiply(0.4f,0.4f,0.4f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.dispatcher.camera.getPosition();
            THBullet.BULLET_QUALITY_LEVEL cull = THBullet.BULLET_QUALITY_LEVEL.getQualityLevel(bullet,camPos.x,camPos.y,camPos.z);
            int edgeA = cull.edgeANum;
            int edgeB = cull.edgeBNum;
            Vec3 offset = new Vec3(0.0f,-0.45f,0.0f);
            /*
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.LIGHTNING_NO_CULL);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,2,
                    offset.add(0.0f,0.4,0.0f),
                    coreScale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    coreColor.multiply(0.6f),0,coreColor);
            //vertexconsumer = bufferSource.getBuffer(THRenderType.LIGHTNING2);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,1.2f,
                    offset,
                    scale,
                    edgeA,edgeB,true,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color.multiply(0.9f), 0,coreColor);
             */
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE);
            RenderSystem.disableCull();
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,2,
                    offset.add(0.0f,0.4f,0.0f),
                    coreScale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    coreColor.multiply(0.6f),0,THBullet.Color.WHITE().multiply(0.5f));
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,1.2f,
                    offset,
                    scale,
                    edgeA,edgeB,true,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color, 0,coreColor.multiply(0.4f));

        }

        public static void ball_mid(EntityTHObjectContainerRenderer renderer, THBullet bullet, MultiBufferSource bufferSource, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor){
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.5f,0.5f,0.5f);
            Vec3 coreScale = scale.multiply(0.6f,0.6f,0.6f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.dispatcher.camera.getPosition();
            THBullet.BULLET_QUALITY_LEVEL cull = THBullet.BULLET_QUALITY_LEVEL.getQualityLevel(bullet,camPos.x,camPos.y,camPos.z);
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
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,1,
                    Vec3.ZERO,
                    scale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color,color,coreColor);
        }

        public static void ball_big(EntityTHObjectContainerRenderer renderer, THBullet bullet, MultiBufferSource bufferSource, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor) {
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.8f, 0.8f, 0.8f);
            Vec3 coreScale = scale.multiply(0.8f, 0.8f, 0.8f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.dispatcher.camera.getPosition();
            THBullet.BULLET_QUALITY_LEVEL cull = THBullet.BULLET_QUALITY_LEVEL.getQualityLevel(bullet, camPos.x, camPos.y, camPos.z);
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
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE);
            THObjectRenderHelper.renderSphere(vertexconsumer, posestack_pose, p_254296_, 1,
                    Vec3.ZERO,
                    scale,
                    edgeA, edgeB, false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color, color, coreColor);


            /*
            RenderTarget inTarget = Minecraft.getInstance().getMainRenderTarget();
            int inSize[] = {inTarget.width, inTarget.height};
            int outSize[] = {inTarget.width , inTarget.height};
            ShaderInstance customShader = ShaderLoader.getShader(new ResourceLocation(THDanmakuCraftCore.MODID,"depth_outline"));
            if(customShader != null) {

                customShader.setSampler("Sampler0", inTarget.getColorTextureId());
                customShader.setSampler("Sampler1", inTarget.getDepthTextureId());
                customShader.safeGetUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
                customShader.safeGetUniform("ModelViewMat").set(RenderSystem.getModelViewMatrix());
                customShader.safeGetUniform("InSize").set((float) inSize[0], (float) inSize[1]);
                customShader.safeGetUniform("OutSize").set((float) outSize[0], (float) outSize[1]);
                customShader.apply();

                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                );
                RenderSystem.depthFunc(515);

                BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().getBuilder();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);

                THObjectRenderHelper.renderSphere(bufferbuilder, posestack_pose, p_254296_, 1,
                        Vec3.ZERO,
                        scale,
                        edgeA, edgeB, false,
                        Vec2.ZERO,
                        Vec2.ONE,
                        color.multiply(0.0f), color);

                BufferUploader.draw(bufferbuilder.end());

                customShader.clear();
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.depthFunc(515);
            }
            */

        }

        public static void ellipse(EntityTHObjectContainerRenderer renderer, THBullet bullet, MultiBufferSource bufferSource, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor){
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.5f,1.0f,0.5f);
            Vec3 coreScale = scale.multiply(0.7f,0.7f,0.7f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.dispatcher.camera.getPosition();
            THBullet.BULLET_QUALITY_LEVEL cull = THBullet.BULLET_QUALITY_LEVEL.getQualityLevel(bullet,camPos.x,camPos.y,camPos.z);
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
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,1,
                    Vec3.ZERO,
                    scale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color, 0, coreColor);
        }

        public static void grain_a(EntityTHObjectContainerRenderer renderer, THBullet bullet, MultiBufferSource bufferSource, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor){
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.2f,0.4f,0.2f);
            Vec3 coreScale = scale.multiply(0.6f,0.6f,0.6f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.dispatcher.camera.getPosition();
            THBullet.BULLET_QUALITY_LEVEL cull = THBullet.BULLET_QUALITY_LEVEL.getQualityLevel(bullet,camPos.x,camPos.y,camPos.z);
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
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,2,
                    offset,
                    scale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color, color.multiply(0.4f), coreColor);
        }

        public static void grain_b(EntityTHObjectContainerRenderer renderer, THBullet bullet, MultiBufferSource bufferSource, PoseStack poseStack, int p_254296_,float partialTicks,THObject.Color color,THObject.Color coreColor){
            poseStack.scale(bullet.getScale().x, bullet.getScale().y, bullet.getScale().z);
            Vec3 scale = new Vec3(0.25f,0.5f,0.25f);
            Vec3 coreScale = scale.multiply(0.6f,0.6f,0.6f);
            PoseStack.Pose posestack_pose = poseStack.last();
            Vec3 camPos = renderer.dispatcher.camera.getPosition();
            THBullet.BULLET_QUALITY_LEVEL cull = THBullet.BULLET_QUALITY_LEVEL.getQualityLevel(bullet,camPos.x,camPos.y,camPos.z);
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
            VertexConsumer vertexconsumer = bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE);
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,2,
                    Vec3.ZERO,
                    coreScale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    coreColor, coreColor.multiply(0.5f), coreColor.multiply(0.5f));
            THObjectRenderHelper.renderSphere(vertexconsumer,posestack_pose, p_254296_,2,
                    Vec3.ZERO,
                    scale,
                    edgeA,edgeB,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color, color.multiply(0.5f), coreColor.multiply(0.4f));
        }
    }

    public interface THBulletRenderFactory{
        void render(EntityTHObjectContainerRenderer renderer, THBullet bullet, MultiBufferSource bufferSource, PoseStack poseStack, int p_254296_, float partialTicks, THObject.Color color, THObject.Color coreColor);
    }
}
