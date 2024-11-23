package com.asrian.thDanmakuCraft.world.entity.danmaku;

import com.asrian.thDanmakuCraft.registries.THDanmakuCraftRegistries;
import com.asrian.thDanmakuCraft.util.MultiMap;
import com.asrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.utils.Lists;

import java.util.Comparator;
import java.util.List;

public class THObjectManager{

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
        if(this.storage.size() >= this.container.getMaxObjectAmount()){
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
                tag.put("object_" + index, object.save(new CompoundTag()));
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
