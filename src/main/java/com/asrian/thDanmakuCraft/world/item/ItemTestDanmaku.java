package com.asrian.thDanmakuCraft.world.item;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;
import com.asrian.thDanmakuCraft.util.ResourceLoader;
import com.asrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.asrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.asrian.thDanmakuCraft.world.entity.danmaku.laser.THCurvedLaser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;

public class ItemTestDanmaku extends Item {
    public ItemTestDanmaku(Properties properties) {
        super(properties);
        this.getDescriptionId();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand p_43144_) {
        ItemStack itemstack = player.getItemInHand(p_43144_);
        EntityTHObjectContainer container = new EntityTHObjectContainer(player,level,player.position());

        /*
        container.task.add(()->{
            THDanmakuCraftCore.LOGGER.info("fffffffffffff");

        },0);*/

        String script = null;
        try {
            script = ResourceLoader.readRescource(THDanmakuCraftCore.JSLOADER.getResource(new ResourceLocation(THDanmakuCraftCore.MODID,"data/js/test.js")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //THDanmakuCraftCore.LOGGER.info(script);

        for(int i=0;i<1;i++) {
            THCurvedLaser laser = (THCurvedLaser) new THCurvedLaser(container, THBullet.BULLET_COLOR.COLOR_DEEP_PURPLE, 180, 0.5f).initPosition(container.position()).shoot(new Vec3(0.0f, 0.1f, 0));
            laser.setLifetime(1200);
            laser.getScriptManager().enableScript();
            laser.injectScript(script);

            /*
            laser.injectScript(
                    "var Mth = Java.type('net.minecraft.util.Mth');" +
                    "var Vec2 = Java.type('net.minecraft.world.phys.Vec2');" +
                    "var Vec3 = Java.type('net.minecraft.world.phys.Vec3');" +
                    "var THObject = Java.type('com.asrian.thDanmakuCraft.world.entity.danmaku.THObject');" +
                    "var THBullet = Java.type('com.asrian.thDanmakuCraft.world.entity.danmaku.THBullet');" +
                    "var THCurvedLaser = Java.type('com.asrian.thDanmakuCraft.world.entity.danmaku.laser.THCurvedLaser');" +
                    "function onTick(object){" +
                    "   object.setVelocity(0.2,new Vec2(0.0," + i + "*360/16+60*Mth.cos(object.getTimer()*0.3)),true,true);" +
                    //"   new THCurvedLaser(object.getContainer(), THBullet.BULLET_COLOR.COLOR_DEEP_PURPLE, 3, 2.0).initPosition(object.getContainer().position()).shoot(new Vec3(0.0, 0, 0)).setVelocity(0.2,new Vec2(90,0.0),true,true);" +
                    "}");
            */

        }

        level.addFreshEntity(container);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
