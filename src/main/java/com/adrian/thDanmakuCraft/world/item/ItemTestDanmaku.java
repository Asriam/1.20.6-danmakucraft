package com.adrian.thDanmakuCraft.world.item;

import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THLaser;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
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
        EntityTHObjectContainer entityTHObjectContainer = new EntityTHSpellCard(player, level, "");
        entityTHObjectContainer.setPos(player.position());
        level.addFreshEntity(entityTHObjectContainer);

        THObjectContainer container = entityTHObjectContainer.getContainer();
        container.setUser(player);
        //container.setLuaClass("testContainer");
        //container.scriptInit();
        THLaser laser = new THLaser(container);
        laser.setWidth(1.0f);
        laser.setLength(6.0f);
        laser.spawn();
        //container.scriptEvent("onInit",container.ofLuaValue());

        //player.getCooldowns().addCooldown(this, container.getLifetime());
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
