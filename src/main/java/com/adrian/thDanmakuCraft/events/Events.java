package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.client.gui.editor.EditorScreen;
import com.adrian.thDanmakuCraft.lua.LuaCore;
import com.adrian.thDanmakuCraft.lua.LuaLoader;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.spellcard.EntityTHSpellCard;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Events {

    public static void onEnttiyTick(EntityEvent.EntityConstructing event) {

    }

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
}
