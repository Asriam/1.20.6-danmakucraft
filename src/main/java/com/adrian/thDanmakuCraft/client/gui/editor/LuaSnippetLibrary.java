package com.adrian.thDanmakuCraft.client.gui.editor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 從 {@code data/thdanmakucraft/lua/snippet} 載入代碼樣板。
 *
 * <p>每個 {@code .lua} 就是一段可直接插入的程式碼，檔頭用標註描述它：</p>
 * <pre>
 * ---@label 定義子彈
 * ---@icon bulletdefine.png
 *
 * local bulletClass = core.defineClass()
 * ...
 * </pre>
 *
 * <p>分類 = 子目錄名稱（{@code bullet} / {@code object} / {@code laser} / {@code task}）；
 * 標註行不會被插入，插入的只有空行之後的程式碼，所以樣板庫可以直接在資源目錄裡增刪。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class LuaSnippetLibrary {

    /**
     * @param label       按鈕名稱
     * @param icon        {@code textures/gui/editor/icons} 下的檔名
     * @param code        要插入的程式碼（參數以 {@code ${name}} 當佔位符）
     * @param description {@code ---@description} 的說明（可空）
     * @param parameters  {@code ---@parameter} 宣告的參數（依宣告順序）
     */
    public record Entry(String label, String icon, String code, String description,
                        List<Parameter> parameters) {
        public boolean hasParameters() {
            return !this.parameters.isEmpty();
        }
    }

    /** 參數型別。 */
    public enum ParameterKind {
        /** 自由輸入（---@parameter）。 */
        TEXT,
        /** 布林開關（---@parameter_boolean）。 */
        BOOLEAN,
        /** 多選一（---@parameter_switch）：在表單裡點擊／左右鍵切換。 */
        SWITCH
    }

    /**
     * 樣板參數。
     * @param name         佔位符名稱（程式碼裡寫 {@code ${name}}）
     * @param label        對話框顯示的說明
     * @param defaultValue 預設值（可直接按 Enter 採用）
     * @param kind         參數型別
     * @param options      SWITCH 的可選值（其他型別為空）
     */
    public record Parameter(String name, String label, String defaultValue,
                            ParameterKind kind, List<String> options) {

        public boolean isBoolean() {
            return this.kind == ParameterKind.BOOLEAN;
        }

        public boolean isSwitch() {
            return this.kind == ParameterKind.SWITCH && !this.options.isEmpty();
        }
    }

    public static final String ROOT = "data/thdanmakucraft/lua/snippet";

    private static Map<String, List<Entry>> cache;

    private LuaSnippetLibrary() {
    }

    /** key = 分類目錄名稱（小寫），value = 該分類的樣板（依檔名排序）。 */
    public static synchronized Map<String, List<Entry>> byCategory() {
        if (cache != null) {
            return cache;
        }
        Map<String, List<Entry>> map = new LinkedHashMap<>();
        LuaScriptFiles.BuiltinRoot root = new LuaScriptFiles.BuiltinRoot("snippet", ROOT);
        for (String relative : LuaScriptFiles.filesUnder(root)) {
            String category = relative.contains("/")
                    ? relative.substring(0, relative.indexOf('/'))
                    : "";
            try {
                String content = LuaScriptFiles.readBuiltin(root, relative);
                map.computeIfAbsent(category.toLowerCase(Locale.ROOT), key -> new ArrayList<>())
                        .add(parse(relative, content));
            } catch (IOException ignored) {
                // 單一檔案壞掉不影響其他樣板
            }
        }
        cache = map;
        return cache;
    }

    /** 解析一個樣板檔：標頭標註 + 之後的程式碼。 */
    /**
     * 解析 {@code ---@parameter 格式}：
     * <pre>name: 說明 = 預設值</pre>
     * 說明與預設值都可省略。
     */
    public static Parameter parseParameter(String text) {
        return parseParameter(text, ParameterKind.TEXT);
    }

    public static Parameter parseParameter(String text, ParameterKind kind) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String value = "";
        int equal = text.indexOf('=');
        if (equal >= 0) {
            value = text.substring(equal + 1).trim();
            text = text.substring(0, equal).trim();
        }
        String name = text;
        String label = text;
        int colon = text.indexOf(':');
        if (colon >= 0) {
            name = text.substring(0, colon).trim();
            label = text.substring(colon + 1).trim();
        }
        if (name.isEmpty()) {
            return null;
        }
        if (label.isEmpty()) {
            label = name;
        }

        List<String> options = List.of();
        String defaultValue;
        switch (kind) {
            case BOOLEAN -> {
                defaultValue = value.equalsIgnoreCase("false") ? "false" : "true";
            }
            case SWITCH -> {
                options = parseOptions(value);
                defaultValue = options.isEmpty() ? "" : options.get(0);
            }
            default -> {
                defaultValue = unquote(value);
            }
        }
        return new Parameter(name, label, defaultValue, kind, options);
    }

    /** 解析 {@code {a, b, c}} 這種選項清單。 */
    static List<String> parseOptions(String text) {
        String body = text.trim();
        if (body.startsWith("{")) {
            body = body.substring(1);
        }
        if (body.endsWith("}")) {
            body = body.substring(0, body.length() - 1);
        }
        List<String> options = new ArrayList<>();
        for (String part : body.split(",")) {
            String option = unquote(part.trim());
            if (!option.isEmpty()) {
                options.add(option);
            }
        }
        return options;
    }

    private static String unquote(String text) {
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    public static Entry parse(String path, String content) {
        String label = null;
        String icon = null;
        String description = null;
        List<Parameter> parameters = new ArrayList<>();
        String[] lines = content.replace("\r\n", "\n").split("\n", -1);
        int index = 0;
        while (index < lines.length) {
            String line = lines[index].trim();
            if (line.startsWith("---@label ")) {
                label = line.substring("---@label ".length()).trim();
            } else if (line.startsWith("---@icon ")) {
                icon = line.substring("---@icon ".length()).trim();
            } else if (line.startsWith("---@description ")) {
                description = line.substring("---@description ".length()).trim();
            } else if (line.startsWith("---@parameter_switch ")) {
                Parameter parameter = parseParameter(
                        line.substring("---@parameter_switch ".length()).trim(), ParameterKind.SWITCH);
                if (parameter != null) {
                    parameters.add(parameter);
                }
            } else if (line.startsWith("---@parameter_boolean ")) {
                Parameter parameter = parseParameter(
                        line.substring("---@parameter_boolean ".length()).trim(), ParameterKind.BOOLEAN);
                if (parameter != null) {
                    parameters.add(parameter);
                }
            } else if (line.startsWith("---@parameter ")) {
                Parameter parameter = parseParameter(
                        line.substring("---@parameter ".length()).trim(), ParameterKind.TEXT);
                if (parameter != null) {
                    parameters.add(parameter);
                }
            } else if (line.isEmpty()) {
                index++;
                break;
            } else {
                break;
            }
            index++;
        }

        StringBuilder code = new StringBuilder();
        for (int i = index; i < lines.length; i++) {
            if (code.length() == 0 && lines[i].isEmpty()) {
                continue;
            }
            code.append(lines[i]);
            if (i < lines.length - 1) {
                code.append('\n');
            }
        }

        String fileName = path.substring(path.lastIndexOf('/') + 1);
        if (fileName.endsWith(".lua")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }
        if (label == null || label.isEmpty()) {
            label = fileName.replace('_', ' ');
        }
        if (icon == null || icon.isEmpty()) {
            icon = "code.png";
        }
        return new Entry(label, icon, code.toString(), description, List.copyOf(parameters));
    }
}
