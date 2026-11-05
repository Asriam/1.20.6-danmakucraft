package com.adrian.thDanmakuCraft.client.gui.editor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 樣板標記的解析、配對與「顯示字串」還原。
 *
 * <p>插入樣板時會加上帶索引的行內註解：</p>
 * <pre>
 * --[[@SnippetStart 3 bulletcreate.png ]] local bullet = ...
 * bullet:spawn() --[[@SnippetEnd 3]]
 * </pre>
 *
 * <p>規則：</p>
 * <ul>
 *   <li>只有 Start / End 的 index 相同（配對成功）時，才隱藏標記並在行號欄顯示圖示</li>
 *   <li>沒配對的標記視為普通文字照常顯示，避免半截標記讓整段程式碼消失</li>
 *   <li>index 讓「在樣板裡再插入樣板」時不會把內外兩層的 Start/End 配錯</li>
 *   <li>{@code --[[} 是「已結束的長註解」，所以標記後面的程式碼仍然是有效程式碼</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public final class LuaSnippetMarkers {

    /** 標記：{@code from} ~ {@code to} 是標記在原始行裡的範圍（to 不含）。 */
    public record Marker(boolean start, int index, String icon, int from, int to) {
    }

    /**
     * @param display  剝掉標記後要顯示的字串
     * @param toBuffer {@code toBuffer[displayIndex]} = 對應的原始字元索引；長度為 {@code display.length() + 1}
     */
    public record Mapped(String display, int[] toBuffer) {

        /** 原始欄位 → 顯示欄位（畫游標與選取用）。 */
        public int toDisplay(int bufferColumn) {
            for (int i = 0; i < this.toBuffer.length; i++) {
                if (this.toBuffer[i] >= bufferColumn) {
                    return i;
                }
            }
            return this.display.length();
        }
    }

    /** 產生用（兩條橫線：{@code --[[} 是長註解，關閉後程式碼仍然有效）。 */
    public static final String START_PREFIX = "--[[@SnippetStart ";
    public static final String END_PREFIX = "--[[@SnippetEnd";

    /** 相容舊格式（三條橫線、沒有 index）。 */
    private static final String[] START_TOKENS = { "---[[@SnippetStart ", START_PREFIX };
    private static final String[] END_TOKENS = { "---[[@SnippetEnd", END_PREFIX };
    private static final String CLOSE = "]]";

    private LuaSnippetMarkers() {
    }

    /** 這一行裡的所有標記（依出現順序）。 */
    public static List<Marker> markersOf(String line) {
        List<Marker> markers = new ArrayList<>();
        int i = 0;
        while (i < line.length()) {
            Marker marker = markerAt(line, i);
            if (marker != null) {
                markers.add(marker);
                i = marker.to();
            } else {
                i++;
            }
        }
        return markers;
    }

    private static Marker markerAt(String line, int position) {
        for (String token : START_TOKENS) {
            if (line.startsWith(token, position)) {
                int close = line.indexOf(CLOSE, position + token.length());
                if (close < 0) {
                    return null;
                }
                String body = line.substring(position + token.length(), close).trim();
                int index = -1;
                String icon = body;
                int space = body.indexOf(' ');
                if (space > 0 && isDigits(body.substring(0, space))) {
                    index = Integer.parseInt(body.substring(0, space));
                    icon = body.substring(space + 1).trim();
                } else if (isDigits(body)) {
                    index = Integer.parseInt(body);
                    icon = "";
                }
                return new Marker(true, index, icon, position, close + CLOSE.length());
            }
        }
        for (String token : END_TOKENS) {
            if (line.startsWith(token, position)) {
                int close = line.indexOf(CLOSE, position + token.length());
                if (close < 0) {
                    return null;
                }
                String body = line.substring(position + token.length(), close).trim();
                int index = isDigits(body) ? Integer.parseInt(body) : -1;
                return new Marker(false, index, "", position, close + CLOSE.length());
            }
        }
        return null;
    }

    private static boolean isDigits(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** 找出所有配對成功的 index（Start 必須出現在 End 之前）。 */
    public static Set<Integer> pairedIndices(List<String> lines) {
        Set<Integer> paired = new HashSet<>();
        Set<Integer> open = new HashSet<>();
        for (String line : lines) {
            for (Marker marker : markersOf(line)) {
                if (marker.index() < 0) {
                    continue;
                }
                if (marker.start()) {
                    open.add(marker.index());
                } else if (open.remove(marker.index())) {
                    paired.add(marker.index());
                }
            }
        }
        return paired;
    }

    /** 剝掉「已配對」的標記；未配對的留在顯示字串裡。 */
    public static Mapped strip(String line, Set<Integer> hiddenIndices) {
        List<Marker> markers = markersOf(line);
        if (markers.isEmpty()) {
            return plain(line);
        }
        StringBuilder display = new StringBuilder();
        int[] buffer = new int[line.length() + 1];
        int out = 0;
        int i = 0;
        while (i < line.length()) {
            Marker marker = null;
            for (Marker candidate : markers) {
                if (candidate.from() == i) {
                    marker = candidate;
                    break;
                }
            }
            if (marker != null && hiddenIndices.contains(marker.index())) {
                i = marker.to();
                continue;
            }
            buffer[out] = i;
            display.append(line.charAt(i));
            out++;
            i++;
        }
        buffer[out] = line.length();

        int[] toBuffer = new int[out + 1];
        System.arraycopy(buffer, 0, toBuffer, 0, out + 1);
        return new Mapped(display.toString(), toBuffer);
    }

    /** 這一行第一個「已配對」的 Start 標記，沒有就 null。 */
    public static Marker startMarker(String line, Set<Integer> paired) {
        for (Marker marker : markersOf(line)) {
            if (marker.start() && paired.contains(marker.index())) {
                return marker;
            }
        }
        return null;
    }

    /** 這一行任何 Start 標記（不論是否配對），用於保護欄位不被破壞。 */
    public static Marker anyStartMarker(String line) {
        for (Marker marker : markersOf(line)) {
            if (marker.start()) {
                return marker;
            }
        }
        return null;
    }

    /** 這一行第一個 End 標記，沒有就 null。 */
    public static Marker endMarker(String line) {
        for (Marker marker : markersOf(line)) {
            if (!marker.start()) {
                return marker;
            }
        }
        return null;
    }

    private static Mapped plain(String line) {
        int[] map = new int[line.length() + 1];
        for (int i = 0; i <= line.length(); i++) {
            map[i] = i;
        }
        return new Mapped(line, map);
    }
}
