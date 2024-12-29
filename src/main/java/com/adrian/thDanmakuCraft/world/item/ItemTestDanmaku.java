package com.adrian.thDanmakuCraft.world.item;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.script.js.JSLoader;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.THCurvedLaser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ItemTestDanmaku extends Item {
    public ItemTestDanmaku(Properties properties) {
        super(properties);
        this.getDescriptionId();
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        EntityTHObjectContainer entityTHObjectContainer = new EntityTHObjectContainer(level, player.position());
        THObjectContainer container = entityTHObjectContainer.getContainer();
        container.setUser(player);
        /*
        THTasker task = container.taskerManager.create();

        task.add(()->{
            THDanmakuCraftCore.LOGGER.info("fffffffffffff");
        });

        task.wait(10);

        task.add(()->{
            THDanmakuCraftCore.LOGGER.info("fffffffffffff2");
        });

        task.wait(10);

        task.add(()->{
            THDanmakuCraftCore.LOGGER.info("fffffffffffff3");
        });*/

        String script = JSLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MOD_ID,"data/js/test.js"));
        for(int i=0;i<1;i++) {
            THCurvedLaser laser = new THCurvedLaser(container, THBullet.BULLET_COLOR.COLOR_DEEP_PURPLE, 180, 0.5f).initPosition(container.position()).shoot(new Vec3(0.0f, 0.1f, 0));
            laser.setLifetime(1200);
            laser.getScriptManager().enableScript();
            laser.injectScript(script);
        }
        level.addFreshEntity(entityTHObjectContainer);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
