package com.adrian.thDanmakuCraft.world.danmaku.thobject;

public enum Blend {
    normal("add", "src_alpha", "one_minus_src_alpha", "one", "one_minus_src_alpha"),
    add("add", "src_alpha", "one"),
    sub("subtract", "src_alpha", "one_minus_src_alpha"),
    max("max", "src_alpha", "one_minus_src_alpha"),
    min("min", "src_alpha", "one_minus_src_alpha"),
    mul_add("add", "dst_color", "1-srcalpha", "one", "1-srcalpha"),
    mul_rev("reverse_subtract", "dstcolor", "1-srcalpha", "one", "1-srcalpha"),
    mul_rev2("reverse_subtract", "src_alpha", "one_minus_src_alpha", "zero", "one");

    private final String blendFunc,
            srcColorFactor,
            dstColorFactor,
            srcAlphaFactor,
            dstAlphaFactor;
    private final boolean separateBlend;

    Blend(boolean separateBlend, String blendFunc, String srcColor, String dstColor, String srcAlpha, String dstAlpha) {
        this.separateBlend = separateBlend;
        this.blendFunc = blendFunc;
        this.srcColorFactor = srcColor;
        this.dstColorFactor = dstColor;
        this.srcAlphaFactor = srcAlpha;
        this.dstAlphaFactor = dstAlpha;
    }

    Blend(String blendFunc, String src, String dst) {
        this(false, blendFunc, src, dst, src, dst);
    }

    Blend(String blendFunc, String srcColor, String dstColor, String srcAlpha, String dstAlpha) {
        this(true, blendFunc, srcColor, dstColor, srcAlpha, dstAlpha);
    }

    public String getBlendFunc() {
        return this.blendFunc;
    }

    public String getSrcColor() {
        return this.srcColorFactor;
    }

    public String getDstColor() {
        return this.dstColorFactor;
    }

    public String getSrcAlpha() {
        return this.srcAlphaFactor;
    }

    public String getDstAlpha() {
        return this.dstAlphaFactor;
    }

    public boolean isSeparateBlend() {
        return this.separateBlend;
    }

    public String[] getALL() {
        return new String[]{
                blendFunc,
                srcColorFactor,
                dstColorFactor,
                srcAlphaFactor,
                dstAlphaFactor
        };
    }
}
