package com.adrian.thDanmakuCraft.mixins;

import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHSpellCardRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "resizeDisplay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;resize(IIZ)V",ordinal = 0, shift = At.Shift.AFTER))
    private void onResizeScreen(CallbackInfo ci){
        EntityTHSpellCardRenderer.onResizeScreen();
    }
}
