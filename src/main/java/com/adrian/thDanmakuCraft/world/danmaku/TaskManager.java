package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.world.IDataStorage;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.compress.utils.Lists;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.List;
import java.util.Map;

public class TaskManager<T> implements IDataStorage, ILuaValue{
    public static final Integer MAX_TASK_SIZE = 256;
    /// 註冊好的Task
    private final Map<String, AbstractTask<T>> registryTasks = Maps.newHashMap();
    /// 正在ticking的Task
    private final List<AbstractTask<T>> tickingTasks = Lists.newArrayList();
    /// 等待加載的任務
    private final List<LazyTask> lazyTasks = Lists.newArrayList();
    /// 任務的目標對象
    private final T target;
    ///  LuaValue形式
    private final LuaValue luaValueForm;

    public boolean canRegister = false;

    public TaskManager(T target){
        this.target = target;
        this.luaValueForm = this.ofLuaClass();
    }

    /// tick添加的Task
    public void tickTasks(){
        if(this.tickingTasks.isEmpty()){
            return;
        }

        /*List<LazyTask> removeLazy = Lists.newArrayList();
        for (LazyTask lazyTask : lazyTasks){
            AbstractTask<T> task = this.getRegisteredTask(lazyTask.taskName);
            if (task != null) {
                task.timer = lazyTask.timer;
                task.lifetime = lazyTask.lifetime;
                task.delay = lazyTask.delay;
                tickingTasks.add(task);
                removeLazy.add(lazyTask);
            }
        }
        for (LazyTask t : removeLazy){
            lazyTasks.remove(t);
        }*/

        List<AbstractTask<T>> removeList = Lists.newArrayList();
        for(AbstractTask<T> task : tickingTasks){
            if(task.isAlive()) {
                if(task.startTime <= 0) {
                    task.tick(target);
                }
                task.startTime = Math.max(task.startTime -1,0);
            }else {
                removeList.add(task);
            }
        }
        //THDanmakuCraftMod.LOGGER.warn("ticking tasks!" + tickingTasks);
        for (AbstractTask<T> task : removeList){
            tickingTasks.remove(task);
        }
        removeList.clear();
    }

