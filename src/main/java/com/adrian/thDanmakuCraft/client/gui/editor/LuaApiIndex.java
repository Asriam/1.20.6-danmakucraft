package com.adrian.thDanmakuCraft.client.gui.editor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 從 {@code data/thdanmakucraft/lua/apidoc/**.lua}（EmmyLua 標註格式）建立補全索引。
 *
 * <p>比單純抓函式名更進一步，會把每個成員歸到它的類別底下：</p>
 * <pre>
 * ---@class THObject
 * local object = {}          → object 屬於 THObject
 * function object:setPosition(x,y,z) end
 * ---@param x number
 * ---@return number
 * </pre>
 * <p>這樣 {@code object:} 之後只會列出 THObject 的成員，而不是全部 167 筆。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class LuaApiIndex {

    /**
     * @param name  成員名稱
     * @param detail 簽名（{@code fun(a, b) -> ret}）或 {@code field: type}
     * @param owner 所屬類別（由 {@code ---@class} 決定，沒有就是接收者名稱）
     * @param doc   給快速文件彈窗用的說明（參數型別 + 回傳 + 註解文字）
     */
    public record Entry(String name, String detail, String owner, String doc) {
        public boolean isFunction() {
            return detail.startsWith("fun(");
        }
    }

    private static List<Entry> cache;
    private static Map<String, List<Entry>> byOwner;
    /** 區域變數 → 類別（local object = {} 搭配 ---@class THObject）。 */
    private static Map<String, String> varToClass;

    private LuaApiIndex() {
    }

    public static synchronized List<Entry> entries() {
        if (cache != null) {
            return cache;
        }
        List<Entry> list = new ArrayList<>();
        Map<String, String> vars = new HashMap<>();
        // apidoc 與 lib 都納入索引；apidoc 先解析，同名成員優先保留它的說明
        for (LuaScriptFiles.BuiltinRoot root : LuaScriptFiles.BUILTIN_ROOTS) {
            for (String file : LuaScriptFiles.filesUnder(root)) {
                try {
                    String content = LuaScriptFiles.readBuiltin(root, file);
                    parse(new BufferedReader(new java.io.StringReader(content)), list, vars);
                } catch (IOException ignored) {
                    // 單一檔案失敗不影響其他補全
                }
            }
        }
        // 同名去重（優先保留有 owner 的）
        Map<String, Entry> unique = new LinkedHashMap<>();
        for (Entry entry : list) {
            Entry existing = unique.get(entry.name());
            if (existing == null || (existing.owner().isEmpty() && !entry.owner().isEmpty())) {
                unique.put(entry.name(), entry);
            }
        }
        List<Entry> result = new ArrayList<>(unique.values());
        result.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));

        Map<String, List<Entry>> owners = new HashMap<>();
        for (Entry entry : list) {
            if (!entry.owner().isEmpty()) {
                owners.computeIfAbsent(entry.owner().toLowerCase(), key -> new ArrayList<>()).add(entry);
            }
        }
        byOwner = owners;
        varToClass = vars;
        cache = result;
        return cache;
    }

    /**
     * 依接收者名稱取成員：{@code object} 會對到 {@code THObject}（透過 {@code local object = {}} 的對應）。
     * 找不到對應時回退成全部成員，避免「打了卻沒東西」。
     */
    public static List<Entry> entriesFor(String receiver) {
        entries();
        if (receiver == null || receiver.isEmpty()) {
            return cache;
        }
        String owner = varToClass.getOrDefault(receiver, receiver);
        List<Entry> exact = byOwner.get(owner.toLowerCase());
        if (exact != null && !exact.isEmpty()) {
            return exact;
        }
        // Vec3 / core.Vec3 這類帶命名空間的類別
        for (Map.Entry<String, List<Entry>> entry : byOwner.entrySet()) {
            String key = entry.getKey();
            int dot = key.lastIndexOf('.');
            String shortName = dot >= 0 ? key.substring(dot + 1) : key;
            if (shortName.equalsIgnoreCase(receiver) || shortName.equalsIgnoreCase(owner)) {
                return entry.getValue();
            }
        }
        return cache;
    }

    /** 解析一份 apidoc。 */
    static void parse(BufferedReader reader, List<Entry> out, Map<String, String> localToClass) throws IOException {
        String currentClass = "";
        List<String> docLines = new ArrayList<>();
        List<String> paramLines = new ArrayList<>();
        String lastReturn = null;
        String lastLocalVar = null;

        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.startsWith("---@class ")) {
                String declared = trimmed.substring(10).trim();
                int colon = declared.indexOf(':');
                currentClass = colon >= 0 ? declared.substring(0, colon).trim() : declared;
                docLines.clear();
                paramLines.clear();
                lastReturn = null;
            } else if (trimmed.startsWith("---@field ")) {
                String[] parts = trimmed.substring(10).trim().split("\\s+", 2);
                if (parts.length == 2) {
                    out.add(new Entry(parts[0], "field: " + parts[1], currentClass, "欄位 " + parts[1]));
                }
            } else if (trimmed.startsWith("---@param ")) {
                String rest = trimmed.substring(10).trim();
                String[] parts = rest.split("\\s+", 3);
                if (parts.length >= 2) {
                    paramLines.add(parts[0] + ": " + parts[1]
                            + (parts.length >= 3 ? " — " + parts[2] : ""));
                }
            } else if (trimmed.startsWith("---@return ")) {
                lastReturn = trimmed.substring(11).trim();
            } else if (trimmed.startsWith("---@overload ")) {
                docLines.add("多載 " + trimmed.substring(12).trim());
            } else if (trimmed.startsWith("---") && trimmed.length() > 3) {
                docLines.add(trimmed.substring(3).trim());
            } else if (trimmed.startsWith("local ") && trimmed.contains("= {}")) {
                // local object = {}  → 之後的 function object:xxx 屬於 currentClass
                String name = trimmed.substring(6).trim();
                int eq = name.indexOf('=');
                if (eq > 0) {
                    lastLocalVar = name.substring(0, eq).trim();
                    if (!currentClass.isEmpty()) {
                        localToClass.put(lastLocalVar, currentClass);
                    }
                }
            } else if (trimmed.startsWith("function ")) {
                int open = trimmed.indexOf('(');
                int close = trimmed.indexOf(')', open + 1);
                if (open > 9 && close > open) {
                    String head = trimmed.substring(9, open).trim();
                    int sep = head.lastIndexOf(':');
                    if (sep < 0) {
                        sep = head.lastIndexOf('.');
                    }
                    String receiver = sep >= 0 ? head.substring(0, sep) : "";
                    String name = sep >= 0 ? head.substring(sep + 1) : head;
                    if (isIdentifier(name)) {
                        String owner = localToClass.getOrDefault(receiver, receiver);
                        String params = trimmed.substring(open + 1, close).trim();
                        String detail = "fun(" + params + ")"
                                + (lastReturn == null ? "" : " -> " + lastReturn);
                        out.add(new Entry(name, detail, owner, buildDoc(paramLines, lastReturn, docLines)));
                    }
                }
                docLines.clear();
                paramLines.clear();
                lastReturn = null;
            } else if (!trimmed.startsWith("---") && !trimmed.isEmpty()) {
                // 非註解、非 function 的一般程式碼會打斷文件累積
                if (!trimmed.startsWith("local ")) {
                    docLines.clear();
                    paramLines.clear();
                    lastReturn = null;
                }
            }
        }
    }

    private static String buildDoc(List<String> params, String returns, List<String> docLines) {
        StringBuilder builder = new StringBuilder();
        for (String param : params) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append("參數 ").append(param);
        }
        if (returns != null) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append("回傳 ").append(returns);
        }
        for (String doc : docLines) {
            if (doc.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(doc);
        }
        return builder.toString();
    }

    private static boolean isIdentifier(String text) {
        if (text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_')) {
                return false;
            }
        }
        return true;
    }
}
