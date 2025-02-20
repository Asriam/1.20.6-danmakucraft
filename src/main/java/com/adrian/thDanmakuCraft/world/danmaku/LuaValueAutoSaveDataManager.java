package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.world.IDataStorage;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.compress.utils.Lists;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.List;

public class LuaValueAutoSaveDataManager implements IDataStorage, ILuaValue {

    public LuaValueStorageHelper luaValueStorageHelper;
    public List<String> keys = Lists.newArrayList();
    public IGetContainer target;
    private final LuaValue luaValueForm;

    public LuaValueAutoSaveDataManager(IGetContainer getContainer){
        this.luaValueStorageHelper = new LuaValueStorageHelper(getContainer);
        this.target = getContainer;
        this.luaValueForm = this.ofLuaClass();
    }

    public void registerAutoSaveData(String key){
        if(!keys.contains(key)){
            keys.add(key);
        }
    }

    public void unregisterAutoSaveData(String key){
        keys.remove(key);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeShort(keys.size());
        for(String key: keys) {
            buffer.writeUtf(key);
            luaValueStorageHelper.encodeLuaValue(buffer, target.ofLuaValue().get(key));
        }
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        keys.clear();
        int size = buffer.readShort();
        for (int i=0;i<size;i++){
            String key = buffer.readUtf();
            LuaValue value = luaValueStorageHelper.decodeLuaValue(buffer);
            keys.add(key);
            target.ofLuaValue().set(key, value);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        for(String key: keys) {
            CompoundTag valueTag = new CompoundTag();
            valueTag.putString("key", key);
            valueTag.put("value", luaValueStorageHelper.saveLuaValue(target.ofLuaValue().get(key)));
            list.add(valueTag);
        }
        compoundTag.put("LuaValues", list);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        keys.clear();
        ListTag list = compoundTag.getList("LuaValues", CompoundTag.TAG_COMPOUND);
        for(Tag _tag:list){
            if(_tag instanceof CompoundTag tag){
                String key = tag.getString("key");
                LuaValue value = luaValueStorageHelper.loadLuaValue(tag.getCompound("value"));
                keys.add(key);
                target.ofLuaValue().set(key, value);
            }
        }
    }

    private static class LuaAPI{
        private static LuaValueAutoSaveDataManager checkLuaValueAutoSaveDataManager(LuaValue value){
            if(value.get("source").checkuserdata() instanceof LuaValueAutoSaveDataManager manager){
                return manager;
            }
            throw new NullPointerException();
        }
        private static final LibFunction register = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkLuaValueAutoSaveDataManager(varargs.arg(1)).registerAutoSaveData(varargs.checkjstring(2));
                return LuaValue.NIL;
            }
        };
        private static final LibFunction unregister = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkLuaValueAutoSaveDataManager(varargs.arg(1)).unregisterAutoSaveData(varargs.checkjstring(2));
                return LuaValue.NIL;
            }
        };
        public static LuaValue functions(){
            LuaValue library = LuaValue.tableOf();
            library.set("register",register);
            library.set("unregister",unregister);
            return library;
        }

        public static final LuaValue meta = ILuaValue.setMeta(functions());
    }

    @Override
    public LuaValue ofLuaClass(){
        LuaValue library = LuaValue.tableOf();
        library.setmetatable(this.getMeta());
        library.set("source", LuaValue.userdataOf(this));
        return library;
    }

    @Override
    public LuaValue ofLuaValue() {
        return this.luaValueForm;
    }

    @Override
    public LuaValue getMeta() {
        return LuaAPI.meta;
    }
}
