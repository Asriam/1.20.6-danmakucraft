package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderEvents {
    private static final Map<String,RenderHelper2> renderHelpers = new HashMap<>();

    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(event.getPoseStack());
        float partialTick = event.getPartialTick();

        for (RenderHelper2 renderHelper:renderHelpers.values()){
            if (event.getStage() == renderHelper.stage){
                renderHelper.renderHelper.render(poseStack,partialTick);
            }
        }
        /*
        THDanmakuCraftCore.LOGGER.info(event.getStage().toString());

         */
    }

    @SubscribeEvent
    public static void aaa(RenderHighlightEvent event){}


    public static void registryRenderLevelStageTask(String name, RenderLevelStageEvent.Stage stage, RenderHelper renderHelper){
        if(renderHelpers.get(name) == null) {
            renderHelpers.put(name, new RenderHelper2(stage, renderHelper));
        }else {
            //THDanmakuCraftCore.LOGGER.warn("");
        }
    }

    public static void clearRenderLevelStage(String name){
        renderHelpers.remove(name);
    }

    public static void clearAllRenderLevelStage(){
        renderHelpers.clear();
    }

    static class RenderHelper2{
        RenderLevelStageEvent.Stage stage;
        RenderHelper renderHelper;

        RenderHelper2(RenderLevelStageEvent.Stage stage, RenderHelper renderHelper){
            this.stage = stage;
            this.renderHelper = renderHelper;
        }
    }

    public interface RenderHelper {

        void render(PoseStack poseStack,float partialTick);
    }
}
