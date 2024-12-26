package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.client.renderer.THObjectRenderHelper;
import com.adrian.thDanmakuCraft.client.renderer.RenderEvents;
import com.adrian.thDanmakuCraft.client.renderer.ShaderLoader;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.AbstractTHObjectRenderer;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.THObjectRendererProvider;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.THObjectRenderers;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObjectType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class EntityTHObjectContainerRenderer extends EntityRenderer<EntityTHObjectContainer> {
    private static final RenderTarget TEST_RENDER_TARGET = new TextureTarget(1000,1000,true,true);
    private static final RenderTarget MAIN_RENDER_TARGET = Minecraft.getInstance().getMainRenderTarget();
    private static final RenderTarget DEPTH_BUFFER = new TextureTarget(1000,1000,true,true);
    private final Map<THObjectType<?>, AbstractTHObjectRenderer<?>> thobjectRenderers;
    //private Frustum frustum;

    public EntityTHObjectContainerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.thobjectRenderers = THObjectRenderers.createEntityRenderers(new THObjectRendererProvider.Context(this));

        RenderEvents.registerRenderLevelStageTask("test_effect_clear", RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS, (poseStack, partialTick) -> {
            TEST_RENDER_TARGET.resize(MAIN_RENDER_TARGET.width , MAIN_RENDER_TARGET.height, false);
            TEST_RENDER_TARGET.setClearColor(0.0f,0.0f,0.0f,0.0f);
            TEST_RENDER_TARGET.clear(true);
        });

        RenderEvents.registerRenderLevelStageTask("test_effect_applier", RenderLevelStageEvent.Stage.AFTER_WEATHER, (poseStack, partialTick) -> {
            applyEffect();
        });
    }

    public static void applyEffect(){
        ShaderInstance customShader = ShaderLoader.getShader(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"box_blur"));
        if (customShader != null) {
            RenderTarget inTarget = TEST_RENDER_TARGET;
            RenderTarget outTarget = MAIN_RENDER_TARGET;
            MAIN_RENDER_TARGET.unbindWrite();

            int[] inSize = {inTarget.width, inTarget.height};
            int[] outSize = {outTarget.width , outTarget.height};

            outTarget.bindWrite(true);
            customShader.setSampler("DiffuseSampler", inTarget.getColorTextureId());
            Matrix4f projMat = new Matrix4f().setOrtho(0.0F, (float) outSize[0], 0.0F, (float) outSize[1], 0.1F, 1000.0F);
            customShader.safeGetUniform("ProjMat").set(projMat);
            //customShader.safeGetUniform("ModelViewMat").set(new Matrix4f().translation(0.0F, 0.0F, -2000.0F));
            customShader.safeGetUniform("InSize").set((float) inSize[0], (float) inSize[1]);
            customShader.safeGetUniform("OutSize").set((float) outSize[0], (float) outSize[1]);
            customShader.safeGetUniform("BlurDir").set(1.0f,1.0f);
            customShader.safeGetUniform("Radius").set(0.0f);
            customShader.safeGetUniform("RadiusMultiplier").set(1.0f);
            customShader.apply();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.depthFunc(519);
            BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferbuilder.vertex(0.0, 0.0, 0.0).endVertex();
            bufferbuilder.vertex(outSize[0], 0.0, 0.0).endVertex();
            bufferbuilder.vertex(outSize[0], outSize[1], 0.0).endVertex();
            bufferbuilder.vertex(0.0, outSize[1], 0.0).endVertex();
            BufferUploader.draw(bufferbuilder.end());
            customShader.clear();
            RenderSystem.depthFunc(515);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            outTarget.unbindWrite();

            MAIN_RENDER_TARGET.bindWrite(true);

        }
    }

    public void drawOnScreen(float screenWidth, float screenHeight){
        BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(0.0, 0.0, 0.0).endVertex();
        bufferbuilder.vertex(screenWidth, 0.0, 0.0).endVertex();
        bufferbuilder.vertex(screenWidth, screenHeight, 0.0).endVertex();
        bufferbuilder.vertex(0.0, screenHeight, 0.0).endVertex();
        BufferUploader.draw(bufferbuilder.end());
    }

    @Override
    public void render(EntityTHObjectContainer entity, float rotationX, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay){
        //super.render(entity, rotationX, partialTicks, poseStack, bufferSource, combinedOverlay);
        if (this.entityRenderDispatcher.shouldRenderHitBoxes()) {
            renderContainerBound(entity, poseStack, bufferSource.getBuffer(RenderType.lines()));
        }

        poseStack.popPose();
        poseStack.pushPose();


        Vec3 cameraPosition = this.entityRenderDispatcher.camera.getPosition();
        double camX = cameraPosition.x;
        double camY = cameraPosition.y;
        double camZ = cameraPosition.z;

        RenderTarget mainRenderTarget = MAIN_RENDER_TARGET;
        ShaderInstance shader = ShaderLoader.DANMAKU_DEPTH_OUTLINE_SHADER;
        if(shader != null) {
            DEPTH_BUFFER.copyDepthFrom(mainRenderTarget);
            shader.setSampler("DepthBuffer", DEPTH_BUFFER.getDepthTextureId());
            mainRenderTarget.bindWrite(true);
        }

        boolean shouldApplyEffect = false;

        if (shouldApplyEffect) {
            mainRenderTarget.unbindWrite();
            TEST_RENDER_TARGET.copyDepthFrom(mainRenderTarget);
            TEST_RENDER_TARGET.bindWrite(true);
        }

        RenderSystem.enableBlend();
        //final List<? extends THObject> objectList = layerObjects(entity.getObjectManager().getTHObjectsForRender(),camX,camY,camZ);
        final List<? extends THObject> objectList = entity.getObjectManager().getTHObjectsForRender();
        if (!objectList.isEmpty()) {
            Frustum frustum = this.getFrustum();
            if (this.entityRenderDispatcher.shouldRenderHitBoxes()) {
                for (THObject object:objectList) {
                    if (object != null && (object instanceof THCurvedLaser || this.shouldRenderTHObject(object, frustum, camX, camY, camZ))/*shouldRenderTHObject.test(object)*/) {
                        poseStack.pushPose();
                        Vec3 objectPos = object.getOffsetPosition(partialTicks);
                        poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
                        if (object.collision) {
                            if (object instanceof THCurvedLaser laser) {
                                renderTHCurvedLaserHitBoxes(laser, objectPos, poseStack, bufferSource.getBuffer(RenderType.lines()), partialTicks, frustum, cameraPosition);
                            } else {
                                renderTHObjectsHitBox(object, poseStack, bufferSource.getBuffer(RenderType.lines()));
                            }
                        }
                        poseStack.popPose();
                    }
                }
            }

            Map<RenderType,List<THObject>> map = new HashMap<>();
            for (THObject object:objectList) {
                RenderType renderType;// = getRenderType(new THRenderType.TEST_RENDER_TYPE_FUNCTION_CONTEXT(THObjectRenderHelper.parseBlend(object.getBlend()), false));
                if (object.getClass() == THObject.class) {
                    renderType = THRenderType.RENDER_TYPE_2D_DANMAKU.apply(new THRenderType.RENDER_TYPE_2D_DANMAKU_CONTEXT(
                            object.getTexture(),
                            THObjectRenderHelper.parseBlend(object.getBlend()))
                    );
                }else if(object instanceof THBullet bullet) {
                    if(bullet.getStyle().getIs3D()){
                        boolean shouldCull = bullet.getStyle() != THBullet.BULLET_STYLE.arrow_big;
                        renderType = getRenderType(new THRenderType.TEST_RENDER_TYPE_FUNCTION_CONTEXT(THObjectRenderHelper.parseBlend(object.getBlend()), shouldCull));
                    }else {
                        renderType = THRenderType.RENDER_TYPE_2D_DANMAKU.apply(new THRenderType.RENDER_TYPE_2D_DANMAKU_CONTEXT(
                                bullet.getTexture(),
                                THObjectRenderHelper.parseBlend(bullet.getBlend()))
                        );
                    }
                }else{
                    renderType = getRenderType(new THRenderType.TEST_RENDER_TYPE_FUNCTION_CONTEXT(THObjectRenderHelper.parseBlend(object.getBlend()), true));
                }
                map.computeIfAbsent(renderType, (key) -> new ArrayList<>());
                List<THObject> list1 = map.get(renderType);
                list1.add(object);
            }

            map.forEach((renderType, list) -> {
                var list1 = layerObjects(list,camX,camY,camZ);
                BufferBuilder vertexConsumer = (BufferBuilder) bufferSource.getBuffer(renderType);
                for (THObject object:list1) {
                    if (object != null && (object instanceof THCurvedLaser || this.shouldRenderTHObject(object, frustum, camX, camY, camZ))) {
                        poseStack.pushPose();
                        Vec3 objectPos = object.getOffsetPosition(partialTicks);
                        poseStack.translate(objectPos.x() - camX, objectPos.y() - camY, objectPos.z() - camZ);
                        this.getTHObjectRenderer(object).render(object, objectPos, partialTicks, poseStack, vertexConsumer, combinedOverlay);
                        poseStack.popPose();
                    }
                }
                renderType.setupRenderState();
                if(shouldApplyEffect) {
                    TEST_RENDER_TARGET.bindWrite(true);
                }
                BufferUploader.drawWithShader(vertexConsumer.end());
                if(shouldApplyEffect) {
                    TEST_RENDER_TARGET.unbindWrite();
                }
                renderType.clearRenderState();
                //vertexConsumer.begin(renderType.mode(),renderType.format());
            });
            //bufferSource.getBuffer(RenderType.lines());
        }
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        //profiler.pop();

        if(shouldApplyEffect) {
            TEST_RENDER_TARGET.unbindWrite();
            mainRenderTarget.copyDepthFrom(TEST_RENDER_TARGET);
            mainRenderTarget.bindWrite(true);
            //testRenderTarget.blitToScreen(mainRenderTarget.width,mainRenderTarget.height,true);
        }

        poseStack.popPose();
        poseStack.pushPose();
        Vec3 entityPos = entity.getPosition(partialTicks);
        poseStack.translate(entityPos.x-camX, entityPos.y-camY, entityPos.z-camZ);
    }

    public static RenderType getRenderType(THRenderType.TEST_RENDER_TYPE_FUNCTION_CONTEXT context) {
        return THRenderType.TEST_RENDER_TYPE_FUNCTION.apply(context);
        //return THRenderType.TEST_RENDER_TYPE;
    }

    public <T extends THObject> AbstractTHObjectRenderer<T> getTHObjectRenderer(@NotNull T object) {
        return (AbstractTHObjectRenderer<T>) this.thobjectRenderers.get(object.getType());
    }

    private static void renderTHObjectsHitBox(@NotNull THObject object, PoseStack poseStack, VertexConsumer vertexConsumer) {
        AABB aabb = object.getBoundingBox().move(-object.getX(), -object.getY(), -object.getZ());
        if(object.getCollisionType() == THObject.CollisionType.AABB) {
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, 0.0F, 1.0F, 1.0F, 1.0F);
        }else if(object.getCollisionType() == THObject.CollisionType.SPHERE){
            THObject.Color color = THObject.Color(0,255,255,255);
            THObjectRenderHelper.renderSphere(vertexConsumer,poseStack.last(), 0,1,
                    Vec3.ZERO,
                    new Vec3(object.getSize().x,object.getSize().x,object.getSize().x),
                    6,6,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color,color,color);
        }else if(object.getCollisionType() == THObject.CollisionType.ELLIPSOID){
            poseStack.pushPose();
            Vector3f rotation = object.getRotation();
            poseStack.mulPose(new Quaternionf().rotationYXZ(rotation.y,-rotation.x,rotation.z));
            THObject.Color color = THObject.Color(0,255,255,255);
            THObjectRenderHelper.renderSphere(vertexConsumer,poseStack.last(), 0,1,
                    Vec3.ZERO,
                    object.getSize(),
                    6,6,false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    color,color,color);
            poseStack.popPose();


        }

        Vec3 vec31 = object.getMotionDirection();
        Matrix4f matrix4f = poseStack.last().pose();
        //Matrix3f matrix3f = poseStack.last().normal();
        vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F).color(0, 0, 255, 255).normal(poseStack.last(), (float)vec31.x, (float)vec31.y, (float)vec31.z).endVertex();
        vertexConsumer.vertex(matrix4f, (float)(vec31.x * 2.0D), (float)((vec31.y * 2.0D)), (float)(vec31.z * 2.0D)).color(0, 0, 255, 255).normal(poseStack.last(), (float)vec31.x, (float)vec31.y, (float)vec31.z).endVertex();
    }

    private static void renderTHCurvedLaserHitBoxes(THCurvedLaser laser, Vec3 laserPos,PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks, Frustum frustum, Vec3 cameraPosition){
        List<THCurvedLaser.LaserNode> nodes = laser.nodeManager.getNodes();
        for(THCurvedLaser.LaserNode node: nodes){
            Vec3 pos = node.getPosition();
            AABB aabb = node.getBoundingBoxForCulling().inflate(0.5D);
            if (aabb.hasNaN() || aabb.getSize() == 0.0D) {
                aabb = new AABB(pos.x() - 2.0D, pos.y() - 2.0D, pos.z() - 2.0D, pos.x() + 2.0D, pos.y() + 2.0D, pos.z() + 2.0D);
            }

            if (node.isValid() && frustum.isVisible(aabb)) {
                poseStack.pushPose();
                AABB aabb2 = node.getBoundingBox().move(-pos.x(), -pos.y(), -pos.z());
                Vec3 offsetPos = laserPos.vectorTo(node.getOffsetPosition(partialTicks));
                poseStack.translate(offsetPos.x(), offsetPos.y(), offsetPos.z());
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb2, 0.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        }
    }

    public static List<THObject> layerObjects(List<THObject> list, double camX, double camY, double camZ){
        try {
            list.sort((o1, o2) -> {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                Vec3 pos1 = o1.getPosition();
                Vec3 pos2 = o2.getPosition();
                double d1x = pos1.x - camX;
                double d1y = pos1.y - camY;
                double d1z = pos1.z - camZ;
                double dist1Square = (d1x * d1x + d1y * d1y + d1z * d1z);
                double d2x = pos2.x - camX;
                double d2y = pos2.y - camY;
                double d2z = pos2.z - camZ;
                double dist2Square = (d2x * d2x + d2y * d2y + d2z * d2z);
                if (dist1Square < dist2Square) {
                    return 1;
                } else {
                    return -1;
                }
            });
        }catch (Exception e){
            THDanmakuCraftCore.LOGGER.error(e.toString());
        }
        return list;
    }


    private static void renderContainerBound(EntityTHObjectContainer entity, PoseStack poseStack, VertexConsumer vertexConsumer) {
        AABB aabb = entity.getAabb().move(-entity.getX(), -entity.getY(), -entity.getZ());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean shouldRender(@NotNull EntityTHObjectContainer entity, @NotNull Frustum frustum, double camX, double camY, double camZ) {
        //this.frustum = frustum;
        return true;
    }

    public boolean shouldRenderTHObject(THObject object, Frustum frustum, double camX, double camY, double camZ) {
        if (!object.shouldRender(camX,camY,camZ)) {
            return false;
        } else if (object.noCulling) {
            return true;
        } else {
            AABB aabb = object.getBoundingBoxForCulling().inflate(0.5D);
            if (aabb.hasNaN() || aabb.getSize() == 0.0D) {
                aabb = new AABB(object.getX() - 2.0D, object.getY() - 2.0D, object.getZ() - 2.0D, object.getX() + 2.0D, object.getY() + 2.0D, object.getZ() + 2.0D);
            }

            return frustum.isVisible(aabb);
        }
    }

    public EntityRenderDispatcher getRenderDispatcher(){
        return this.entityRenderDispatcher;
    }

    public Frustum getFrustum(){
        return Minecraft.getInstance().levelRenderer.getFrustum();
    }

    @Override
    @NotNull
    public ResourceLocation getTextureLocation(@NotNull EntityTHObjectContainer container) {
        return null;
    }
}
