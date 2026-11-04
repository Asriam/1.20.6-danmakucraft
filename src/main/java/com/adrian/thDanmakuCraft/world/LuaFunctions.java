package com.adrian.thDanmakuCraft.world;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * 函式表建構器：取代各處手寫的 {@code LuaValue.tableOf() + set("name", fn)…}。
 *
 * <p>優點：
 * <ul>
 *   <li>預先配置 hash slot，避免重複 rehash</li>
 *   <li>鍵統一走 {@link LuaKeys#of(String)} 快取，不重複建 LuaString</li>
 *   <li>讀起來是「宣告式」的清單，新增 API 函式一目瞭然</li>
 * </ul>
 * </p>
 */
public final class LuaFunctions {

    private final LuaTable table;

    private LuaFunctions(int capacity) {
        this.table = LuaValue.tableOf(0, capacity);
    }

    public static LuaFunctions create() {
        return new LuaFunctions(8);
    }

    public static LuaFunctions create(int capacity) {
        return new LuaFunctions(capacity);
    }

    public LuaFunctions add(String name, LuaValue function) {
        this.table.set(LuaKeys.of(name), function);
        return this;
    }

    public LuaValue build() {
        return this.table;
    }

    /** 直接得到帶 {@code __index} 的 metatable。 */
    public LuaValue meta() {
        return ILuaValue.setMeta(build());
    }
}
