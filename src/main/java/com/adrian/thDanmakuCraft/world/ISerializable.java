package com.adrian.thDanmakuCraft.world;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.compress.utils.Lists;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;

public interface ISerializable{

    public void encode(FriendlyByteBuf buffer);
    public void decode(FriendlyByteBuf buffer);

    @Retention(RetentionPolicy.RUNTIME)  // 註解在運行時保留
    @Target(ElementType.FIELD)          // 註解僅用於字段
    public static @interface Serializable {
        Type codec();
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

    static void encodeAnnotatedField(ByteBuf buffer, Class<? super ISerializable> clazz){
        List<Field> fields = getAnnotatedFieldNames(clazz);
        for (Field field : fields){
        }
    }

    public static enum Type{
        Byte((Encoder<Byte>) ByteBuf::writeByte, (Decoder<Byte>) ByteBuf::readByte),
        Short((Encoder<Short>) ByteBuf::writeShort, (Decoder<Short>) ByteBuf::readShort),
        Integer((Encoder<Integer>) ByteBuf::writeInt, (Decoder<Integer>) ByteBuf::readInt),
        Long((Encoder<Long>) ByteBuf::writeLong, (Decoder<Long>) ByteBuf::readLong),
        Float((Encoder<Float>) ByteBuf::writeFloat, (Decoder<Float>) ByteBuf::readFloat),
        Double((Encoder<Double>) ByteBuf::writeDouble, (Decoder<Double>) ByteBuf::readDouble),
        Boolean((Encoder<Boolean>) ByteBuf::writeBoolean, (Decoder<Boolean>) ByteBuf::readBoolean);

        public final Encoder<?> encoder;
        public final Decoder<?> decoder;

        Type(Encoder<?> encoder, Decoder<?> decoder){
            this.encoder = encoder;
            this.decoder = decoder;
        }
    }

    public interface Encoder<T>{
        public void encode(ByteBuf buffer, T value);
    }

    public interface Decoder<T>{
        public T decode(ByteBuf buffer);
    }

    public interface Codec<T> extends Encoder<T>, Decoder<T>{

    }
}
