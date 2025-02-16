package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.THObjectContainerRenderer;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderEvents {
    private static final Map<String, RenderLevelStageTask> renderLevelStageTasks = new HashMap<>();

    public static int renderTickCount = 0;

    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(event.getPoseStack());
        float partialTick = event.getPartialTick();

        for (RenderLevelStageTask renderHelper: renderLevelStageTasks.values()){
            if (event.getStage() == renderHelper.stage){
                renderHelper.render(poseStack,partialTick);
            }
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES){
            for(Entity entity : Minecraft.getInstance().level.entitiesForRendering()){
                if (entity instanceof EntityTHObjectContainer container){
                    poseStack.pushPose();
                    final Vec3 cameraPosition = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
                    final double camX = cameraPosition.x;
                    final double camY = cameraPosition.y;
                    final double camZ = cameraPosition.z;
                    //poseStack.translate(camX,camY,camZ);
                    THObjectContainerRenderer.render(
                            Minecraft.getInstance().getEntityRenderDispatcher(),
                            Minecraft.getInstance().levelRenderer.getFrustum(),
                            container.getContainer(),
                            partialTick,
                            poseStack,
                            Minecraft.getInstance().renderBuffers().bufferSource(),
                            1);
                    poseStack.popPose();
                }
            };
        }
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

        public void render(PoseStack poseStack,float partialTick){
            renderTask.render(poseStack,partialTick);
        }
    }

    public interface RenderTask {

        void render(PoseStack poseStack,float partialTick);
    }

    @SubscribeEvent
    public static void renderTickPre(TickEvent.RenderTickEvent.Pre event) {
        RenderUtil.quadList.clear();
    }

    @SubscribeEvent
    public static void renderTickPost(TickEvent.RenderTickEvent.Post event) {
        //THDanmakuCraftCore.LOGGER.info("Quads mount:" + RenderUtil.quadList.size());
        renderTickCount++;
    }

    @SubscribeEvent
    public static void playerRenderTick(RenderPlayerEvent.Post event) {
        Entity player = event.getEntity();
        /*
        THObjectContainer container = MyPlayer.getEntityTHObjectContainer(player);
        if(container != null){
            THObjectContainerRenderer.render(Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().levelRenderer.getFrustum(), container, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }*/
        /*
        player.getCapability(THObjectContainerProvider.PLAYER_THOBJECT_CONTAINER_CAPABILITY).ifPresent(container -> {
            THObjectContainerRenderer.render(
                    Minecraft.getInstance().getEntityRenderDispatcher(),
                    Minecraft.getInstance().levelRenderer.getFrustum(),
                    container, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        });*/
    }

}
