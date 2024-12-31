package com.adrian.thDanmakuCraft.world.item;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.script.lua.LuaLoader;
import com.adrian.thDanmakuCraft.world.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ItemTestCurvedLaser extends Item {
    public ItemTestCurvedLaser(Properties properties) {
        super(properties);
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        EntityTHObjectContainer entityTHObjectContainer = new EntityTHObjectContainer(level, player.position());
        THObjectContainer container = entityTHObjectContainer.getContainer();
        container.setUser(player);
        String script = LuaLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"data/lua/testcontainer.lua"));
        THDanmakuCraftCore.LOGGER.info(script);
        container.getScriptManager().enableScript();
        container.injectScript(script);
        /*
        String script = LuaLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"data/lua/testobject.lua"));
        THDanmakuCraftCore.LOGGER.info(script);
        for(int i=0;i<1;i++) {
            THCurvedLaser laser = new THCurvedLaser(container, THBullet.BULLET_COLOR.COLOR_DEEP_PURPLE, 180, 0.5f).initPosition(container.position()).shoot(new Vec3(0.0f, 0.1f, 0));
            laser.setLifetime(1200);
            laser.getScriptManager().enableScript();
            laser.injectScript(script);
        }*/
        level.addFreshEntity(entityTHObjectContainer);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
