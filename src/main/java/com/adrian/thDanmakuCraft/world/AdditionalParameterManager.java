package com.adrian.thDanmakuCraft.world;

import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.danmaku.laser.THLaser;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.*;

import java.util.Map;
import java.util.UUID;

public class AdditionalParameterManager implements IDataStorage, ILuaValue{

    private final THObjectContainer container;
    private final Map<String, Parameter<?>> parameterMap;
    private final LuaValue luaValueForm;
    public AdditionalParameterManager(THObjectContainer container) {
        this.container = container;
        this.parameterMap = Maps.newHashMap();
        this.luaValueForm = this.ofLuaValue();
    }

    public void register(Type type, String key, Object value) {

        if (type == Type.THObject && value instanceof THObject object) {
            this.parameterMap.put(key, new Parameter<>(type, object.getUUID()));
        }else if (type == Type.Integer){
            this.parameterMap.put(key, new Parameter<>(type, (int) value));
        }else if (type == Type.Float){
            this.parameterMap.put(key, new Parameter<>(type, (float) value));
        }else if (type == Type.Double){
            this.parameterMap.put(key, new Parameter<>(type, (double) value));
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

    public double getDouble(String key){
        return (double) this.parameterMap.get(key).getValue();
    }

    public boolean getBoolean(String key){
        return (boolean) this.parameterMap.get(key).getValue();
    }

    public THObject getTHObject(String key){
        UUID uuid = (UUID) this.parameterMap.get(key).getValue();
        return this.container.getObjectFromUUID(uuid);
    }

    /*
    public LuaValue getTHObject(String key){
        UUID uuid = (UUID) this.parameterMap.get(key).getValue();
        return this.container.getObjectFromUUID(uuid).ofLuaValue();
    }*/

    public THBullet getTHBullet(String key){
        return (THBullet) this.getTHObject(key);
    }

    public THLaser getTHLaser(String key){
        return (THLaser) this.getTHObject(key);
    }

    public THCurvedLaser getTHCurvedLaser(String key){
        return (THCurvedLaser) this.getTHObject(key);
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        //THDanmakuCraftCore.LOGGER.info(this.parameterMap+"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + this.parameterMap.size());
        buffer.writeInt(this.parameterMap.size());
        this.parameterMap.forEach((key, parameter) -> {
            buffer.writeUtf(key);
            buffer.writeEnum(parameter.type);
            ((Type.Factory<Object>) parameter.type.factory).writeData(buffer,(Parameter<Object>) parameter);
        });
    }

    @Override
    public void readData(FriendlyByteBuf buffer) {
        //THDanmakuCraftCore.LOGGER.info(this.parameterMap.toString()+"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + this.parameterMap.size());
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            String key = buffer.readUtf();
            Type type = buffer.readEnum(Type.class);
            Parameter<Object> parameter = new Parameter<>(type, type.factory.readData(buffer));
            this.parameterMap.put(key, parameter);
        }
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
            tag.putInt("type", parameter.type.ordinal());
            ((Type.Factory<Object>) parameter.type.factory).writeNBT(tag,(Parameter<Object>) parameter);
            nbt.put(key, tag);
        });
        return nbt;
    }

    public void readFromNBT(CompoundTag nbt) {
        for(String key:nbt.getAllKeys()){
            CompoundTag tag = nbt.getCompound(key);
            Type type = Type.values()[tag.getInt("type")];
            Parameter<Object> parameter = new Parameter<>(type, type.factory.readNBT(tag));
            if (parameter.value != null){
                this.parameterMap.put(key, parameter);
            }
        }
    }

    public enum Type{
        String(new Factory<String>() {
            @Override
            public void writeData(FriendlyByteBuf buffer, Parameter<String> parameter) {
                buffer.writeUtf(parameter.value);
            }

            @Override
            public String readData(FriendlyByteBuf buffer) {
                return buffer.readUtf();
            }

            @Override
            public void writeNBT(CompoundTag tag, Parameter<String> parameter) {
                tag.putString("value", parameter.value);
            }

            @Override
            public String readNBT(CompoundTag tag) {
                return tag.getString("value");
            }

            @Override
            public LuaValue toLuaValue(Parameter<String> parameter){
                return LuaValue.valueOf(parameter.value);
            }

            @Override
            public java.lang.String fromLuaValue(LuaValue value) {
                return value.checkjstring();
            }
        }),
        Integer(new Factory<Integer>() {
            @Override
            public void writeData(FriendlyByteBuf buffer, Parameter<Integer> parameter) {
                buffer.writeInt(parameter.value);
            }

            @Override
            public Integer readData(FriendlyByteBuf buffer) {
                return buffer.readInt();
            }

            @Override
            public void writeNBT(CompoundTag tag, Parameter<Integer> parameter) {
                tag.putInt("value", parameter.value);
            }

            @Override
            public Integer readNBT(CompoundTag tag) {
                return tag.getInt("value");
            }

            @Override
            public LuaValue toLuaValue(Parameter<Integer> parameter) {
                return LuaValue.valueOf(parameter.value);
            }

            @Override
            public Integer fromLuaValue(LuaValue value) {
                return value.checkint();
            }
        }),
        Float(new Factory<Float>() {
            @Override
            public void writeData(FriendlyByteBuf buffer, Parameter<java.lang.Float> parameter) {
                buffer.writeFloat(parameter.value);
            }

            @Override
            public java.lang.Float readData(FriendlyByteBuf buffer) {
                return buffer.readFloat();
            }

            @Override
            public void writeNBT(CompoundTag tag, Parameter<java.lang.Float> parameter) {
                tag.putFloat("value", parameter.value);
            }

            @Override
            public java.lang.Float readNBT(CompoundTag tag) {
                return tag.getFloat("value");
            }

            @Override
            public LuaValue toLuaValue(Parameter<java.lang.Float> parameter) {
                return LuaValue.valueOf(parameter.value);
            }

            @Override
            public java.lang.Float fromLuaValue(LuaValue value) {
                return value.tofloat();
            }
        }),
        Double(new Factory<Double>() {
            @Override
            public void writeData(FriendlyByteBuf buffer, Parameter<java.lang.Double> parameter) {
                buffer.writeDouble(parameter.value);
            }

            @Override
            public java.lang.Double readData(FriendlyByteBuf buffer) {
                return buffer.readDouble();
            }

            @Override
            public void writeNBT(CompoundTag tag, Parameter<java.lang.Double> parameter) {
                tag.putDouble("value", parameter.value);
            }

            @Override
            public java.lang.Double readNBT(CompoundTag tag) {
                return tag.getDouble("value");
            }

            @Override
            public LuaValue toLuaValue(Parameter<java.lang.Double> parameter) {
                return LuaValue.valueOf(parameter.value);
            }

            @Override
            public java.lang.Double fromLuaValue(LuaValue value) {
                return value.checkdouble();
            }
        }),
        Boolean(new Factory<Boolean>() {
            @Override
            public void writeData(FriendlyByteBuf buffer, Parameter<java.lang.Boolean> parameter) {
                buffer.writeBoolean(parameter.value);
            }

            @Override
            public java.lang.Boolean readData(FriendlyByteBuf buffer) {
                return buffer.readBoolean();
            }

            @Override
            public void writeNBT(CompoundTag tag, Parameter<java.lang.Boolean> parameter) {
                tag.putBoolean("value", parameter.value);
            }

            @Override
            public java.lang.Boolean readNBT(CompoundTag tag) {
                return tag.getBoolean("value");
            }

            @Override
            public LuaValue toLuaValue(Parameter<java.lang.Boolean> parameter) {
                return LuaValue.valueOf(parameter.value);
            }

            @Override
            public java.lang.Boolean fromLuaValue(LuaValue value) {
                return value.checkboolean();
            }
        }),
        THObject(new Factory<UUID>() {
            @Override
            public void writeData(FriendlyByteBuf buffer, Parameter<UUID> parameter) {
                buffer.writeUUID(parameter.value);
            }

            @Override
            public UUID readData(FriendlyByteBuf buffer) {
                return buffer.readUUID();
            }

            @Override
            public void writeNBT(CompoundTag tag, Parameter<UUID> parameter) {
                tag.putUUID("value", parameter.value);
            }

            @Override
            public UUID readNBT(CompoundTag tag) {
                return tag.getUUID("value");
            }

            @Override
            public LuaValue toLuaValue(Parameter<UUID> parameter) {
                return LuaValue.valueOf(parameter.value.toString());
            }

            @Override
            public UUID fromLuaValue(LuaValue value) {
                return UUID.fromString(value.checkjstring());
            }
        });

        final Factory<?> factory;

        Type(Factory<?> factory){
            this.factory = factory;
        }

        public static interface Factory<T>{
            void writeData(FriendlyByteBuf buffer, Parameter<T> parameter);

            T readData(FriendlyByteBuf buffer);

            void writeNBT(CompoundTag tag, Parameter<T> parameter);

            T readNBT(CompoundTag tag);

            LuaValue toLuaValue(Parameter<T> parameter);

            T fromLuaValue(LuaValue value);
        }
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

    private final LibFunction register = new VarArgFunction() {
        @Override
        public LuaValue invoke(Varargs varargs) {
            Type type = Type.valueOf(varargs.arg(1).checkjstring());
            String key = varargs.arg(2).checkjstring();
            LuaValue value = varargs.arg(3);
            Object outValue = null;
            switch (type){
                case THObject -> {
                    LuaValue luaFormTHObject = value.checktable();
                    outValue = UUID.fromString(luaFormTHObject.get("uuid").checkjstring());
                }
                case String -> {
                    outValue = value.checkjstring();
                }
                case Integer -> {
                    outValue = value.checkint();
                }
                case Float -> {
                    outValue = value.tofloat();
                }
                case Double -> {
                    outValue = value.checkdouble();
                }
                case Boolean -> {
                    outValue = value.checkboolean();
                }
            }
            AdditionalParameterManager.this.parameterMap.put(key,new Parameter<>(type, outValue));
            return LuaValue.NIL;
        }
    };

    private final LibFunction setValue = new VarArgFunction() {
        @Override
        public LuaValue invoke(Varargs varargs) {
            String key = varargs.arg(1).checkjstring();
            LuaValue value = varargs.arg(2);
            Type type = AdditionalParameterManager.this.parameterMap.get(key).type;

            Object outValue = null;
            switch (type){
                case THObject -> {
                    LuaValue luaFormTHObject = value.checktable();
                    //luaFormTHObject.get("onTick");
                    outValue = UUID.fromString(luaFormTHObject.get("uuid").checkjstring());
                }
                case String -> outValue = value.checkjstring();
                case Integer -> outValue = value.checkint();
                case Float -> outValue = value.tofloat();
                case Double -> outValue = value.checkdouble();
                case Boolean -> outValue = value.checkboolean();
            }
            AdditionalParameterManager.this.parameterMap.put(key,new Parameter<>(type, outValue));
            return LuaValue.NIL;
        }
    };

    private final LibFunction getValue = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue) {
            String key = luaValue.checkjstring();
            LuaValue outValue = LuaValue.NIL;
            Parameter<?> parameter = AdditionalParameterManager.this.parameterMap.get(key);
            Type type = parameter.type;
            switch (type){
                case THObject -> outValue = AdditionalParameterManager.this.container.getObjectFromUUID((UUID)parameter.value).getLuaValue();
                case String -> outValue = LuaValue.valueOf((String) parameter.value);
                case Integer -> outValue = LuaValue.valueOf((int) parameter.value);
                case Float -> outValue = LuaValue.valueOf((float) parameter.value);
                case Double -> outValue = LuaValue.valueOf((double) parameter.value);
                case Boolean -> outValue = LuaValue.valueOf((boolean) parameter.value);
            }
            return outValue;
        }
    };

    private final LibFunction getString = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue) {
            String key = luaValue.checkjstring();
            return LuaValue.valueOf((String) AdditionalParameterManager.this.parameterMap.get(key).value);
        }
    };

    private final LibFunction getInteger = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue) {
            String key = luaValue.checkjstring();
            return LuaValue.valueOf((int) AdditionalParameterManager.this.parameterMap.get(key).value);
        }
    };

    private final LibFunction getFloat = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue) {
            String key = luaValue.checkjstring();
            return LuaValue.valueOf((float) AdditionalParameterManager.this.parameterMap.get(key).value);
        }
    };

    private final LibFunction getDouble = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue) {
            String key = luaValue.checkjstring();
            return LuaValue.valueOf((double) AdditionalParameterManager.this.parameterMap.get(key).value);
        }
    };

    private final LibFunction getBoolean = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue) {
            String key = luaValue.checkjstring();
            return LuaValue.valueOf((boolean) AdditionalParameterManager.this.parameterMap.get(key).value);
        }
    };

    private final LibFunction getTHObject = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue) {
            String key = luaValue.checkjstring();
            THObject object = AdditionalParameterManager.this.getTHObject(key);
            return object != null ? object.getLuaValue() : LuaValue.NIL;
        }
    };

    private final LibFunction getTHBullet = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue) {
            String key = luaValue.checkjstring();
            THObject object = AdditionalParameterManager.this.getTHBullet(key);
            return object != null ? object.getLuaValue() : LuaValue.NIL;
        }
    };

    private final LibFunction getTHCurvedLaser = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue) {
            String key = luaValue.checkjstring();
            THObject object = AdditionalParameterManager.this.getTHCurvedLaser(key);
            return object != null ? object.getLuaValue() : LuaValue.NIL;
        }
    };

    @Override
    public LuaValue ofLuaValue() {
        LuaValue library = LuaValue.tableOf();
        library.set("register",this.register);
        library.set("setValue",this.setValue);
        library.set("getValue",this.getValue);
        library.set("getString",this.getString);
        library.set("getInteger",this.getInteger);
        library.set("getFloat",this.getFloat);
        library.set("getDouble",this.getDouble);
        library.set("getBoolean",this.getBoolean);
        library.set("getTHObject",this.getTHObject);
        library.set("getTHBullet",this.getTHBullet);
        library.set("getTHCurvedLaser",this.getTHCurvedLaser);
        return library;
    }

    @Override
    public LuaValue getLuaValue() {
        return this.luaValueForm;
    }
}
