package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.THObjectContainer;

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
