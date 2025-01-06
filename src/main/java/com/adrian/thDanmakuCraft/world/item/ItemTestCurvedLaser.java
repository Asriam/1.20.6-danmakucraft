package com.adrian.thDanmakuCraft.world.item;

import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemTestCurvedLaser extends Item {
    public ItemTestCurvedLaser(Properties properties) {
        super(properties);
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        List<Entity> passengers = player.getPassengers();
        EntityTHObjectContainer entityTHObjectContainer = null;
        if (!passengers.isEmpty()){
            for(Entity entity : passengers){
                if (entity instanceof EntityTHObjectContainer container){
                    entityTHObjectContainer = container;
                    break;
                }
            }
        }

        if(entityTHObjectContainer == null){
            entityTHObjectContainer = new EntityTHObjectContainer(level, player.position());
            entityTHObjectContainer.startRiding(player);
            level.addFreshEntity(entityTHObjectContainer);
        }

        THObjectContainer container = entityTHObjectContainer.getContainer();
        container.setUser(player);
        container.setLuaClassKey("testContainer");
        container.scriptEvent("onInit",container.getLuaValue());
        //player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
