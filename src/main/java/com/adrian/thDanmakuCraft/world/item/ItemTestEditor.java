package com.adrian.thDanmakuCraft.world.item;

import com.adrian.thDanmakuCraft.client.gui.editor.LuaScriptEditorScreen;
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

    /**
     * 開啟腳本編輯器。
     *
     * <p>注意：單人遊戲時 {@code use()} 會在「客戶端執行緒」與「伺服器執行緒」各被呼叫一次，
     * 而 {@link DistExecutor#unsafeRunWhenOn} 只判斷物理端、不判斷執行緒。
     * 直接在伺服器執行緒呼叫 {@code Minecraft.setScreen} 會觸發
     * 「RenderSystem called from wrong thread」；而且 setScreen 中途拋出時畫面已經掛上、
     * 卻還沒初始化（{@code Screen.minecraft} 仍是 null），之後 narration 就會 NPE。</p>
     *
     * <p>因此這裡同時做兩件事：只在客戶端邏輯端觸發，並用
     * {@code Minecraft.execute} 把開畫面的動作排到客戶端主執行緒。</p>
     */
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance()
                    .execute(() -> Minecraft.getInstance().setScreen(new LuaScriptEditorScreen("untitled"))));
        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
