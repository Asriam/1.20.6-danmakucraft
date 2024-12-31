package com.adrian.thDanmakuCraft.world;

import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.checkerframework.checker.units.qual.A;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class AdditionalParameterManager implements IDataStorage{

    private final THObjectContainer container;
    private final Map<Type, Map<String, Object>> mapMap = Maps.newHashMap();
    private final Map<String, Parameter<?>> parameterMap = Maps.newHashMap();

    public AdditionalParameterManager(THObjectContainer container) {
        this.container = container;
        for(Type type : Type.values()){
            mapMap.put(type, Maps.newHashMap());
        }
    }

    public void register(Type type, String key, Object value) {
        if(type == Type.THObject && value instanceof THObject object){
            mapMap.get(Type.THObject).put(key,object.getUUID());
        }

        mapMap.get(type).put(key,value);
    }

    public void register(String type, String key, Object value) {
        this.register(Type.valueOf(type), key, value);
    }

    public Object get(Type type, String key) {
        return mapMap.get(type).get(key);
    }

    public void set(Type type, String key, Object value) {
        if(type == Type.THObject && value instanceof THObject object){
            mapMap.get(Type.THObject).put(key,object.getUUID());
        }
        mapMap.get(type).put(key,value);
    }

    public void set(String type, String key, Object value) {
        set(Type.valueOf(type), key, value);
    }

    public String getString(String key){
        return (String) this.mapMap.get(Type.String).get(key);
    }

    public int getInteger(String key){
        return (int) this.mapMap.get(Type.Integer).get(key);
    }

    public float getFloat(String key){
        return (float) this.mapMap.get(Type.Float).get(key);
    }

    public boolean getBoolean(String key){
        return (boolean) this.mapMap.get(Type.Boolean).get(key);
    }

    public THObject getTHObject(String key){
        UUID uuid = (UUID) this.mapMap.get(Type.THObject).get(key);
        return this.container.getObjectFromUUID(uuid);
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        Map<String,Object> stringMap = mapMap.get(Type.String);
        buffer.writeInt(stringMap.size());
        stringMap.forEach((key, value) -> {
            String string = (String) value;
            buffer.writeUtf(key);
            buffer.writeUtf(string);
        });

        Map<String,Object> integerMap = mapMap.get(Type.Integer);
        buffer.writeInt(stringMap.size());
        stringMap.forEach((key, value) -> {
            int integer = (int) value;
            buffer.writeUtf(key);
            buffer.writeInt(integer);
        });
    }

    @Override
    public void readData(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        for (int i=0;i<size;i++){
            String key   = buffer.readUtf();
            String value = buffer.readUtf();
            this.mapMap.get(Type.String).put(key,value);
        }

        size = buffer.readInt();
        for (int i=0;i<size;i++){
            String key = buffer.readUtf();
            int value  = buffer.readInt();
            this.mapMap.get(Type.Integer).put(key,value);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        if(this.mapMap.isEmpty()){
            return compoundTag;
        }

        return this.toNBT(compoundTag);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.readFromNBT(compoundTag);
    }

    private CompoundTag toNBT(CompoundTag nbt){
        mapMap.forEach((type, map)-> {
            if (type == Type.String) {
                CompoundTag tag = new CompoundTag();
                map.forEach((key, value) -> {
                    if (value instanceof String string)
                        tag.putString(key, string);
                });
                nbt.put("String", tag);
            } else if (type == Type.Integer) {
                CompoundTag tag = new CompoundTag();
                map.forEach((key, value) -> {
                    if (value instanceof Integer integer)
                        tag.putInt(key, integer);
                });
                nbt.put("Integer", tag);
            } else if (type == Type.Float) {
                CompoundTag tag = new CompoundTag();
                map.forEach((key, value) -> {
                    if (value instanceof Float floatValue)
                        tag.putFloat(key, floatValue);
                });
                nbt.put("Float", tag);
            } else if (type == Type.Boolean) {
                CompoundTag tag = new CompoundTag();
                map.forEach((key, value) -> {
                    if (value instanceof Boolean booleanValue)
                        tag.putBoolean(key, booleanValue);
                });
                nbt.put("Boolean", tag);
            } else if (type == Type.THObject) {
                CompoundTag tag = new CompoundTag();
                map.forEach((key, value) -> {
                    if (value instanceof THObject thObject)
                        tag.putUUID(key, thObject.getUUID());
                });
                nbt.put("THObject", tag);
            }
        });
        return nbt;
    }

    public void readFromNBT(CompoundTag nbt) {
        CompoundTag StringTag   = nbt.getCompound("String");
        CompoundTag IntegerTag  = nbt.getCompound("Integer");
        CompoundTag FloatTag    = nbt.getCompound("Float");
        CompoundTag BooleanTag  = nbt.getCompound("Boolean");
        CompoundTag THObjectTag = nbt.getCompound("THObject");

        for(String key:StringTag.getAllKeys()){
            mapMap.get(Type.String).put(key,StringTag.getString(key));
        }

        for(String key:IntegerTag.getAllKeys()){
            mapMap.get(Type.Integer).put(key,IntegerTag.getInt(key));
        }

        for(String key:FloatTag.getAllKeys()){
            mapMap.get(Type.Float).put(key,FloatTag.getFloat(key));
        }

        for(String key:BooleanTag.getAllKeys()){
            mapMap.get(Type.Boolean).put(key,BooleanTag.getBoolean(key));
        }

        for(String key:THObjectTag.getAllKeys()){
            mapMap.get(Type.THObject).put(key,this.container.getObjectFromUUID(THObjectTag.getUUID(key)));
        }
    }

    private THObjectContainer getContainer() {
        return container;
    }

    public enum Type{
        String(),
        Integer(),
        Float(),
        //Double(),
        Boolean(),
        THObject();
    }

    public static class Parameter<T>{
        private T value;

        public Parameter(T value){
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}
