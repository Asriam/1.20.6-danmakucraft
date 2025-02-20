package com.adrian.thDanmakuCraft.world.danmaku.thobject.laser;

import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.CompoundTagUtil;
import com.adrian.thDanmakuCraft.util.FriendlyByteBufUtil;
import com.adrian.thDanmakuCraft.world.danmaku.ITHObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.TaskManager;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet.THBullet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public class THLaser extends THObject {

    protected float width = 0.0f;
    protected float length = 0.0f;
    protected float lastWidth = 0.0f;
    protected float lastLength = 0.0f;
    protected Color laserColor = THBullet.BULLET_INDEX_COLOR.COLOR_RED.getColor();

    public THLaser(THObjectType<THLaser> type, ITHObjectContainer container) {
        super(type, container);
    }

    public THLaser(THObjectContainer container){
        this(THObjectInit.TH_LASER.get(),container);
        this.setPosition(container.getPosition());
    }

    @Override
    public void onTick(){
        super.onTick();
        this.lastWidth = width;
        this.lastLength = length;
    }

    @Override
    public void registerTasks(){
        super.registerTasks();
        this.taskManager.registerTask("laser_width_grow", new TaskManager.Task<>(120, (self, timer) ->{

        }));
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

    public float getWidthForRender(float partialTick){
        return Mth.lerp(partialTick,lastWidth,width);
    }

    public float getLengthForRender(float partialTick){
        return Mth.lerp(partialTick,lastLength,length);
    }

    public Color getLaserColor(){
        return this.laserColor;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
        buffer.writeFloat(this.width);
        buffer.writeFloat(this.length);
        FriendlyByteBufUtil.writeColor(buffer,this.laserColor);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        super.decode(buffer);
        this.width = buffer.readFloat();
        this.length = buffer.readFloat();
        this.laserColor = FriendlyByteBufUtil.readColor(buffer);
    }
    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putFloat("Width",this.width);
        tag.putFloat("Length",this.length);
        CompoundTagUtil.putColor(tag, "LaserColor", this.laserColor);
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        this.width = tag.getFloat("Width");
        this.length = tag.getFloat("Length");
        this.laserColor = CompoundTagUtil.getColor(tag, "LaserColor");
    }
}
