package com.adrian.thDanmakuCraft.client.renderer.entity;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class TestPostEffectEntity<T extends Entity> extends EntityRenderer<T> {
    private final RenderTarget renderTarget;
    private ShaderInstance postEffectShader;

    public TestPostEffectEntity(EntityRendererProvider.Context context) {
        super(context);
        this.renderTarget = new MainTarget(256, 256); // Create a RenderTarget for the entity

        try {
            // Load the post-effect shader
            this.postEffectShader = new ShaderInstance(
                    Minecraft.getInstance().getResourceManager(),
                    new ResourceLocation("yourmodid", "shaders/post/entity_effect"),
                    DefaultVertexFormat.POSITION_TEX
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Bind the custom RenderTarget
        renderTarget.bindWrite(true);
        //RenderSystem.clear(GlUtil.GL_COLOR_BUFFER_BIT | GlUtil.GL_DEPTH_BUFFER_BIT, false);

        // Render the entity to the RenderTarget
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        // Unbind the RenderTarget
        renderTarget.unbindWrite();

        // Apply the shader effect
        if (postEffectShader != null) {
            postEffectShader.setSampler("Sampler0", renderTarget.getColorTextureId());
            postEffectShader.safeGetUniform("Time").set((float) (Minecraft.getInstance().level.getGameTime() + partialTicks));

            postEffectShader.apply();

            // Render the quad with the post-processed texture
            renderPostProcessedEntity();
        }
    }

    private void renderPostProcessedEntity() {
        // Render a full-screen quad or a screen-aligned quad with the post-processed texture
        RenderSystem.setShaderTexture(0, renderTarget.getColorTextureId());
        // Render your quad here (vertex buffer code not shown for brevity)
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        // Return the default texture or a placeholder
        return new ResourceLocation("yourmodid", "textures/entity/example.png");
    }

    public void cleanup() {
        if (postEffectShader != null) {
            postEffectShader.close();
        }
    }
}