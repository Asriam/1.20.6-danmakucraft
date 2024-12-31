package com.adrian.thDanmakuCraft.world;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.api.script.IScriptTHObjectContainerAPI;
import com.adrian.thDanmakuCraft.script.IScript;
import com.adrian.thDanmakuCraft.script.ScriptManager;
import com.adrian.thDanmakuCraft.script.lua.LuaManager;
import com.adrian.thDanmakuCraft.world.danmaku.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.laser.THCurvedLaser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class THObjectContainer implements IScript, IScriptTHObjectContainerAPI {

    private final Entity hostEntity;
    private int maxObjectAmount = 2000;
    protected final TargetUserManager targetUserManager;
    protected final THObjectManager objectManager;
    protected final ScriptManager scriptManager;
    protected final THTasker.THTaskerManager taskerManager;
    protected final RandomSource random = RandomSource.create();
    private int timer = 0;
    public AABB aabb = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    public AABB bound = new AABB(-60.0D,-60.0D,-60.0D,60.0D,60.0D,60.0D);
    //public boolean bindingToUserPosition = false;
    public boolean autoRemove = true;
    public int autoRemoveLife = 60;
    private List<Entity> entitiesInBound;

    public THObjectContainer(Entity hostEntity) {
        this.hostEntity = hostEntity;
        this.targetUserManager = new TargetUserManager(this);
        this.objectManager     = new THObjectManager(this);
        this.taskerManager     = new THTasker.THTaskerManager(this);
        this.scriptManager     = new LuaManager();
        this.entitiesInBound   = new ArrayList<>();
        this.setMaxObjectAmount(2000);
    }

    public int getMaxObjectAmount() {
        return this.maxObjectAmount;
    }

    public void setMaxObjectAmount(int maxObjectAmount) {
        this.maxObjectAmount = maxObjectAmount;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void task(){
        boolean flag = true;

        if(this.objectManager.isEmpty() && !flag) {
            for (int j = 0; j< THBullet.DefaultBulletStyle.class.getEnumConstants().length; j++) {
                for (int i = 0; i < 16; i++) {
                    THObject a = (THObject) new THBullet(this, THBullet.DefaultBulletStyle.getStyleByIndex(j),THBullet.BULLET_COLOR.getColorByIndex(i + 1))
                            .initPosition(this.position().add(i*2, 0.0d, j*1))
                            .shoot(
                                    0.0f,
                                    Vec3.ZERO
                            );
                    a.setLifetime(100);
                    a.setBlend(THObject.Blend.add);
                    a.setBlend(THObject.Blend.class.getEnumConstants()[(int) ((THObject.Blend.class.getEnumConstants().length)*random.nextFloat())]);
                    //a.blend = THObject.BlendMode.add;
                }
            }
        }

        if(/*(this.timer+2)%1==0 &&*/ flag) {
            Vec3 pos = this.position();
            Vec3 rotation = Vec3.directionFromRotation(0.0f,0.0f);
            Vec2 rotate = new Vec2(Mth.DEG_TO_RAD*((float) Math.pow(this.timer*0.1f,2)+360.0f/5),-Mth.DEG_TO_RAD*((float) Math.pow(this.timer*0.08f,2)+360.0f/5));

            Vec3 angle = rotation.xRot(Mth.DEG_TO_RAD*90.0f).normalize().xRot(rotate.x).yRot(rotate.y);
            THBullet.DefaultBulletStyle style = THBullet.DefaultBulletStyle.grain_a;
            THObject danmaku = new THBullet(this,style, THBullet.BULLET_COLOR.COLOR_PURPLE).initPosition(pos).shoot(
                    0.2f,
                    angle
            );

            danmaku.setAccelerationFromDirection(0.02f,angle);
            danmaku.setLifetime(120);

            //danmaku.getScriptManager().enableScript();

            int way = 8;
            for(int i=1;i<=3;i++){
                Vec3 angle2 = rotation.xRot(Mth.DEG_TO_RAD*90.0f-Mth.DEG_TO_RAD*60.0f*i).yRot(Mth.DEG_TO_RAD*(180.0f/way)*i);
                for(int j=0;j<way;j++) {
                    Vec3 angle3 = angle2.yRot(-Mth.DEG_TO_RAD * (360.0f/way)*j).normalize().xRot(rotate.x).yRot(rotate.y);
                    THObject danmaku2 = (THObject) new THBullet(this,style,
                            THBullet.BULLET_COLOR.COLOR_PURPLE).initPosition(pos).shoot(
                            0.2f,
                            angle3
                    );
                    danmaku2.setAccelerationFromDirection(0.02f, angle3);
                    danmaku2.setLifetime(120);
                    //danmaku2.setBlend(THObject.Blend.class.getEnumConstants()[(int) ((THObject.Blend.class.getEnumConstants().length)*random.nextFloat())]);
                }
            }

            Vec3 angle3 = rotation.xRot(Mth.DEG_TO_RAD*90.0f- Mth.DEG_TO_RAD * 180.0f).normalize().xRot(rotate.x).yRot(rotate.y);
            THBullet danmaku3 = (THBullet) new THBullet(this,style,
                    THBullet.BULLET_COLOR.COLOR_PURPLE).initPosition(pos).shoot(
                    0.2f,
                    angle3
            );
            danmaku3.setAccelerationFromDirection(0.02f, angle3);
            danmaku3.setLifetime(120);
        }
    }

    public void tick() {
        /*
        if(this.bindingToUserPosition && this.getUser() != null){
            this.getHostEntity().setPos(this.getUser().position());
        }*/
        this.setBound(this.position(),this.bound);
        //this.task();
        this.entitiesInBound = this.level().getEntities(this.hostEntity,this.getAabb()).stream().filter((entity -> !(entity.equals(this.hostEntity)))).toList();
        this.objectManager.THObjectsTick();
        this.timer++;

        if(this.autoRemove) {
            if (this.objectManager.isEmpty() && --this.autoRemoveLife < 0) {
                this.getHostEntity().remove(Entity.RemovalReason.DISCARDED);
            }
        }
        this.scriptManager.invokeScript("onTick", (exception) -> {
            THDanmakuCraftCore.LOGGER.error("Failed invoke script!", exception);
            this.getHostEntity().remove(Entity.RemovalReason.DISCARDED);
        }, this);
    }

    public void clearObjects(){
        this.getObjectManager().clearStorage();
    }

    public Vec3 position() {
        return this.hostEntity.position();
    }

    public Level level(){
        return this.hostEntity.level();
    }

    public final void setBound(AABB boundingBox) {
        this.aabb = boundingBox;
    }

    public final void setBound(Vec3 pos, Vec3 size) {
        setBound(new AABB(
                pos.x - size.x / 2, pos.y - size.y / 2, pos.z - size.z / 2,
                pos.x + size.x / 2, pos.y + size.y / 2, pos.z + size.z / 2
        ));
    }

    public final void setBound(Vec3 pos, AABB aabb) {
        setBound(new AABB(
                pos.x + aabb.minX, pos.y + aabb.minY, pos.z + aabb.minZ,
                pos.x + aabb.maxX, pos.y + aabb.maxY, pos.z + aabb.maxZ
        ));
    }

    public final AABB getAabb(){
        return this.aabb;
    }

    public THObjectManager getObjectManager(){
        return this.objectManager;
    }

    public void setUser(Entity entity){
        this.targetUserManager.setUser(entity);
    }

    @Nullable
    public Entity getUser(){
        //THDanmakuCraftCore.LOGGER.info(""+this.targetUserManager.safeGetUser());
        return this.targetUserManager.safeGetUser();
    }

    public void setTarget(Entity target) {
        this.targetUserManager.setTarget(target);
    }

    @Nullable
    public Entity getTarget() {
        return this.targetUserManager.safeGetTarget();
    }

    public List<Entity> getEntitiesInBound(){
        return this.entitiesInBound;//this.level().getEntities(this,this.getAabb()).stream().filter((entity -> !(entity instanceof EntityTHObjectContainer))).toList();
    }

    public Entity getHostEntity(){
        return this.hostEntity;
    }

    public THObject createTHObject(Vec3 pos) {
        return new THObject(this,pos).spawn();
    }

    public THBullet createTHBullet(Vec3 pos, String style, int color) {
        return new THBullet(this, THBullet.DefaultBulletStyle.valueOf(style),THBullet.BULLET_COLOR.getColorByIndex(color)).spawn();
    }

    public THCurvedLaser createTHCurvedLaser(Vec3 pos, int color, int length, float width) {
        return new THCurvedLaser(this,THBullet.BULLET_COLOR.getColorByIndex(color),length,width).spawn();
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.maxObjectAmount);
        buffer.writeInt(this.timer);
        //buffer.writeBoolean(this.bindingToUserPosition);
        this.targetUserManager.writeData(buffer);
        this.objectManager.writeData(buffer);
        this.scriptManager.writeData(buffer);
        //this.taskerManager.writeData(buffer);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.maxObjectAmount = additionalData.readInt();
        this.timer = additionalData.readInt();
        //this.bindingToUserPosition = additionalData.readBoolean();
        this.targetUserManager.readData(additionalData);
        this.objectManager.readData(additionalData);
        this.scriptManager.readData(additionalData);
        //this.taskerManager.readData(additionalData);
        this.setBound(this.position(),this.bound);
    }

    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("Timer",this.timer);
        compoundTag.putInt("MaxObjectAmount",this.maxObjectAmount);
        //compoundTag.putBoolean("PositionBinding",this.bindingToUserPosition);
        compoundTag.put("object_storage", this.objectManager.save(new CompoundTag()));
        compoundTag.put("script",this.scriptManager.save(new CompoundTag()));
        compoundTag.put("user_target", this.targetUserManager.save(new CompoundTag()));
    }

    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.timer = compoundTag.getInt("Timer");
        this.maxObjectAmount = compoundTag.getInt("MaxObjectAmount");
        //this.bindingToUserPosition = compoundTag.getBoolean("PositionBinding");
        this.objectManager.load(compoundTag.getCompound("object_storage"));
        this.scriptManager.load(compoundTag.getCompound("script"));
        this.targetUserManager.load(compoundTag.getCompound("user_target"));
    }

    public void injectScript(String script) {
        this.scriptManager.setScript(script);
    }

    @Override
    public ScriptManager getScriptManager() {
        return this.scriptManager;
    }

    public RandomSource getRandomSource() {
        return this.random;
    }

    public Vec3 getPosition(){
        return this.position();
    }

}
