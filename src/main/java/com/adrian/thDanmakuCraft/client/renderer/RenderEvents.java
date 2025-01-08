package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderEvents {
    private static final Map<String, RenderLevelStageTask> renderLevelStageTasks = new HashMap<>();

    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(event.getPoseStack());
        float partialTick = event.getPartialTick();

        for (RenderLevelStageTask renderHelper: renderLevelStageTasks.values()){
            if (event.getStage() == renderHelper.stage){
                renderHelper.renderTask.render(poseStack,partialTick);
            }
        }
        /*
        THDanmakuCraftCore.LOGGER.info(event.getStage().toString());

         */
    }

    public static void registerRenderLevelStageTask(String name, RenderLevelStageEvent.Stage stage, RenderTask renderHelper){
        //if(renderLevelStageTasks.get(name) == null) renderLevelStageTasks.put(name, new RenderLevelStageTask(stage, renderHelper));
        renderLevelStageTasks.computeIfAbsent(name, k -> new RenderLevelStageTask(stage, renderHelper));
    }

    public static void clearRenderLevelStage(String name){
        renderLevelStageTasks.remove(name);
    }

    public static void clearAllRenderLevelStage(){
        renderLevelStageTasks.clear();
    }

    static class RenderLevelStageTask {
        RenderLevelStageEvent.Stage stage;
        RenderTask renderTask;

        RenderLevelStageTask(RenderLevelStageEvent.Stage stage, RenderTask renderHelper){
            this.stage = stage;
            this.renderTask = renderHelper;
        }
    }

    public interface RenderTask {

        void render(PoseStack poseStack,float partialTick);
    }

    @SubscribeEvent
    public static void renderTickPre(TickEvent.RenderTickEvent.Pre event) {
        //RenderUtil.quadList.clear();
    }

    @SubscribeEvent
    public static void renderTickPost(TickEvent.RenderTickEvent.Post event) {
        //THDanmakuCraftCore.LOGGER.info("Quads mount:" + RenderUtil.quadList.size());
    }
}
