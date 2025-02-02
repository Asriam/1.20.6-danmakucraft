package com.adrian.thDanmakuCraft.util;

import com.adrian.thDanmakuCraft.world.IDataStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class TaskManager implements IDataStorage {
    private int taskTick = 0;
    private int taskDuration = 120;

    public void tick(){
        if(taskTick <= taskDuration) {
            taskTick++;
        }else {
            taskTick = 0;
        }
    }

    public void setTaskDuration(int taskDuration){
        this.taskDuration = taskDuration;
    }
    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(taskTick);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        taskTick = buffer.readInt();
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("taskTick",this.taskTick);
        compoundTag.putInt("taskDuration",this.taskDuration);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.taskTick = compoundTag.getInt("taskTick");
        this.taskDuration = compoundTag.getInt("taskDuration");
    }

    public interface ITask{
        void run();
    }

    public static class JTask implements ITask{
        private final Runnable runnable;

        public JTask(Runnable runnable){
            this.runnable = runnable;
        }

        public void run(){
            runnable.run();
        }
    }
}
