package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.client.renderer.THBlendMode;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.IImage;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityTHSpellCardRenderer extends EntityTHObjectContainerRenderer<EntityTHSpellCard>{
    public EntityTHSpellCardRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityTHSpellCard entity, float rotationX, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedOverlay) {
        super.render(entity, rotationX, partialTicks, poseStack, bufferSource, combinedOverlay);
        THObjectContainer container = entity.getContainer();
        if(container.isSpellCard() && (container.shouldRenderMagicAura || container.shouldRenderLineAura)) {
            renderSpellCardAura(entity.getContainer(), poseStack, entity.getPosition(partialTicks), partialTicks);
        }
    }

    private IImage.Image spellCardAura = new IImage.Image(
            ResourceLocation.fromNamespaceAndPath(THDanmakuCraftCore.MOD_ID,"textures/spellcard/eff_line.png"), 0.0f, 0.0f, 1.0f, 1.0f);

    private IImage.Image SPELLCARD_MAGIC_SQUAR = new IImage.Image(
            ResourceLocation.fromNamespaceAndPath(THDanmakuCraftCore.MOD_ID,"textures/spellcard/eff_magicsquare.png"), 0.0f, 0.0f, 1.0f, 1.0f);
    public void renderSpellCardAura(THObjectContainer container, PoseStack poseStack, Vec3 pos, float partialTicks){
        IImage.Image image = SPELLCARD_MAGIC_SQUAR;
        RenderType renderType = THRenderType.RENDER_TYPE_THOBJECT.apply(
                new THRenderType.RENDER_TYPE_2D_DANMAKU_CONTEXT(image.getTextureLocation(), THBlendMode.getBlendMode(THObject.Blend.add))
        );

        Color color = new Color(255,255,255,160);
        Vec2 uvStart = image.getUVStart();
        Vec2 uvEnd =   image.getUVEnd();
        float timer = container.getTimer()+partialTicks;
        float rotate = (timer)/10.0f;
        float time = 30.0f;
        float open = timer > time ? 1.0f : (float) Math.pow(Math.min(timer / time,1.0f),0.8f);
        float close = timer < container.getLifetime()-time ? 1.0f : 1.0f - (float) Math.pow(Math.clamp((timer-(container.getLifetime())) / time,0.0f,1.0f),0.4f);
        float timeLeft = 1.0f-(float) container.getTimer()/container.getLifetime();
        Vec2 faceCamRotation = THObject.VectorAngleToRadAngle(this.getRenderDispatcher().camera.getPosition().vectorTo(pos));
        Vec2 scale = Vec2.ONE/*.scale((1.0f-(float) container.getTimer()/container.getLifetime()))*/.scale(10.0f + 0.8f*Mth.cos(timer/8.0f))
                .scale(open*close);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
        if(container.shouldRenderMagicAura) {
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            //poseStack.pushPose();
            {
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                poseStack.mulPose(new Quaternionf()
                        .rotateY(faceCamRotation.y + Mth.cos(timer/60.0f) * 0.8f)
                        .rotateX(-faceCamRotation.x + Mth.sin(-timer/40.0f) * 0.8f)
                        .rotateZ(rotate));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                builder.vertex(pose, -scale.x, -scale.y, 0.0f)
                        .color(color.r, color.g, color.b, color.a)
                        .uv(uvStart.x, uvEnd.y)
                        .endVertex();
                builder.vertex(pose, scale.x, -scale.y, 0.0f)
                        .color(color.r, color.g, color.b, color.a)
                        .uv(uvStart.x, uvStart.y)
                        .endVertex();
                builder.vertex(pose, scale.x, scale.y, 0.0f)
                        .color(color.r, color.g, color.b, color.a)
                        .uv(uvEnd.x, uvStart.y)
                        .endVertex();
                builder.vertex(pose, -scale.x, scale.y, 0.0f)
                        .color(color.r, color.g, color.b, color.a)
                        .uv(uvEnd.x, uvEnd.y)
                        .endVertex();
                poseStack.popPose();
            }
            renderType.setupRenderState();
            RenderSystem.disableCull();
            BufferUploader.drawWithShader(builder.end());
            renderType.clearRenderState();
        }

        if(!container.shouldRenderLineAura){
            return;
        }

        color = new Color(255,255,255,100);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        {
            int sample = 24;

            time = 40.0f;
            open = timer > time ? 1.0f : (float) Math.pow(Math.min(timer / time,1.0f),0.8f);
            close = timer < container.getLifetime()-time ? 1.0f : 1.0f - (float) Math.pow(Math.clamp((timer-(container.getLifetime())) / time,0.0f,1.0f),0.8f);
            float width = 1.6f * timeLeft;
            float width2 = width*3.0f;
            float num = 4.0f;
            float radius = (20.0f + 8.0f) * open*close * timeLeft;
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
                    builder.vertex(pose2, rotation.x, rotation.y, 0)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvStart.x, v1)
                            .endVertex();
                    builder.vertex(pose2, rotation2.x, rotation2.y, 0)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvStart.x, v2)
                            .endVertex();
                    builder.vertex(pose2, rotation3.x, rotation3.y, 0)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvEnd.x, v2)
                            .endVertex();
                    builder.vertex(pose2, rotation4.x, rotation4.y, 0)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvEnd.x, v1)
                            .endVertex();
                }
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf()
                    .rotateY(rotate/6.0f)
                    .rotateX(rotate/6.0f)
            );
            num = 8.0f;
            for (int g=0;g<=2;g++) {
                radius = (20.0f+g*2.0f) * open*close * timeLeft;
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                poseStack.mulPose(new Quaternionf()
                        .rotateY(Math.min(g,1)*Mth.PI/2.0f)
                        .rotateX(Math.max(g-1,0)*Mth.PI/2.0f)
                        .rotateZ(rotate*2));
                for (int i = 0; i < sample; i++) {
                    Vec3 vec3 = new Vec3(0.0f, radius, 0.0f);
                    Vector3f rotation = vec3.zRot((float) (i * Math.PI * 2.0f / sample)).toVector3f();
                    Vector3f rotation2 = vec3.zRot((float) ((i + 1) * Math.PI * 2.0f / sample)).toVector3f();
                    Vector3f rotation3 = vec3.add(0.0f, width, 0.0f).zRot((float) ((i + 1) * Math.PI * 2.0f / sample)).toVector3f();
                    Vector3f rotation4 = vec3.add(0.0f, width, 0.0f).zRot((float) ((i) * Math.PI * 2.0f / sample)).toVector3f();

                    float v1 = i / (sample / num);
                    float v2 = (i + 1) / (sample / num);
                    uvStart = new Vec2(1.0f / 8.0f * ((2-g)), 0.0f);
                    uvEnd = new Vec2(1.0f / 8.0f * (2-g+1), 1.0f);

                    builder.vertex(pose, rotation.x, rotation.y, width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvStart.x, v1)
                            .endVertex();
                    builder.vertex(pose, rotation2.x, rotation2.y, width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvStart.x, v2)
                            .endVertex();
                    builder.vertex(pose, rotation3.x, rotation3.y, width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvEnd.x, v2)
                            .endVertex();
                    builder.vertex(pose, rotation4.x, rotation4.y, width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvEnd.x, v1)
                            .endVertex();

                    builder.vertex(pose, rotation4.x, rotation4.y, -width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvEnd.x, v1)
                            .endVertex();
                    builder.vertex(pose, rotation3.x, rotation3.y, -width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvEnd.x, v2)
                            .endVertex();
                    builder.vertex(pose, rotation2.x, rotation2.y, -width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvStart.x, v2)
                            .endVertex();
                    builder.vertex(pose, rotation.x, rotation.y, -width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvStart.x, v1)
                            .endVertex();

                    //uvStart = new Vec2(0.0f, 0.0f);
                    //uvEnd = new Vec2(1.0f / 8.0f, 1.0f);

                    builder.vertex(pose, rotation.x, rotation.y, -width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvStart.x, v1)
                            .endVertex();
                    builder.vertex(pose, rotation2.x, rotation2.y, -width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvStart.x, v2)
                            .endVertex();
                    builder.vertex(pose, rotation2.x, rotation2.y, width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvEnd.x, v2)
                            .endVertex();
                    builder.vertex(pose, rotation.x, rotation.y, width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvEnd.x, v1)
                            .endVertex();

                    builder.vertex(pose, rotation3.x, rotation3.y, -width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvStart.x, v1)
                            .endVertex();
                    builder.vertex(pose, rotation4.x, rotation4.y, -width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvStart.x, v2)
                            .endVertex();
                    builder.vertex(pose, rotation4.x, rotation4.y, width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvEnd.x, v2)
                            .endVertex();
                    builder.vertex(pose, rotation3.x, rotation3.y, width / 2)
                            .color(color.r, color.g, color.b, color.a)
                            .uv(uvEnd.x, v1)
                            .endVertex();
                }
                poseStack.popPose();
            }
            poseStack.popPose();
        }
        renderType.setupRenderState();
        RenderSystem.enableCull();
        //RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, spellCardAura.getTextureLocation());
        BufferUploader.drawWithShader(builder.end());
        renderType.clearRenderState();

        //poseStack.popPose();
    }
}
