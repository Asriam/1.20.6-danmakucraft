package com.adrian.thDanmakuCraft.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Lua 端的「常數」支援。
 *
 * <pre>
 * -- 1. 表：就地凍結（讀取照常，寫入／新增都會丟錯）
 * local t = { text = "1111" }
 * constantize(t)
 * t.text = "2222"   -- error
 * t.text2 = "1111"  -- error
 *
 * -- 2. 純量／區域變數：靠 {@link LuaConstantGuard} 在載入時改寫賦值
 * local s = "1111"
 * constantize(s)
 * s = "2222"        -- error（改寫成 __lua_const_guard__("s")）
 *
 * -- 3. 全域常數
 * constantize("MAX", 100)
 * MAX = 1           -- error
 * </pre>
 *
 * <p><b>限制</b>：Lua 的區域變數賦值是 VM 的 {@code SETLOCAL}，執行期無法攔截；
 * 純量與 local 的保護完全依賴載入時的原始碼改寫（{@link LuaConstantGuard}）。
 * 也就是說「先 constantize、後面才賦值」的寫法才會被擋下。</p>
 */
public final class LuaConstants {

    /** 已鎖定的全域名稱。 */
    private static final Set<String> LOCKED = new LinkedHashSet<>();
    /** 由原始碼改寫登記的常數名稱（可能也是 local）。 */
    private static final Set<String> SOURCE_CONSTANTS = new LinkedHashSet<>();
    /** 已凍結的表（身分比對）。 */
    private static final Set<LuaTable> FROZEN = Collections.newSetFromMap(new IdentityHashMap<>());

    /** 被鎖定名稱的實際值（因為鍵已從 Globals 移除）。 */
    private static final LuaTable STORE = new LuaTable();

    private static Globals globals;

    private LuaConstants() {
    }

    /** 安裝 {@code __index} / {@code __newindex}（可重複呼叫）。 */
    public static synchronized void install(Globals target) {
        globals = target;
        LuaTable meta = new LuaTable();
        meta.set("__index", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue table, LuaValue key) {
                if (key.isstring() && LOCKED.contains(key.tojstring())) {
                    return STORE.get(key);
                }
                return LuaValue.NIL;
            }
        });
        meta.set("__newindex", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue table, LuaValue key, LuaValue value) {
                if (key.isstring() && LOCKED.contains(key.tojstring())) {
                    throw new LuaError("attempt to assign to constant '" + key.tojstring() + "'");
                }
                table.rawset(key, value);
                return LuaValue.NIL;
            }
        });
        target.setmetatable(meta);
    }

    // ------------------------------------------------------------------ 全域常數

    /** 把已存在的全域（或還不存在的名稱）鎖成常數。 */
    public static synchronized void lockGlobal(String name) {
        if (globals == null) {
            throw new LuaError("constantize: globals not ready");
        }
        LuaValue current = globals.get(name);
        STORE.set(name, current);
        globals.rawset(name, LuaValue.NIL);
        LOCKED.add(name);
    }

    /** 建立／覆寫一個全域常數。 */
    public static synchronized void defineConstant(String name, LuaValue value) {
        if (globals == null) {
            throw new LuaError("constantize: globals not ready");
        }
        globals.rawset(name, value);
        STORE.set(name, value);
        globals.rawset(name, LuaValue.NIL);
        LOCKED.add(name);
    }

    // ------------------------------------------------------------------ 表：就地凍結

    /** 就地凍結表：把鍵搬到隱藏表，之後所有寫入都會丟錯；回傳同一個表。 */
    public static LuaValue freezeInPlace(LuaValue value) {
        if (!value.istable()) {
            return value;
        }
        LuaTable table = value.checktable();
        if (FROZEN.contains(table)) {
            return table;
        }
        LuaTable hidden = new LuaTable();
        LuaValue key = table.next(LuaValue.NIL).arg1();
        while (!key.isnil()) {
            LuaValue next = table.next(key).arg1();
            hidden.set(key, table.get(key));
            table.rawset(key, LuaValue.NIL);
            key = next;
        }

        LuaValue oldMetaValue = table.getmetatable();
        LuaTable oldMeta = oldMetaValue != null && oldMetaValue.istable() ? oldMetaValue.checktable() : null;
        LuaValue oldIndex = oldMeta == null ? LuaValue.NIL : oldMeta.get("__index");

        LuaTable meta = new LuaTable();
        if (oldMeta != null) {
            LuaValue metaKey = oldMeta.next(LuaValue.NIL).arg1();
            while (!metaKey.isnil()) {
                LuaValue next = oldMeta.next(metaKey).arg1();
                if (!metaKey.tojstring().equals("__index") && !metaKey.tojstring().equals("__newindex")) {
                    meta.set(metaKey, oldMeta.get(metaKey));
                }
                metaKey = next;
            }
        }
        meta.set("__index", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue target, LuaValue field) {
                LuaValue stored = hidden.get(field);
                if (!stored.isnil()) {
                    return stored;
                }
                if (oldIndex.istable()) {
                    return oldIndex.get(field);
                }
                if (oldIndex.isfunction()) {
                    return oldIndex.invoke(target, field).arg1();
                }
                return LuaValue.NIL;
            }
        });
        meta.set("__newindex", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue target, LuaValue field, LuaValue newValue) {
                throw new LuaError("attempt to modify constant table (key '" + field.tojstring() + "')");
            }
        });
        table.setmetatable(meta);
        FROZEN.add(table);
        return table;
    }

    // ------------------------------------------------------------------ 改寫用入口

    public static synchronized void registerSourceConstant(String name) {
        if (name != null && !name.isEmpty()) {
            SOURCE_CONSTANTS.add(name);
        }
    }

    public static boolean isSourceConstant(String name) {
        return SOURCE_CONSTANTS.contains(name);
    }

    /** 被改寫的賦值會先呼叫這個，直接丟錯。 */
    public static void guard(String name) {
        throw new LuaError("attempt to assign to constant '" + name + "'", 2);
    }

    public static synchronized boolean isLocked(String name) {
        return LOCKED.contains(name) || SOURCE_CONSTANTS.contains(name);
    }

    public static boolean isFrozenTable(LuaValue value) {
        return value.istable() && FROZEN.contains(value.checktable());
    }

    public static synchronized void clear() {
        LOCKED.clear();
        SOURCE_CONSTANTS.clear();
        FROZEN.clear();
        LuaValue key = STORE.next(LuaValue.NIL).arg1();
        while (!key.isnil()) {
            LuaValue next = STORE.next(key).arg1();
            STORE.set(key, LuaValue.NIL);
            key = next;
        }
    }

    /**
     * Lua 端入口。
     *
     * <ul>
     *   <li>{@code constantize(name, value)}：建立全域常數</li>
     *   <li>{@code constantize(table)}：就地凍結表</li>
     *   <li>{@code constantize(value)}：純量原樣返回（由 {@link LuaConstantGuard} 負責擋賦值）</li>
     * </ul>
     */
    public static LuaValue constantize(Varargs args) {
        LuaValue first = args.arg(1);
        if (args.narg() >= 2 && first.isstring()) {
            defineConstant(first.tojstring(), args.arg(2));
            return args.arg(2);
        }
        if (first.istable()) {
            return freezeInPlace(first);
        }
        return first;
    }

    /** Lua 端入口：{@code isConstant("MAX")}。 */
    public static boolean isConstant(String name) {
        return isLocked(name);
    }
}
