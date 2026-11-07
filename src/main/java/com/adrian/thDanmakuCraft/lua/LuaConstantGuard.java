package com.adrian.thDanmakuCraft.lua;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 把 {@code constantize(名稱)} 之後對同名變數的賦值，改寫成「先呼叫警衛函式」。
 *
 * <p>Lua 的區域變數賦值是 VM 指令（{@code SETLOCAL}），執行期無法攔截；因此
 * {@code local x = 1; constantize(x); x = 2} 這種寫法只能靠載入時改寫原始碼來擋。</p>
 *
 * <p>改寫規則（保守）：</p>
 * <ul>
 *   <li>只認「整條敘述」形式的 {@code constantize(名稱)} 或 {@code core.constantize(名稱)}</li>
 *   <li>之後在敘述開頭出現 {@code 名稱 = ...} 時，插入
 *       {@code __lua_const_guard__("名稱") ;}（一定會丟錯，後面的賦值不會執行）</li>
 *   <li>{@code local} 宣告、{@code for 名稱 = ...}、表建構子 {@code { 名稱 = ... }} 不會被改寫</li>
 *   <li>字串、長字串、註解內容不會被動到</li>
 * </ul>
 */
public final class LuaConstantGuard {

    /** 換行後仍然「延續同一條敘述」的結尾 token。 */
    private static final Set<String> CONTINUATIONS = Set.of(
            ",", "(", "[", "{", "=", "..", "+", "-", "*", "/", "%", "^", "#",
            "and", "or", "not", "<", ">", "<=", ">=", "==", "~=", ".", ":", "return");

    private static final Set<String> STATEMENT_KEYWORDS = Set.of(
            "then", "do", "else", "repeat", "until", "end");

    private LuaConstantGuard() {
    }

    public static String transform(String source) {
        if (source == null || !source.contains("constantize")) {
            return source;
        }
        StringBuilder out = new StringBuilder(source.length() + 64);
        Set<String> frozen = new LinkedHashSet<>();

        int i = 0;
        int length = source.length();
        boolean stmtStart = true;
        boolean declMode = false;
        boolean forHeader = false;
        String lastToken = "";

        while (i < length) {
            char c = source.charAt(i);

            // ---- 註解 ----
            if (c == '-' && i + 1 < length && source.charAt(i + 1) == '-') {
                int end = skipComment(source, i);
                out.append(source, i, end);
                i = end;
                continue;
            }

            // ---- 字串 ----
            if (c == '"' || c == '\'') {
                int end = skipQuoted(source, i, c);
                out.append(source, i, end);
                i = end;
                lastToken = "str";
                stmtStart = false;
                continue;
            }

            // ---- 長字串 / 長註解 ----
            if (c == '[') {
                int equals = countLongBracket(source, i);
                if (equals >= 0) {
                    int end = skipLongBracket(source, i, equals);
                    out.append(source, i, end);
                    i = end;
                    lastToken = "str";
                    stmtStart = false;
                    continue;
                }
            }

            // ---- 換行 ----
            if (c == '\n') {
                out.append(c);
                i++;
                if (!CONTINUATIONS.contains(lastToken)) {
                    stmtStart = true;
                    declMode = false;
                    forHeader = false;
                }
                continue;
            }

            // ---- 空白 ----
            if (Character.isWhitespace(c)) {
                out.append(c);
                i++;
                continue;
            }

            // ---- 識別字 ----
            if (Character.isJavaIdentifierStart(c)) {
                int start = i;
                while (i < length && Character.isJavaIdentifierPart(source.charAt(i))) {
                    i++;
                }
                String word = source.substring(start, i);

                if (stmtStart && !forHeader && (word.equals("constantize") || word.equals("core"))) {
                    int after = matchConstantizeCall(source, start, word.equals("core"));
                    if (after > 0) {
                        String name = matchedName(source, start, word.equals("core"));
                        out.append(source, start, after);
                        if (name != null && !name.isEmpty()) {
                            frozen.add(name);
                            out.append("; __lua_const_register__(\"").append(name).append("\")");
                        }
                        i = after;
                        stmtStart = false;
                        lastToken = ")";
                        continue;
                    }
                }

                if (stmtStart && !declMode && !forHeader && frozen.contains(word)
                        && isPlainAssignment(source, i)) {
                    out.append("__lua_const_guard__(\"").append(word).append("\") ; ");
                }

                if (word.equals("local")) {
                    declMode = true;
                } else if (word.equals("for")) {
                    forHeader = true;
                } else if (STATEMENT_KEYWORDS.contains(word)) {
                    stmtStart = true;
                } else {
                    stmtStart = false;
                }
                out.append(word);
                lastToken = word;
                continue;
            }

            // ---- 數字 ----
            if (Character.isDigit(c) || (c == '.' && i + 1 < length && Character.isDigit(source.charAt(i + 1)))) {
                int start = i;
                while (i < length && (Character.isLetterOrDigit(source.charAt(i)) || source.charAt(i) == '.')) {
                    i++;
                }
                out.append(source, start, i);
                lastToken = "num";
                stmtStart = false;
                continue;
            }

            // ---- 標點與運算子 ----
            if (c == '{') {
                // 表建構子：內部的 name = value 不是賦值
                int end = skipTableConstructor(source, i);
                out.append(source, i, end);
                i = end;
                lastToken = "}";
                stmtStart = false;
                continue;
            }

            if (c == ';') {
                stmtStart = true;
                declMode = false;
                forHeader = false;
            } else if (c == '=') {
                if (declMode) {
                    declMode = false;   // local x = ... 的等號
                }
                stmtStart = false;
            } else if (c == ')' || c == ']') {
                stmtStart = false;
            } else if (c != ',' && c != '.') {
                stmtStart = false;
            }

            char two = (i + 1 < length) ? source.charAt(i + 1) : '\0';
            if ((c == '=' || c == '~' || c == '<' || c == '>' || c == '.') && two == (c == '~' ? '=' : c)) {
                out.append(source, i, i + 2);
                lastToken = source.substring(i, i + 2);
                i += 2;
                continue;
            }
            if (c == '.' && two == '.') {
                out.append("..");
                lastToken = "..";
                i += 2;
                continue;
            }
            out.append(c);
            lastToken = String.valueOf(c);
            i++;
        }
        return out.toString();
    }

