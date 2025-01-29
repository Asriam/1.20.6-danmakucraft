package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.world.ILuaValue;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.ArrayList;
import java.util.List;

public class LuaTaskManager {

    private final List<LuaValue> tasks = new ArrayList<>();
    private final ILuaValue target;

    public LuaTaskManager(ILuaValue target){
        this.target = target;
    }

    public void addTask(LuaValue task){
        LuaThread coroutine = getTaskCoroutine(task);
        if(coroutine != null && coroutine.state.status != LuaThread.STATUS_DEAD) {
            tasks.add(task);
        }
    }

    public void removeTask(LuaValue task){
        tasks.remove(task);
    }

    public void doTasks(){
        for (LuaValue task : tasks) {
            LuaThread coroutine = getTaskCoroutine(task);
            if(coroutine != null && coroutine.state.status != LuaThread.STATUS_DEAD) {
                coroutine.resume(target.ofLuaValue());
                LuaValue LProgress = task.get("progress");
                int progress = (LProgress.isnil() ? 0 : LProgress.checkint()) + 1;
                task.set("progress", progress);
            }else {
                tasks.remove(task);
            }
        }
    }

    public static LuaThread getTaskCoroutine(LuaValue task){
        LuaValue co = task.get("co");
        if (co.isthread()) {
            return co.checkthread();
        }
        return null;
    }
}
