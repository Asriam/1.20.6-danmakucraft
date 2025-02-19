package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityExampleRenderer;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHObjectContainerRenderer;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHSpellCardRenderer;
import com.adrian.thDanmakuCraft.init.EntityInit;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event){
        EntityRenderers.register(EntityInit.EXAMPLE_ENTITY.get(), EntityExampleRenderer::new);
        EntityRenderers.register(EntityInit.ENTITY_THOBJECT_CONTAINER.get(), EntityTHObjectContainerRenderer::new);
        EntityRenderers.register(EntityInit.ENTITY_THSPELLCARD.get(), EntityTHSpellCardRenderer::new);
        //event.registerEntityRenderer(EntityInit.EXAMPLE_ENTITY.get(), EntityExampleRenderer::new);
        //event.registerEntityRenderer(EntityInit.ENTITY_THOBJECT_CONTAINER.get(), EntityTHObjectContainerRenderer::new);
    }
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event){
    }

}
