package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.world.IDataStorage;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import org.luaj.vm2.LuaValue;

import java.lang.reflect.Field;
import java.util.List;

@Deprecated
public class SmoothSetValueManager<T extends ILuaValue> implements IDataStorage, ILuaValue {

    List<ValueSetter<T,Double>> setters = Lists.newArrayList();
    private final T target;

    /// LuaValue形式
    private final LuaValue luaValueForm;

    public SmoothSetValueManager(T target) {
        this.target = target;
        this.luaValueForm = this.ofLuaClass();
    }

    public void smoothSetValue(String valueKey, double startValue, double targetValue, int duration, int startTime){
        setters.add(new LuaValueSetter<>(valueKey,startValue,targetValue,duration,startTime));
    }

    public void tick(){
        List<ValueSetter<T,?>> removeList = Lists.newArrayList();
        for(ValueSetter<T,?> setter: setters){
            if (setter.isAlive()) {
                if(setter.startTime <= 0) {
                    setter.tick(target);
                    setter.timer++;
                }
                setter.startTime = Math.max(setter.startTime -1,0);
            }else{
                removeList.add(setter);
            }
        }

        for (var task : removeList){
            setters.remove(task);
        }
        removeList.clear();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(setters.size());
        for (var setter : setters){
            buffer.writeUtf(setter.valueKey);
            buffer.writeDouble(setter.startValue);
            buffer.writeDouble(setter.targetValue);
            buffer.writeInt(setter.duration);
            buffer.writeInt(setter.startTime);
            buffer.writeInt(setter.timer);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        for (int i=0;i<size;i++){
            String valueKey = buffer.readUtf();
            double startValue = buffer.readDouble();
            double targetValue = buffer.readDouble();
            int duration = buffer.readInt();
            int startTime = buffer.readInt();
            int timer = buffer.readInt();
            LuaValueSetter<T> setter = new LuaValueSetter<>(valueKey,startValue,targetValue,duration,startTime);
            setter.timer = timer;
            setters.add(setter);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return null;
    }

    @Override
    public void load(CompoundTag compoundTag) {

    }

    public abstract static class ValueSetter<T,V extends Number>{

        int timer = 0;
        int startTime;
        int duration;
        V startValue;
        V targetValue;
        String valueKey;

        protected ValueSetter(String valueKey, V startValue, V targetValue, int duration, int startTime) {
            this.valueKey = valueKey;
            this.startValue = startValue;
            this.targetValue = targetValue;
            this.duration = duration;
            this.startTime = startTime;
        }

        public abstract void tick(T target);

        public boolean isAlive(){
            return timer <= duration;
        }
    }

    /*@Deprecated
    public static class ObjectValueSetter<T> extends ValueSetter<T,Double>{

        protected ObjectValueSetter(String valueKey, double startValue, double targetValue, int duration, int startTime) {
            super(valueKey, startValue, targetValue, duration, startTime);
        }

        @Override
        public void tick(T target) {
            double value = Mth.lerp((double)this.timer / (double)this.duration, this.startValue, this.targetValue);
            if (getFieldValue(target, valueKey) instanceof Number number) {
                switch (number) {
                    case Double v -> setFieldValue(this, valueKey, value);
                    case Float v -> setFieldValue(this, valueKey, (float) value);
                    case Integer i -> setFieldValue(this, valueKey, (int) value);
                    default -> {}
                }
            }
        }

        private static Object getFieldValue(Object obj, String fieldName) {
            Class<?> clazz = obj.getClass();
            while (clazz != null) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true); // 允许访问私有字段
                    return field.get(obj);
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass(); // 继续查找父类
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("无法访问字段 '" + fieldName + "'", e);
                }
            }
            throw new RuntimeException("未找到字段 '" + fieldName + "' 在类及其父类中");
        }
        private static void setFieldValue(Object obj, String fieldName, Object value) {
            Class<?> clazz = obj.getClass();
            while (clazz != null) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true); // 允许访问私有字段
                    if (field.getType() == value.getClass()) {
                        field.set(obj, value);
                    }else {
                        throw new RuntimeException("字段 '" + fieldName + "' 的类型不匹配");
                    }
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass(); // 继续查找父类
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("无法访问字段 '" + fieldName + "'", e);
                }
            }
            throw new RuntimeException("未找到字段 '" + fieldName + "' 在类及其父类中");
        }
    }*/

    public static class LuaValueSetter<T extends ILuaValue> extends ValueSetter<T,Double>{

        public LuaValueSetter(String valueKey, double startValue, double targetValue, int duration, int startTime) {
            super(valueKey, startValue, targetValue, duration, startTime);
        }

        public void tick(T target){
            double value = Mth.lerp((double)this.timer / (double)this.duration, this.startValue, this.targetValue);
            target.ofLuaValue().set(valueKey, value);
        }
    }

    public static class LuaAPI{

        private static LuaValue functions() {
            LuaValue library = LuaValue.tableOf();

            return library;
        }
        public static final LuaValue meta = ILuaValue.setMeta(functions());
    }

    @Override
    public LuaValue ofLuaValue() {
        return luaValueForm;
    }

    @Override
    public LuaValue getMeta() {
        return null;
    }

    public enum SmoothType{
        LINEAR, SIN, QUADRATIC_ACCELERATE, QUADRATIC_DECELERATE,
    }
}
