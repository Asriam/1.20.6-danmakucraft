package com.adrian.thDanmakuCraft.mixins;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * 診斷用 mixin 設定外掛：在正式環境（沒有 -Dmixin.debug.verbose）也能從日誌看出
 * mixin 到底有沒有被載入、有沒有套用到目標類別。
 *
 * <p>日誌關鍵字：{@code [thdanmakucraft] mixin}</p>
 */
public class THMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        THDanmakuCraftMod.LOGGER.info("[thdanmakucraft] mixin plugin onLoad，package = {}", mixinPackage);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        THDanmakuCraftMod.LOGGER.info("[thdanmakucraft] mixin 準備套用：{} -> {}",
                mixinClassName, targetClassName);
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // 不需要
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName,
                         IMixinInfo mixinInfo) {
        THDanmakuCraftMod.LOGGER.info("[thdanmakucraft] mixin 開始注入：{}", targetClassName);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName,
                          IMixinInfo mixinInfo) {
        THDanmakuCraftMod.LOGGER.info("[thdanmakucraft] mixin 注入完成：{}", targetClassName);
    }
}
