package com.adrian.thDanmakuCraft.world.danmaku;

import com.google.common.collect.Maps;
import org.luaj.vm2.LuaFunction;

import java.util.Map;

public class MyCaches {

    public static final Map<String, LuaFunction> scriptEventCache = Maps.newHashMap();
    
    public static void clearMyCaches(){
        scriptEventCache.clear();
    }
}
