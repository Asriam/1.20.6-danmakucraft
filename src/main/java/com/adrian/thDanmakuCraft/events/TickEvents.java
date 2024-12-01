package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickEvents {

    @SubscribeEvent
    public static void worldTick(TickEvent.LevelTickEvent event){
    }

    private static final Map<String, TickTask> tasks = new HashMap<>();

    @SubscribeEvent
    public static void TickEvent(TickEvent event){
        for (TickTask task:tasks.values().stream().filter((t)->t.type == event.type).toList()){
            task.task.run();
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event){

    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event){
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event){
    }

    public static void registryTickTask(String name,TickEvent.Type type, Task task){
        if (tasks.get(name) == null) {
            tasks.put(name, new TickTask(type,task));
        }else {
            // TODO
        }
    }

    public static void clearTask(String name){
        tasks.remove(name);
    }

    public static void clearAllTasks(){
        tasks.clear();
    }

    static class TickTask {
        Task task;
        TickEvent.Type type;

        TickTask(TickEvent.Type type,Task task){
            this.type = type;
            this.task = task;
        }
    }

    public interface Task{
        void run();
    }
}
