package com.adrian.thDanmakuCraft;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.*;
import java.lang.reflect.Field;

public class ObjectToBytesTest {

    public static void main(String[] args) throws Exception {
        Globals globals = JsePlatform.standardGlobals();
        globals.load("function a() return print(\"aaa\") end").call();
        LuaFunction a = globals.get("a").checkfunction();
        LuaFunction a3 = new VarArgFunction() {
            @Override
            public LuaValue call() {
                System.out.println("aaa3");
                return LuaValue.NIL;
            }
        };
        System.out.println(a);
        System.out.println(a3);
        a.call();
        a3.call();

        byte[] bytes = LuaFunctionReflectionSerializer.serialize(a);
        System.out.println("bytes:"+bytes.length);
        LuaFunction a2 = LuaFunctionReflectionSerializer.deserialize(bytes, globals);
        System.out.println(a2);
        a2.call();
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

    public static class LuaFunctionReflectionSerializer {

        public static byte[] serialize(LuaFunction func) throws Exception {
            if (!(func instanceof LuaClosure)) {
                throw new IllegalArgumentException("Only Lua closures supported");
            }
            LuaClosure closure = (LuaClosure) func;
            Prototype prototype = closure.p;
            int[] code = prototype.code;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            for (int c : code) {
                dos.write(c);
            }
            return bos.toByteArray();
        }
        public static LuaFunction deserialize(byte[] data, Globals globals) throws Exception {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int length = data.length / 4;
            int[] code = new int[length];
            for (int i = 0; i < length; i++) {
                code[i] = dis.readInt();
            }

            Prototype prototype = new Prototype();
            prototype.code = code;
            LuaClosure function = new LuaClosure(prototype, globals);
            return function;
        }
    }
}
