package com.adrian.thDanmakuCraft.mixin;

import com.adrian.thDanmakuCraft.client.renderer.danmaku.THObjectContainerRenderer;
import com.adrian.thDanmakuCraft.events.RenderEvents;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.Entity;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    private LevelRenderer self(){
        return (LevelRenderer) (Object) this;
    }

    private Minecraft minecraft = Minecraft.getInstance();

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
    private void renderLevel(float partialTick,
                            long p_109602_,
                            boolean p_109603_,
                            Camera camera,
                            GameRenderer gameRenderer,
                            LightTexture lightTexture,
                            Matrix4f pose,
                            Matrix4f p_330527_,
                            CallbackInfo callback){
        RenderEvents.beforeRenderingEntities(self(), partialTick, camera);
    }
}
