package com.adrian.thDanmakuCraft.events;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityEvents {

    public static void onEnttiyTick(EntityEvent.EntityConstructing event) {

    }


}
