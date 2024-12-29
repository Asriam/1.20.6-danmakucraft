package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.mojang.blaze3d.shaders.BlendMode;

import java.util.EnumMap;

public class THBlendMode {
    private static BlendMode parseBlendNode(String blendFunc, String srcColor, String dstColor, String srcAlpha, String dstAlpha) {
        return new BlendMode(
                BlendMode.stringToBlendFactor(srcColor),
                BlendMode.stringToBlendFactor(dstColor),
                BlendMode.stringToBlendFactor(srcAlpha),
                BlendMode.stringToBlendFactor(dstAlpha),
                BlendMode.stringToBlendFunc(blendFunc));
    }

    private static BlendMode parseBlend(THObject.Blend blend) {
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

    private static final EnumMap<THObject.Blend, BlendMode> BLEND_MAP = new EnumMap<>(THObject.Blend.class);

    static {
        for(THObject.Blend blend:THObject.Blend.class.getEnumConstants()){
            BLEND_MAP.put(blend,parseBlend(blend));
        }
    }

    public static BlendMode getBlendMode(THObject.Blend blend) {
        return BLEND_MAP.get(blend);
    }
}
