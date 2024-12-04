package com.adrian.thDanmakuCraft.world.item;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.script.js.JSLoader;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.adrian.thDanmakuCraft.world.entity.danmaku.laser.THCurvedLaser;
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
        EntityTHObjectContainer container = new EntityTHObjectContainer(player,level,player.position());


        container.task.add(()->{
            THDanmakuCraftCore.LOGGER.info("fffffffffffff");

        },1);

        String script = "";
        try {
            //script = ResourceLoader.readRescource(JSLoader.getResource(new ResourceLocation(THDanmakuCraftCore.MODID,"data/js/api/testLaserScript.js")));
            //script = JSLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MODID,"testLaserScript.js"));
            script = JSLoader.getResourceAsString(new ResourceLocation(THDanmakuCraftCore.MODID,"data/js/test.js"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //THDanmakuCraftCore.LOGGER.info(script);


        for(int i=0;i<1;i++) {
            THCurvedLaser laser = (THCurvedLaser) new THCurvedLaser(container, THBullet.BULLET_COLOR.COLOR_DEEP_PURPLE, 180, 0.5f).initPosition(container.position()).shoot(new Vec3(0.0f, 0.1f, 0));
            laser.setLifetime(1200);
            laser.getScriptManager().enableScript();
            laser.injectScript(script);
        }

        level.addFreshEntity(container);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
