package com.adrian.thDanmakuCraft.world.danmaku.laser;

import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectType;

public class THLaser extends THObject {

    public float width;
    public float length;

    public THLaser(THObjectType<THLaser> type, THObjectContainer container) {
        super(type, container);
    }

    public THLaser(THObjectContainer container){
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
