package com.adrian.thDanmakuCraft.world.danmaku.thobject.laser;

import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.danmaku.ITHObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class THLaser extends THObject {

    private float width;
    private float length;

    public THLaser(THObjectType<THLaser> type, ITHObjectContainer container) {
        super(type, container);
    }

    public THLaser(THObjectContainer container){
        this(THObjectInit.TH_LASER.get(),container);
    }

    @Override
    public void onTick(){
        super.onTick();
    }

    @Override
    public void collisionLogic(){

    }

    public void setWidth(float width){
        this.width = width;
    }

    public void setLength(float length){
        this.length = length;
    }

    public float getWidth(){
        return this.width;
    }

    public float getLength(){
        return this.length;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.width);
        buffer.writeFloat(this.length);
        super.encode(buffer);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        this.width = buffer.readFloat();
        this.length = buffer.readFloat();
        super.decode(buffer);
    }
    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putFloat("Width",this.width);
        tag.putFloat("Length",this.length);
        //return tag;
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        this.width = tag.getFloat("Width");
        this.length = tag.getFloat("Length");
    }
}
