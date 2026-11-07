package com.adrian.thDanmakuCraft.client.gui.editor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 計算「樣板圖示要畫在哪個欄位」。
 *
 * <pre>
 * local bullet = [圖示]self:createTHBullet(...)   -- 放在接收者前面
 * [圖示]self:setStyle("ball_huge")                -- 放在接收者前面（不是 :setStyle 前面）
 * [圖示]function object:onInit()                  -- function 定義放在 function 前面
 * </pre>
 */
@OnlyIn(Dist.CLIENT)
final class LuaSnippetAnchor {

    private LuaSnippetAnchor() {
    }

    /** 把位置往前推到接收者（self / object / math ...）的開頭。 */
    static int receiverStart(String line, int index) {
        int start = index;
        while (start > 0) {
            char c = line.charAt(start - 1);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == ':') {
                start--;
            } else {
                break;
            }
        }
        return start;
    }

    /** 圖示錨點：一般是接收者開頭；{@code function X:y()} 則放到 {@code function} 前面。 */
    static int snippetAnchor(String line, int index) {
        int start = receiverStart(line, index);
        int probe = start - 1;
        while (probe >= 0 && Character.isWhitespace(line.charAt(probe))) {
            probe--;
        }
        int wordEnd = probe + 1;
        int wordStart = wordEnd;
        while (wordStart > 0 && Character.isLetterOrDigit(line.charAt(wordStart - 1))) {
            wordStart--;
        }
        if (wordStart < wordEnd && line.substring(wordStart, wordEnd).equals("function")) {
            return wordStart;
        }
        return start;
    }

    /** 這個位置是否在程式碼裡（不在字串或註解中）。 */
    static boolean isCodeAt(String line, int index) {
        boolean inSingle = false;
        boolean inDouble = false;
        for (int i = 0; i < index; i++) {
            char c = line.charAt(i);
            if (c == '\\' && (inSingle || inDouble)) {
                i++;
                continue;
            }
            if (!inDouble && c == '\'') {
                inSingle = !inSingle;
            } else if (!inSingle && c == '"') {
                inDouble = !inDouble;
            } else if (!inSingle && !inDouble && c == '-' && i + 1 < index && line.charAt(i + 1) == '-') {
                return false;
            }
        }
        return !inSingle && !inDouble;
    }

    /**
     * 在 {@code line} 裡找 {@code keyword}，跳過字串／註解中的同名文字；
     * 回傳錨點欄位，找不到回傳 -1。
     */
    static int findAnchor(String line, String keyword) {
        int index = line.indexOf(keyword);
        while (index >= 0) {
            if (isCodeAt(line, index)) {
                return snippetAnchor(line, index);
            }
            index = line.indexOf(keyword, index + 1);
        }
        return -1;
    }
}
