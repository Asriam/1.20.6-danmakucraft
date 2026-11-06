package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.client.gui.widgets.SpellCardNameOverlay;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = THDanmakuCraftMod.MOD_ID/*, bus = EventBusSubscriber.Bus.GAME*/)
public class TickEvents {

    /** 原本是 {@code TickEvent.Type}，NeoForge 把它拆成各自的事件類別。 */
    public enum TickType { LEVEL, CLIENT, SERVER }

    @SubscribeEvent
    public static void levelTick(LevelTickEvent.Post event) {
        runTasks(TickType.LEVEL);
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        runTasks(TickType.SERVER);
    }

    private static final Map<String, TickTask> tasks = new HashMap<>();

    public static int clientTicks = 0;
    public static int BulletMount = 0;

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        clientTicks++;
        BulletMount = 0;
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            for (Entity entity : level.entitiesForRendering()) {
                if (entity instanceof EntityTHObjectContainer container) {
                    BulletMount += container.getContainer().getObjectManager().getTHObjects().size();
                }

                if (entity instanceof EntityTHSpellCard spellCard) {
                    if (spellCard.getContainer().isSpellCard() && !SpellCardNameOverlay.spellCards.contains(spellCard)) {
                        SpellCardNameOverlay.spellCards.add(spellCard);
                    }
                }
            }

            List<EntityTHSpellCard> removeList = new ArrayList<>();
            List<Entity> entities = Lists.newArrayList(level.entitiesForRendering().iterator());

            AABB playerBoundingBox = Minecraft.getInstance().player.getBoundingBox();
            for (EntityTHSpellCard spellCard : SpellCardNameOverlay.spellCards) {
                spellCard.paramsForRender.lastMagicSquareRotation = spellCard.paramsForRender.magicSquareRotation;
                Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
                Vec3 rotation = spellCard.position().vectorTo(camera != null ? camera.getPosition() : Vec3.ZERO);
                spellCard.paramsForRender.magicSquareRotation =
                        spellCard.paramsForRender.magicSquareRotation.lerp(rotation, 0.03f);
                boolean flag = entities.contains(spellCard) && spellCard.getContainer().isInLifeTime()
                        && spellCard.getContainer().getContainerBound().intersects(playerBoundingBox);
                spellCard.paramsForRender.flag1 = flag;
                if (flag) {
                    spellCard.paramsForRender.deathTimer = Math.min(spellCard.paramsForRender.deathTimer, 10.0f);
                    spellCard.paramsForRender.lastDeathTimer = spellCard.paramsForRender.deathTimer;
                    spellCard.paramsForRender.deathTimer = Math.max(spellCard.paramsForRender.deathTimer - 0.6f, 0.0f);
                } else {
                    spellCard.paramsForRender.lastDeathTimer = spellCard.paramsForRender.deathTimer;
                    spellCard.paramsForRender.deathTimer += 0.6f;
                    if (spellCard.paramsForRender.deathTimer > 100.0f) {
                        removeList.add(spellCard);
                    }
                }

                spellCard.paramsForRender.timer1 += 0.6f;
            }

            removeList.forEach(SpellCardNameOverlay.spellCards::remove);
        }
        runTasks(TickType.CLIENT);
    }

    private static void runTasks(TickType type) {
        for (TickTask task : tasks.values().stream().filter(t -> t.type == type).toList()) {
            task.task.run();
        }
    }

    public static void addTickTask(String name, TickType type, Task task) {
        tasks.putIfAbsent(name, new TickTask(type, task));
    }

    public static void clearTask(String name) {
        tasks.remove(name);
    }

    public static void clearAllTasks() {
        tasks.clear();
    }

    static class TickTask {
        Task task;
        TickType type;

        TickTask(TickType type, Task task) {
            this.type = type;
            this.task = task;
        }
    }

    public interface Task {
        void run();
    }
}
