package com.adrian.thDanmakuCraft.world.entity.danmaku;

import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;

public class THLaser extends THObject {

    public float width;
    public float length;

    public THLaser(THObjectType<THLaser> type, EntityTHObjectContainer container) {
        super(type, container);
    }

    public THLaser(EntityTHObjectContainer container){
        this(THObjectInit.TH_LASER.get(),container);
    }

    @Override
    public void onTick(){
        super.onTick();
    }

    @Override
    public void collisionLogic(){

    }


}
