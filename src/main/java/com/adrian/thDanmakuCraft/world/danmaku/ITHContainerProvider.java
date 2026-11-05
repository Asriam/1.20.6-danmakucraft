package com.adrian.thDanmakuCraft.world.danmaku;

import org.luaj.vm2.LuaValue;

public interface ITHContainerProvider {

    public ITHObjectContainer getContainer();

    public LuaValue ofLuaValue();
}
