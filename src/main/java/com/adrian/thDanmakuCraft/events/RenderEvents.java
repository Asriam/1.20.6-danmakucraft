package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.client.renderer.type.RenderTypes;
import com.adrian.thDanmakuCraft.client.renderer.RenderUtil;
import com.adrian.thDanmakuCraft.client.renderer.danmaku.THObjectContainerRenderer;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHSpellCardRenderer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL46;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID,/* bus = EventBusSubscriber.Bus.GAME,*/ value = Dist.CLIENT)
public class RenderEvents {
    private static final Map<String, RenderLevelStageTask> renderLevelStageTasks = new HashMap<>();
    public static int renderTickCount = 0;
    private static Minecraft minecraft = Minecraft.getInstance();
    public static Matrix4f ModelViewMatrix = new Matrix4f();
    public static Matrix4f ProjectionMatrix = new Matrix4f();
    public static int RenderTicks = 0;
    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        //Matrix4f pose = event.getPoseStack();
        PoseStack poseStack = new PoseStack();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        for (RenderLevelStageTask renderHelper: renderLevelStageTasks.values()){
            if (event.getStage() == renderHelper.stage){
                renderHelper.render(poseStack,partialTick);
            }
        }

        //System.out.println(RenderSystem.getProjectionMatrix().get(3,2));
        //RenderSystem.getProjectionMatrix().set(3,2,-0.3f);
        //GL46.glDepthFunc(GL46.GL_LESS);
        //GL46.glDepthRange(0.1f,0.5f);

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES
                && !mixinApplied) {
            // 備援：mixin 沒生效時，改用 Forge 事件把彈幕畫出來（否則完全看不到彈幕）
            if (!fallbackLogged) {
                fallbackLogged = true;
                THDanmakuCraftMod.LOGGER.warn("[thdanmakucraft] mixin NOT applied -> fallback render via RenderLevelStageEvent");
            }
            beforeRenderEntities(event.getLevelRenderer(), partialTick);
            afterRenderEntities(event.getLevelRenderer(), partialTick);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER){
            ModelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());
            ProjectionMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
            RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
            RenderTypes.DEPTH_BUFFER_TARGET.copyDepthFrom(target);
            target.bindWrite(false);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER){
            afterRenderWeather(event.getLevelRenderer(),partialTick);
        }
    }

    private static boolean mixinFiredLogged = false;
    private static boolean fallbackLogged = false;
    /** 只有 mixin 呼叫進來時才會設為 true（備援路徑不會動它）。 */
    public static volatile boolean mixinApplied = false;

    /** mixin 注入後實際呼叫的入口（設定 mixinApplied 後再交給一般流程）。 */
    public static void mixinBeforeRenderEntities(LevelRenderer levelRenderer, float partialTick) {
        mixinApplied = true;
        beforeRenderEntities(levelRenderer, partialTick);
    }

    public static void beforeRenderEntities(LevelRenderer levelRenderer, float partialTick){
        PoseStack poseStack = new PoseStack();
        List<THObject> objects = Lists.newArrayList();
        if (minecraft.level != null) {
            for(Entity entity : minecraft.level.entitiesForRendering()){
                if (entity instanceof EntityTHObjectContainer container){
                    objects.addAll(container.getContainer().getObjectManager().getTHObjects());
                }
            }
        }
        if (!mixinFiredLogged) {
            mixinFiredLogged = true;
            THDanmakuCraftMod.LOGGER.info("[thdanmakucraft] render callbacks fired, objects = {}",
                    objects.size());
        }
        poseStack.pushPose();
        THObjectContainerRenderer.renderTHObjects(
                minecraft.getEntityRenderDispatcher(),
                levelRenderer.getFrustum(),
                objects,
                minecraft.level.tickRateManager().isFrozen() ? 1.0f : partialTick,
                poseStack,
                minecraft.renderBuffers().bufferSource(),
                1);
        poseStack.popPose();

        EntityTHSpellCardRenderer.beforeRenderEntities(levelRenderer,partialTick);
    }

    public static void afterRenderEntities(LevelRenderer levelRenderer, float partialTick){
        EntityTHSpellCardRenderer.afterRenderEntities(levelRenderer,partialTick);
        EntityTHSpellCardRenderer.afterRenderLevel(levelRenderer,partialTick);
        //THObjectContainerRenderer.afterRenderEntities(levelRenderer,partialTick);
    }

    public static void afterRenderWeather(LevelRenderer levelRenderer, float partialTick){
        //EntityTHSpellCardRenderer.afterRenderLevel(levelRenderer,partialTick);
    }

    public static void afterRenderLevel(LevelRenderer levelRenderer, float partialTick){
        //EntityTHSpellCardRenderer.afterRenderLevel(levelRenderer,partialTick);
    }



    public static void registerRenderLevelStageTask(String name, RenderLevelStageEvent.Stage stage, RenderTask renderHelper){
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
    public static void renderTickPre(RenderFrameEvent.Pre event) {
        RenderUtil.quadList.clear();
    }

    @SubscribeEvent
    public static void renderTickPost(RenderFrameEvent.Post event) {
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
