package com.adrian.thDanmakuCraft.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.EnumMap;

public class AdditionalParameterManager implements IDataStorage{


    public void register(String key, Type type){

    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {

    }

    @Override
    public void readData(FriendlyByteBuf buffer) {

    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return null;
    }

    @Override
    public void load(CompoundTag compoundTag) {

    }


    public enum Type{
        String(String.class),
        Integer(Integer.class),
        Float(Float.class),
        Double(Double.class),
        Boolean(Boolean.class);

        Type(Class clazz){
        }
    }
}
