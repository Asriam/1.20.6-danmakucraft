package com.adrian.thDanmakuCraft.world.entity.mob;

import com.google.common.collect.Lists;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.List;

public class THMob extends Mob {

    public final List<MobSpellCard> spellCards = Lists.newArrayList();

    protected THMob(EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
    }


    public void addSpellCard(String spellCardKey, float hitPoint){

    }

    public class MobSpellCard{
        final String spellCardKey;
        float hitPoint;

        public MobSpellCard(String spellCardKey, float hitPoint) {
            this.spellCardKey = spellCardKey;
            this.hitPoint = hitPoint;
        }
    }
}
