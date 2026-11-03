package com.adrian.thDanmakuCraft.world.danmaku.thobject;

import com.adrian.thDanmakuCraft.world.danmaku.ITHObjectContainer;

public class THParticle extends THObject {
    public THParticle(THObjectType<? extends THObject> type, ITHObjectContainer container) {
        super(type, container);
    }

    @Override
    public boolean shouldCollide(){
        return false;
    }


}
