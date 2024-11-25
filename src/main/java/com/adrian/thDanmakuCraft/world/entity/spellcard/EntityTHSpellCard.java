package com.adrian.thDanmakuCraft.world.entity.spellcard;

import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class EntityTHSpellCard extends EntityTHObjectContainer {

    @Nullable
    public String cardName;

    public EntityTHSpellCard(EntityType<EntityTHSpellCard> type, Level level) {
        super(type, level);
    }

    public boolean isNonCard(){
        return this.cardName == null || this.cardName.equals("");
    }
}
