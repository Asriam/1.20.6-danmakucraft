package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.client.renderer.THBlendMode;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.client.renderer.VertexBuilder;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.IImage;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
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
import org.lwjgl.opengl.GL11;

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
        RenderType renderType = THRenderType.RENDER_TYPE_SPELLCARD_AURA.apply(
                new THRenderType.RENDER_TYPE_2D_DANMAKU_CONTEXT(image.getTextureLocation(), THBlendMode.getBlendMode(THObject.Blend.add))
        );

        Color color = new Color(255,255,255,160);
        Vec2 uvStart = image.getUVStart();
        Vec2 uvEnd =   image.getUVEnd();
        float timer = container.getTimer()+partialTicks;
        float rotate = (timer)/10.0f;
        float time = 30.0f;
        float open = timer > time ? 1.0f : (float) Math.pow(Math.min(timer / time,1.0f),0.8f);
        float close = timer < container.getLifetime()-time ? 1.0f : 1.0f - (float) Math.pow(Math.clamp((timer-(container.getLifetime())) / time,0.0f,1.0f),2.0f);
        float timeLeft = 1.0f-(float) container.getTimer()/container.getLifetime();
        Vec2 faceCamRotation = THObject.VectorAngleToRadAngle(this.getRenderDispatcher().camera.getPosition().vectorTo(pos));
        Vec2 scale = Vec2.ONE/*.scale((1.0f-(float) container.getTimer()/container.getLifetime()))*/
                .scale(8.0f + 0.8f*Mth.cos(timer/8.0f))
                .scale(open*close);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
        if(container.shouldRenderMagicAura) {
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            {
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                poseStack.mulPose(this.getRenderDispatcher().cameraOrientation()
                        .rotateZ(rotate));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                VertexBuilder vertexBuilder = new VertexBuilder(builder);
                vertexBuilder.vertexPositionColorUV(pose.pose(), -scale.x, -scale.y, 0.0f, color, uvStart.x, uvEnd.y);
                vertexBuilder.vertexPositionColorUV(pose.pose(), scale.x, -scale.y, 0.0f, color, uvStart.x, uvStart.y);
                vertexBuilder.vertexPositionColorUV(pose.pose(), scale.x, scale.y, 0.0f, color, uvEnd.x, uvStart.y);
                vertexBuilder.vertexPositionColorUV(pose.pose(), -scale.x, scale.y, 0.0f, color, uvEnd.x, uvEnd.y);
                poseStack.popPose();
            }
            renderType.setupRenderState();
            RenderSystem.enableCull();
            BufferUploader.drawWithShader(builder.end());
            RenderSystem.disableCull();
            renderType.clearRenderState();
        }

        if(!container.shouldRenderLineAura){
            return;
        }

        color = new Color(180,180,255,160);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        {
            int sample = 36;

            time = 40.0f;
            open = timer > time ? 1.0f : (float) Math.pow(Math.min(timer / time,1.0f),0.8f);
            close = timer < container.getLifetime()-time ? 1.0f : 1.0f - (float) Math.pow(Math.clamp((timer-(container.getLifetime())) / time,0.0f,1.0f),0.8f);
            float width = 1.6f * timeLeft * 1.4f;
            float width2 = width*3.0f;
            float num = 4.0f;
            float radius = (20.0f + 10.0f) * open*close * timeLeft;
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
                    VertexBuilder vertexBuilder = new VertexBuilder(builder);
                    vertexBuilder.vertexPositionColorUV(pose2.pose(), rotation, color, uvStart.x, v1);
                    vertexBuilder.vertexPositionColorUV(pose2.pose(), rotation2, color, uvStart.x, v2);
                    vertexBuilder.vertexPositionColorUV(pose2.pose(), rotation3, color, uvEnd.x, v2);
                    vertexBuilder.vertexPositionColorUV(pose2.pose(), rotation4, color, uvEnd.x, v1);
                }
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf()
                    .rotateY(rotate/6.0f)
                    .rotateX(rotate/6.0f)
            );
            num = 8.0f;
            int aaa = 4;

            float rotateOffset = Mth.PI/4.0f + timer / 6.0f;
            for (int g=0;g<=2;g++) {
                radius = (20.0f+g*2.0f) * open*close * timeLeft;
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
                        VertexBuilder vertexBuilder = new VertexBuilder(builder);
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
                        vertexBuilder.vertexPositionColorUV(pose.pose(), pos1,  color, uvStart.x, v1);
                        vertexBuilder.vertexPositionColorUV(pose.pose(), pos2, color, uvStart.x, v2);
                        vertexBuilder.vertexPositionColorUV(pose.pose(), pos3, color, uvEnd.x, v2);
                        vertexBuilder.vertexPositionColorUV(pose.pose(), pos4, color, uvEnd.x, v1);
                    }

                    /*Vec3 vec3 = new Vec3(0.0f, radius, 0.0f);
                    Vector3f rotation = vec3.zRot((float) (i * Math.PI * 2.0f / sample)).toVector3f();
                    Vector3f rotation2 = vec3.zRot((float) ((i + 1) * Math.PI * 2.0f / sample)).toVector3f();
                    Vector3f rotation3 = vec3.add(0.0f, width, 0.0f).zRot((float) ((i + 1) * Math.PI * 2.0f / sample)).toVector3f();
                    Vector3f rotation4 = vec3.add(0.0f, width, 0.0f).zRot((float) ((i) * Math.PI * 2.0f / sample)).toVector3f();
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
                            .endVertex();*/
                }
                poseStack.popPose();
            }
            poseStack.popPose();
        }
        renderType.setupRenderState();
        //RenderSystem.depthMask(false);
        RenderSystem.setShaderTexture(0, spellCardAura.getTextureLocation());
        RenderSystem.enableCull();
        BufferUploader.drawWithShader(builder.end());
        //RenderSystem.disableCull();
        //RenderSystem.depthMask(true);
        renderType.clearRenderState();
    }
}
