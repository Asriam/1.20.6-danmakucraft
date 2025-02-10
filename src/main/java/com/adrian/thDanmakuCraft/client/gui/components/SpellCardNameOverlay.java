package com.adrian.thDanmakuCraft.client.gui.components;

import com.adrian.thDanmakuCraft.events.TickEvents;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class SpellCardNameOverlay implements IGuiOverlay{

    private final List<EntityTHSpellCard> spellCards = Lists.newArrayList();
    private final Minecraft minecraft;


    public SpellCardNameOverlay(){
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public boolean shouldRender() {
        return true;
    }

    public void render(GuiGraphics graphics, Window window){
        //System.out.print("sadasdas");
        graphics.drawString(Minecraft.getInstance().font, "THDanmakuCraft" + TickEvents.BulletMount, 0, 0, 0xFFFFFF);

        for(EntityTHSpellCard spellCard : spellCards){
            Component component = Component.literal(spellCard.getSpellCardName());
            graphics.drawString(minecraft.font, component, 0, 0, 16777215);
        }
    }
}
