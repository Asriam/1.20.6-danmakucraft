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
    public boolean bindingToUserPosition = false;
    public boolean autoRemove = true;
    public int autoRemoveLife = 60;
    public final THTasker.THTaskerManager taskerManager;
    public final RandomSource random = RandomSource.create();
    private List<Entity> entitiesInBound;

    public EntityTHObjectContainer(EntityType<? extends EntityTHObjectContainer> type, Level level) {
        super(type, level);
        this.objectManager = new THObjectManager(this);
        this.taskerManager = new THTasker.THTaskerManager(this);
        this.scriptManager = new JSManager();
        this.entitiesInBound = new ArrayList<>();
        this.noCulling = true;
    }

    public EntityTHObjectContainer(@Nullable LivingEntity user, Level level, Vec3 pos) {
        this(EntityInit.ENTITY_THDANMAKU_CONTAINER.get(), level);
        //this.setUser(user);
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
        this.taskerManager.resume();
        if(this.bindingToUserPosition && this.user != null){
            this.setPos(this.user.position());
        }
        this.setBound(this.position(),this.bound);
        this.loadUserAndTarget();

        if(this.objectManager.isEmpty() && true) {
            for (int j = 0; j< THBullet.BULLET_STYLE.class.getEnumConstants().length; j++) {
                for (int i = 0; i < 16; i++) {
                    THObject a = (THObject) new THBullet(this,THBullet.BULLET_STYLE.getStyleByIndex(j),THBullet.BULLET_COLOR.getColorByIndex(i + 1))
                    .initPosition(this.position().add(i, 0.0d, j*2))
                    .shoot(
                            0.0f,
                            Vec3.ZERO
                    );
                    //a.setRotationByDirectionalVector(new Vec3(0.0f,1.0f,0.0f));
                    a.setLifetime(3600);
                    a.colli = true;
                    a.blend = THRenderType.BLEND.NONE;
                }
            }
        }

        if(/*(this.timer+2)%1==0 &&*/ false) {
            Vec3 pos = this.position();
            Vec3 rotation = Vec3.directionFromRotation(0.0f,0.0f);
            Vec2 rotate = new Vec2(Mth.DEG_TO_RAD*((float) Math.pow(this.timer*0.1f,2)+360.0f/5),-Mth.DEG_TO_RAD*((float) Math.pow(this.timer*0.08f,2)+360.0f/5));

            Vec3 angle = rotation.xRot(Mth.DEG_TO_RAD*90.0f).normalize().xRot(rotate.x).yRot(rotate.y);
            THBullet.BULLET_STYLE style = THBullet.BULLET_STYLE.ball_mid;
            THObject danmaku = (THObject) new THBullet(this,style, THBullet.BULLET_COLOR.COLOR_PURPLE).initPosition(pos).shoot(
                    0.2f,
                    angle
            );

            danmaku.setAccelerationFromDirection(0.02f,angle);
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
                    danmaku2.setAccelerationFromDirection(0.02f, angle3);
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
            danmaku3.setAccelerationFromDirection(0.02f, angle3);
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
        buffer.writeVarInt(this.user   != null ? this.user.getId() : 0);
        buffer.writeVarInt(this.target != null ? this.target.getId() : 0);
        buffer.writeInt(this.maxObjectAmount);
        buffer.writeInt(this.timer);
        buffer.writeBoolean(this.bindingToUserPosition);
        this.objectManager.writeData(buffer);
        this.scriptManager.writeData(buffer);
        this.taskerManager.writeData(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        Entity user   = this.level().getEntity(additionalData.readVarInt());
        Entity target = this.level().getEntity(additionalData.readVarInt());
        this.setUser(user);
        this.setTarget(target);
        this.maxObjectAmount = additionalData.readInt();
        this.timer = additionalData.readInt();
        this.bindingToUserPosition = additionalData.readBoolean();
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
        String user = this.user != null ? this.user.getUUID().toString() : "";
        String target = this.target != null ? this.target.getUUID().toString() : "";
        compoundTag.putString("UserUUID",user);
        compoundTag.putString("TargetUUID",target);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.timer = compoundTag.getInt("Timer");
        this.maxObjectAmount = compoundTag.getInt("MaxObjectAmount");
        this.bindingToUserPosition = compoundTag.getBoolean("PositionBinding");
        this.objectManager.load(compoundTag.getCompound("object_storage"));
        this.scriptManager.load(compoundTag.getCompound("script"));
        String userUUID = compoundTag.getString("UserUUID");
        String targetUUID = compoundTag.getString("TargetUUID");
        this.userUUID = !userUUID.isEmpty() ? UUID.fromString(userUUID) : null;
        this.targetUUID = !targetUUID.isEmpty() ? UUID.fromString(targetUUID) : null;
    }

    @Override
    public ScriptManager getScriptManager() {
        return this.scriptManager;
    }

    
    /**
     * The THObjectManager class is responsible for managing the collection of THObjects associated with the EntityTHObjectContainer.
     * It provides methods to add, remove, sort, and manipulate these objects within the collection.
     */
    public static class THObjectManager{

        private final MultiMap<THObject> storage;
        private final EntityTHObjectContainer container;

        /**
         * Constructs a THObjectManager associated with a specific EntityTHObjectContainer.
         * 
         * @param container The entity container that holds the THObjects.
         */
        public THObjectManager(EntityTHObjectContainer container) {
            this.container = container;
            this.storage = new MultiMap<>(THObject.class);
        }

        /**
         * Clears all THObjects from the storage.
         */
        public void clearStorage(){
            this.storage.clear();
        }

        /**
         * Adds a single THObject to the storage.
         * If the storage exceeds its maximum capacity, the object is not added.
         *
         * @param object The THObject to be added.
         */
        public void addTHObject(THObject object){
            if(this.storage.size() >= this.container.maxObjectAmount){
                //THDanmakuCraftCore.LOGGER.warn("{}'s object pool is full! {} (Max is {})",this.container,this.storage.size(),this.container.getMaxObjectAmount());
                return;
            }
            this.storage.add(object);
        }

        /**
         * Adds a list of THObjects to the storage.
         *
         * @param objects The list of THObjects to be added.
         */
        public void addTHObjects(List<THObject> objects){
            this.storage.addAll(objects);
        }

        /**
         * Removes a specified THObject from the storage.
         *
         * @param object The THObject to be removed.
         */
        public void removeTHObject(THObject object){
            this.storage.remove(object);
        }

        /**
         * Sorts the THObjects in the storage based on the provided comparator.
         *
         * @param comparator The comparator used to define the order of the objects.
         */
        public void sortTHObjects(Comparator<THObject> comparator){
            this.storage.sort(comparator);
        }

        /**
         * Removes a THObject at a specified index from the storage.
         *
         * @param index The index of the THObject to be removed.
         */
        public void removeTHObject(int index){
            this.storage.remove(this.getTHObject(index));
        }

        /**
         * Retrieves a THObject from the storage at a specific index.
         *
         * @param index The index of the THObject to retrieve.
         * @return The THObject at the specified index.
         */
        public THObject getTHObject(int index){
            return this.getTHObjects().get(index);
        }

        /**
         * Retrieves a list of all THObjects contained in the storage.
         *
         * @return The list of THObjects.
         */
        public List<THObject> getTHObjects(){
            return this.storage.getAllInstances();
        }

        /**
         * Retrieves a list of THObjects intended for rendering.
         *
         * @return The list of THObjects for rendering.
         */
        public List<THObject> getTHObjectsForRender(){
            List<THObject> list = new ArrayList<>();
            list.addAll(this.getTHObjects());
            return list;
        }

        /**
         * Clears the current storage and replaces it with a new list of THObjects.
         *
         * @param objects The list of new THObjects to store.
         */
        public void recreate(List<THObject> objects){
            this.clearStorage();
            this.addTHObjects(objects);
        }

        /**
         * Checks if the storage contains a specific THObject.
         *
         * @param object The THObject to check for.
         * @return True if the object is present in the storage, otherwise false.
         */
        public boolean contains(THObject object){
            return this.storage.contains(object);
        }

        /**
         * Determines whether the storage is empty.
         *
         * @return True if the storage is empty, otherwise false.
         */
        public boolean isEmpty(){
            return this.storage.isEmpty();
        }

        /**
         * Saves the current list of THObjects to a CompoundTag for persistence.
         *
         * @return A CompoundTag representing the saved state of the THObjects.
         */
        public CompoundTag save(){
            return THObjectListToTag(this.getTHObjects());
        }

        /**
         * Loads a list of THObjects from a CompoundTag, replacing the current storage.
         *
         * @param tag The CompoundTag containing the THObjects to load.
         */
        public void load(CompoundTag tag){
            this.recreate(TagToTHObjectList(tag,this.container));
        }

        /**
         * Writes the details of all THObjects into a FriendlyByteBuf for network transmission.
         *
         * @param buffer The buffer where the THObject data is written.
         */
        public void writeData(FriendlyByteBuf buffer){
            List<THObject> objects = this.getTHObjects();
            buffer.writeInt(objects.size());
            for(THObject object:this.getTHObjects()){
                //buffer.writeRegistryId(THDanmakuCraftRegistries.THOBJECT_TYPE,object.getType());
                buffer.writeResourceLocation(object.getType().getKey());
                object.writeData(buffer);
            }
        }

        /**
         * Reads THObject data from a FriendlyByteBuf and reconstructs the THObjects in storage.
         *
         * @param buffer The buffer containing the serialized THObject data.
         */
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

        /**
         * Converts a list of THObjects into a CompoundTag for saving to disk.
         *
         * @param objects The list of THObjects to convert.
         * @return A CompoundTag that represents the list of THObjects.
         */
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

        /**
         * Converts a CompoundTag back into a list of THObjects.
         *
         * @param tag       The CompoundTag to convert.
         * @param container The EntityTHObjectContainer to which the THObjects belong.
         * @return A list of THObjects reconstructed from the CompoundTag.
         */
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
