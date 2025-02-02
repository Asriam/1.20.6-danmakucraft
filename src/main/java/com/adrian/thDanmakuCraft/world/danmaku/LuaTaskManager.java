package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.world.IDataStorage;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.ArrayList;
import java.util.List;

public class LuaTaskManager implements IDataStorage, ILuaValue{

    private final List<LuaValue> tasks = new ArrayList<>();
    private final List<Integer> progresses = new ArrayList<>();
    private final ILuaValue target;
    private final LuaValue luaValueForm;

    public LuaTaskManager(ILuaValue target){
        this.target = target;
        this.luaValueForm = this.ofLuaClass();
    }

    public void addTask(LuaValue task){
        if(this.tasks.contains(task)){
            return;
        }

        LuaThread coroutine = getTaskCoroutine(task);
        if(coroutine != null && coroutine.state.status != LuaThread.STATUS_DEAD) {
            tasks.add(task);

            if(tasks.size()-1 < progresses.size()) {
                Integer progress = progresses.get(tasks.size() - 1);
                if (progress != null) {
                    for (int j = 0; j < progress; j++) {
                        coroutine.resume(null);
                    }
                }
            }
        }
    }

    public void removeTask(LuaValue task){
        if(task.isint()){
            tasks.remove(task.checkint()+1);
            return;
        }
        tasks.remove(task);
    }

    public void doTasks(){
        List<LuaValue> removeTasks = new ArrayList<>();
        for (LuaValue task : tasks) {
            LuaThread coroutine = task.get("co").checkthread();
            if(coroutine.isthread() && coroutine.state.status != LuaThread.STATUS_DEAD) {
                coroutine.resume(target.ofLuaValue());
                LuaValue LProgress = task.get("progress");
                int progress = (LProgress.isnil() ? 0 : LProgress.checkint()) + 1;
                task.set("progress", progress);
            }else {
                removeTasks.add(task);
            }
        }

        for(LuaValue task : removeTasks){
            tasks.remove(task);
        }
    }

    public void loadTask(){

    }

    public static LuaThread getTaskCoroutine(LuaValue task){
        LuaValue co = task.get("co");
        if (co.isthread()) {
            return co.checkthread();
        }
        return null;
    }

    public static LuaTaskManager checkLuaTaskManager(LuaValue luaValue) {
        if (luaValue.get("source").checkuserdata() instanceof LuaTaskManager luaTaskManager){
            return luaTaskManager;
        }
        throw new NullPointerException();
        //return null;
    }

    private static final LibFunction addTask = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            checkLuaTaskManager(luaValue0).addTask(luaValue);
            return null;
        }
    };

    private static final LibFunction removeTask = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            checkLuaTaskManager(luaValue0).removeTask(luaValue);
            return null;
        }
    };

    private static final LibFunction doTasks = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0) {
            checkLuaTaskManager(luaValue0).doTasks();
            return null;
        }
    };

    @Override
    public LuaValue ofLuaValue() {
        return this.luaValueForm;
    }

    private static final LuaValue meta = LuaValue.tableOf();
    static {
        meta.set("__index", luaClassFunctions());
    }

    @Override
    public LuaValue getMeta() {
        return meta;
    }
    @Override
    public LuaValue ofLuaClass() {
        LuaValue library = LuaValue.tableOf();
        library.setmetatable(this.getMeta());
        //Params
        library.set("source", LuaValue.userdataOf(this));
        return library;
    }

    public static LuaValue luaClassFunctions(){
        LuaValue library = LuaValue.tableOf();
        library.set("addTask",    addTask);
        library.set("removeTask", removeTask);
        library.set("doTasks",    doTasks);
        return library;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeShort(this.tasks.size());
        for(LuaValue task : this.tasks){
            LuaValue progress = task.get("progress");
            buffer.writeInt(progress.isnil() ? 0 : progress.checkint());
        }
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        short size = buffer.readShort();
        for(int i=0;i<size;i++){
            int progress = buffer.readInt();
            this.progresses.clear();
            this.progresses.add(i,progress);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return null;
    }

    @Override
    public void load(CompoundTag compoundTag) {

    }
}
