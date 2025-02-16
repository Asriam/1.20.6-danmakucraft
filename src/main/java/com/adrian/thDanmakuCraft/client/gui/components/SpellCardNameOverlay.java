package com.adrian.thDanmakuCraft.client.gui.components;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.events.TickEvents;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class SpellCardNameOverlay implements IGuiOverlay{

    public static final List<EntityTHSpellCard> spellCards = Lists.newArrayList();
    private final Minecraft minecraft;


    public SpellCardNameOverlay(){
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public boolean shouldRender() {
        return !spellCards.isEmpty();
    }

    private static final ResourceLocation SPELL_CARD_UI_TEXTURE = new ResourceLocation(THDanmakuCraftCore.MOD_ID,"textures/gui/spellcard/boss_ui.png");

    public void render(GuiGraphics graphics, Window window, float partialTick){
        //System.out.print("sadasdas");
        Font font = minecraft.font;
        graphics.drawString(font, "bullet count:" + TickEvents.BulletMount, 0, 0, 0xFFFFFF);
        PoseStack poseStack = graphics.pose();

        int i = 0;

        int width  = 100;
        int height = 100;

        int barWidth = width;
        int barHeight = height*36/256;

        float y3 = 0;

        List<EntityTHSpellCard> removeList = Lists.newArrayList();
        //poseStack.pushPose();
        for(EntityTHSpellCard spellCard : spellCards) {
            boolean flag = spellCard.flagForRenderSpellCardNameBar;
            if(spellCard.deathTimerForRenderSpellCardNameBar < 20.0f) {
                Component component = Component.literal(spellCard.getSpellCardName());
                int fontWidth = font.width(component);

                float timer = spellCard.timerForRenderSpellCardNameBar + partialTick;
                float timer2 = Mth.lerp(partialTick, spellCard.lastDeathTimerForRenderSpellCardNameBar, spellCard.deathTimerForRenderSpellCardNameBar);

                Color color = Color.of(255, 255, 255, (int) (Math.min((timer + 0.1f)/ 20.0f, 1.0f)*255.0f));

                float scale = (float) Mth.lerp(Math.pow(Math.min(1.0f, timer / 20), 0.5f), 6.0f, 1.2f);

                float finalX = window.getGuiScaledWidth() - barWidth * scale;
                float finalY = barHeight * scale * i;

                float num = scale * Mth.clamp(timer2 / 10.0f, 0.0f, 1.0f);

                float x2 = timer2<=0.0f ? 0.0f : Math.max(fontWidth+3+barWidth,0.0f) * num;
                float y2 = timer2<=0.0f ? 0.0f : barHeight * num;
                y3 += y2;

                //poseStack.translate(0.0f, -y2, 0.0f);
                poseStack.pushPose();
                float pow = (float) Math.pow(Math.min(1.0f, timer / 20), 0.6f);
                poseStack.translate(
                        timer < 20.0f ? Mth.lerp(pow, window.getGuiScaledWidth() * 0.2f, finalX) : finalX + x2,
                        -(timer < 20.0f ? Mth.lerp(pow, window.getGuiScaledHeight() * 0.3f, 0) : 0) +
                                Mth.lerp(Mth.clamp(Math.pow(-Mth.cos(Mth.clamp((timer - 20) / 14, 0.0f, 1.0f) * Mth.PI) / 2 + 0.5f, 1.8f), 0.0f, 1.0f),
                                        window.getGuiScaledHeight() - barHeight * scale, finalY  - y3) + y2
                                ,
                        0);
                poseStack.scale(scale, scale, 1.0f);
                drawBar(SPELL_CARD_UI_TEXTURE, poseStack.last().pose(),
                         -Math.max(fontWidth+3-barWidth,0.0f), barWidth, 0, barHeight, 0, 0, 1.0f, 0, 1.0f * 36 / 256, color);
                graphics.drawString(font, component, -fontWidth + barWidth, 2, color.toInt());

                poseStack.popPose();
                i++;

                if (timer2 > 10.0f) {
                    //removeList.add(spellCard);
                }
            }
        }
        //poseStack.popPose();
        removeList.forEach(spellCards::remove);
    }
    static void drawBar(ResourceLocation textureLocation, Matrix4f pose, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1, Color color
    ) {
        RenderSystem.setShaderTexture(0, textureLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(pose, x0, y0, z).uv(u0, v0).color(color.r,color.g,color.b,color.a).endVertex();
        bufferbuilder.vertex(pose, x0, y1, z).uv(u0, v1).color(color.r,color.g,color.b,color.a).endVertex();
        bufferbuilder.vertex(pose, x1, y1, z).uv(u1, v1).color(color.r,color.g,color.b,color.a).endVertex();
        bufferbuilder.vertex(pose, x1, y0, z).uv(u1, v0).color(color.r,color.g,color.b,color.a).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        float alpha = (float) color.a /255;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(pose, x0, y0, z).color(0.0f,0.0f,0.0f,0.1f*alpha).endVertex();
        bufferbuilder.vertex(pose, x0, y1, z).color(0.0f,0.0f,0.0f,0.1f*alpha).endVertex();
        bufferbuilder.vertex(pose, x1, y1, z).color(0.0f,0.0f,0.0f,0.5f*alpha).endVertex();
        bufferbuilder.vertex(pose, x1, y0, z).color(0.0f,0.0f,0.0f,0.5f*alpha).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }
}
