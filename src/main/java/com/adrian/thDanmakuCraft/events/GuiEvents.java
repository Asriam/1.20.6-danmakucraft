package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.client.gui.widgets.GuiLayers;
import com.adrian.thDanmakuCraft.client.gui.widgets.SpellCardNameOverlay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GuiEvents {
    private static final GuiLayers layers = new GuiLayers();
    @SubscribeEvent
    public static void renderGuiOverlays(CustomizeGuiOverlayEvent.Chat event){
        layers.render(event.getGuiGraphics(),event.getWindow(), Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
    }

    public static void addLayers(){
        layers.add(new SpellCardNameOverlay());
    }
}
