package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.world.danmaku.thobject.Blend;
import com.mojang.blaze3d.shaders.BlendMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumMap;

@OnlyIn(Dist.CLIENT)
public class THBlendMode {
    private static BlendMode parseBlendNode(String blendFunc, String srcColor, String dstColor, String srcAlpha, String dstAlpha) {
        return new BlendMode(
                BlendMode.stringToBlendFactor(srcColor),
                BlendMode.stringToBlendFactor(dstColor),
                BlendMode.stringToBlendFactor(srcAlpha),
                BlendMode.stringToBlendFactor(dstAlpha),
                BlendMode.stringToBlendFunc(blendFunc));
    }

    private static BlendMode parseBlend(Blend blend) {
        return blend.isSeparateBlend() ? new BlendMode(
                BlendMode.stringToBlendFactor(blend.getSrcColor()),
                BlendMode.stringToBlendFactor(blend.getDstColor()),
                BlendMode.stringToBlendFactor(blend.getSrcAlpha()),
                BlendMode.stringToBlendFactor(blend.getDstAlpha()),
                BlendMode.stringToBlendFunc(blend.getBlendFunc()))
                : new BlendMode(
                BlendMode.stringToBlendFactor(blend.getSrcColor()),
                BlendMode.stringToBlendFactor(blend.getDstColor()),
                BlendMode.stringToBlendFunc(blend.getBlendFunc()));
    }

    private static final EnumMap<Blend, BlendMode> BLEND_MAP = new EnumMap<>(Blend.class);

    static {
        for(Blend blend: Blend.class.getEnumConstants()){
            BLEND_MAP.put(blend,parseBlend(blend));
        }
    }

    public static BlendMode getBlendMode(Blend blend) {
        return BLEND_MAP.get(blend);
    }
}
