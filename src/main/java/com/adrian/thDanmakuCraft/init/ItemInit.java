package com.adrian.thDanmakuCraft.init;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.world.item.ItemTestCurvedLaser;
import com.adrian.thDanmakuCraft.world.item.ItemTestDanmaku;
import com.adrian.thDanmakuCraft.world.item.ItemTestEditor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, THDanmakuCraftMod.MOD_ID);

    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build())));
    public static final RegistryObject<Item> TESTDANMAKU = ITEMS.register("test_danmaku", () -> new ItemTestDanmaku(new Item.Properties()));
    public static final RegistryObject<Item> TEST_CURVED_LASER_ITEM = ITEMS.register("test_curved_laser_item", () -> new ItemTestCurvedLaser(new Item.Properties()));

    public static final RegistryObject<Item> TEST_EDITOR = ITEMS.register("test_editor", () -> new ItemTestEditor(new Item.Properties()));
}
