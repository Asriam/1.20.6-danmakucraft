package com.adrian.thDanmakuCraft;

import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.*;

public class ObjectToBytesTest {

    public static void main(String[] args) {
        THObjectContainer container = new THObjectContainer(null);
        System.out.println(container);

        byte[] bytes = serialize(container);
        System.out.println(bytes.length);
        THObjectContainer container2 = deserialize(bytes,THObjectContainer.class);
        System.out.println(container2);

        FriendlyByteBuf buf =  new FriendlyByteBuf(Unpooled.buffer());
        container.encode(buf);
        byte[] bytes2 = buf.array();
        System.out.print(bytes2.length);
    }

    static byte[] serialize(final Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            //out.flush();
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return (T) in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
