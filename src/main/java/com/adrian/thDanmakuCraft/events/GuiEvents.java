package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.client.gui.components.MyLayeredDraw;
import com.adrian.thDanmakuCraft.client.gui.components.SpellCardNameOverlay;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GuiEvents {
    private static final MyLayeredDraw layers = new MyLayeredDraw();
    @SubscribeEvent
    public static void renderGuiOverlays(CustomizeGuiOverlayEvent.Chat event){
        layers.render(event.getGuiGraphics(),event.getWindow(),event.getPartialTick());
    }

    public static void addLayers(){
        layers.add(new SpellCardNameOverlay());
    }
}
