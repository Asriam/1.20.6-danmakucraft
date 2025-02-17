package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.client.gui.components.SpellCardNameOverlay;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            //SpellCardNameOverlay.spellCards.clear();
            for (Entity entity:level.entitiesForRendering()) {
                if (entity instanceof EntityTHObjectContainer container) {
                    BulletMount += container.getContainer().getObjectManager().getTHObjects().size();
                }

                if(entity instanceof EntityTHSpellCard spellCard){
                    if(spellCard.getContainer().isSpellCard() && !SpellCardNameOverlay.spellCards.contains(spellCard)){
                        SpellCardNameOverlay.spellCards.add(spellCard);
                    }
                }
            }

            List<EntityTHSpellCard> removeList = new ArrayList<>();
            List<Entity> entities = Lists.newArrayList(level.entitiesForRendering().iterator());

            AABB playerBoundingBox = Minecraft.getInstance().player.getBoundingBox();
            for(EntityTHSpellCard spellCard:SpellCardNameOverlay.spellCards){
                spellCard.paramsForRender.lastMagicSquareRotation = spellCard.paramsForRender.magicSquareRotation;
                Vec3 rotation = spellCard.position().vectorTo(Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition());
                spellCard.paramsForRender.magicSquareRotation =
                        spellCard.paramsForRender.magicSquareRotation.lerp(
                                rotation,
                                0.03f);
                boolean flag = entities.contains(spellCard) && spellCard.getContainer().isInLifeTime() && spellCard.getContainer().getContainerBound().intersects(playerBoundingBox);
                spellCard.paramsForRender.flag1 = flag;
                if (flag/*entities.contains(spellCard)*/) {
                    spellCard.paramsForRender.deathTimer = Math.min(spellCard.paramsForRender.deathTimer,10.0f);
                    spellCard.paramsForRender.lastDeathTimer = spellCard.paramsForRender.deathTimer;
                    spellCard.paramsForRender.deathTimer = Math.max(spellCard.paramsForRender.deathTimer -0.6f,0.0f);
                } else {
                    spellCard.paramsForRender.lastDeathTimer = spellCard.paramsForRender.deathTimer;
                    spellCard.paramsForRender.deathTimer += 0.6f;
                    //System.out.print(spellCard.deathTimerForRenderSpellCardNameBar);
                    if (spellCard.paramsForRender.deathTimer > 100.0f) {
                        removeList.add(spellCard);
                    }
                }

                spellCard.paramsForRender.timer1 += 0.6f;
            }

            removeList.forEach(SpellCardNameOverlay.spellCards::remove);
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
