package com.adrian.thDanmakuCraft.world.entity.spellcard;

import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class EntityTHSpellCard extends EntityTHObjectContainer {

    public String cardName = "";

    public EntityTHSpellCard(EntityType<EntityTHSpellCard> type, Level level) {
        super(type, level);
    }

    public EntityTHSpellCard(@Nullable LivingEntity user, Level level, Vec3 pos) {
        this(EntityInit.ENTITY_THSPELLCARD.get(), level);
    }

    public boolean isNonCard(){
        return this.cardName == null || this.cardName.isEmpty();
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeUtf(this.cardName);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.cardName = additionalData.readUtf();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putString("CardName", this.cardName);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.cardName = compoundTag.getString("CardName");
    }
}
