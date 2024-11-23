package com.asrian.thDanmakuCraft.world.entity.danmaku.laser;

import com.asrian.thDanmakuCraft.init.THObjectInit;
import com.asrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.asrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.asrian.thDanmakuCraft.world.entity.danmaku.THObjectType;

public class THLaser extends THObject {

    public THLaser(THObjectType<THLaser> type, EntityTHObjectContainer container) {
        super(type, container);
    }

    public THLaser(EntityTHObjectContainer container){
        this(THObjectInit.TH_LASER.get(),container);
    }


}
