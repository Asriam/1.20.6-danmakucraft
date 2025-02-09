package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        //System.out.print(LuaThread.coroutine_count+"\n");
    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event){
        /*
        for(THObjectContainer container:THObjectContainer.allContainers){
        }*/
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event){
    }

    public static void addTickTask(String name, TickEvent.Type type, Task task){
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
