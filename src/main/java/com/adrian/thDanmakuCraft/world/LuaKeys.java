package com.adrian.thDanmakuCraft.world;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Lua 字串鍵的快取。
 *
 * <p>luaj 的 {@code LuaValue.get(String)} 每次呼叫都會新建一個 {@code LuaString}
 * 並重新計算雜湊；在「每個物件每 tick 呼叫數次」的熱路徑上（本模組同時可能有上千個
 * THObject）這會產生大量短命物件。這裡把常用的鍵快取起來，改用
 * {@code get(LuaValue)} 直接比對。</p>
 */
public final class LuaKeys {

    public static final LuaString SOURCE = LuaString.valueOf("source");
    public static final LuaString CLASS_NAME = LuaString.valueOf("className");
    public static final LuaString X = LuaString.valueOf("x");
    public static final LuaString Y = LuaString.valueOf("y");
    public static final LuaString Z = LuaString.valueOf("z");
    public static final LuaString CO = LuaString.valueOf("co");
    public static final LuaString PROGRESS = LuaString.valueOf("progress");

    private static final Map<String, LuaString> CACHE = new HashMap<>();

    private LuaKeys() {
    }

    /** 取得（並快取）事件名稱等動態字串鍵。鍵的數量有限（腳本事件名），不會無限成長。 */
    public static LuaString of(String key) {
        LuaString cached = CACHE.get(key);
        if (cached == null) {
            cached = LuaString.valueOf(key);
            CACHE.put(key, cached);
        }
        return cached;
    }
}
