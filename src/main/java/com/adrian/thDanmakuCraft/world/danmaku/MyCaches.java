package com.adrian.thDanmakuCraft.world.danmaku;

import com.google.common.collect.Maps;
import org.luaj.vm2.LuaValue;

import java.util.Map;

/**
 * 腳本事件解析結果的快取。
 *
 * <p>鍵是 {@code 類別名 + "$" + 事件名}（與原本一致，單層查表）；
 * 值為 {@link LuaValue#NIL} 代表「這個類別沒有實作這個事件」，也就是負快取。</p>
 *
 * <p>負快取的價值：腳本通常只實作少數事件（onTick、onHit…），其餘事件過去每次呼叫
 * 都會重新查一次 metatable 並印一次警告；現在只查一次。</p>
 */
public class MyCaches {

    public static final Map<String, LuaValue> scriptEventCache = Maps.newHashMap();

    private MyCaches() {
    }

    public static LuaValue getScriptEvent(String className, String eventName) {
        return scriptEventCache.get(className + "$" + eventName);
    }

    public static void putScriptEvent(String className, String eventName, LuaValue function) {
        scriptEventCache.put(className + "$" + eventName, function);
    }

    public static void clearMyCaches() {
        scriptEventCache.clear();
    }
}
