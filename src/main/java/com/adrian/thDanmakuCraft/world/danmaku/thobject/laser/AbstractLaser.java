package com.adrian.thDanmakuCraft.world.danmaku.thobject.laser;

import com.adrian.thDanmakuCraft.world.danmaku.ITHObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;

public abstract class AbstractLaser extends THObject {
    public AbstractLaser(THObjectType<? extends THObject> type, ITHObjectContainer container) {
        super(type, container);
    }


}
