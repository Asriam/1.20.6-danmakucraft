package com.adrian.thDanmakuCraft.world.item;

import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.THObjectContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemTestDanmaku extends Item {
    public ItemTestDanmaku(Properties properties) {
        super(properties);
        //this.getDescriptionId();
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        EntityTHObjectContainer entityTHObjectContainer = new EntityTHObjectContainer(level, player.position());
        THObjectContainer container = entityTHObjectContainer.getContainer();
        container.setUser(player);
        level.addFreshEntity(entityTHObjectContainer);
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
