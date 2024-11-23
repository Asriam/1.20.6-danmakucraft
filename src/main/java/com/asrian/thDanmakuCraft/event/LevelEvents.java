package com.asrian.thDanmakuCraft.event;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;
import com.asrian.thDanmakuCraft.world.entity.danmaku.THObject;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

@Mod.EventBusSubscriber(modid = THDanmakuCraftCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelEvents {

    public static List<THObject> THOBJECT_LIST = Lists.newArrayList();

    @SubscribeEvent
    public static void worldTick(TickEvent.LevelTickEvent event){
        /*
        Level level = event.level;
        Player player = level.getNearestPlayer(0.0f,0.0f,0.0f, -1.0D, false);
        if(player!=null) {
            List<EntityTHObjectContainer> containers = level.getEntitiesOfClass(EntityTHObjectContainer.class,new AABB(-100,-100,-100,100,100,100));
            THDanmakuCraftCore.LOGGER.info(""+containers.size());
            int num = 0;
            for (EntityTHObjectContainer container:containers){
                num += container.getTHObjects().size();
            }
            THDanmakuCraftCore.LOGGER.info(""+num);
        }

         */
    }
}
