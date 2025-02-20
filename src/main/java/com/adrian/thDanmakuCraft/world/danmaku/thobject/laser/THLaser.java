package com.adrian.thDanmakuCraft.world.danmaku.thobject.laser;

import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.CompoundTagUtil;
import com.adrian.thDanmakuCraft.util.FriendlyByteBufUtil;
import com.adrian.thDanmakuCraft.world.danmaku.ITHObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.TaskManager;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.CollisionType;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet.THBullet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public class THLaser extends THObject {

    protected float width = 0.0f;
    protected float length = 0.0f;
    protected float lastWidth = 0.0f;
    protected float lastLength = 0.0f;
    protected float targetWidth = 0.0f;
    protected float targetLength = 0.0f;
    protected Color laserColor = THBullet.BULLET_INDEX_COLOR.COLOR_RED.getColor();
    //public Vec3 lastDirection = Vec3.ZERO;
    //public Vec3 laserDirection = Vec3.ZERO;

    public THLaser(THObjectType<THLaser> type, ITHObjectContainer container) {
        super(type, container);
    }

    public THLaser(THObjectContainer container){
        this(THObjectInit.TH_LASER.get(),container);
        this.setPosition(container.getPosition());
    }

    @Override
    public void onTick(){
        this.lastWidth = width;
        this.lastLength = length;
        super.onTick();
    }

    @Override
    public void registerTasks(){
        super.registerTasks();
        this.taskManager.registerTask("laser_width_set", new TaskManager.Task<>(120, (target, timer,lifetime) ->{
            if(target instanceof THLaser self){
                float v = (float) timer /lifetime;
                self.width = Mth.lerp(v,self.width,self.targetWidth);
            }
        }));
        this.taskManager.registerTask("laser_length_set", new TaskManager.Task<>(120, (target, timer,lifetime) ->{
            if(target instanceof THLaser self){
                float v = (float) timer /lifetime;
                self.length = Mth.lerp(v,self.length,self.targetLength);
            }
        }));
    }

    public void growWidth(float width){
        this.targetWidth = width;
        this.taskManager.startTask("laser_width_set");
    }

    public void growLength(float length){
        this.targetLength = length;
        this.taskManager.startTask("laser_length_set");
    }

    public void grow(float width, float length){
        this.growWidth(width);
        this.growLength(length);
    }

    public Vec3 getLaserCenter(){
        Vector3f rotation = this.getRotation();
        return new Vec3(0.0f,0.0f,1.0f).xRot(-rotation.x).yRot(-rotation.y).scale(length);
    }

    public Vec3 getLaserCenterForRender(float partialTick){
        Vector3f rotation = this.getOffsetRotation(partialTick);
        float length = Mth.lerp(partialTick,this.lastLength,this.length);
        return new Vec3(0.0f,0.0f,1.0f).xRot(-rotation.x).yRot(-rotation.y).scale(length);
    }

    @Override
    public void collisionLogic(){
        List<Entity> entitiesInBound = this.container.getEntitiesInBound();
        if (entitiesInBound.isEmpty()) {
            return;
        }

        for (Entity entity : entitiesInBound) {
            if (!this.canHitUser && entity.equals(this.getContainer().getUser())) {
                continue;
            }
            Vector3f r = this.getRotation();

            if (CollisionType.Ellipsoid(
                    this.getPosition().add(this.getLaserCenter()),
                    new Vec3(width,width,length),
                    new Vector3f(-r.x,r.y,r.z),
                    entity.getBoundingBox())) {
                this.onHit(new EntityHitResult(entity, this.getPosition()));
            }
        }
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
        buffer.writeFloat(this.targetWidth);
        buffer.writeFloat(this.targetLength);
        FriendlyByteBufUtil.writeColor(buffer,this.laserColor);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        super.decode(buffer);
        this.width = buffer.readFloat();
        this.length = buffer.readFloat();
        this.targetWidth = buffer.readFloat();
        this.targetLength = buffer.readFloat();
        this.laserColor = FriendlyByteBufUtil.readColor(buffer);
    }
    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putFloat("Width",this.width);
        tag.putFloat("Length",this.length);
        tag.putFloat("TargetWidth",this.targetWidth);
        tag.putFloat("TargetLength",this.targetLength);
        CompoundTagUtil.putColor(tag, "LaserColor", this.laserColor);
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        this.width = tag.getFloat("Width");
        this.length = tag.getFloat("Length");
        this.targetWidth = tag.getFloat("TargetWidth");
        this.targetLength = tag.getFloat("TargetLength");
        this.laserColor = CompoundTagUtil.getColor(tag, "LaserColor");
    }
}
