package com.adrian.thDanmakuCraft.world.entity;

import com.adrian.thDanmakuCraft.api.script.IScriptTHObjectContainerAPI;
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

public class EntityTHObjectContainer extends Entity implements ITHObjectContainer , IEntityAdditionalSpawnData, IScript, IScriptTHObjectContainerAPI {

    private int maxObjectAmount = 2000;
    protected final TargetUserManager targetUserManager;
    protected final THObjectManager objectManager;
    protected final ScriptManager scriptManager;
    public final THTasker.THTaskerManager taskerManager;
    public final RandomSource random = RandomSource.create();
    private int timer = 0;
    public AABB aabb = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    public AABB bound = new AABB(-60.0D,-60.0D,-60.0D,60.0D,60.0D,60.0D);
    public boolean bindingToUserPosition = false;
    public boolean autoRemove = true;
    public int autoRemoveLife = 60;
    private List<Entity> entitiesInBound;

    public EntityTHObjectContainer(EntityType<? extends EntityTHObjectContainer> type, Level level) {
        super(type, level);
        this.targetUserManager = new TargetUserManager(this.level());
        this.objectManager = new THObjectManager(this);
        this.taskerManager = new THTasker.THTaskerManager(this);
        this.scriptManager = new JSManager();
        //this.entitiesInBound = new ArrayList<>();
        this.noCulling = true;
        this.setMaxObjectAmount(2000);
    }

