package com.adrian.thDanmakuCraft.client.gui.components;

import com.adrian.thDanmakuCraft.events.TickEvents;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.client.renderer.VertexBuilder;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix4f;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SpellCardNameOverlay implements IGuiOverlay {

    public static final List<EntityTHSpellCard> spellCards = Lists.newArrayList();
    private final Minecraft minecraft;


    public SpellCardNameOverlay(){
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public boolean shouldRender() {
        return !spellCards.isEmpty();
    }

    private static final ResourceLocation SPELL_CARD_UI_TEXTURE = ResourceLocationUtil.thdanmakucraft("textures/gui/spellcard/boss_ui.png");

    //private static final BufferBuilder BUFFER_1 = new BufferBuilder(221);
    private static final BufferBuilder BUFFER_2 = new BufferBuilder(222);
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

        BufferBuilder BUFFER_1 = RenderSystem.renderThreadTesselator().getBuilder();
        BUFFER_1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        BUFFER_2.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        List<EntityTHSpellCard> removeList = Lists.newArrayList();
        //poseStack.pushPose();
        for(EntityTHSpellCard spellCard : spellCards) {
            boolean flag = spellCard.paramsForRender.flag1;
            if(spellCard.paramsForRender.deathTimer < 20.0f) {
                Component component = Component.literal(spellCard.getSpellCardName());
                int fontWidth = font.width(component);

                float timer = spellCard.paramsForRender.timer1 + partialTick;
                float timer2 = Mth.lerp(partialTick, spellCard.paramsForRender.lastDeathTimer, spellCard.paramsForRender.deathTimer);

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
                drawBar(BUFFER_1,BUFFER_2,poseStack.last().pose(),
                         -Math.max(fontWidth+3-barWidth,0.0f), barWidth, 0, barHeight, 0, 0, 1.0f, 0, 1.0f * 36 / 256, color);
                graphics.drawString(font, component, -fontWidth + barWidth, 2, color.toInt());

                poseStack.popPose();
                i++;

                if (timer2 > 10.0f) {
                    //removeList.add(spellCard);
                }
            }
        }
        RenderSystem.setShaderTexture(0, SPELL_CARD_UI_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        BufferUploader.drawWithShader(BUFFER_1.end());
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.drawWithShader(BUFFER_2.end());
        RenderSystem.disableBlend();
        //poseStack.popPose();
        removeList.forEach(spellCards::remove);
    }
    static void drawBar(BufferBuilder BUFFER_1, BufferBuilder BUFFER_2,Matrix4f pose, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1, Color color
    ) {
        VertexBuilder builder = new VertexBuilder(BUFFER_1);
        builder.positionColorUV(pose, x0, y0, z, color, u0, v0);
        builder.positionColorUV(pose, x0, y1, z, color, u0, v1);
        builder.positionColorUV(pose, x1, y1, z, color, u1, v1);
        builder.positionColorUV(pose, x1, y0, z, color, u1, v0);
        float alpha = (float) color.a /255;
        VertexBuilder builder2 = new VertexBuilder(BUFFER_2);
        builder2.positionColor(pose, x0, y0, z,0,0,0,(int) (25 * alpha));
        builder2.positionColor(pose, x0, y1, z,0,0,0,(int) (25 * alpha));
        builder2.positionColor(pose, x1, y1, z,0,0,0,(int) (125 * alpha));
        builder2.positionColor(pose, x1, y0, z,0,0,0,(int) (125 * alpha));
    }
}
