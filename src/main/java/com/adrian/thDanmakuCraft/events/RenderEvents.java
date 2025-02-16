package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.THObjectContainerRenderer;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
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
import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderEvents {
    private static final Map<String, RenderLevelStageTask> renderLevelStageTasks = new HashMap<>();

    public static int renderTickCount = 0;

    private static Minecraft minecraft = Minecraft.getInstance();
    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        Matrix4f pose = event.getPoseStack();
        PoseStack poseStack = new PoseStack();
        float partialTick = event.getPartialTick();

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES){
            List<THObject> objects = Lists.newArrayList();
            for(Entity entity : minecraft.level.entitiesForRendering()){
                if (entity instanceof EntityTHObjectContainer container){
                    objects.addAll(container.getContainer().getObjectManager().getTHObjects());
                }
            }

            //poseStack.mulPose(pose);
            poseStack.pushPose();
            THObjectContainerRenderer.renderTHObjects(
                    minecraft.getEntityRenderDispatcher(),
                    minecraft.levelRenderer.getFrustum(),
                    objects,
                    minecraft.level.tickRateManager().isFrozen() ? 1.0f : partialTick,
                    poseStack,
                    minecraft.renderBuffers().bufferSource(),
                    1);
            poseStack.popPose();
        }

        for (RenderLevelStageTask renderHelper: renderLevelStageTasks.values()){
            if (event.getStage() == renderHelper.stage){
                renderHelper.render(poseStack,partialTick);
            }
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