    /** 名稱後面是不是單純的賦值（`=` 且不是 `==`）。 */
    private static boolean isPlainAssignment(String source, int index) {
        int j = index;
        while (j < source.length() && Character.isWhitespace(source.charAt(j))) {
            j++;
        }
        if (j >= source.length() || source.charAt(j) != '=') {
            return false;
        }
        return j + 1 >= source.length() || source.charAt(j + 1) != '=';
    }

    /** 比對 `constantize(名稱)` 或 `core.constantize(名稱)`；回傳右括號後的位置，沒有回傳 -1。 */
    private static int matchConstantizeCall(String source, int start, boolean withCore) {
        int i = start;
        if (withCore) {
            i = skipSpaces(source, i + "core".length());
            if (i >= source.length() || source.charAt(i) != '.') {
                return -1;
            }
            i = skipSpaces(source, i + 1);
            if (!source.startsWith("constantize", i)) {
                return -1;
            }
            i += "constantize".length();
        } else {
            i += "constantize".length();
        }
        i = skipSpaces(source, i);
        if (i >= source.length() || source.charAt(i) != '(') {
            return -1;
        }
        i = skipSpaces(source, i + 1);
        if (i >= source.length() || !Character.isJavaIdentifierStart(source.charAt(i))) {
            return -1;
        }
        i++;
        while (i < source.length() && Character.isJavaIdentifierPart(source.charAt(i))) {
            i++;
        }
        i = skipSpaces(source, i);
        if (i >= source.length() || source.charAt(i) != ')') {
            return -1;
        }
        return i + 1;
    }

    /** 取出 `constantize(名稱)` 裡的名稱。 */
    private static String matchedName(String source, int start, boolean withCore) {
        int i = start;
        if (withCore) {
            i = skipSpaces(source, i + "core".length()) + 1;
            i = skipSpaces(source, i) + "constantize".length();
        } else {
            i += "constantize".length();
        }
        i = skipSpaces(source, i) + 1;
        i = skipSpaces(source, i);
        int begin = i;
        while (i < source.length() && Character.isJavaIdentifierPart(source.charAt(i))) {
            i++;
        }
        return source.substring(begin, i);
    }

    private static int skipSpaces(String source, int i) {
        while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
            i++;
        }
        return i;
    }

    private static int skipComment(String source, int start) {
        int i = start + 2;
        if (i < source.length() && source.charAt(i) == '[') {
            int equals = countLongBracket(source, i);
            if (equals >= 0) {
                return skipLongBracket(source, i, equals);
            }
        }
        while (i < source.length() && source.charAt(i) != '\n') {
            i++;
        }
        return i;
    }

    private static int skipQuoted(String source, int start, char quote) {
        int i = start + 1;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == quote) {
                return i + 1;
            }
            i++;
        }
        return i;
    }

    /** 若 source[i] 是 `[=*[`，回傳等號數量；否則 -1。 */
    private static int countLongBracket(String source, int i) {
        int j = i + 1;
        int equals = 0;
        while (j < source.length() && source.charAt(j) == '=') {
            equals++;
            j++;
        }
        if (j < source.length() && source.charAt(j) == '[') {
            return equals;
        }
        return -1;
    }

    private static int skipLongBracket(String source, int start, int equals) {
        String closer = "]" + "=".repeat(equals) + "]";
        int end = source.indexOf(closer, start + equals + 2);
        return end < 0 ? source.length() : end + closer.length();
    }

    /** 跳過整個表建構子（含嵌套），回傳 `}` 之後的位置。 */
    private static int skipTableConstructor(String source, int start) {
        int depth = 0;
        int i = start;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (c == '"' || c == '\'') {
                i = skipQuoted(source, i, c);
                continue;
            }
            if (c == '-' && i + 1 < source.length() && source.charAt(i + 1) == '-') {
                i = skipComment(source, i);
                continue;
            }
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i + 1;
                }
            }
            i++;
        }
        return source.length();
    }
}
