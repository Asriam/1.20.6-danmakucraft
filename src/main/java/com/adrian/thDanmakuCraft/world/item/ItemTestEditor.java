package com.adrian.thDanmakuCraft.world.item;

import com.adrian.thDanmakuCraft.client.gui.editor.EditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

public class ItemTestEditor extends Item {
    public ItemTestEditor(Properties p_41383_) {
        super(p_41383_);
    }

    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()-> ()-> {
            Minecraft.getInstance().setScreen(new EditorScreen());
        });
        return InteractionResultHolder.sidedSuccess(itemstack,level.isClientSide());
    }
}
