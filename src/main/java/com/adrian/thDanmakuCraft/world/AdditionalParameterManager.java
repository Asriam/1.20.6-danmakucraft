package com.adrian.thDanmakuCraft.world;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;
import java.util.UUID;

public class AdditionalParameterManager implements IDataStorage{

    private final THObjectContainer container;
    private final Map<String, Parameter<?>> parameterMap;

    public AdditionalParameterManager(THObjectContainer container) {
        this.container = container;
        this.parameterMap = Maps.newHashMap();
    }

    public void register(Type type, String key, Object value) {
        if (type == Type.THObject && value instanceof THObject object) {
            this.parameterMap.put(key, new Parameter<>(type, object.getUUID()));
        }else {
            this.parameterMap.put(key, new Parameter<>(type, value));
            //THDanmakuCraftCore.LOGGER.info(this.parameterMap + "zzzzzzzzzzzzzzzzzzz");
        }
    }

    public void register(String type, String key, Object value) {
        this.register(Type.valueOf(type), key, value);
    }

    public Parameter<Object> getParam(String key) {
        return (Parameter<Object>) this.parameterMap.get(key);
    }

    public void setValue(String key, Object value) {
        ((Parameter<Object>) this.parameterMap.get(key)).setValue(value);
    }

    public String getString(String key){
        return (String) this.parameterMap.get(key).getValue();
    }

    public int getInteger(String key){
        return (int) this.parameterMap.get(key).getValue();
    }

    public float getFloat(String key){
        return (float) this.parameterMap.get(key).getValue();
    }

    public boolean getBoolean(String key){
        return (boolean) this.parameterMap.get(key).getValue();
    }

    public THObject getTHObject(String key){
        UUID uuid = (UUID) this.parameterMap.get(key).getValue();
        return this.container.getObjectFromUUID(uuid);
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        //THDanmakuCraftCore.LOGGER.info(this.parameterMap+"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + this.parameterMap.size());
        buffer.writeInt(this.parameterMap.size());
        this.parameterMap.forEach((key, parameter) -> {
            buffer.writeUtf(key);
            buffer.writeEnum(parameter.type);
            switch (parameter.type) {
                case String -> buffer.writeUtf((String) parameter.value);
                case Integer -> buffer.writeInt((Integer) parameter.value);
                case Float -> buffer.writeFloat((Float) parameter.value);
                case Boolean -> buffer.writeBoolean((Boolean) parameter.value);
                case THObject -> buffer.writeUUID((UUID) parameter.value);
            }
        });
    }

    @Override
    public void readData(FriendlyByteBuf buffer) {
        //THDanmakuCraftCore.LOGGER.info(this.parameterMap.toString()+"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + this.parameterMap.size());
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            String key = buffer.readUtf();
            Type type = buffer.readEnum(Type.class);
            Parameter<?> parameter = this.readParam(buffer,type);
            this.parameterMap.put(key, parameter);
        }
    }


    public Parameter<?> readParam(FriendlyByteBuf buffer,Type type) {
        Object value = null;
        switch (type) {
            case String -> value = buffer.readUtf();
            case Integer -> value = buffer.readInt();
            case Float -> value = buffer.readFloat();
            case Boolean -> value = buffer.readBoolean();
            case THObject -> value = buffer.readUUID();
        }
        return new Parameter<>(type, value);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return this.toNBT(compoundTag);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.readFromNBT(compoundTag);
    }

    private CompoundTag toNBT(CompoundTag nbt){
        this.parameterMap.forEach((key, parameter) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", parameter.type.name());
            switch (parameter.type) {
                case String  -> tag.putString("value", (String) parameter.getValue());
                case Integer -> tag.putInt("value", (Integer) parameter.getValue());
                case Float -> tag.putFloat("value", (Float) parameter.getValue());
                case Boolean -> tag.putBoolean("value", (Boolean) parameter.getValue());
                case THObject -> tag.putUUID("value", (UUID) parameter.getValue());
            }
            nbt.put(key, tag);
        });
        return nbt;
    }

    public void readFromNBT(CompoundTag nbt) {
        for(String key:nbt.getAllKeys()){
            CompoundTag tag = nbt.getCompound(key);
            Type type = Type.valueOf(tag.getString("type"));
            Object value = null;
            switch (type) {
                case String -> value=tag.getString("value");
                case Integer -> value=tag.getInt("value");
                case Float -> value=tag.getFloat("value");
                case Boolean -> value=tag.getBoolean("value");
                case THObject -> value=tag.getUUID("value");
            }

            if (value != null){
                this.parameterMap.put(key, new Parameter<>(type, value));
            }
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
        private final Type type;
        private T value;

        public Parameter(Type type, T value){
            this.value = value;
            this.type = type;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public Type getType() {
            return type;
        }
    }
}
