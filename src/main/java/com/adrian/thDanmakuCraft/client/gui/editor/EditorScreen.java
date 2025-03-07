package com.adrian.thDanmakuCraft.client.gui.editor;

import com.adrian.thDanmakuCraft.client.gui.editor.buttons.EditorButton;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class EditorScreen extends Screen {

    private static final BufferBuilder EDITOR_BUFFER_BUILDER = new BufferBuilder(223);
    private final Map<String,List<EditorButton>> BUTTON_LISTS = Maps.newHashMap();

    public EditorScreen() {
        super(Component.literal("editor"));
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public void init(){
        super.init();
        this.addButtons();
    }

    @Override
    public void tick() {
        super.tick();
        this.addButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        /// Render buttons
        for(List<EditorButton> buttonList : BUTTON_LISTS.values()){
            for (EditorButton button : buttonList){
                button.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    public void addButton(String buttonListName, EditorButton button){
        BUTTON_LISTS.computeIfAbsent(buttonListName, k -> Lists.newArrayList()).add(button);
    }

    public void clearButtons(String buttonListName){
        BUTTON_LISTS.remove(buttonListName);
    }

    public void addButtons(){
        BUTTON_LISTS.clear();
        int width = 50;
        int height = 15;
        int yPos = 10;
        int gapWidth = 2;
        EditorButton lastButton = new EditorButton(0, 10, width, height);
        this.addButton("main", lastButton);
        lastButton = new EditorButton(lastButton.xPos+lastButton.width+gapWidth, 10, width, height);
        this.addButton("main", lastButton);
    }

    public static BufferBuilder getBufferBuilder(){
        return EDITOR_BUFFER_BUILDER;
    }
}
