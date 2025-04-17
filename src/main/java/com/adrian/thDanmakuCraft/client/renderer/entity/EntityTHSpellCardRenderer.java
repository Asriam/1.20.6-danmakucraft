package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.adrian.thDanmakuCraft.client.renderer.*;
import com.adrian.thDanmakuCraft.client.renderer.shape.ShapeVertexHelper;
import com.adrian.thDanmakuCraft.client.renderer.shape.SphereVertexHelper;
import com.adrian.thDanmakuCraft.events.RenderEvents;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.ConstantUtil;
import com.adrian.thDanmakuCraft.util.IImage;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.Blend;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.adrian.thDanmakuCraft.client.renderer.MyRenderTypes.TEST_RENDER_TARGET;

public class EntityTHSpellCardRenderer extends EntityTHObjectContainerRenderer<EntityTHSpellCard>{
    public EntityTHSpellCardRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull EntityTHSpellCard entity, float rotationX, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedOverlay) {
        super.render(entity, rotationX, partialTicks, poseStack, bufferSource, combinedOverlay);
        THObjectContainer container = entity.getContainer();
        if(container.isSpellCard() && (container.shouldRenderMagicAura || container.shouldRenderLineAura)) {
            renderSpellCardAura(entity,container, poseStack, entity.getPosition(partialTicks), partialTicks);
        }
    }

    private static final BufferBuilder MAGIC_SQUARE_BUFFER = new BufferBuilder(250);
    private static final BufferBuilder AURA_BUFFER = new BufferBuilder(251);
    private static final BufferBuilder BACKGROUND_WARP_EFFECT_BUFFER = new BufferBuilder(252);

    private static final IImage.Image spellCardAura = new IImage.Image(ResourceLocationUtil.thdanmakucraft("textures/spellcard/eff_line.png"), 0.0f, 0.0f, 1.0f, 1.0f);
    private static final IImage.Image SPELLCARD_MAGIC_SQUARE = new IImage.Image(ResourceLocationUtil.thdanmakucraft("textures/spellcard/eff_magicsquare.png"), 0.0f, 0.0f, 1.0f, 1.0f);
    public void renderSpellCardAura(EntityTHSpellCard spellCard, THObjectContainer container, PoseStack poseStack, Vec3 pos, float partialTicks){
        IImage.Image image = SPELLCARD_MAGIC_SQUARE;
        Color color = new Color(255,255,255,160);
        Vec2 uvStart = image.getUVStart();
        Vec2 uvEnd =   image.getUVEnd();
        float timer = container.getTimer()+partialTicks;
        float rotate = (timer)/10.0f;
        float time = 30.0f;
        float open = timer > time ? 1.0f : (float) Math.pow(Math.min(timer / time,1.0f),0.8f);
        float close = timer < container.getLifetime()-time ? 1.0f : 1.0f - (float) Math.pow(Math.clamp((timer-container.getLifetime()+time) / time,0.0f,1.0f),2.0f);
        float timeLeft = 1.0f-(float) container.getTimer()/container.getLifetime();
        Vec2 faceCamRotation = THObject.VectorAngleToRadAngle(this.getRenderDispatcher().camera.getPosition().vectorTo(pos));
        Vec2 scale = Vec2.ONE
                .scale(8.0f + 0.8f*Mth.cos(timer/8.0f))
                .scale(open*close);
        if(container.shouldRenderMagicAura) {
            {
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                Vec2 rot = THObject.VectorAngleToRadAngle(spellCard.paramsForRender.lastMagicSquareRotation.lerp(spellCard.paramsForRender.magicSquareRotation,Minecraft.getInstance().getPartialTick()));
                /*poseStack.mulPose(this.getRenderDispatcher().cameraOrientation()
                        .rotateZ(rotate));*/
                poseStack.mulPose(new Quaternionf()
                        .rotateY(rot.y - Mth.cos(timer/60.0f)  * (0.8f + 0.2f* Mth.sin(timer/80.0f)))
                        .rotateX(-rot.x + Mth.sin(timer/40.0f) * 0.5f)
                        .rotateZ(rotate));
                //poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                VertexBuilder vertexBuilder = new VertexBuilder(MAGIC_SQUARE_BUFFER);
                vertexBuilder.positionColorUV(pose.pose(), -scale.x, -scale.y, 0.0f, color, uvStart.x, uvEnd.y);
                vertexBuilder.positionColorUV(pose.pose(), scale.x, -scale.y, 0.0f, color, uvStart.x, uvStart.y);
                vertexBuilder.positionColorUV(pose.pose(), scale.x, scale.y, 0.0f, color, uvEnd.x, uvStart.y);
                vertexBuilder.positionColorUV(pose.pose(), -scale.x, scale.y, 0.0f, color, uvEnd.x, uvEnd.y);
                poseStack.popPose();
            }
        }

        if(container.shouldRenderLineAura){
            final float radius0 = 15.0f;
            int sample = 12;
            color = new Color(180,180,255,160);
            time = 40.0f;
            //open = timer > time ? 1.0f : (float) Math.pow(Math.min(timer / time,1.0f),0.8f);
            close = timer < container.getLifetime()-time ? 1.0f : 1.0f - (float) Math.pow(Math.clamp((timer-container.getLifetime()) / time,0.0f,1.0f),0.8f);
            float width = 1.6f * timeLeft * 1.4f;
            float width2 = width*3.0f;
            float num = 4.0f;
            float radius = (radius0 + 10.0f) * open*close * timeLeft;
            uvStart = new Vec2(1.0f / 8.0f * (5), 0.0f);
            uvEnd = new Vec2(1.0f / 8.0f * (5+1), 1.0f);

            poseStack.pushPose();
                PoseStack.Pose pose2 = poseStack.last();
                poseStack.mulPose(new Quaternionf().rotateY(faceCamRotation.y).rotateX(-faceCamRotation.x).rotateZ(-rotate*2.0f));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                for (int i = 0; i < sample; i++) {
                    Vec3 vec3 = new Vec3(0.0f, radius, 0.0f);
                    Vector3f rotation = vec3.zRot((float) (i * Math.PI * 2.0f / sample)).toVector3f();
                    Vector3f rotation2 = vec3.zRot((float) ((i + 1) * Math.PI * 2.0f / sample)).toVector3f();
                    Vector3f rotation3 = vec3.add(0.0f, width2, 0.0f).zRot((float) ((i + 1) * Math.PI * 2.0f / sample)).toVector3f();
                    Vector3f rotation4 = vec3.add(0.0f, width2, 0.0f).zRot((float) ((i) * Math.PI * 2.0f / sample)).toVector3f();
                    float v1 = i / (sample / num);
                    float v2 = (i + 1) / (sample / num);
                    VertexBuilder vertexBuilder = new VertexBuilder(AURA_BUFFER);
                    vertexBuilder.positionColorUV(pose2.pose(), rotation, color, uvStart.x, v1);
                    vertexBuilder.positionColorUV(pose2.pose(), rotation2, color, uvStart.x, v2);
                    vertexBuilder.positionColorUV(pose2.pose(), rotation3, color, uvEnd.x, v2);
                    vertexBuilder.positionColorUV(pose2.pose(), rotation4, color, uvEnd.x, v1);
                }
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf()
                    .rotateY(rotate/6.0f)
                    .rotateX(rotate/6.0f)
            );
            num = 8.0f;
            int aaa = 4;
            sample = 24;

            float rotateOffset = Mth.PI/4.0f + timer / 6.0f;
            for (int g=0;g<=2;g++) {
                radius = (radius0+g*2.0f) * open*close * timeLeft;
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                poseStack.mulPose(new Quaternionf()
                        .rotateY(Math.min(g,1)*Mth.PI/2.0f + (float) Math.pow(-1,(g+1))*timer/30.0f)
                        .rotateX(Math.max(g-1,0)*Mth.PI/2.0f  + (float) Math.pow(-1,(g+2))*timer/30.0f)
                        .rotateZ(rotate*2));
                Vector3f vec3f = new Vector3f(0.0f, radius + width/2, 0.0f);
                Vector3f _cos = new Vector3f(0.0f,0.0f,width/2);
                for (int i = 0; i < sample; i++) {
                    float v1 = i / (sample / num);
                    float v2 = (i + 1) / (sample / num);
                    uvStart = new Vec2(1.0f / 8.0f * ((2-g)), 0.0f);
                    uvEnd = new Vec2(1.0f / 8.0f * (2-g+1), 1.0f);
                    for (int s = 0; s < aaa; s++) {
                        VertexBuilder vertexBuilder = new VertexBuilder(AURA_BUFFER);
                        Vector3f cos  = _cos.rotateX(s*(Mth.PI*2.0f/aaa) + rotateOffset, new Vector3f());
                        Vector3f cos2 = _cos.rotateX((s+1)*(Mth.PI*2.0f/aaa) + rotateOffset , new Vector3f());

                        Vector3f add1 = vec3f.add(cos,new Vector3f());
                        Vector3f add2 = vec3f.add(cos2,new Vector3f());
                        float rot1 = (float) (i * Math.PI * 2.0f / sample);
                        float rot2 = (float) ((i + 1) * Math.PI * 2.0f / sample);

                        Vector3f pos1 = add1.rotateZ(rot1, new Vector3f());
                        Vector3f pos2 = add1.rotateZ(rot2, new Vector3f());
                        Vector3f pos3 = add2.rotateZ(rot2, new Vector3f());
                        Vector3f pos4 = add2.rotateZ(rot1, new Vector3f());
                        vertexBuilder.positionColorUV(pose.pose(), pos1,  color, uvStart.x, v1);
                        vertexBuilder.positionColorUV(pose.pose(), pos2, color, uvStart.x, v2);
                        vertexBuilder.positionColorUV(pose.pose(), pos3, color, uvEnd.x, v2);
                        vertexBuilder.positionColorUV(pose.pose(), pos4, color, uvEnd.x, v1);
                    }
                }
                poseStack.popPose();
            }
            poseStack.popPose();
        }

        if(true){
            open = timer > time ? 1.0f : (float) Math.pow(Math.min(timer / time,1.0f),0.5f);
            renderBackgroundWarpEffect(spellCard,container, poseStack, pos, partialTicks,40.0f*open*close*Mth.lerp(timeLeft,0.4f,1.0f),open*close);
        }
    }

    public static void renderBackgroundWarpEffect(EntityTHSpellCard spellCard, THObjectContainer container, PoseStack poseStack, Vec3 pos, float partialTicks, float radius, float alpha){
        //float radius = 40.0f;
        poseStack.pushPose();
        //poseStack.translate(pos.x,pos.y,pos.z);
        final VertexBuilder builder = new VertexBuilder(BACKGROUND_WARP_EFFECT_BUFFER);
        final Color color2 = new Color(255,0,200, (int) (180*alpha));
        RenderUtil.enableCull();
        new SphereVertexHelper(poseStack.last().pose(), poseStack.last().normal(),
                ConstantUtil.VECTOR3F_ZERO, new Vector3f(radius, radius, radius),
                64, 64, 1.0f, false,
                new Color(255,255,255,255),
                new Color(255,255,255,255), true, false
        ).vertex((vertexPos,normal,color)->
                builder.vertex(vertexPos).normal(normal).color(color2).endVertex());
        /*new SphereVertexHelper(
                poseStack.last().pose(), poseStack.last().normal(),
                ConstantUtil.VECTOR3F_ZERO, new Vector3f(radius, radius, radius),
                16, 16, 1.0f, false,
                new Color(255,255,255,255),
                new Color(255,255,255,255), true, false)
                .vertex((vertexPos,normal,color)->{
            builder.vertex(vertexPos).normal(normal).color(color2).endVertex();
        });*/
        RenderUtil.enableCull();
        poseStack.popPose();
    }

    public static void beforeRenderEntities(LevelRenderer levelRenderer, float partialTick){
        MAGIC_SQUARE_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        AURA_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        BACKGROUND_WARP_EFFECT_BUFFER.begin(VertexFormat.Mode.QUADS, MyVertexFormats.POSITION_NORMAL_COLOR);
        BACKGROUND_WARP_EFFECT_BUFFER.color(0,0,0,255);
        //BACKGROUND_WARP_EFFECT_BUFFER.clear();
    }

    public static void afterRenderEntities(LevelRenderer levelRenderer, float partialTick){
        RenderType renderType = MyRenderTypes.RENDER_TYPE_SPELLCARD_AURA.apply(
                new MyRenderTypes.RENDER_TYPE_2D_DANMAKU_CONTEXT(SPELLCARD_MAGIC_SQUARE.getTextureLocation(), THBlendMode.getBlendMode(Blend.add))
        );
        renderType.setupRenderState();
        RenderSystem.setShaderTexture(0, spellCardAura.getTextureLocation());
        RenderSystem.enableCull();
        BufferUploader.drawWithShader(AURA_BUFFER.end());
        renderType.clearRenderState();

        renderType.setupRenderState();
        RenderSystem.enableCull();
        BufferUploader.drawWithShader(MAGIC_SQUARE_BUFFER.end());
        renderType.clearRenderState();
    }

    public static void afterRenderLevel(LevelRenderer levelRenderer, float partialTick){
        Minecraft mc = Minecraft.getInstance();
        RenderTarget target = mc.getMainRenderTarget();
        int[] window = new int[]{target.width, target.height};
        TEST_RENDER_TARGET.resize(window[0],window[1],false);
        //TEST_RENDER_TARGET.copyDepthFrom(target);
        /*TEST_RENDER_TARGET.bindWrite(false);
            //TEST_RENDER_TARGET.blitToScreen(window[0],window[1],false);
        TEST_RENDER_TARGET.unbindWrite();*/


        RenderType bg_warp_ef_rt = MyRenderTypes.RENDER_TYPE_BACKGROUND_WARP_EFFECT;
        bg_warp_ef_rt.setupRenderState();
        RenderSystem.enableCull();
        ShaderInstance shader = RenderSystem.getShader();
        RenderSystem.disableDepthTest();
        TEST_RENDER_TARGET.bindWrite(true);
        BufferUploader.drawWithShader(BACKGROUND_WARP_EFFECT_BUFFER.end());
        bg_warp_ef_rt.clearRenderState();

        target.bindWrite(false);

        GlStateManager._enableBlend();
        TEST_RENDER_TARGET.blitToScreen(window[0],window[1],false);
    }
}
