package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.lua.LuaCore;
import com.adrian.thDanmakuCraft.lua.LuaLoader;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber
public class Events {


    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        CommandBuildContext context = event.getBuildContext();
        event.getDispatcher().register(Commands.literal("refresh_lua_lib").executes(arguments -> {
            LuaLoader.init();
            LuaCore.init();
            return 15;
        }));

        event.getDispatcher().register(Commands.literal("spellcard")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("test")
                        .then(Commands.argument("spellcardkey", StringArgumentType.string())
                                .suggests((commandContext, builder) -> {
                                    for (String keys : LuaCore.getInstance().spellCardClassKeys) {
                                        builder.suggest(keys);
                                    }
                                    return builder.buildFuture();
                                }).executes(arguments -> {
                                    String spellCardKey = StringArgumentType.getString(arguments, "spellcardkey");
                                    /*if (!LuaCore.getInstance().spellCardClassKeys.contains(spellCardKey)){
                                        return 0;
                                    }*/
                                    var source = arguments.getSource();
                                    var player = source.getPlayer();
                                    var level = source.getLevel();
                                    EntityTHObjectContainer entityTHObjectContainer = new EntityTHSpellCard(null, level, "");
                                    entityTHObjectContainer.setPos(player.position());
                                    level.addFreshEntity(entityTHObjectContainer);
                                    entityTHObjectContainer.getContainer().setLuaClass(spellCardKey);
                                    return 15;
                                }).then(Commands.argument("position", Vec3Argument.vec3()).executes(arguments -> {
                                    String spellCardKey = StringArgumentType.getString(arguments, "spellcardkey");
                                    var source = arguments.getSource();
                                    var player = source.getPlayer();
                                    var level = source.getLevel();
                                    EntityTHObjectContainer entityTHObjectContainer = new EntityTHSpellCard(null, level, "");
                                    entityTHObjectContainer.setPos(Vec3Argument.getVec3(arguments, "position"));
                                    level.addFreshEntity(entityTHObjectContainer);
                                    entityTHObjectContainer.getContainer().setLuaClass(spellCardKey);
                                    return 15;
                                }))))
                .then(Commands.literal("get")
                        .then(Commands.argument("spellcardkey", StringArgumentType.string())
                                .suggests((commandContext, builder) -> {
                                    for (String keys : LuaCore.getInstance().spellCardClassKeys) {
                                        builder.suggest(keys);
                                    }
                                    return builder.buildFuture();
                                }).executes(arguments -> {
                                    var source = arguments.getSource();
                                    var player = source.getPlayer();
                                    var level = source.getLevel();
                                    return 15;
                                })))
                .then(Commands.literal("clear")
                        .executes(arguments -> {
                            ServerLevel serverLevel = arguments.getSource().getLevel();
                            for (Entity entity : serverLevel.getAllEntities()) {
                                if (entity instanceof EntityTHSpellCard spellCard) {
                                    spellCard.discard();
                                }
                            }
                            return 15;
                        })));
    }

    /*@SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        THDanmakuCraftMod.LOGGER.info("资源正在重新加载...aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        // 添加自定义的资源重新加载监听器
        event.addListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) ->
                CompletableFuture.runAsync(() -> {
                            // 后台线程中执行的准备工作
                            preparationsProfiler.startTick();
                            // ... 准备资源
                            THDanmakuCraftMod.LOGGER.info("资源正在重新加载...ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc");
                            preparationsProfiler.endTick();
                        }, backgroundExecutor).thenCompose(stage::wait)
                        .thenRunAsync(() -> {
                            // 主线程中执行的实际重新加载
                            reloadProfiler.startTick();
                            THDanmakuCraftMod.LOGGER.info("资源正在重新加载...bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
                            LuaEventCache.clearMyCaches();
                            // 重新初始化你的资源
                            reloadProfiler.endTick();
                        }, gameExecutor)
        );
    }*/

    /*@SubscribeEvent
    public static void onResourcePackChange(ResourcePackChangeEvent event) {
        System.out.println("资源包发生变化！");

        // 检查是否是 F3+T 触发的重载
        if (Minecraft.getInstance().isLocalServer()) {
            // 在单人游戏中
            System.out.println("单人游戏资源包重载");
        }
    }*/
}
