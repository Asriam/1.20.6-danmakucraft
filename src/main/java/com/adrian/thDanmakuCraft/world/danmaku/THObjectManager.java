package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.util.MultiMap;
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.adrian.thDanmakuCraft.world.IDataStorage;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.utils.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class THObjectManager implements IDataStorage {

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

    public void removeTHObjects(List<THObject> objects){
        this.storage.removeAll(objects);
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
                this.getTHObjects().get(i-1).remove();
            }
        }

        List<THObject> removeList = Lists.newArrayList();
        int index = 0;
        for (THObject object: this.getTHObjects()){
            //THDanmakuCraftCore.LOGGER.warn("tickTHObject"+this.getTHObjects().size()+object.removeFlag);
            if (object != null && !object.removeFlag){
                if(object.getContainer() == null){
                    //object.container = this.container;
                    object.setContainer(this.container);
                }
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
        removeList.clear();
    }

    public CompoundTag save(CompoundTag compoundTag) {
        return THObjectListToTag(compoundTag, this.getTHObjects());
    }

    public void load(CompoundTag tag) {
        this.recreate(TagToTHObjectList(tag, this.container));
    }

    private final StreamCodec<FriendlyByteBuf, THObject> THOBJECT_CODEC = new StreamCodec<FriendlyByteBuf, THObject>() {
        @Override
        public THObject decode(FriendlyByteBuf buffer) {
            THObject object = THObjectType.getValue(buffer.readResourceLocation()).create(THObjectManager.this.container);
            object.decode(buffer);
            return object;
        }

        @Override
        public void encode(FriendlyByteBuf buffer, THObject object) {
            buffer.writeResourceLocation(object.getType().getKey());
            object.encode(buffer);
        }
    };
    public void encode(FriendlyByteBuf buffer) {
        //buffer.writeCollection(this.getTHObjects(),codec);
        ByteBufOutputStream bos = new ByteBufOutputStream(buffer);
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.buffer());
            tempBuffer.writeCollection(this.getTHObjects(), THOBJECT_CODEC);
            gzip.write(tempBuffer.array());
        } catch (IOException e) {
            throw new RuntimeException("壓縮失敗!", e);
        }
    }

    public void decode(FriendlyByteBuf buffer) {
        /*List<THObject> objects = buffer.readCollection(ArrayList::new,codec);
        this.recreate(objects);*/
        ByteBufInputStream bis = new ByteBufInputStream(buffer);
        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(gzip.readAllBytes()));
            List<THObject> objects = tempBuffer.readCollection(ArrayList::new, THOBJECT_CODEC);
            this.recreate(objects);
        } catch (IOException e) {
            throw new RuntimeException("解壓失敗!",e);
        }
    }

    public static CompoundTag THObjectListToTag(CompoundTag tag, List<THObject> objects) {
        ListTag list = new ListTag();
        for (THObject object : objects) {
            if (object.shouldSave) {
                CompoundTag tag2 = new CompoundTag();
                tag2.putString("thobject_type", object.getType().getKey().toString());
                object.save(tag2);
                list.add(tag2);
            }
        }
        tag.put("objects", list);
        return tag;
    }

    public static List<THObject> TagToTHObjectList(CompoundTag tag, THObjectContainer container) {
        List<THObject> objectList = Lists.newArrayList();
        ListTag list = tag.getList("objects", ListTag.TAG_COMPOUND);
        for (Tag _objectTag: list){
            if(_objectTag instanceof CompoundTag objectTag){
                ResourceLocation object_type = ResourceLocationUtil.of(objectTag.getString("thobject_type"));
                THObjectType<? extends THObject> type = THObjectType.getValue(object_type);
                if (type != null) {
                    THObject object = type.create(container);
                    object.load(objectTag);
                    objectList.add(object);
                }
            }
        }
        return objectList;
    }
}
