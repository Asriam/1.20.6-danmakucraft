package com.asrian.thDanmakuCraft.init;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeModeTabInit {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, THDanmakuCraftCore.MODID);

    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> BlockInit.EXAMPLE_BLOCK_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ItemInit.EXAMPLE_ITEM.get());
                output.accept(BlockInit.EXAMPLE_BLOCK_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(ItemInit.TESTDANMAKU.get());
            }).build());
}
