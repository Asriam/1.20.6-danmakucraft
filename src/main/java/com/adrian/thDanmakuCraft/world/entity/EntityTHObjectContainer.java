package com.adrian.thDanmakuCraft.world.entity;

import com.adrian.thDanmakuCraft.api.script.IScriptTHObjectContainerAPI;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.init.EntityInit;
import com.adrian.thDanmakuCraft.script.IScript;
import com.adrian.thDanmakuCraft.script.js.JSManager;
import com.adrian.thDanmakuCraft.script.ScriptManager;
import com.adrian.thDanmakuCraft.util.MultiMap;
import com.adrian.thDanmakuCraft.world.entity.danmaku.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class EntityTHObjectContainer extends Entity implements IEntityAdditionalSpawnData, IScript, IScriptTHObjectContainerAPI {

    private @Nullable Entity user,    target;
    private @Nullable UUID   userUUID,targetUUID;
    private int maxObjectAmount = 2000;
    protected final JSManager scriptManager;
    protected final THObjectManager objectManager;
    protected int timer = 0;
    public AABB aabb = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    public AABB bound = new AABB(-60.0D,-60.0D,-60.0D,60.0D,60.0D,60.0D);
    public boolean positionBinding = false;
    public boolean autoRemove = true;
    public int autoRemoveLife = 60;
    public final THTask task = new THTask();
    public final RandomSource random = RandomSource.create();
    private List<Entity> entitiesInBound = new ArrayList<>();

    public EntityTHObjectContainer(EntityType<? extends EntityTHObjectContainer> type, Level level) {
        super(type, level);
        this.objectManager = new THObjectManager(this);
        this.scriptManager = new JSManager();
        this.noCulling = true;
    }

    public EntityTHObjectContainer(@Nullable LivingEntity user, Level level, Vec3 pos) {
        this(EntityInit.ENTITY_THDANMAKU_CONTAINER.get(), level);
        this.user = user;
        this.setPos(pos);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {

    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
    }

    public int getMaxObjectAmount() {
        return maxObjectAmount;
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

    public void loadUserAndTarget(){
        if(this.user == null && this.userUUID != null) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            this.user = serverLevel.getEntity(this.userUUID);
        }

        if(this.target == null && this.targetUUID != null) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            this.target = serverLevel.getEntity(this.targetUUID);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.task.tick();
        if(this.positionBinding && this.user != null){
            this.setPos(this.user.position());
        }
        this.setBound(this.position(),this.bound);
        this.loadUserAndTarget();

        if(this.objectManager.isEmpty() && false) {
            for (int j = 0; j< THBullet.BULLET_STYLE.class.getEnumConstants().length; j++) {
                for (int i = 0; i < 16; i++) {
                    THObject a = (THObject) new THBullet(this,THBullet.BULLET_STYLE.getStyleByIndex(j),THBullet.BULLET_COLOR.getColorByIndex(i + 1))
                    .initPosition(this.position().add(i, 0.0d, j*2))
                    .shoot(
                            0.0f,
                            Vec3.ZERO
                    );
                    a.setLifetime(3600);
                    a.colli = true;
                    a.blend = THRenderType.BLEND.NONE;
                }
            }
        }

        if(/*(this.timer+2)%1==0 &&*/ true) {
            Vec3 pos = this.position();
            Vec3 rotation = Vec3.directionFromRotation(0.0f,0.0f);
            Vec2 rotate = new Vec2(Mth.DEG_TO_RAD*((float) Math.pow(this.timer*0.1f,2)+360.0f/5),-Mth.DEG_TO_RAD*((float) Math.pow(this.timer*0.08f,2)+360.0f/5));

            Vec3 angle = rotation.xRot(Mth.DEG_TO_RAD*90.0f).normalize().xRot(rotate.x).yRot(rotate.y);
            THBullet.BULLET_STYLE style = THBullet.BULLET_STYLE.ball_mid;
            THObject danmaku = (THObject) new THBullet(this,style, THBullet.BULLET_COLOR.COLOR_PURPLE).initPosition(pos).shoot(
                    0.2f,
                    angle
            );

            danmaku.setAcceleration(0.02f,angle);
            danmaku.setLifetime(120);

            //danmaku.getScriptManager().enableScript();

            int way = 8;
            for(int i=1;i<=2;i++){
                Vec3 angle2 = rotation.xRot(Mth.DEG_TO_RAD*90.0f-Mth.DEG_TO_RAD*60.0f*i).yRot(Mth.DEG_TO_RAD*(180.0f/way)*i);
                for(int j=0;j<way;j++) {
                    Vec3 angle3 = angle2.yRot(-Mth.DEG_TO_RAD * (360.0f/way)*j).normalize().xRot(rotate.x).yRot(rotate.y);
                    THObject danmaku2 = (THObject) new THBullet(this,style,
                            THBullet.BULLET_COLOR.COLOR_PURPLE).initPosition(pos).shoot(
                            0.2f,
                            angle3
                    );
                    danmaku2.setAcceleration(0.02f, angle3);
                    danmaku2.setLifetime(120);
                    //danmaku2.setBlend(THRenderType.BLEND.NONE);
                }
            }

            Vec3 angle3 = rotation.xRot(Mth.DEG_TO_RAD*90.0f- Mth.DEG_TO_RAD * 180.0f).normalize().xRot(rotate.x).yRot(rotate.y);
            THBullet danmaku3 = (THBullet) new THBullet(this,style,
                    THBullet.BULLET_COLOR.COLOR_PURPLE).initPosition(pos).shoot(
                    0.2f,
                    angle3
            );
            danmaku3.setAcceleration(0.02f, angle3);
            danmaku3.setLifetime(120);
        }

        this.entitiesInBound = this.level().getEntities(this,this.getAabb()).stream().filter((entity -> !(entity instanceof EntityTHObjectContainer))).toList();
        this.updateObjects();
        this.timer++;


        if(this.autoRemove){
            if(this.objectManager.isEmpty() && --this.autoRemoveLife < 0){
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    /*
    @OnlyIn(Dist.CLIENT)
    public void layerObjects(double camX, double camY, double camZ){
        this.objectManager.sortTHObjects((o1, o2) -> {
            if (o1 == null || o2 == null){
                return 0;
            }
            Vec3 pos1 = o1.getPosition();
            Vec3 pos2 = o2.getPosition();
            double d1x = pos1.x - camX;
            double d1y = pos1.y - camY;
            double d1z = pos1.z - camZ;
            double dist1Square = (d1x * d1x + d1y * d1y + d1z * d1z);
            double d2x = pos2.x - camX;
            double d2y = pos2.y - camY;
            double d2z = pos2.z - camZ;
            double dist2Square = (d2x * d2x + d2y * d2y + d2z * d2z);
            if (dist1Square < dist2Square){
                return 1;
            }else {
                return -1;
            }
        });
    }*/



    public void updateObjects(){
        if(this.objectManager.isEmpty()){
            return;
        }

        List<THObject> removeList = Lists.newArrayList();
        for (THObject object: objectManager.getTHObjects()){
            if (object != null && !object.removeFlag){
                object.onTick();
            }else {
                removeList.add(object);
            }
        }

        for(THObject object:removeList){
            object.onRemove();
            this.objectManager.removeTHObject(object);
        }
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

    public void setUser(@Nullable Entity entity){
        this.user = entity;
        this.userUUID = entity != null ? entity.getUUID() : null;
    }

    @Nullable
    public Entity getUser(){
        return this.user;
    }

    public void setTarget(@Nullable Entity target) {
        this.target = target;
        this.userUUID = target != null ? target.getUUID() : null;
    }

    @Nullable
    public Entity getTarget() {
        return target;
    }

    public List<Entity> getEntitiesInBound(){
        return this.entitiesInBound;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.user != null ? this.user.getId() : 0);
        buffer.writeVarInt(this.target != null ? this.target.getId() : 0);
        buffer.writeInt(this.maxObjectAmount);
        buffer.writeInt(this.timer);
        buffer.writeBoolean(this.positionBinding);
        this.objectManager.writeData(buffer);
        this.scriptManager.writeData(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        Entity user = this.level().getEntity(additionalData.readVarInt());
        this.setUser(user);
        Entity target = this.level().getEntity(additionalData.readVarInt());
        this.setTarget(target);
        this.maxObjectAmount = additionalData.readInt();
        this.timer = additionalData.readInt();
        this.positionBinding = additionalData.readBoolean();
        this.objectManager.readData(additionalData);
        this.scriptManager.readData(additionalData);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        compoundTag.putInt("Timer",this.timer);
        compoundTag.putInt("MaxObjectAmount",this.maxObjectAmount);
        compoundTag.putBoolean("PositionBinding",this.positionBinding);
        compoundTag.put("object_storage", this.objectManager.save());
        compoundTag.put("script",this.scriptManager.save(new CompoundTag()));
        String user = this.user != null ? this.user.getUUID().toString() : "";
        String target = this.target != null ? this.target.getUUID().toString() : "";
        compoundTag.putString("UserUUID",user);
        compoundTag.putString("TargetUUID",target);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.timer = compoundTag.getInt("Timer");
        this.maxObjectAmount = compoundTag.getInt("MaxObjectAmount");
        this.positionBinding = compoundTag.getBoolean("PositionBinding");
        this.objectManager.load(compoundTag.getCompound("object_storage"));
        this.scriptManager.load(compoundTag.getCompound("script"));
        String userUUID = compoundTag.getString("UserUUID");
        String targetUUID = compoundTag.getString("TargetUUID");
        this.userUUID = !userUUID.equals("") ? UUID.fromString(userUUID) : null;
        this.targetUUID = !targetUUID.equals("") ? UUID.fromString(targetUUID) : null;
    }

    @Override
    public ScriptManager getScriptManager() {
        return this.scriptManager;
    }

    public static class THObjectManager{

        private final MultiMap<THObject> storage;
        private final EntityTHObjectContainer container;

        public THObjectManager(EntityTHObjectContainer container) {
            this.container = container;
            this.storage = new MultiMap<>(THObject.class);
        }

        public void clearStorage(){
            this.storage.clear();
        }

        public void addTHObject(THObject object){
            if(this.storage.size() >= this.container.maxObjectAmount){
                //THDanmakuCraftCore.LOGGER.warn("{}'s object pool is full! {} (Max is {})",this.container,this.storage.size(),this.container.getMaxObjectAmount());
                return;
            }
            this.storage.add(object);
        }

        public void addTHObjects(List<THObject> objects){
            this.storage.addAll(objects);
        }

        public void removeTHObject(THObject object){
            this.storage.remove(object);
        }

        public void sortTHObjects(Comparator<THObject> comparator){
            this.storage.sort(comparator);
        }

        public void removeTHObject(int index){
            this.storage.remove(this.getTHObject(index));
        }

        public THObject getTHObject(int index){
            return this.getTHObjects().get(index);
        }

        public List<THObject> getTHObjects(){
            return this.storage.getAllInstances();
        }

        public List<THObject> getTHObjectsForRender(){
            List<THObject> list = new ArrayList<>();
            list.addAll(this.getTHObjects());
            return list;
        }

        public void recreate(List<THObject> objects){
            this.clearStorage();
            this.addTHObjects(objects);
        }

        public boolean contains(THObject object){
            return this.storage.contains(object);
        }

        public boolean isEmpty(){
            return this.storage.isEmpty();
        }

        public CompoundTag save(){
            return THObjectListToTag(this.getTHObjects());
        }

        public void load(CompoundTag tag){
            this.recreate(TagToTHObjectList(tag,this.container));
        }

        public void writeData(FriendlyByteBuf buffer){
            List<THObject> objects = this.getTHObjects();
            buffer.writeInt(objects.size());
            for(THObject object:this.getTHObjects()){
                //buffer.writeRegistryId(THDanmakuCraftRegistries.THOBJECT_TYPE,object.getType());
                buffer.writeResourceLocation(object.getType().getKey());
                object.writeData(buffer);
            }
        }

        public void readData(FriendlyByteBuf buffer){
            int listSize = buffer.readInt();
            List<THObject> objects = Lists.newArrayList();
            for(int i=0;i<listSize;i++){
                //THObject object = buffer.readRegistryIdSafe(THObjectType.class).create(this.container);
                THObject object = THObjectType.getValue(buffer.readResourceLocation()).create(this.container);
                assert object != null;
                object.readData(buffer);
                objects.add(object);
            }
            this.recreate(objects);
        }

        public static CompoundTag THObjectListToTag(List<THObject> objects){
            CompoundTag tag = new CompoundTag();
            int index = 0;
            for (THObject object:objects) {
                if (object.shouldSave) {
                    CompoundTag tag2 = new CompoundTag();
                    tag2.putString("type", object.getType().getKey().toString());
                    tag.put("object_" + index, object.save(tag2));
                    index++;
                }
            }
            return tag;
        }

        public static List<THObject> TagToTHObjectList(CompoundTag tag, EntityTHObjectContainer container){
            int list_size = tag.getAllKeys().size();
            List<THObject> objectList = Lists.newArrayList();
            for (int i=0;i<list_size;i++){
                CompoundTag objectTag = tag.getCompound("object_"+i);
                ResourceLocation object_type = new ResourceLocation(objectTag.getString("type"));
                THObjectType<? extends THObject> type = THObjectType.getValue(object_type);
                if(type != null){
                    THObject object = type.create(container);
                    object.load(objectTag);
                    objectList.add(object);
                }
            }
            return objectList;
        }
    }
}
