package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickEvents{
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

    public static int BulletMount = 0;
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event){
        //System.out.print(LuaThread.coroutine_count+"\n");
        BulletMount = 0;
        ClientLevel level = Minecraft.getInstance().level;
        if(level != null) {
            for (Entity entity:level.entitiesForRendering()) {
                if (entity instanceof EntityTHObjectContainer container) {
                    BulletMount += container.getContainer().getObjectManager().getTHObjects().size();
                }
            }

        }
        //System.out.print(level == null);
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
