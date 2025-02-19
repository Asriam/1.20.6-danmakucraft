package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.world.IDataStorage;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.compress.utils.Lists;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

import java.util.List;
import java.util.Map;

public class TaskManager<T> implements IDataStorage {
    /// 註冊好的Task
    private final Map<String, AbstractTask<T>> registryTasks = Map.of();
    /// 正在ticking的Task
    private final List<AbstractTask<T>> tickingTasks = Lists.newArrayList();

    /// tick添加的Task
    public void tick(){
        if(this.tickingTasks.isEmpty()){
            return;
        }

        List<AbstractTask<T>> removeList = Lists.newArrayList();
        for(AbstractTask<T> task : tickingTasks){
            if(!task.shouldRemove()) {
                task.tick();
            }else {
                removeList.add(task);
            }
        }
        for (AbstractTask<T> task : removeList){
            tickingTasks.remove(task);
        }
        removeList.clear();
    }

    public void registerTask(String taskName, AbstractTask<T> task){
        registryTasks.put(taskName, task);
        task.taskName = taskName;
    }

    public AbstractTask<T> getRegisteredTask(String taskName){
        if(registryTasks.containsKey(taskName)){
            return registryTasks.get(taskName).copy();
        }else {
            THDanmakuCraftMod.LOGGER.error("Attempted to get a unregistered task! task name:{}",taskName);
            return null;
        }
    }
    public void startTask(String taskName){
        tickingTasks.add(getRegisteredTask(taskName));
    }


    public void loadTask(String taskName, int timer){
        AbstractTask<T> task = this.getRegisteredTask(taskName);
        task.timer = timer;
        tickingTasks.add(task);
    }

    public void clearTasks(){
        this.tickingTasks.clear();
    }

    public void removeTask(String taskName){
        if(registryTasks.containsKey(taskName)){
            tickingTasks.remove(registryTasks.get(taskName));
        }else {
            THDanmakuCraftMod.LOGGER.warn("Attempted to remove a unregistered task! task name:{}",taskName);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        /// write task list size
        buffer.writeShort(this.tickingTasks.size());
        /// write task names
        for(AbstractTask<T> task : this.tickingTasks){
            buffer.writeUtf(task.taskName);
            buffer.writeInt(task.timer);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        /// read task list size
        int size = buffer.readShort();
        /// read tasks form task names
        for (int i=0;i<size;i++){
            String taskName = buffer.readUtf();
            int timer = buffer.readInt();
            this.loadTask(taskName,timer);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag listtag = new ListTag();
        for (AbstractTask<T> task : this.tickingTasks){
            CompoundTag taskTag = new CompoundTag();
            taskTag.putString("task_name", task.taskName);
            taskTag.putInt("timer", task.timer);
            listtag.add(taskTag);
        }
        compoundTag.put("tasks", listtag);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        ListTag listtag = compoundTag.getList("tasks", Tag.TAG_COMPOUND);
        for (Tag _tag : listtag){
            if(_tag instanceof CompoundTag tag){
                String taskName = tag.getString("task_name");
                int timer = tag.getInt("timer");
                this.loadTask(taskName,timer);
            }
        }
    }

    public static abstract class AbstractTask<T> {
        public int timer = 0;
        public final int lifetime;
        public final T target;
        public String taskName;

        public AbstractTask(T target, int lifetime){
            this.target = target;
            this.lifetime = lifetime;
        }

        abstract void tick();
        boolean shouldRemove(){
            return this.timer > this.lifetime;
        }

        abstract AbstractTask<T> copy();
        //abstract AbstractTask<T> clone();
    }

    public static class Task<T> extends AbstractTask<T> {

        public TaskRunnable<T> runnable;

        public Task(T target, int lifetime, TaskRunnable<T> runnable){
            super(target,lifetime);
            this.runnable = runnable;
        }

        void tick(){
            runnable.run(target, this.timer);
            this.timer++;
        }

        Task<T> copy(){
            return new Task<>(this.target, this.lifetime, this.runnable);
        }

        public interface TaskRunnable<T> {
            void run(T target, int timer);
        }
    }

    /// Lua任务类，用于执行Lua脚本
    public static class LuaTask<T extends ILuaValue> extends AbstractTask<T> {

        public final LuaFunction runnable;
        public boolean canInvoke = true;

        public LuaTask(T target, int lifetime, LuaValue runnable) {
            super(target, lifetime);
            this.canInvoke = runnable.isfunction();
            this.runnable = runnable.checkfunction();
        }

        @Override
        void tick() {
            try {
                this.runnable.invoke(this.target.ofLuaValue(), LuaValue.valueOf(this.timer));
            }catch (Exception e){
                this.canInvoke = false;
            }
            this.timer++;
        }

        @Override
        boolean shouldRemove(){
            return !this.canInvoke || super.shouldRemove();
        }

        @Override
        LuaTask<T> copy() {
            return new LuaTask<T>(this.target, this.lifetime, this.runnable);
        }
    }
}
