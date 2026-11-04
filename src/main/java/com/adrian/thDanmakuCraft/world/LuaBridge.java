package com.adrian.thDanmakuCraft.world;

import org.luaj.vm2.LuaValue;

/**
 * Java 物件 ↔ Lua 的共用橋接器。
 *
 * <p>原本每個管理器都各自手寫同樣的三段程式碼：
 * {@code tableOf() + setmetatable + set("source", userdataOf(this))} 與
 * {@code get("source").checkuserdata() instanceof Xxx}。這裡統一收斂，減少重複，
 * 也把字串鍵固定為 {@link LuaKeys#SOURCE}。</p>
 */
public final class LuaBridge {

    private LuaBridge() {
    }

    /**
     * 建立物件的 Lua proxy table（含 metatable 與 source userdata）。
     * 只在建構時呼叫一次，並由 {@code ofLuaValue()} 回傳快取。
     */
    public static LuaValue proxy(ILuaValue self) {
        LuaValue form = LuaValue.tableOf(0, 1);
        form.setmetatable(self.getMeta());
        form.set(LuaKeys.SOURCE, LuaValue.userdataOf(self));
        return form;
    }

    /** 從 Lua 傳入的參數取出被包裝的 Java 物件；型別不符時拋 {@link NullPointerException}（與舊行為一致）。 */
    public static <T> T target(LuaValue luaValue, Class<T> type) {
        Object object = luaValue.get(LuaKeys.SOURCE).checkuserdata();
        if (type.isInstance(object)) {
            return type.cast(object);
        }
        throw new NullPointerException();
    }
}
