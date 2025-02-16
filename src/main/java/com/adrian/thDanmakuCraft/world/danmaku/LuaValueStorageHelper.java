package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class LuaValueStorageHelper {
    //private static final Map<Integer,LuaValue> userDataMap = new HashMap<>();

    private final ITHObjectContainer container;
    public LuaValueStorageHelper(ITHObjectContainer container){
        this.container = container;
    }

    public void writeTHObject(FriendlyByteBuf byteBuf, THObject object){
        byteBuf.writeUUID(object.getUUID());
    }

    public LuaTable readTHObject(FriendlyByteBuf byteBuf){
        THObject object = this.container.getObjectFromUUID(byteBuf.readUUID());
        return object == null ? LuaValue.tableOf() : object.ofLuaValue().checktable();
    }

    public void writeLuaTable(FriendlyByteBuf byteBuf, LuaTable table) {
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
    public LuaTable readLuaTable(FriendlyByteBuf byteBuf) {
        LuaTable table = LuaValue.tableOf();
        int length = byteBuf.readShort();
        for (int i = 0; i < length; i++) {
            String keyName = byteBuf.readUtf();
            LuaValue value = readLuaValue(byteBuf);
            table.set(keyName, value);
        }
        return table;
    }

    public void writeLuaValue(FriendlyByteBuf byteBuf, LuaValue luaValue) {
        short type = (short) luaValue.type();
        byteBuf.writeShort(type);
        switch (type) {
            case LuaValue.TBOOLEAN -> byteBuf.writeBoolean(luaValue.checkboolean());
            case LuaValue.TINT -> byteBuf.writeInt(luaValue.checkint());
            case LuaValue.TNUMBER -> byteBuf.writeDouble(luaValue.checkdouble());
            case LuaValue.TSTRING -> byteBuf.writeUtf(luaValue.checkjstring());
            case LuaValue.TTABLE -> {
                if (THObject.isTHObject(luaValue)) {
                    byteBuf.writeEnum(TableType.THOBJECT);
                    writeTHObject(byteBuf, THObject.checkTHObject(luaValue));
                }else {
                    byteBuf.writeEnum(TableType.TABLE);
                    writeLuaTable(byteBuf, luaValue.checktable());
                }
            }
        }
    }

    public LuaValue readLuaValue(FriendlyByteBuf byteBuf) {
        short type = byteBuf.readShort();
        return switch (type) {
            case LuaValue.TBOOLEAN -> LuaValue.valueOf(byteBuf.readBoolean());
            case LuaValue.TINT -> LuaValue.valueOf(byteBuf.readInt());
            case LuaValue.TNUMBER -> LuaValue.valueOf(byteBuf.readDouble());
            case LuaValue.TSTRING -> LuaValue.valueOf(byteBuf.readUtf());
            case LuaValue.TTABLE -> {
                TableType tableType = byteBuf.readEnum(TableType.class);
                if (tableType == TableType.THOBJECT) {
                    yield readTHObject(byteBuf);
                }else if (tableType == TableType.TABLE) {
                    yield readLuaTable(byteBuf);
                }
                yield LuaValue.NIL;
            }
            default -> LuaValue.NIL;
        };
    }

    public CompoundTag saveTHObject(THObject object){
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid",object.getUUID());
        return tag;
    }

    public LuaTable loadTHObject(CompoundTag tag){
        THObject object = this.container.getObjectFromUUID(tag.getUUID("uuid"));
        return object == null ? LuaValue.tableOf() : object.ofLuaValue().checktable();
    }
    public CompoundTag saveLuaTable(LuaValue table) {
        CompoundTag tag = new CompoundTag();
        if(!table.istable()){
            return tag;
        }
        LuaValue[] keys = table.checktable().keys();
        for (LuaValue key : keys) {
            String keyName = key.checkjstring();
            tag.put(keyName, saveLuaValue(table.get(key)));
        }
        return tag;
    }
    public LuaTable loadLuaTable(CompoundTag tag) {
        LuaTable table = LuaValue.tableOf();
        for (String key : tag.getAllKeys()) {
            LuaValue value = loadLuaValue(tag.getCompound(key));
            table.set(key, value);
        }
        return table;
    }

    public CompoundTag saveLuaValue(LuaValue luaValue) {
        short type = (short) luaValue.type();
        CompoundTag valueTag = new CompoundTag();
        valueTag.putShort("type", type);
        switch (type) {
            case LuaValue.TBOOLEAN -> valueTag.putBoolean("value", luaValue.checkboolean());
            case LuaValue.TINT -> valueTag.putInt("value", luaValue.checkint());
            case LuaValue.TNUMBER -> valueTag.putDouble("value", luaValue.checkdouble());
            case LuaValue.TSTRING -> valueTag.putString("value", luaValue.checkjstring());
            case LuaValue.TTABLE -> {
                if (THObject.isTHObject(luaValue)) {
                    valueTag.putInt("table_type", TableType.THOBJECT.ordinal());
                    valueTag.put("value",saveTHObject(THObject.checkTHObject(luaValue)));
                }else {
                    valueTag.put("value", saveLuaTable(luaValue.checktable()));
                }
            }
        }
        return valueTag;
    }

    public LuaValue loadLuaValue(CompoundTag tag) {
        short type = tag.getShort("type");
        return switch (type) {
            case LuaValue.TBOOLEAN -> LuaValue.valueOf(tag.getBoolean("value"));
            case LuaValue.TINT -> LuaValue.valueOf(tag.getInt("value"));
            case LuaValue.TNUMBER -> LuaValue.valueOf(tag.getDouble("value"));
            case LuaValue.TSTRING -> LuaValue.valueOf(tag.getString("value"));
            case LuaValue.TTABLE -> {
                TableType tableType = TableType.values()[tag.getInt("table_type")];
                if (tableType == TableType.THOBJECT){
                    yield loadTHObject(tag.getCompound("value"));
                }else if (tableType == TableType.TABLE) {
                    yield loadLuaTable(tag.getCompound("value"));
                }
                yield LuaValue.NIL;
            }
            default -> LuaValue.NIL;
        };
    }

    private enum TableType {
        TABLE,
        THOBJECT,
        THOBJECT_CONTAINER
    }

    /*
    @TestOnly
    public static void main(String[] args) {

        LuaTable testTable = LuaValue.tableOf();
        LuaTable testTable3 = LuaValue.tableOf();

        testTable.set("test", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        testTable3.set("test", testTable);

        LuaValueStorageHelper helper = new LuaValueStorageHelper();
        CompoundTag tag = helper.saveLuaTable(testTable3);
        LuaTable testTable2 = helper.loadLuaTable(tag);

        //System.out.print("ssssssssssssssssssssssssssssssssssss");
        //System.out.print(testTable2.get("test"));


        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        helper.writeLuaTable(buffer, testTable3);
        LuaTable table = helper.readLuaTable(buffer);
        System.out.print(table.get("test").get("test"));
    }*/
}
