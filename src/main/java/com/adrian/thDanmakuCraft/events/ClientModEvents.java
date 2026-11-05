package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityExampleRenderer;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHObjectContainerRenderer;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHSpellCardRenderer;
import com.adrian.thDanmakuCraft.client.renderer.entity.mount.BroomMountRenderer;
import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.world.danmaku.LuaEventCache;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event){
        EntityRenderers.register(EntityInit.EXAMPLE_ENTITY.get(), EntityExampleRenderer::new);
        EntityRenderers.register(EntityInit.ENTITY_THOBJECT_CONTAINER.get(), EntityTHObjectContainerRenderer::new);
        EntityRenderers.register(EntityInit.ENTITY_THSPELLCARD.get(), EntityTHSpellCardRenderer::new);
        EntityRenderers.register(EntityInit.BROOM_MOUNT.get(), BroomMountRenderer::new);
        //event.registerEntityRenderer(EntityInit.EXAMPLE_ENTITY.get(), EntityExampleRenderer::new);
        //event.registerEntityRenderer(EntityInit.ENTITY_THOBJECT_CONTAINER.get(), EntityTHObjectContainerRenderer::new);
    }
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event){
        // 診斷用：列出 mixin 到底註冊了哪些設定（正式環境不開 verbose 也看得到）
        try {
            java.util.List<String> configs = org.spongepowered.asm.mixin.MixinEnvironment
                    .getCurrentEnvironment().getMixinConfigs();
            THDanmakuCraftMod.LOGGER.info("[thdanmakucraft] mixin configs = {}", configs);
            THDanmakuCraftMod.LOGGER.info("[thdanmakucraft] 本模組 mixin 設定已註冊? {}",
                    configs.contains("mixins.thdanmakucraft.json"));
        } catch (Throwable error) {
            THDanmakuCraftMod.LOGGER.warn("[thdanmakucraft] 無法讀取 mixin 設定清單", error);
        }
        event.enqueueWork(() -> {

        });
    }
}
