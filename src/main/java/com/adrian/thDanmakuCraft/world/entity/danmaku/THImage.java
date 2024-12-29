package com.adrian.thDanmakuCraft.world.entity.danmaku;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class THImage {

    private final ResourceLocation textureLocation;
    private final Vec2 uvStart;
    private final Vec2 uvEnd;

    public THImage(ResourceLocation textureLocation, Vec2 uvStart, Vec2 uvEnd) {
        this.textureLocation = textureLocation;
        this.uvStart = uvStart;
        this.uvEnd = uvEnd;
    }

    public THImage(ResourceLocation textureLocation, float uStart, float vStart, float uEnd, float vEnd) {
        this(textureLocation, new Vec2(uStart, vStart), new Vec2(uEnd, vEnd));
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public Vec2 getUVStart() {
        return uvStart;
    }

    public Vec2 getUVEnd() {
        return uvEnd;
    }
}
