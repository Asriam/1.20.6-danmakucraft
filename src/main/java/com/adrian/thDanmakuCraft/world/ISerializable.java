package com.adrian.thDanmakuCraft.world;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.compress.utils.Lists;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public interface ISerializable{

    public void encode(FriendlyByteBuf buffer);
    public void decode(FriendlyByteBuf buffer);

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Serializable {
        @Nullable Class<? extends Codec<?>> codec();
    }

    private static List<Field> getAnnotatedFieldNames(Class<? super ISerializable> clazz) {
        List<Field> fields = Lists.newArrayList();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Serializable.class)) {
                fields.add(field);
            }
        }
        return fields;
    }

    static void encodeAnnotatedField(Object object, ByteBuf buffer, Class<? super ISerializable> clazz) throws IllegalAccessException {
        for (Field field : getAnnotatedFieldNames(clazz)){
            if(field.getGenericType() instanceof ISerializable){
                ((ISerializable) (field.get(object))).encode(new FriendlyByteBuf(buffer));
            }else {

            }
        }
    }

    static void decodeAnnotatedField( Object object, ByteBuf buffer, Class<? super ISerializable> clazz){
        for (Field field : getAnnotatedFieldNames(clazz)){
        }
    }

    public interface Codec<T>{
        Encoder<?> getEncoder();
        Decoder<?> getDecoder();
    }

    public static enum DefaultCodec implements Codec{
        Byte((Encoder<Byte>) ByteBuf::writeByte, (Decoder<Byte>) ByteBuf::readByte),
        Short((Encoder<Short>) ByteBuf::writeShort, (Decoder<Short>) ByteBuf::readShort),
        Integer((Encoder<Integer>) ByteBuf::writeInt, (Decoder<Integer>) ByteBuf::readInt),
        Long((Encoder<Long>) ByteBuf::writeLong, (Decoder<Long>) ByteBuf::readLong),
        Float((Encoder<Float>) ByteBuf::writeFloat, (Decoder<Float>) ByteBuf::readFloat),
        Double((Encoder<Double>) ByteBuf::writeDouble, (Decoder<Double>) ByteBuf::readDouble),
        Boolean((Encoder<Boolean>) ByteBuf::writeBoolean, (Decoder<Boolean>) ByteBuf::readBoolean),
        Vec2((Encoder<Vec2>) (buffer, value)->{
            buffer.writeFloat(value.x);
            buffer.writeFloat(value.y);
        }, (Decoder<Vec2>) buffer-> new Vec2(buffer.readFloat(), buffer.readFloat())),
        Vec3((Encoder<Vec3>) (buffer, value)->{
            buffer.writeDouble(value.x);
            buffer.writeDouble(value.y);
            buffer.writeDouble(value.z);
        }, (Decoder<Vec3>) buffer-> new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())),
        Vector3f((Encoder<Vector3f>) (buffer, value)->{
            buffer.writeFloat(value.x);
            buffer.writeFloat(value.y);
            buffer.writeFloat(value.z);
        }, (Decoder<Vector3f>) buffer-> new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()));

        public final Encoder<?> encoder;
        public final Decoder<?> decoder;

        DefaultCodec(Encoder<?> encoder, Decoder<?> decoder){
            this.encoder = encoder;
            this.decoder = decoder;
        }

        @Override
        public Encoder<?> getEncoder() {
            return encoder;
        }

        @Override
        public Decoder<?> getDecoder() {
            return decoder;
        }
    }

    public static Map<Class<?>,Codec<?>> codecs = Map.of(
            Byte.class, DefaultCodec.Byte,
            Short.class, DefaultCodec.Short,
            Integer.class, DefaultCodec.Integer,
            Long.class, DefaultCodec.Long,
            Float.class, DefaultCodec.Float,
            Double.class, DefaultCodec.Double,
            Boolean.class, DefaultCodec.Boolean,
            Vec2.class, DefaultCodec.Vec2,
            Vec3.class, DefaultCodec.Vec3
    );

    public interface Encoder<T>{
        public void encode(ByteBuf buffer, T value);
    }

    public interface Decoder<T>{
        public T decode(ByteBuf buffer);
    }
}
