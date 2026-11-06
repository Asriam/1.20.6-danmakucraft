package com.adrian.thDanmakuCraft.client.gui.editor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 從「使用者自己寫的腳本」裡抽出可補全的識別字：區域變數、全域變數、函式、參數、迴圈變數。
 *
 * <p>純文字掃描（不建 AST），因為編輯器每打幾個字就要重算，成本必須很低；
 * 也和 {@link LuaApiIndex} 一樣抽成獨立類別，方便離線測試。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class LuaSymbolIndex {

    /** @param detail 例如 {@code local 變數}、{@code 參數}、{@code 全域函式} */
    public record Symbol(String name, String detail) {
    }

    private static final Set<String> KEYWORDS = Set.of(
            "and", "break", "do", "else", "elseif", "end", "false", "for", "function", "goto",
            "if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "true",
            "until", "while");

    private static final Pattern LOCAL_FUNC = Pattern.compile(
            "\\blocal\\s+function\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(([^)]*)\\)");
    private static final Pattern GLOBAL_FUNC = Pattern.compile(
            "(?m)^\\s*function\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(([^)]*)\\)");
    private static final Pattern METHOD_FUNC = Pattern.compile(
            "\\bfunction\\s+([A-Za-z_][A-Za-z0-9_.:]*)\\s*\\(([^)]*)\\)");
    private static final Pattern FOR_LOOP = Pattern.compile(
            "\\bfor\\s+([A-Za-z_][A-Za-z0-9_]*(?:\\s*,\\s*[A-Za-z_][A-Za-z0-9_]*)*)\\s*(?:=|\\bin\\b)");
    private static final Pattern LOCAL_VARS = Pattern.compile(
            "\\blocal\\s+(?!function\\b)([A-Za-z_][A-Za-z0-9_]*(?:\\s*,\\s*[A-Za-z_][A-Za-z0-9_]*)*)");
    private static final Pattern GLOBAL_ASSIGN = Pattern.compile(
            "(?m)^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*=(?!=)");

    private LuaSymbolIndex() {
    }

    public static List<Symbol> parse(String source) {
        Map<String, String> found = new LinkedHashMap<>();
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        for (Matcher matcher = LOCAL_FUNC.matcher(source); matcher.find(); ) {
            add(found, matcher.group(1), "local 函式");
            addParams(found, matcher.group(2));
        }
        for (Matcher matcher = GLOBAL_FUNC.matcher(source); matcher.find(); ) {
            add(found, matcher.group(1), "全域函式");
            addParams(found, matcher.group(2));
        }
        for (Matcher matcher = METHOD_FUNC.matcher(source); matcher.find(); ) {
            String head = matcher.group(1);
            int separator = Math.max(head.lastIndexOf(':'), head.lastIndexOf('.'));
            add(found, head.substring(separator + 1), "方法");
            addParams(found, matcher.group(2));
        }
        for (Matcher matcher = FOR_LOOP.matcher(source); matcher.find(); ) {
            for (String name : matcher.group(1).split(",")) {
                add(found, name.trim(), "迴圈變數");
            }
        }
        for (Matcher matcher = LOCAL_VARS.matcher(source); matcher.find(); ) {
            for (String name : matcher.group(1).split(",")) {
                add(found, name.trim(), "local 變數");
            }
        }
        for (Matcher matcher = GLOBAL_ASSIGN.matcher(source); matcher.find(); ) {
            add(found, matcher.group(1), "全域變數");
        }

        List<Symbol> symbols = new ArrayList<>(found.size());
        for (Map.Entry<String, String> entry : found.entrySet()) {
            symbols.add(new Symbol(entry.getKey(), entry.getValue()));
        }
        symbols.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        return symbols;
    }

    private static void add(Map<String, String> out, String name, String detail) {
        if (name == null) {
            return;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty() || KEYWORDS.contains(trimmed) || !isIdentifier(trimmed)) {
            return;
        }
        out.putIfAbsent(trimmed, detail);
    }

    private static void addParams(Map<String, String> out, String params) {
        if (params == null || params.isBlank()) {
            return;
        }
        for (String param : params.split(",")) {
            String name = param.trim();
            int space = name.indexOf(' ');
            if (space > 0) {
                name = name.substring(0, space);
            }
            if (name.equals("...") || name.isEmpty()) {
                continue;
            }
            add(out, name, "參數");
        }
    }

    private static boolean isIdentifier(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_')) {
                return false;
            }
        }
        return !text.isEmpty();
    }
}
