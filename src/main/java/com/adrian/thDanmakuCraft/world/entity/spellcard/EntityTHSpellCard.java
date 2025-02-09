package com.adrian.thDanmakuCraft.world.entity.spellcard;

import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.LuaValue;

import javax.annotation.Nullable;

public class EntityTHSpellCard extends EntityTHObjectContainer {

    private String spellCardName = "";

    public EntityTHSpellCard(EntityType<EntityTHSpellCard> type, Level level) {
        super(type, level);
    }

    public EntityTHSpellCard(@Nullable LivingEntity user, Level level, String name) {
        this(EntityInit.ENTITY_THSPELLCARD.get(), level);
        this.getContainer().setUser(user);
        this.spellCardName = name;
    }

    public boolean isNonCard(){
        return this.spellCardName == null || this.spellCardName.isEmpty();
    }

    public void setSpellCardName(String name){
        this.spellCardName = name;
    }

    public String getSpellCardName(){
        return this.spellCardName;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeUtf(this.spellCardName);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.spellCardName = additionalData.readUtf();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putString("SpellCardName", this.spellCardName);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.spellCardName = compoundTag.getString("SpellCardName");
    }
}
