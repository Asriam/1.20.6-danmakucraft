package com.adrian.thDanmakuCraft.world.entity.spellcard;

import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class EntityTHSpellCard extends EntityTHObjectContainer {

    @OnlyIn(Dist.CLIENT)
    public static class ParamsForRender{
        public float timer1 = 0;
        public float deathTimer = 0;
        public float lastDeathTimer = 0;
        public boolean flag1 = true;
        public Vec3 magicSquareRotation = new Vec3(0,0,0);
        public Vec3 lastMagicSquareRotation = new Vec3(0,0,0);
    }

    @OnlyIn(Dist.CLIENT)
    public ParamsForRender paramsForRender = new ParamsForRender();

    public EntityTHSpellCard(EntityType<EntityTHSpellCard> type, Level level) {
        super(type, level);
    }

    public EntityTHSpellCard(@Nullable LivingEntity user, Level level, String spellCardName) {
        this(EntityInit.ENTITY_THSPELLCARD.get(), level);
        this.getContainer().setUser(user);
        this.getContainer().setSpellCardName(spellCardName);
    }

    public boolean isNonSpellCard(){
        return this.getContainer().isNonSpellCard();
    }

    public void setSpellCardName(String name){
        this.getContainer().setSpellCardName( name);
    }

    public String getSpellCardName(){
        return this.getContainer().getSpellCardName();
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
    }
}
