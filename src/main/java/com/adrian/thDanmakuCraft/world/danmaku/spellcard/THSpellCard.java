package com.adrian.thDanmakuCraft.world.danmaku.spellcard;

import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class THSpellCard extends THObjectContainer {
    protected String spellCardName;

    public THSpellCard(Entity hostEntity) {
        super(hostEntity);
    }

    public String getSpellCardName() {
        return Component.translatable(spellCardName).getString();
    }

    public void setSpellCardName(String spellCardName) {
        this.spellCardName = spellCardName;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
        buffer.writeUtf(spellCardName);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        super.decode(buffer);
        this.spellCardName = buffer.readUtf();
    }

    @Override
    public void save(CompoundTag tag){
        super.save(tag);
        tag.putString("SpellCardName", spellCardName);
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        this.spellCardName = tag.getString("SpellCardName");
    }
}
