package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.client.gui.components.IGuiOverlay;
import com.adrian.thDanmakuCraft.client.gui.components.SpellCardNameOverlay;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GuiEvents {
    private static final Map<String, IGuiOverlay> GUI_OVERLAYS = Maps.newLinkedHashMap();
    @SubscribeEvent
    public static void renderGuiOverlays(CustomizeGuiOverlayEvent.Chat event){
        for(IGuiOverlay overlay : GUI_OVERLAYS.values()){
            if(overlay.shouldRender()){
                overlay.render(event.getGuiGraphics(),Minecraft.getInstance().getWindow(), event.getPartialTick());
            }
        }
    }

    public static void register(){
        registerGuiOverlay("spell_card_name", new SpellCardNameOverlay());
    }

    public static void registerGuiOverlay(String name, IGuiOverlay overlay){
        GUI_OVERLAYS.put(name, overlay);
    }

    public static IGuiOverlay getGuiOverlay(String name){
        return GUI_OVERLAYS.get(name);
    }
}
