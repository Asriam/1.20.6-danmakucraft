package com.adrian.thDanmakuCraft.world.entity.danmaku.laser;

import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObjectType;

public class THLaser extends THObject {

    public float width;
    public float length;

    public THLaser(THObjectType<THLaser> type, EntityTHObjectContainer container) {
        super(type, container);
    }

    public THLaser(EntityTHObjectContainer container){
        this(THObjectInit.TH_LASER.get(),container);
    }


}
