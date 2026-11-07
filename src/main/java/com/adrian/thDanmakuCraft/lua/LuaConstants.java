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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Lua 端的「常數」支援。
 *
 * <p>用法：</p>
 * <pre>
 * constantize("MAX", 100)      -- 建立全域常數
 * constantize("MAX")           -- 把已存在的全域鎖成常數
 * local CONFIG = constantize({ speed = 1 })   -- 回傳唯讀表
 * </pre>
 *
 * <p>被鎖定的全域名稱一旦重新賦值就會丟出 {@link LuaError}。作法是把值搬到隱藏的
 * {@link #STORE}，再從 {@link Globals} 移除該鍵，如此賦值才會經過 {@code __newindex}。</p>
 *
 * <p><b>限制</b>：Lua 的區域變數（local）賦值是 VM 的 {@code SETLOCAL} 指令，沒有
 * 任何執行期掛鉤可以攔截；因此 local 純量無法被鎖定。要讓 local 也受保護，請把它
 * 存成唯讀表：{@code local C = constantize({ value = 5 })}，之後 {@code C.value = 6} 會丟出例外。</p>
 */
public final class LuaConstants {

    /** 已鎖定的全域名稱。 */
    private static final Set<String> LOCKED = new LinkedHashSet<>();
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

    /**
     * 回傳唯讀表（淺層）。傳入非表值時原樣返回——純量沒有可攔截的寫入點。
     */
    public static LuaValue readonly(LuaValue value) {
        if (!value.istable()) {
            return value;
        }
        LuaTable proxy = new LuaTable();
        LuaTable meta = new LuaTable();
        meta.set("__index", value);
        meta.set("__newindex", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue table, LuaValue key, LuaValue newValue) {
                throw new LuaError("attempt to modify constant table (key '" + key.tojstring() + "')");
            }
        });
        meta.set("__len", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue table) {
                return LuaValue.valueOf(value.length());
            }
        });
        proxy.setmetatable(meta);
        return proxy;
    }

    public static synchronized boolean isLocked(String name) {
        return LOCKED.contains(name);
    }

    public static synchronized void clear() {
        LOCKED.clear();
        LuaValue key = STORE.next(LuaValue.NIL).arg1();
        while (!key.isnil()) {
            LuaValue next = STORE.next(key).arg1();
            STORE.set(key, LuaValue.NIL);
            key = next;
        }
    }

    /** Lua 端入口：{@code constantize(name)} / {@code constantize(name, value)} / {@code constantize(table)}。 */
    public static LuaValue constantize(Varargs args) {
        LuaValue first = args.arg(1);
        if (first.isstring()) {
            String name = first.tojstring();
            if (args.narg() >= 2) {
                defineConstant(name, args.arg(2));
                return args.arg(2);
            }
            lockGlobal(name);
            return LuaValue.TRUE;
        }
        if (first.isnil()) {
            throw new LuaError("constantize: expected a name or a table");
        }
        return readonly(first);
    }

    /** Lua 端入口：{@code isConstant("MAX")}。 */
    public static boolean isConstant(String name) {
        return isLocked(name);
    }
}