    public void registerTask(String taskName, AbstractTask<T> task){
        if(!canRegister){
            try {
                throw new IllegalAccessException("You cannot register any tasks outer register method!");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
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

    public boolean hasRegisteredTask(String taskName){
        return registryTasks.containsKey(taskName);
    }
    public void startTask(String taskName, int lifetime, int startTime){
        if(this.tickingTasks.size() > MAX_TASK_SIZE){
            THDanmakuCraftMod.LOGGER.warn("task list is full! you cannot simultaneously run more than {} tasks!",MAX_TASK_SIZE);
            return;
        }

        if (registryTasks.containsKey(taskName)){
            AbstractTask<T> task = getRegisteredTask(taskName);
            task.lifetime = lifetime;
            task.startTime = startTime;
            tickingTasks.add(task);
            //THDanmakuCraftMod.LOGGER.warn("task started! task name:{}",taskName);
        }else {
            THDanmakuCraftMod.LOGGER.warn("Attempted to start a unregistered task! task name:{}",taskName);
        }
    }

    public void startTask(String taskName, int lifetime){
        this.startTask(taskName,lifetime,0);
    }

    public void loadTask(String taskName, int timer, int lifetime, int startTime){
        lazyTasks.add(new LazyTask(taskName,timer,lifetime,startTime));
    }

    public void restartLazyTasks(){
        List<LazyTask> removeList = Lists.newArrayList();
        for(LazyTask lazyTask:lazyTasks){
            if (this.hasRegisteredTask(lazyTask.taskName)) {
                AbstractTask<T> task = this.getRegisteredTask(lazyTask.taskName);
                if (task != null) {
                    task.timer = lazyTask.timer;
                    task.lifetime = lazyTask.lifetime;
                    task.startTime = lazyTask.delay;
                    tickingTasks.add(task);
                    removeList.add(lazyTask);
                } else {
                    THDanmakuCraftMod.LOGGER.warn("failed restart lazy tasks! task name:{}",lazyTask.taskName);
                }
            }
        }
        for (LazyTask t : removeList){
            lazyTasks.remove(t);
        }
        removeList.clear();
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
        buffer.writeShort(this.tickingTasks.size()+this.lazyTasks.size());
        /// write task names
        for(AbstractTask<T> task : this.tickingTasks){
            buffer.writeUtf(task.taskName);
            buffer.writeInt(task.timer);
            buffer.writeInt(task.lifetime);
            buffer.writeInt(task.startTime);
        }
        for(LazyTask lazyTask : this.lazyTasks){
            buffer.writeUtf(lazyTask.taskName);
            buffer.writeInt(lazyTask.timer);
            buffer.writeInt(lazyTask.lifetime);
            buffer.writeInt(lazyTask.delay);
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
            int lifetime = buffer.readInt();
            int delay = buffer.readInt();
            this.loadTask(taskName,timer,lifetime,delay);
        }
        this.restartLazyTasks();
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag listtag = new ListTag();
        for (AbstractTask<T> task : this.tickingTasks){
            CompoundTag taskTag = new CompoundTag();
            taskTag.putString("task_name", task.taskName);
            taskTag.putInt("timer", task.timer);
            taskTag.putInt("lifetime", task.lifetime);
            taskTag.putInt("delay", task.startTime);
            listtag.add(taskTag);
        }
        for (LazyTask lazyTask : this.lazyTasks){
            CompoundTag taskTag = new CompoundTag();
            taskTag.putString("task_name", lazyTask.taskName);
            taskTag.putInt("timer", lazyTask.timer);
            taskTag.putInt("lifetime", lazyTask.lifetime);
            taskTag.putInt("delay", lazyTask.delay);
            listtag.add(taskTag);
        }
        compoundTag.put("Tasks", listtag);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        ListTag listtag = compoundTag.getList("Tasks", Tag.TAG_COMPOUND);
        for (Tag _tag : listtag){
            if(_tag instanceof CompoundTag tag){
                String taskName = tag.getString("task_name");
                int timer = tag.getInt("timer");
                int lifetime = tag.getInt("lifetime");
                int delay = tag.getInt("delay");
                this.loadTask(taskName,timer,lifetime,delay);
            }
        }
        this.restartLazyTasks();
    }

    public record LazyTask(String taskName, int timer, int lifetime, int delay){ }

    public static abstract class AbstractTask<T>{
        public int timer = 0;
        public int lifetime = 0;
        public int startTime = 0;
        public String taskName;

        public AbstractTask(){
        }

        abstract void tick(T target);
        boolean isAlive(){
            return this.timer <= this.lifetime;
        }

        abstract AbstractTask<T> copy();
    }

    public static class Task<T> extends AbstractTask<T> {

        public TaskRunnable<T> runnable;

        public Task(TaskRunnable<T> runnable){
            super();
            this.runnable = runnable;
        }

        void tick(T target){
            runnable.run(target, this.timer, this.lifetime);
            this.timer++;
        }

        Task<T> copy(){
            Task<T> task = new Task<>( this.runnable);
            task.lifetime = this.lifetime;
            task.taskName = this.taskName;
            task.startTime = this.startTime;
            return task;
        }

        public interface TaskRunnable<T> {
            void run(T target, int timer, int lifetime);
        }
    }

    /// Lua任务类，用于执行Lua脚本
    public static class LuaTask<T extends ILuaValue> extends AbstractTask<T> {

        public final LuaFunction runnable;
        public boolean canInvoke = true;

        public LuaTask(LuaValue runnable) {
            this.canInvoke = runnable.isfunction();
            this.runnable = runnable.checkfunction();
        }

        @Override
        void tick(T target) {
            try {
                this.runnable.invoke(target.ofLuaValue(), LuaValue.valueOf(this.timer), LuaValue.valueOf(this.lifetime));
            }catch (Exception e){
                this.canInvoke = false;
            }
            this.timer++;
        }

        @Override
        boolean isAlive(){
            return this.canInvoke && super.isAlive();
        }

        @Override
        LuaTask<T> copy() {
            LuaTask<T> task = new LuaTask<>(this.runnable);
            task.lifetime = this.lifetime;
            task.taskName = this.taskName;
            task.startTime = this.startTime;
            return task;
        }
    }

    @Override
    public LuaValue ofLuaClass() {
        LuaValue library = LuaValue.tableOf();
        library.setmetatable(this.getMeta());
        library.set("source", LuaValue.userdataOf(this));
        return library;
    }

    @Override
    public LuaValue ofLuaValue(){
        return this.luaValueForm;
    }

    @Override
    public LuaValue getMeta() {
        return LuaAPI.meta;
    }

    private static class LuaAPI {
        private static TaskManager<ILuaValue> checkTaskManager(LuaValue luaValue) {
            if (luaValue.get("source").checkuserdata() instanceof TaskManager<?> taskManager){
                return (TaskManager<ILuaValue>) taskManager;
            }
            throw new NullPointerException();
        }

        private static final LibFunction registerTask = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                TaskManager<ILuaValue> taskManager = checkTaskManager(varargs.arg(1));
                taskManager.registerTask(varargs.checkjstring(2), new TaskManager.LuaTask<>(varargs.checkfunction(3)));
                return LuaValue.NIL;
            }
        };

        private static final LibFunction startTask = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                TaskManager<ILuaValue> taskManager = checkTaskManager(varargs.arg(1));
                LuaValue delay = varargs.arg(4);
                taskManager.startTask(varargs.checkjstring(2), varargs.checkint(3), delay.isnil() ? 0 : delay.checkint());
                return LuaValue.NIL;
            }
        };

        private static final LibFunction removeTask = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                TaskManager<ILuaValue> taskManager = checkTaskManager(varargs.arg(1));
                taskManager.removeTask(varargs.checkjstring(2));
                return LuaValue.NIL;
            }
        };

        private static final LibFunction clearTasks = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTaskManager(varargs.arg(1)).clearTasks();
                return LuaValue.NIL;
            }
        };

        public static LuaValue functions() {
            LuaValue library = LuaValue.tableOf();
            library.set("registerTask", registerTask);
            library.set("startTask", startTask);
            library.set("removeTask", removeTask);
            library.set("clearTasks", clearTasks);
            return library;
        }
        public static final LuaValue meta = ILuaValue.setMeta(functions());
    }
}
