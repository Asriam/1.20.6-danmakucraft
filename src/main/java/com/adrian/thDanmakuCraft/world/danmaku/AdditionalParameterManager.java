package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.world.IDataStorage;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THLaser;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.*;

import java.util.Map;
import java.util.UUID;

public class AdditionalParameterManager implements IDataStorage, ILuaValue {

    private final ITHObjectContainer container;
    private final Map<String, Parameter<?>> parameterMap;
    private final LuaValue luaValueForm;
    public AdditionalParameterManager(ITHObjectContainer container) {
        this.container = container;
        this.parameterMap = Maps.newHashMap();
        this.luaValueForm = this.ofLuaClass();
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
    public void encode(FriendlyByteBuf buffer) {
        //THDanmakuCraftCore.LOGGER.info(this.parameterMap+"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + this.parameterMap.size());
        buffer.writeInt(this.parameterMap.size());
        this.parameterMap.forEach((key, parameter) -> {
            buffer.writeUtf(key);
            buffer.writeEnum(parameter.type);
            ((Type.Factory<Object>) parameter.type.factory).writeData(buffer,(Parameter<Object>) parameter);
        });
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
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

    public static AdditionalParameterManager checkParameterManager(LuaValue luaValue) {
        if (luaValue.get("source").checkuserdata() instanceof AdditionalParameterManager parameterManager){
            return parameterManager;
        }
        throw new NullPointerException();
        //return null;
    }

    private static final LibFunction register = new VarArgFunction() {
        @Override
        public LuaValue invoke(Varargs varargs) {
            Type type = Type.valueOf(varargs.arg(2).checkjstring());
            String key = varargs.arg(3).checkjstring();
            LuaValue value = varargs.arg(4);
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
            checkParameterManager(varargs.arg(1)).parameterMap.put(key,new Parameter<>(type, outValue));
            return LuaValue.NIL;
        }
    };

    private static final LibFunction setValue = new VarArgFunction() {
        @Override
        public LuaValue invoke(Varargs varargs) {
            AdditionalParameterManager parameterManager = checkParameterManager(varargs.arg(1));
            String key = varargs.arg(2).checkjstring();
            LuaValue value = varargs.arg(3);
            Type type = parameterManager.parameterMap.get(key).type;

            Object outValue = null;
            switch (type){
                case THObject -> {
                    LuaValue luaFormTHObject = value.checktable();
                    outValue = UUID.fromString(luaFormTHObject.get("uuid").checkjstring());
                }
                case String -> outValue = value.checkjstring();
                case Integer -> outValue = value.checkint();
                case Float -> outValue = value.tofloat();
                case Double -> outValue = value.checkdouble();
                case Boolean -> outValue = value.checkboolean();
            }
            parameterManager.parameterMap.put(key,new Parameter<>(type, outValue));
            return LuaValue.NIL;
        }
    };

    private static final LibFunction getValue = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            AdditionalParameterManager parameterManager = checkParameterManager(luaValue0);
            String key = luaValue.checkjstring();
            LuaValue outValue = LuaValue.NIL;
            Parameter<?> parameter = parameterManager.parameterMap.get(key);
            Type type = parameter.type;
            switch (type){
                case THObject -> outValue = parameterManager.container.getObjectFromUUID((UUID)parameter.value).ofLuaValue();
                case String -> outValue = LuaValue.valueOf((String) parameter.value);
                case Integer -> outValue = LuaValue.valueOf((int) parameter.value);
                case Float -> outValue = LuaValue.valueOf((float) parameter.value);
                case Double -> outValue = LuaValue.valueOf((double) parameter.value);
                case Boolean -> outValue = LuaValue.valueOf((boolean) parameter.value);
            }
            return outValue;
        }
    };

    private static final LibFunction getString = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            String key = luaValue.checkjstring();
            return LuaValue.valueOf((String) checkParameterManager(luaValue0).parameterMap.get(key).value);
        }
    };

    private static final LibFunction getInteger = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            String key = luaValue.checkjstring();
            return LuaValue.valueOf((int) checkParameterManager(luaValue0).parameterMap.get(key).value);
        }
    };

    private static final LibFunction getFloat = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            String key = luaValue.checkjstring();
            return LuaValue.valueOf((float) checkParameterManager(luaValue0).parameterMap.get(key).value);
        }
    };

    private static final LibFunction getDouble = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            String key = luaValue.checkjstring();
            return LuaValue.valueOf((double) checkParameterManager(luaValue0).parameterMap.get(key).value);
        }
    };

    private static final LibFunction getBoolean = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            String key = luaValue.checkjstring();
            return LuaValue.valueOf((boolean) checkParameterManager(luaValue0).parameterMap.get(key).value);
        }
    };

    private static final LibFunction getTHObject = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            String key = luaValue.checkjstring();
            THObject object = checkParameterManager(luaValue0).getTHObject(key);
            return object != null ? object.ofLuaValue() : LuaValue.NIL;
        }
    };

    private static final LibFunction getTHBullet = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            String key = luaValue.checkjstring();
            THObject object = checkParameterManager(luaValue0).getTHBullet(key);
            return object != null ? object.ofLuaValue() : LuaValue.NIL;
        }
    };

    private static final LibFunction getTHCurvedLaser = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            String key = luaValue.checkjstring();
            THObject object = checkParameterManager(luaValue0).getTHCurvedLaser(key);
            return object != null ? object.ofLuaValue() : LuaValue.NIL;
        }
    };

    @Override
    public LuaValue ofLuaClass() {
        LuaValue library = LuaValue.tableOf();
        library.setmetatable(this.getMeta());
        //Params
        library.set("source", LuaValue.userdataOf(this));
        return library;
    }

    /*private static final LuaValue meta = LuaValue.tableOf();
    static {
        meta.set("__index", functions());
    }*/
    public static final LuaValue meta = THObjectContainer.setMeta(functions());

    @Override
    public LuaValue getMeta() {
        return meta;
    }

    public static LuaValue functions(){
        LuaValue library = LuaValue.tableOf();
        library.set("define", register);
        library.set("set", setValue);
        library.set("get", getValue);
        library.set("getString", getString);
        library.set("getInteger", getInteger);
        library.set("getFloat", getFloat);
        library.set("getDouble", getDouble);
        library.set("getBoolean", getBoolean);
        library.set("getTHObject", getTHObject);
        library.set("getTHBullet", getTHBullet);
        library.set("getTHCurvedLaser", getTHCurvedLaser);
        return library;
    }

    @Override
    public LuaValue ofLuaValue() {
        return this.luaValueForm;
    }


}
