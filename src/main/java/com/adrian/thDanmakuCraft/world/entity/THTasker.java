package com.adrian.thDanmakuCraft.world.entity;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.google.common.collect.Lists;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

public class THTasker {
    //public LinkedList<BooleanSupplier> SUPPLIERS = Lists.newLinkedList();
    private final List<BooleanSupplier> SUPPLIERS = Lists.newArrayList();
    private int tick = 0;
    private boolean isLocked = false;

    private boolean close = false;

    public void add(final Runnable runnable){
        if(this.isLocked){
            return;
        }
        AtomicInteger remainingTicks = new AtomicInteger(this.tick);
        SUPPLIERS.add(()->{
            if(remainingTicks.decrementAndGet() <= 0) {
                runnable.run();
                return true;
            }
            return false;
        });
    }

    public void wait(int tick){
        if(tick <= 0) return;
        this.tick += tick;
    }

    public void resume(){
        if(SUPPLIERS.isEmpty()){
            close();
        }
        SUPPLIERS.removeIf(BooleanSupplier::getAsBoolean);
        tick = Math.max(0, tick - 1);
    }

    public void lock(){
        this.isLocked = true;
    }

    public void unlock(){
        this.isLocked = false;
    }

    public void close(){
        this.close = true;
    }

    public static class THTaskerManager{

        private static final Map<String,THTaskerManager> taskerManagerMap = new WeakHashMap<>();
        private final        List<THTasker>              taskerList       = new ArrayList<>();
        private final        EntityTHObjectContainer     container;

        public THTaskerManager(EntityTHObjectContainer container){
            this.container = container;
        }

        public THTasker create(){
            THDanmakuCraftCore.LOGGER.info("asdsadasd1");
            THTasker task = new THTasker();
            taskerList.add(task);
            return task;
        }

        public void resume(){
            List<THTasker> removeList = Lists.newArrayList();
            taskerList.forEach((task)->{
                if (task.close){
                    removeList.add(task);
                    return;
                }
                task.resume();
            });

            taskerList.removeAll(removeList);
        }

        public void writeData(FriendlyByteBuf buffer){
            String uuid = this.container.getStringUUID();
            taskerManagerMap.put(uuid,this);
            buffer.writeUtf(uuid);
        }

        public void readData(FriendlyByteBuf buffer){
            String uuid = buffer.readUtf();
            this.taskerList.addAll(taskerManagerMap.get(uuid).taskerList);
        }

        public void close(){
            taskerList.forEach(THTasker::close);
            taskerList.clear();
        }
    }
}