    public EntityTHObjectContainer(@Nullable LivingEntity user, Level level, Vec3 pos) {
        this(EntityInit.ENTITY_THOBJECT_CONTAINER.get(), level);
        this.setUser(user);
        this.setPos(pos);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {

    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        this.taskerManager.close();
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
        if(this.objectManager.isEmpty() && true) {
            for (int j = 0; j< THBullet.BULLET_STYLE.class.getEnumConstants().length; j++) {
                for (int i = 0; i < 16; i++) {
                    THObject a = (THObject) new THBullet(this,THBullet.BULLET_STYLE.getStyleByIndex(j),THBullet.BULLET_COLOR.getColorByIndex(i + 1))
                            .initPosition(this.position().add(i*2, 0.0d, j*1))
                            .shoot(
                                    0.0f,
                                    Vec3.ZERO
                            );
                    //a.setRotationByDirectionalVector(new Vec3(0.0f,1.0f,0.0f));
                    a.setLifetime(100);
                    //a.collision = false;
                    a.setBlend(THObject.Blend.normal);
                    //a.setBlend(THObject.Blend.class.getEnumConstants()[(int) ((THObject.Blend.class.getEnumConstants().length)*random.nextFloat())]);
                    //a.blend = THObject.BlendMode.add;
                }
            }
        }

        if(/*(this.timer+2)%1==0 &&*/ false) {
            Vec3 pos = this.position();
            Vec3 rotation = Vec3.directionFromRotation(0.0f,0.0f);
            Vec2 rotate = new Vec2(Mth.DEG_TO_RAD*((float) Math.pow(this.timer*0.1f,2)+360.0f/5),-Mth.DEG_TO_RAD*((float) Math.pow(this.timer*0.08f,2)+360.0f/5));

            Vec3 angle = rotation.xRot(Mth.DEG_TO_RAD*90.0f).normalize().xRot(rotate.x).yRot(rotate.y);
            THBullet.BULLET_STYLE style = THBullet.BULLET_STYLE.grain_b;
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

    @Override
    public void tick() {
        //super.tick();
        //this.taskerManager.resume();
        //this.bindingToUserPosition = true;
        if(this.bindingToUserPosition && this.getUser() != null){
            this.setPos(this.getUser().position());
        }
        this.setBound(this.position(),this.bound);

        this.task();
        //this.loadUserAndTarget();
        this.entitiesInBound = this.level().getEntities(this,this.getAabb()).stream().filter((entity -> !(entity instanceof EntityTHObjectContainer))).toList();
        this.updateObjects();
        this.timer++;


        if(this.autoRemove){
            if(this.objectManager.isEmpty() && --this.autoRemoveLife < 0){
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

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

    public void setUser(Entity entity){
        this.targetUserManager.setUser(entity);
    }

    @Nullable
    public Entity getUser(){
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

    public Entity getThis(){
        return this;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.maxObjectAmount);
        buffer.writeInt(this.timer);
        buffer.writeBoolean(this.bindingToUserPosition);
        this.targetUserManager.writeSpawnData(buffer);
        this.objectManager.writeData(buffer);
        this.scriptManager.writeData(buffer);
        this.taskerManager.writeData(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.maxObjectAmount = additionalData.readInt();
        this.timer = additionalData.readInt();
        this.bindingToUserPosition = additionalData.readBoolean();
        this.targetUserManager.readSpawnData(additionalData);
        this.objectManager.readData(additionalData);
        this.scriptManager.readData(additionalData);
        this.taskerManager.readData(additionalData);
        this.setBound(this.position(),this.bound);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("Timer",this.timer);
        compoundTag.putInt("MaxObjectAmount",this.maxObjectAmount);
        compoundTag.putBoolean("PositionBinding",this.bindingToUserPosition);
        compoundTag.put("object_storage", this.objectManager.save());
        compoundTag.put("script",this.scriptManager.save(new CompoundTag()));
        this.targetUserManager.save(compoundTag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.timer = compoundTag.getInt("Timer");
        this.maxObjectAmount = compoundTag.getInt("MaxObjectAmount");
        this.bindingToUserPosition = compoundTag.getBoolean("PositionBinding");
        this.objectManager.load(compoundTag.getCompound("object_storage"));
        this.scriptManager.load(compoundTag.getCompound("script"));
        this.targetUserManager.load(compoundTag);
    }

    @Override
    public ScriptManager getScriptManager() {
        return this.scriptManager;
    }

    public Level getLevel(){
        return this.level();
    }

    public RandomSource getRandomSource() {
        return this.random;
    }

    public Vec3 getPosition(){
        return this.position();
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
            return new ArrayList<>(this.getTHObjects());
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

    public static class TargetUserManager{
        private @Nullable Entity user,    target;
        private @Nullable UUID   userUUID,targetUUID;
        private final Level level;

        public TargetUserManager(Level level, Entity user, Entity target){
            this.level = level;
            this.user = user;
            this.target = target;
            this.userUUID = user.getUUID();
            this.targetUUID = target.getUUID();
        }

        public TargetUserManager(Level level){
            this.level = level;
        }

        public void setUser(@Nullable Entity user){
            if(user == null){
                this.user = null;
                return;
            }
            this.user = user;
            this.userUUID = user.getUUID();
        }

        public void setTarget(@Nullable Entity target){
            if(target == null){
                this.target = null;
                return;
            }
            this.target = target;
            this.targetUUID = target.getUUID();
        }

        public Entity getEntityFromUUID(UUID uuid){
            ServerLevel serverLevel = (ServerLevel) this.level;
            return serverLevel.getEntity(uuid);
        }

        @Nullable
        public Entity safeGetUser(){
            if(this.user == null && this.userUUID != null){
                ServerLevel serverLevel = (ServerLevel) this.level;
                this.user = serverLevel.getEntity(this.userUUID);
                return this.user;
            }
            return this.user;
        }

        @Nullable
        public Entity safeGetTarget(){
            if(this.target == null && this.targetUUID != null){
                ServerLevel serverLevel = (ServerLevel) this.level;
                this.target = serverLevel.getEntity(this.targetUUID);
                return this.target;
            }
            return this.target;
        }

        public Entity setUserFromUUID(UUID uuid){
            this.user = this.getEntityFromUUID(uuid);
            return this.user;
        }

        public Entity setTargetFromUUID(UUID uuid){
            this.target = this.getEntityFromUUID(uuid);
            return this.target;
        }

        @Nullable
        public Entity unsafeGetUser(){
            return this.user;
        }

        @Nullable
        public Entity unsafeGetTarget(){
            return this.target;
        }

        public void writeSpawnData(FriendlyByteBuf buffer){
            buffer.writeVarInt(this.user   != null ? user.getId() : 0);
            buffer.writeVarInt(this.target != null ? target.getId() : 0);
        }

        public void readSpawnData(FriendlyByteBuf buffer){
            this.setUser(this.level.getEntity(buffer.readVarInt()));
            this.setTarget(this.level.getEntity(buffer.readVarInt()));
        }

        public CompoundTag save(CompoundTag compoundTag){
            String user = this.user != null ? this.user.getUUID().toString() : "";
            String target = this.target != null ? this.target.getUUID().toString() : "";
            compoundTag.putString("UserUUID",user);
            compoundTag.putString("TargetUUID",target);
            return compoundTag;
        }

        public void load(CompoundTag compoundTag){
            String userUUID = compoundTag.getString("UserUUID");
            String targetUUID = compoundTag.getString("TargetUUID");
            this.userUUID = userUUID.isEmpty() ? null : UUID.fromString(userUUID);
            this.targetUUID = targetUUID.isEmpty() ? null : UUID.fromString(targetUUID);
        }

        public void loadUserAndTarget(Level level){
            if(this.user == null && this.userUUID != null) {
                ServerLevel serverLevel = (ServerLevel) level;
                this.user = serverLevel.getEntity(this.userUUID);
            }

            if(this.target == null && this.targetUUID != null) {
                ServerLevel serverLevel = (ServerLevel) level;
                this.target = serverLevel.getEntity(this.targetUUID);
            }
        }


    }
}
