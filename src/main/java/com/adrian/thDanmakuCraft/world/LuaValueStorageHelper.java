package com.adrian.thDanmakuCraft.world;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.TestOnly;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.Map;

public class LuaValueStorageHelper {
    private static final Map<Integer,LuaValue> userDataMap = new HashMap<>();

    public static void writeLuaTable(FriendlyByteBuf byteBuf, LuaTable table) {
        if(!table.istable()){
            byteBuf.writeShort(0);
            return;
        }
        LuaValue[] keys = table.checktable().keys();
        byteBuf.writeShort(keys.length);
        for (LuaValue key : keys) {
            String keyName = key.checkjstring();
            byteBuf.writeUtf(keyName);
            writeLuaValue(byteBuf, table.get(key));
        }
    }
    public static LuaTable readLuaTable(FriendlyByteBuf byteBuf) {
        LuaTable table = LuaValue.tableOf();
        int length = byteBuf.readShort();
        for (int i = 0; i < length; i++) {
            String keyName = byteBuf.readUtf();
            LuaValue value = readLuaValue(byteBuf);
            table.set(keyName, value);
        }
        return table;
    }

    public static void writeLuaValue(FriendlyByteBuf byteBuf, LuaValue luaValue) {
        short type = (short) luaValue.type();
        byteBuf.writeShort(type);
        switch (type) {
            case LuaValue.TBOOLEAN -> byteBuf.writeBoolean(luaValue.checkboolean());
            case LuaValue.TINT -> byteBuf.writeInt(luaValue.checkint());
            case LuaValue.TNUMBER -> byteBuf.writeDouble(luaValue.checkdouble());
            case LuaValue.TSTRING -> byteBuf.writeUtf(luaValue.checkjstring());
            case LuaValue.TTABLE -> writeLuaTable(byteBuf, luaValue.checktable());
            case LuaValue.TUSERDATA -> {
                int hashCode = luaValue.hashCode();
                userDataMap.put(hashCode, luaValue);
                byteBuf.writeInt(hashCode);
                //byteBuf.writeUtf(luaValue.typename());
                //byteBuf.writeUtf(luaValue.checkjstring());
            }
        }
    }

    public static LuaValue readLuaValue(FriendlyByteBuf byteBuf) {
        short type = byteBuf.readShort();
        return switch (type) {
            case LuaValue.TBOOLEAN -> LuaValue.valueOf(byteBuf.readBoolean());
            case LuaValue.TINT -> LuaValue.valueOf(byteBuf.readInt());
            case LuaValue.TNUMBER -> LuaValue.valueOf(byteBuf.readDouble());
            case LuaValue.TSTRING -> LuaValue.valueOf(byteBuf.readUtf());
            case LuaValue.TTABLE -> readLuaTable(byteBuf);
            case LuaValue.TUSERDATA -> {
                int hashCode = byteBuf.readInt();
                LuaValue luaValue = userDataMap.get(hashCode);
                yield luaValue;
            }
            default -> LuaValue.NIL;
        };
    }

    public static CompoundTag saveLuaTable(LuaValue table) {
        if(!table.istable()){
            return new CompoundTag();
        }

        CompoundTag tag = new CompoundTag();
        LuaValue[] keys = table.checktable().keys();
        for (LuaValue key : keys) {
            String keyName = key.checkjstring();
            tag.put(keyName, saveLuaValue(table.get(key)));
        }
        return tag;
    }
    public static LuaTable loadLuaTable(CompoundTag tag) {
        LuaTable table = LuaValue.tableOf();
        for (String key : tag.getAllKeys()) {
            LuaValue value = loadLuaValue(tag.getCompound(key));
            table.set(key, value);
        }
        return table;
    }

    public static CompoundTag saveLuaValue(LuaValue luaValue) {
        short type = (short) luaValue.type();
        CompoundTag valueTag = new CompoundTag();
        valueTag.putShort("type", type);
        switch (type) {
            case LuaValue.TBOOLEAN -> valueTag.putBoolean("value", luaValue.checkboolean());
            case LuaValue.TINT -> valueTag.putInt("value", luaValue.checkint());
            case LuaValue.TNUMBER -> valueTag.putDouble("value", luaValue.checkdouble());
            case LuaValue.TSTRING -> valueTag.putString("value", luaValue.checkjstring());
            case LuaValue.TTABLE -> valueTag.put("value", saveLuaTable(luaValue.checktable()));
        }
        return valueTag;
    }

    public static LuaValue loadLuaValue(CompoundTag tag) {
        short type = tag.getShort("type");
        return switch (type) {
            case LuaValue.TBOOLEAN -> LuaValue.valueOf(tag.getBoolean("value"));
            case LuaValue.TINT -> LuaValue.valueOf(tag.getInt("value"));
            case LuaValue.TNUMBER -> LuaValue.valueOf(tag.getDouble("value"));
            case LuaValue.TSTRING -> LuaValue.valueOf(tag.getString("value"));
            case LuaValue.TTABLE -> loadLuaTable(tag.getCompound("value"));
            default -> LuaValue.NIL;
        };
    }

    @TestOnly
    public static void main(String[] args) {

        LuaTable testTable = LuaValue.tableOf();
        LuaTable testTable3 = LuaValue.tableOf();

        testTable.set("test", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        testTable3.set("test", testTable);

        CompoundTag tag = saveLuaTable(testTable3);
        LuaTable testTable2 = loadLuaTable(tag);

        //System.out.print("ssssssssssssssssssssssssssssssssssss");
        //System.out.print(testTable2.get("test"));


        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        writeLuaTable(buffer, testTable3);
        LuaTable table = readLuaTable(buffer);
        System.out.print(table.get("test").get("test"));
    }
}
