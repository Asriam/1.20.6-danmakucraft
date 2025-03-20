package com.adrian.thDanmakuCraft.client.gui.editor.buttons;

import com.adrian.thDanmakuCraft.client.gui.editor.EditorScreen;
import com.adrian.thDanmakuCraft.client.renderer.VertexBuilder;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class EditorButton {

    private static final ResourceLocation EDITOR_BUTTON_TEXTURE = ResourceLocationUtil.thdanmakucraft("textures/gui/editor_button.png");

    public int xPos;
    public int yPos;

    public int width;
    public int height;

    public EditorButton(int xPos, int yPos, int width, int height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.xPos && mouseX <= this.xPos + this.width &&
                mouseY >= this.yPos && mouseY <= this.yPos + this.height;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick){
        BufferBuilder builder = EditorScreen.getBufferBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        VertexBuilder vertexBuilder = new VertexBuilder(builder);
        Color color = isMouseOver(mouseX,mouseY) ? new Color(200,200,200,255) : new Color(100,100,100,255);
        vertexBuilder.vertex(xPos,yPos,0)                   .color(color).endVertex();
        vertexBuilder.vertex(xPos+width,yPos,0)          .color(color).endVertex();
        vertexBuilder.vertex(xPos+width,yPos+height,0).color(color).endVertex();
        vertexBuilder.vertex(xPos,yPos+height,0)         .color(color).endVertex();
        BufferBuilder.RenderedBuffer buffer = builder.end();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        BufferUploader.drawWithShader(buffer);
    }

    public void onClicked(EditorScreen screen, int mouseX, int mouseY){

    }
}
