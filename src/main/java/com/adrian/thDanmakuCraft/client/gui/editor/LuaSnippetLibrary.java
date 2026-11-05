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

    /** @param label 按鈕說明；{@code icon} 是 {@code textures/gui/editor/icons} 下的檔名 */
    public record Entry(String label, String icon, String code) {
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
    public static Entry parse(String path, String content) {
        String label = null;
        String icon = null;
        String[] lines = content.replace("\r\n", "\n").split("\n", -1);
        int index = 0;
        while (index < lines.length) {
            String line = lines[index].trim();
            if (line.startsWith("---@label ")) {
                label = line.substring("---@label ".length()).trim();
            } else if (line.startsWith("---@icon ")) {
                icon = line.substring("---@icon ".length()).trim();
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
        return new Entry(label, icon, code.toString());
    }
}
