package com.adrian.thDanmakuCraft.world;

import com.adrian.thDanmakuCraft.util.MultiMap;
import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class THObjectManager implements IDataStorage{

    private final MultiMap<THObject> storage;
    private final THObjectContainer container;

    public THObjectManager(THObjectContainer container) {
        this.container = container;
        this.storage = new MultiMap<>(THObject.class);
    }

    public void clearStorage() {
        this.storage.clear();
    }

    public void addTHObject(THObject object) {
        this.storage.add(object);
    }

    public void addTHObjects(List<THObject> objects) {
        this.storage.addAll(objects);
    }

    public void removeTHObject(THObject object) {
        this.storage.remove(object);
    }


    public void sortTHObjects(Comparator<THObject> comparator) {
        this.storage.sort(comparator);
    }


    public void removeTHObject(int index) {
        this.storage.remove(this.getTHObject(index));
    }

    public THObject getTHObject(int index) {
        return this.getTHObjects().get(index);
    }

    public List<THObject> getTHObjects() {
        return this.storage.getAllInstances();
    }

    public List<THObject> getTHObjectsForRender() {
        return new ArrayList<>(this.getTHObjects());
    }

    public void recreate(List<THObject> objects) {
        this.clearStorage();
        this.addTHObjects(objects);
    }

    public boolean contains(THObject object) {
        return this.storage.contains(object);
    }

    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    public void tickTHObjects(){
        if(this.isEmpty()){
            return;
        }

        if(this.storage.size() > this.container.getMaxObjectAmount()){
            int mount = this.storage.size() - this.container.getMaxObjectAmount();
            for(int i=1;i<=mount;i++) {
                this.getTHObjects().get(i).remove();
            }
        }

        List<THObject> removeList = Lists.newArrayList();
        int index = 0;
        for (THObject object: this.getTHObjects()){
            if (object != null && !object.removeFlag){
                object.index = index;
                object.onTick();
            }else {
                removeList.add(object);
            }
            index++;
        }

        for(THObject object:removeList){
            object.onRemove();
            this.removeTHObject(object);
        }
    }

    public CompoundTag save(CompoundTag compoundTag) {
        return THObjectListToTag(compoundTag, this.getTHObjects());
    }

    public void load(CompoundTag tag) {
        this.recreate(TagToTHObjectList(tag, this.container));
    }


    public void writeData(FriendlyByteBuf buffer) {
        List<THObject> objects = this.getTHObjects();
        buffer.writeInt(objects.size());
        for (THObject object : this.getTHObjects()) {
            //buffer.writeRegistryId(THDanmakuCraftRegistries.THOBJECT_TYPE,object.getType());
            buffer.writeResourceLocation(object.getType().getKey());
            object.writeData(buffer);
        }
    }

    public void readData(FriendlyByteBuf buffer) {
        int listSize = buffer.readInt();
        List<THObject> objects = Lists.newArrayList();
        for (int i = 0; i < listSize; i++) {
            //THObject object = buffer.readRegistryIdSafe(THObjectType.class).create(this.container);
            THObject object = THObjectType.getValue(buffer.readResourceLocation()).create(this.container);
            if(object != null) {
                object.readData(buffer);
                objects.add(object);
            }
        }
        this.recreate(objects);
    }

    public static CompoundTag THObjectListToTag(CompoundTag tag, List<THObject> objects) {
        //CompoundTag tag = new CompoundTag();
        int index = 0;
        for (THObject object : objects) {
            if (object.shouldSave) {
                CompoundTag tag2 = new CompoundTag();
                tag2.putString("type", object.getType().getKey().toString());
                tag.put("object_" + index, object.save(tag2));
                index++;
            }
        }
        return tag;
    }

    public static List<THObject> TagToTHObjectList(CompoundTag tag, THObjectContainer container) {
        int list_size = tag.getAllKeys().size();
        List<THObject> objectList = Lists.newArrayList();
        for (int i = 0; i < list_size; i++) {
            CompoundTag objectTag = tag.getCompound("object_" + i);
            ResourceLocation object_type = new ResourceLocation(objectTag.getString("type"));
            THObjectType<? extends THObject> type = THObjectType.getValue(object_type);
            if (type != null) {
                THObject object = type.create(container);
                object.load(objectTag);
                objectList.add(object);
            }
        }
        return objectList;
    }
}
