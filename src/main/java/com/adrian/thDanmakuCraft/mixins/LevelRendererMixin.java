package com.adrian.thDanmakuCraft.mixins;

import com.adrian.thDanmakuCraft.events.RenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Unique
    private LevelRenderer self(){
        return (LevelRenderer) (Object) this;
    }

    //@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderBuffers;bufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;", shift = At.Shift.AFTER))
    private void beforeRenderEntity(float partialTick,
                            long p_109602_,
                            boolean p_109603_,
                            Camera camera,
                            GameRenderer gameRenderer,
                            LightTexture lightTexture,
                            Matrix4f pose,
                            Matrix4f p_330527_,
                            CallbackInfo callback){
        RenderEvents.beforeRenderEntities(self(), partialTick);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V", ordinal = 0 ,shift = At.Shift.BEFORE))
    private void afterRenderEntity(float partialTick,
                                    long p_109602_,
                                    boolean p_109603_,
                                    Camera camera,
                                    GameRenderer gameRenderer,
                                    LightTexture lightTexture,
                                    Matrix4f pose,
                                    Matrix4f p_330527_,
                                    CallbackInfo callback){
        RenderEvents.afterRenderEntities(self(), partialTick);
    }
}
