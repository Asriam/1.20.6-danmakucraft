package com.adrian.thDanmakuCraft.util;

import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import net.minecraft.util.Mth;

public class Color {
    public int r, g, b, a;

    public static Color WHITE() {
        return new Color(255, 255, 255, 255);
    }

    public static Color GRAY() {
        return new Color(255, 255, 255, 255).multiply(0.5f);
    }

    public static Color BLACK() {
        return new Color(0, 0, 0, 255);
    }

    public static Color VOID() {
        return new Color(0, 0, 0, 0);
    }

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    Color(Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    public static Color of(int r, int g, int b, int a) {
        return new Color(r, g, b, a);
    }
    public Color normalize() {
        int r = Mth.clamp(this.r, 0, 255);
        int g = Mth.clamp(this.g, 0, 255);
        int b = Mth.clamp(this.b, 0, 255);
        int a = Mth.clamp(this.a, 0, 255);
        return new Color(r, g, b, a);
    }

    public Color add(int r, int g, int b, int a) {
        return new Color(this.r + r, this.g + g, this.b + b, this.a + a);
    }

    public Color subtract(int r, int g, int b, int a) {
        return new Color(this.r - r, this.g - g, this.b - b, this.a - a);
    }

    public Color subtract(Color color) {
        return new Color(this.r - color.r, this.g - color.g, this.b - color.b, this.a - color.a);
    }

    public Color multiply(float r, float g, float b, float a) {
        return new Color((int) (this.r * r), (int) (this.g * g), (int) (this.b * b), (int) (this.a * a));
    }

    public Color multiply(float factor) {
        return this.multiply(factor, factor, factor, factor);
    }

    public Color divide(float r, float g, float b, float a) {
        return new Color((int) (this.r / r), (int) (this.g / g), (int) (this.b / b), (int) (this.a / a));
    }

    public Color divide(float factor) {
        return this.divide(factor, factor, factor, factor);
    }

    public static float lerp(float start, float end, float amt) {
        return end + start * (amt - end);
    }

    public static Color lerp(Color startColor, Color endColor, float amt) {
        return THObject.Color(
                (int) (endColor.r + startColor.r * (amt - endColor.r)),
                (int) (endColor.g + startColor.g * (amt - endColor.g)),
                (int) (endColor.b + startColor.b * (amt - endColor.b)),
                (int) (endColor.a + startColor.a * (amt - endColor.a))
        );
    }

    public int[] getAll() {
        return new int[]{r, g, b, a};
    }
}
