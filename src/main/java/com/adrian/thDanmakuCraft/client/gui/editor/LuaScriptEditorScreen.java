package com.adrian.thDanmakuCraft.client.gui.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 內建 Lua 腳本編輯器。
 *
 * <p>功能：多行編輯、行號、語法上色、選取（滑鼠拖曳 / Shift+方向鍵）、
 * 捲動、剪貼簿、存檔。檔案位置：{@code config/thdanmakucraft/scripts/<檔名>.lua}。</p>
 *
 * <p>快捷鍵：Ctrl+S 儲存、Ctrl+A 全選、Ctrl+C/X/V 剪貼簿、Tab 縮排、
 * Home/End、PgUp/PgDn、Ctrl+Left/Right 以詞移動。</p>
 */
@OnlyIn(Dist.CLIENT)
public class LuaScriptEditorScreen extends Screen {

    // 標題LOGO
    private static final String EDITOR_TITLE = "Danmaku Script Editor";
    // ---- 版面 ----
    private static final int LINE_HEIGHT = 10;
    private static final int TOP_BAR_HEIGHT = 20;
    /** 工作欄每一列的高度（分頁太多時往下增列）。 */
    private static final int TASK_BAR_ROW_HEIGHT = 14;
    private static final int BROWSER_TOP = TOP_BAR_HEIGHT;
    private static final int STATUS_HEIGHT = 14;
    private static final int PADDING = 4;

    // ---- 顏色 ----
    private static final int BG = 0xF0141620;
    private static final int BAR_BG = 0xFF1E2230;
    // EmmyLua 預設（Darcula）配色
    private static final int TEXT = 0xFFA9B7C6;
    private static final int KEYWORD = 0xFFCC7832;
    private static final int STRING = 0xFF6A8759;
    private static final int NUMBER = 0xFF6897BB;
    private static final int COMMENT = 0xFF808080;
    private static final int ANNOTATION = 0xFFBBB529;
    private static final int FUNCTION = 0xFFFFC66D;
    private static final int LINE_NO = 0xFF5A6478;
    private static final int CURRENT_LINE = 0x30FFFFFF;
    private static final int SELECTION = 0x553B82F6;
    private static final int CURSOR = 0xFFFFFFFF;
    private static final int STATUS_TEXT = 0xFF9AA4BC;

    private static final Set<String> KEYWORDS = Set.of(
            "and", "break", "do", "else", "elseif", "end", "false", "for", "function", "goto",
            "if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "true",
            "until", "while");

    private static final String TEMPLATE =  """
            """;

    // ---- 檔案 ----
    private String fileName;
    private Path filePath;

    // ---- 檔案瀏覽 ----
    private static final int BROWSER_WIDTH = 156;
    /** 檔案管理器預設就打開。 */
    private boolean browserOpen = true;
    /** 焦點是否在檔案管理器（決定 Delete 鍵要刪檔案還是刪字元）。 */
    private boolean browserFocused;
    /** 檔案管理器的一列：header 非 null 代表分類標題。 */
    private record BrowserRow(String header, LuaScriptFiles.Entry entry, String group,
                              String directory, int depth, String label) {

        /**
         * 分類標題（腳本 / 內建 API / 內建工具庫）。
         * 注意：資料夾列也把標籤放在 header，所以必須排除有 directory 的列，
         * 否則資料夾會被當成分類標題（不能選、不能收合）。
         */
        boolean isSection() {
            return this.header != null && this.directory == null;
        }

        /** 資料夾列。 */
        boolean isFolder() {
            return this.directory != null;
        }

        boolean isSelectable() {
            return this.entry != null || this.isFolder();
        }

        /** 分類標題（內建來源）與資料夾都可以收合。 */
        boolean isCollapsible() {
            return this.isSection() ? this.group != null : this.isFolder();
        }

        /** 收合狀態的鍵。 */
        String collapseKey() {
            return this.isFolder() ? "dir:" + this.directory : this.group;
        }

        /** 選取狀態的鍵（重新整理後用來還原選取）。 */
        String selectionKey() {
            if (this.isFolder()) {
                return "dir:" + this.directory;
            }
            return this.entry == null ? "" : "file:" + this.entry.path();
        }
    }

    /** 分成「腳本」與「內建 API」兩個分類，標題列插在中間。 */
    private List<BrowserRow> browserRows = new ArrayList<>();
    /** 已展開的分類標題（內建 API / 內建工具庫 / Lua 5.3 標準庫），預設收合。 */
    private final Set<String> expandedSections = new HashSet<>();
    /** 工作欄：使用者用 Ctrl+T 加入的檔案（顯示路徑，內建檔案帶 apidoc/ 或 lib/ 前綴）。 */
    private final List<String> taskBar = new ArrayList<>();
    /** 已展開的資料夾（預設收合，所以記錄的是「展開」而不是「收合」）。 */
    private final Set<String> expandedDirs = new HashSet<>();
    /** 開檔後要自動展開的路徑（只生效一次，避免使用者手動收合又被拉開）。 */
    private String revealPath;
    /** 檔案管理器的第一列（獨立於選取項，滑鼠滾輪／拖曳捲軸時改變）。 */
    private int browserScroll;
    private int browserSelected;
    /** 使用者刻意取消選取（點「腳本」分類標題）；重新整理時保持 -1，讓新增落在根目錄。 */
    private boolean noSelection;
    /**
     * 每個檔案的編輯緩衝：切換檔案時整組換掉，切回來就恢復原本的內容、游標、選取與 undo 堆疊。
     */
    private static final class Buffer {
        List<StringBuilder> lines = new ArrayList<>();
        Deque<Snapshot> undo = new ArrayDeque<>();
        Deque<Snapshot> redo = new ArrayDeque<>();
        int cursorLine;
        int cursorColumn;
        int selectionLine = -1;
        int selectionColumn;
        int scroll;
        boolean dirty;
        boolean readOnly;
    }

    /** 目前緩衝裡解析出來的自訂符號（local / 全域 / 參數…），依內容雜湊快取。 */
    private int symbolCacheHash = Integer.MIN_VALUE;
    private List<LuaSymbolIndex.Symbol> symbolCache = List.of();

    /** 已開啟過的檔案緩衝（key = 顯示路徑）。 */
    private final Map<String, Buffer> buffers = new HashMap<>();

    /** 是否已嘗試補做 init()，避免每幀重試。 */
    private boolean reinitRequested;
    private boolean dirty;

    /** 目前開的是不是 jar 內的唯讀 apidoc。 */
    private boolean readOnly;

    // ---- 捲軸 ----
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int DRAG_NONE = -1;
    private static final int DRAG_EDITOR = 0;
    private static final int DRAG_BROWSER = 1;
    private int scrollbarDrag = DRAG_NONE;

    // ---- 拖曳檔案到資料夾 ----
    /** 按下滑鼠時記下的檔案路徑（還沒真的開始拖）。 */
    private String dragCandidate;
    /** 是否已經進入拖曳狀態（移動超過門檻）。 */
    private boolean draggingFile;
    /** 拖曳中的檔案是不是唯讀的內建檔案（內建檔只能插入 doFile，不能搬移）。 */
    private boolean dragBuiltin;
    private double dragStartX;
    private double dragStartY;
    /** 目前滑鼠所在的放置目標資料夾（null = 不可放置）。 */
    private String dropTargetDir;
    /** 搬移後要選取的列（refresh 後還原用）。 */
    private String pendingSelectionKey;
    /** 檔案管理器雙擊偵測。 */
    private String lastClickedPath = "";
    private long lastClickAt;

    /** 存檔時檢查到的語法錯誤行（1 起算，0 = 沒有錯誤）。 */
    private int syntaxErrorLine;
    private String syntaxErrorMessage = "";


    private enum Dialog { NONE, NEW_FILE, NEW_FOLDER, RENAME, DELETE, PARAMETERS }

    private Dialog dialog = Dialog.NONE;

    /** 上一幀滑鼠位置（給頂部按鈕的 hover 用）。 */
    private int lastMouseX;
    private int lastMouseY;
    private int fileButtonX;
    /** 頂部工具列：圖示按鈕（| 分隔線）。 */
    private enum TopAction {
        OPEN("open.png", "檔案", "Ctrl+O"),
        SAVE("save.png", "保存", "Ctrl+S"),
        SAVE_ALL("save_all.png", "保存全部", "Ctrl+Shift+S"),
        SEPARATOR1(null, null, null),
        UNDO("undo.png", "復原", "Ctrl+Z"),
        REDO("redo.png", "重做", "Ctrl+Y"),
        SEPARATOR2(null, null, null),
        CUT("cut.png", "剪下", "Ctrl+X"),
        COPY("copy.png", "複製", "Ctrl+C"),
        PASTE("paste.png", "貼上", "Ctrl+V");

        private final String icon;
        private final String label;
        private final String hotkey;

        TopAction(String icon, String label, String hotkey) {
            this.icon = icon;
            this.label = label;
            this.hotkey = hotkey;
        }

        boolean isSeparator() {
            return this.icon == null;
        }

        ResourceLocation texture() {
            return ICONS.get(this);
        }
    }

    private static final int ICON_SIZE = 16;
    private static final Map<TopAction, ResourceLocation> ICONS = new java.util.EnumMap<>(TopAction.class);

    static {
        for (TopAction action : TopAction.values()) {
            if (!action.isSeparator()) {
                ICONS.put(action, ResourceLocation.fromNamespaceAndPath("thdanmakucraft",
                        "textures/gui/editor/icons/" + action.icon));
            }
        }
    }

    /** 檔案管理器的列高（比文字行高一些，才放得下 12px 圖示）。 */
    private static final int BROWSER_ROW_HEIGHT = LINE_HEIGHT;
    private static final int BROWSER_ICON_SIZE = 10;
    private static final ResourceLocation ICON_FOLDER = ResourceLocation.fromNamespaceAndPath(
            "thdanmakucraft", "textures/gui/editor/icons/folder.png");
    private static final ResourceLocation ICON_CODE = ResourceLocation.fromNamespaceAndPath(
            "thdanmakucraft", "textures/gui/editor/icons/code.png");
    private static final ResourceLocation ICON_CODE_MAIN = ResourceLocation.fromNamespaceAndPath(
            "thdanmakucraft", "textures/gui/editor/icons/code_main.png");

    // ---- 代碼工具列（分類 + 樣板按鈕） ----
    private static final int SNIPPET_CATEGORY_HEIGHT = 11;
    private static final int SNIPPET_TOOL_HEIGHT = 16;
    private static final int SNIPPET_ICON = 14;
    /** 樣板標記的索引配對快取（內容沒變就不重算）。 */
    private int snippetPairHash = Integer.MIN_VALUE;
    private Set<Integer> snippetPairs = Set.of();

    /** 行號欄圖示快取（key = 圖示檔名）。 */
    private final Map<String, ResourceLocation> gutterIcons = new HashMap<>();

    /** 這一行是不是樣板標記；是就回傳 Start 標記，否則 null。 */
    private static LuaSnippetMarkers.Marker snippetMarkerOf(String line) {
        return LuaSnippetMarkers.anyStartMarker(line);
    }

    private static boolean isSnippetEndLine(String line) {
        return LuaSnippetMarkers.endMarker(line) != null;
    }

    /** 目前內容裡「Start/End 配對成功」的樣板索引。 */
    private Set<Integer> pairedSnippetIndices() {
        int hash = this.content().hashCode();
        if (hash != this.snippetPairHash) {
            this.snippetPairHash = hash;
            List<String> all = new ArrayList<>(this.lines.size());
            for (StringBuilder line : this.lines) {
                all.add(line.toString());
            }
            this.snippetPairs = LuaSnippetMarkers.pairedIndices(all);
        }
        return this.snippetPairs;
    }

    /** 剝掉已配對的樣板標記後，實際要顯示的字串。 */
    private LuaSnippetMarkers.Mapped displayOf(String line) {
        return LuaSnippetMarkers.strip(line, this.pairedSnippetIndices());
    }

    /** 行號欄的左緣（圖示畫在這裡）。 */
    private int gutterLeft() {
        return taskBarLeft() - taskBarOffset;
    }

    /** 下一個可用的樣板索引（目前內容裡最大的 +1）。 */
    private int nextSnippetIndex() {
        int max = 0;
        for (StringBuilder line : this.lines) {
            for (LuaSnippetMarkers.Marker marker : LuaSnippetMarkers.markersOf(line.toString())) {
                max = Math.max(max, marker.index());
            }
        }
        return max + 1;
    }

    /** 找出某個 index 的樣板區塊（Start 行/欄 ~ End 行/欄）；找不到回傳 null。 */
    private int[] snippetBlockFor(int index, int endLine, LuaSnippetMarkers.Marker endMarker) {
        for (int line = endLine; line >= 0; line--) {
            for (LuaSnippetMarkers.Marker marker : LuaSnippetMarkers.markersOf(
                    this.lines.get(line).toString())) {
                if (!marker.start()) {
                    continue;
                }
                boolean sameIndex = index >= 0 && marker.index() == index;
                boolean legacy = index < 0 && marker.index() < 0 && line < endLine;
                if (sameIndex || legacy) {
                    return new int[]{line, marker.from(), endLine, endMarker.to()};
                }
            }
        }
        return null;
    }

    private void deleteSnippetBlock(int[] block) {
        this.recordUndo(false);
        this.selectionLine = block[0];
        this.selectionColumn = block[1];
        this.cursorLine = block[2];
        this.cursorColumn = block[3];
        this.deleteSelection();
        this.setStatus("已刪除樣板區塊", STATUS_TEXT);
    }

    /**
     * 刪除時遇到樣板標記：
     * Start 標記直接跳過（不讓它被破壞），End 標記則刪掉整段區塊。
     * @return true 表示已處理（呼叫端不要再往下刪）
     */
    private boolean handleSnippetMarkerDelete(boolean backspace) {
        if (this.cursorLine < 0 || this.cursorLine >= this.lines.size()) {
            return false;
        }
        String line = this.lines.get(this.cursorLine).toString();
        int column = Math.min(this.cursorColumn, line.length());
        for (LuaSnippetMarkers.Marker marker : LuaSnippetMarkers.markersOf(line)) {
            boolean inside = backspace
                    ? column > marker.from() && column <= marker.to()
                    : column >= marker.from() && column < marker.to();
            if (!inside) {
                continue;
            }
            if (marker.start()) {
                this.cursorColumn = backspace ? marker.from() : marker.to();
                this.setStatus("樣板標記不可編輯（要先刪掉整段區塊）", LINE_NO);
                return true;
            }
            int[] block = this.snippetBlockFor(marker.index(), this.cursorLine, marker);
            if (block != null) {
                this.deleteSnippetBlock(block);
            }
            return true;
        }
        return false;
    }

    /** 選取整個樣板區塊（Start 到 End，含兩端）。 */
    private void selectSnippetBlock(int startLine) {
        String line = this.lines.get(startLine).toString();
        LuaSnippetMarkers.Marker start = LuaSnippetMarkers.anyStartMarker(line);
        int index = start == null ? -1 : start.index();
        int endLine = startLine;
        if (index >= 0) {
            for (int i = startLine; i < this.lines.size(); i++) {
                LuaSnippetMarkers.Marker end = LuaSnippetMarkers.endMarker(this.lines.get(i).toString());
                if (end != null && end.index() == index) {
                    endLine = i;
                    break;
                }
            }
        } else {
            for (int i = startLine + 1; i < this.lines.size(); i++) {
                LuaSnippetMarkers.Marker end = LuaSnippetMarkers.endMarker(this.lines.get(i).toString());
                if (end != null) {
                    endLine = i;
                    break;
                }
            }
        }
        this.selectionLine = startLine;
        this.selectionColumn = 0;
        this.cursorLine = endLine;
        this.cursorColumn = this.lines.get(endLine).length();
        this.ensureCursorVisible();
        this.setStatus("已選取樣板區塊（第 " + (startLine + 1) + " ~ " + (endLine + 1) + " 行）",
                STATUS_TEXT);
    }

    /** 把圖示檔名轉成 ResourceLocation（有快取）。 */
    private ResourceLocation gutterIcon(String file) {
        return this.gutterIcons.computeIfAbsent(file, key -> ResourceLocation.fromNamespaceAndPath(
                "thdanmakucraft", "textures/gui/editor/icons/" + key));
    }

    /** 參數表單每一列的高度。 */
    private static final int PARAM_ROW_HEIGHT = LINE_HEIGHT + 8;

    private enum SnippetCategory {
        BULLET("Bullet"), OBJECT("Object"), LASER("Laser"), TASK("Task"), SPELLCARD("SpellCard");

        private final String label;

        SnippetCategory(String label) {
            this.label = label;
        }
    }

    private record Snippet(String label, ResourceLocation icon, String iconFile, String code,
                           String description, List<LuaSnippetLibrary.Parameter> parameters) {
        boolean hasParameters() {
            return !this.parameters.isEmpty();
        }
    }

    private static ResourceLocation icon(String name) {
        return ResourceLocation.fromNamespaceAndPath("thdanmakucraft",
                "textures/gui/editor/icons/" + name);
    }

    private Map<SnippetCategory, List<Snippet>> snippetCache;

    /** 從 data/thdanmakucraft/lua/snippet 載入樣板（第一次用到時載入）。 */
    private List<Snippet> snippetsFor(SnippetCategory category) {
        if (this.snippetCache == null) {
            Map<SnippetCategory, List<Snippet>> built = new java.util.EnumMap<>(SnippetCategory.class);
            for (SnippetCategory each : SnippetCategory.values()) {
                List<Snippet> list = new ArrayList<>();
                for (LuaSnippetLibrary.Entry entry : LuaSnippetLibrary.byCategory()
                        .getOrDefault(each.name().toLowerCase(java.util.Locale.ROOT), List.of())) {
                    list.add(new Snippet(entry.label(), icon(entry.icon()), entry.icon(), entry.code(),
                            entry.description(), entry.parameters()));
                }
                built.put(each, List.copyOf(list));
            }
            this.snippetCache = built;
        }
        return this.snippetCache.getOrDefault(category, List.of());
    }

    private SnippetCategory snippetCategory = SnippetCategory.BULLET;
    private final int[] categoryX = new int[SnippetCategory.values().length];
    private final int[] categoryW = new int[SnippetCategory.values().length];
    private final int[] snippetX = new int[32];
    private int snippetCount;
    private int hoveredSnippet = -1;
    private long hoverSnippetSince;

    // ---- 樣板參數輸入 ----
    private Snippet pendingSnippet;
    private List<LuaSnippetLibrary.Parameter> pendingParams = List.of();
    private int pendingFocus;

    // ---- 參數下拉選單（parameter_switch） ----
    private boolean switchDropdownOpen;
    private int switchDropdownRow = -1;
    private int switchDropdownScroll;
    private int switchDropdownHighlight;
    private boolean switchScrollbarDragging;

    // ---- 參數欄位的文字選取 / 貼上 ----
    private int fieldSelectionAnchor = -1;
    private int fieldSelectionCaret = -1;
    private boolean fieldSelecting;
    /** 每個參數目前輸入的值（與 pendingParams 同索引）。 */
    private final List<String> pendingFieldValues = new ArrayList<>();

    private final int[] actionX = new int[TopAction.values().length];
    private final int[] actionW = new int[TopAction.values().length];
    private int hoveredAction = -1;
    private long hoverActionSince;

    // ---- 工作欄多列版面 ----
    private record TaskRect(int x, int y, int width) {
    }

    private List<TaskRect> taskLayout = List.of();
    private int taskRowCount = 1;
    private long layoutFrame = -1;
    private long frameId;
    private int fileButtonW;
    private String dialogInput = "";
    /** 對話框開啟時的原始檔名（改名／刪除的來源，不受輸入內容影響）。 */
    private String dialogSource = "";

    // ---- 編輯狀態 ----
    private List<StringBuilder> lines = new ArrayList<>();
    private int cursorLine;
    private int cursorColumn;
    private int selectionLine = -1;
    private int selectionColumn;
    /** 滑鼠拖曳選取文字。 */
    private boolean textSelecting;
    private int textAnchorLine;
    private int textAnchorColumn;
    private int scroll;
    private String status = "";
    private int statusColor = STATUS_TEXT;

    // ---- 復原／重做 ----
    private static final int MAX_UNDO = 200;
    private static final long TYPING_COALESCE_MS = 600L;
    private Deque<Snapshot> undoStack = new ArrayDeque<>();
    private Deque<Snapshot> redoStack = new ArrayDeque<>();
    private long lastUndoAt;

    // ---- 自動完成（來源：data/thdanmakucraft/lua/apidoc） ----
    private static final int COMPLETION_ROWS = 8;
    private boolean completionActive;
    private int completionStartColumn;
    private int completionSelected;
    private List<LuaApiIndex.Entry> completionItems = List.of();
    private boolean completionShowAll;
    /** 觸發補全時的接收者（`object:` 的 object），用來只列出該類別成員。 */
    private String completionReceiver = "";

    public LuaScriptEditorScreen(String fileName) {
        super(Component.literal("Lua Script Editor"));
        this.openFile(LuaScriptFiles.normalizeName(fileName));
        this.refreshFileList();
    }

    // ------------------------------------------------------------------ 檔案

    private static Path scriptsDir() {
        return LuaScriptFiles.scriptsDir();
    }


    private void openFile(String name) {
        this.switchTo(name, false);
    }

    /** 依檔案管理器的一列開檔（內建文件走 classpath、唯讀）。 */
    private void openEntry(LuaScriptFiles.Entry entry) {
        if (entry.builtin()) {
            this.openBuiltin(entry.path());
        } else {
            this.openFile(entry.path());
        }
    }

    /** 載入 jar 內的 apidoc：只能閱讀，Ctrl+S 會被拒絕。 */
    private void openBuiltin(String displayPath) {
        this.switchTo(displayPath, true);
    }

    /** 把目前的編輯狀態存回緩衝（切換檔案前呼叫）。 */
    private void stash() {
        if (this.fileName == null) {
            return;
        }
        Buffer buffer = this.buffers.computeIfAbsent(this.fileName, key -> new Buffer());
        buffer.lines = this.lines;
        buffer.undo = this.undoStack;
        buffer.redo = this.redoStack;
        buffer.cursorLine = this.cursorLine;
        buffer.cursorColumn = this.cursorColumn;
        buffer.selectionLine = this.selectionLine;
        buffer.selectionColumn = this.selectionColumn;
        buffer.scroll = this.scroll;
        buffer.dirty = this.dirty;
        buffer.readOnly = this.readOnly;
    }

    /** 切換到某個檔案；有緩衝就恢復原狀，沒有才從磁碟／jar 載入。 */
    private void switchTo(String path, boolean builtin) {
        if (path != null && path.equals(this.fileName) && !this.lines.isEmpty()) {
            this.noSelection = false;
            this.revealPath = path;
            return;
        }
        this.stash();

        Buffer buffer = this.buffers.get(path);
        if (buffer == null) {
            buffer = new Buffer();
            buffer.readOnly = builtin;
            String content = null;
            try {
                if (builtin) {
                    content = LuaScriptFiles.readBuiltin(path);
                } else {
                    java.nio.file.Path file = scriptsDir().resolve(path);
                    content = Files.exists(file) ? LuaScriptFiles.read(file) : TEMPLATE;
                }
            } catch (IOException exception) {
                this.setStatus("讀取失敗：" + exception.getMessage(), 0xFFFF6B6B);
                content = TEMPLATE;
            }
            for (String line : content.split("\n", -1)) {
                buffer.lines.add(new StringBuilder(line));
            }
            if (buffer.lines.isEmpty()) {
                buffer.lines.add(new StringBuilder());
            }
            this.buffers.put(path, buffer);
        }

        this.fileName = path;
        this.filePath = builtin ? null : scriptsDir().resolve(path);
        this.readOnly = buffer.readOnly;
        this.noSelection = false;
        this.revealPath = path;
        this.closeCompletion();
        this.clearSyntaxError();
        this.lines = buffer.lines;
        this.undoStack = buffer.undo;
        this.redoStack = buffer.redo;
        this.cursorLine = buffer.cursorLine;
        this.cursorColumn = buffer.cursorColumn;
        this.selectionLine = buffer.selectionLine;
        this.selectionColumn = buffer.selectionColumn;
        this.scroll = buffer.scroll;
        this.dirty = buffer.dirty;
        this.ensureCursorVisible();
        if (buffer.dirty) {
            this.setStatus(path + "（未儲存的變更已保留）", 0xFFFFD479);
        } else {
            this.setStatus((builtin ? "內建文件（唯讀）：" : "已載入 ") + path, STATUS_TEXT);
        }
    }

    /** 某個檔案是否有未儲存的變更（含目前未切換出去的緩衝）。 */
    private boolean isDirty(String path) {
        if (path != null && path.equals(this.fileName)) {
            return this.dirty;
        }
        Buffer buffer = this.buffers.get(path);
        return buffer != null && buffer.dirty;
    }

    /** 儲存所有有變更的緩衝。 */
    private void saveAll() {
        this.stash();
        int saved = 0;
        int failed = 0;
        for (Map.Entry<String, Buffer> entry : this.buffers.entrySet()) {
            Buffer buffer = entry.getValue();
            if (buffer.readOnly || !buffer.dirty) {
                continue;
            }
            try {
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < buffer.lines.size(); i++) {
                    if (i > 0) {
                        text.append('\n');
                    }
                    text.append(buffer.lines.get(i));
                }
                LuaScriptFiles.write(scriptsDir().resolve(entry.getKey()), text.toString());
                buffer.dirty = false;
                saved++;
            } catch (IOException exception) {
                failed++;
            }
        }
        this.dirty = false;
        this.refreshFileList();
        if (failed > 0) {
            this.setStatus("已儲存 " + saved + " 個檔案，" + failed + " 個失敗", 0xFFFF6B6B);
        } else if (saved == 0) {
            this.setStatus("沒有需要儲存的變更", LINE_NO);
        } else {
            this.setStatus("已儲存全部 " + saved + " 個檔案", 0xFF7BD88F);
        }
    }

    private void save() {
        if (this.readOnly) {
            this.setStatus("內建 API 文件是唯讀的，不能儲存", 0xFFFFD479);
            return;
        }
        try {
            LuaScriptFiles.write(this.filePath, this.content());
            this.dirty = false;
            this.refreshFileList();

            // 存檔時語法檢查：只解析不執行，錯誤會標紅並跳到該行
            LuaSyntaxChecker.Result check = LuaSyntaxChecker.check(this.content(), this.fileName);
            this.applySyntaxResult(check);
            if (check.ok()) {
                this.setStatus("已儲存 " + this.filePath.getFileName() + "（重新載入：/refresh_lua_lib）",
                        0xFF7BD88F);
            } else {
                this.setStatus("第 " + this.syntaxErrorLine + " 行語法錯誤：" + this.syntaxErrorMessage,
                        0xFFFF6B6B);
            }
        } catch (IOException exception) {
            this.setStatus("儲存失敗：" + exception.getMessage(), 0xFFFF6B6B);
        }
    }

    /** 目前檔案相對於 scripts 目錄的路徑（給清單標示用）。 */
    private String relativeName() {
        return this.fileName.replace('\\', '/');
    }

    /** 重新掃描：內建資料夾在最上，腳本在最後。 */
    /** 重新掃描：內建資料夾在最上，腳本（含子資料夾）在最後。 */
    /** 重新掃描：內建資料夾在最上，腳本（含子資料夾）在最後。 */
    private void refreshFileList() {
        if (this.revealPath != null) {
            this.reveal(this.revealPath);
            this.revealPath = null;
        }
        String previousKey = this.pendingSelectionKey != null ? this.pendingSelectionKey : this.selectedKey();
        this.pendingSelectionKey = null;

        List<LuaScriptFiles.Entry> scripts = new ArrayList<>();
        Map<String, List<LuaScriptFiles.Entry>> byRoot = new LinkedHashMap<>();
        for (LuaScriptFiles.Entry entry : LuaScriptFiles.listAll()) {
            if (entry.builtin()) {
                byRoot.computeIfAbsent(entry.category(), key -> new ArrayList<>()).add(entry);
            } else {
                scripts.add(entry);
            }
        }

        List<BrowserRow> rows = new ArrayList<>();
        for (LuaScriptFiles.BuiltinRoot root : LuaScriptFiles.BUILTIN_ROOTS) {
            List<LuaScriptFiles.Entry> entries = byRoot.getOrDefault(root.name(), List.of());
            boolean expanded = this.expandedSections.contains(root.name());
            String marker = expanded ? "▼ " : "▶ ";
            rows.add(new BrowserRow(marker + this.builtinTitle(root.name()) + " (" + entries.size()
                    + ") · 唯讀", null, root.name(), null, 0, null));
            if (expanded) {
                this.addTree(rows, entries, root.name() + "/", "dir:" + root.name() + "/", 1, List.of());
            }
        }

        rows.add(new BrowserRow("脚本 (" + scripts.size() + ")", null, null, null, 0, null));
        this.addTree(rows, scripts, "", "dir:", 1, LuaScriptFiles.listDirs(LuaScriptFiles.scriptsDir()));

        this.browserRows = rows;
        this.restoreSelection(previousKey);
    }

    /** 展開某個檔案的所有上層資料夾，讓它出現在清單裡（資料夾鍵含 apidoc/ 這類前綴）。 */
    private void reveal(String path) {
        if (path == null) {
            return;
        }
        String normalized = path.replace('\\', '/');
        for (LuaScriptFiles.BuiltinRoot root : LuaScriptFiles.BUILTIN_ROOTS) {
            if (normalized.equals(root.name()) || normalized.startsWith(root.name() + "/")) {
                this.expandedSections.add(root.name());
                break;
            }
        }
        int slash = normalized.indexOf('/');
        while (slash > 0) {
            this.expandedDirs.add(normalized.substring(0, slash));
            slash = normalized.indexOf('/', slash + 1);
        }
    }

    private String selectedKey() {
        if (this.browserSelected < 0 || this.browserSelected >= this.browserRows.size()) {
            return "";
        }
        return this.browserRows.get(this.browserSelected).selectionKey();
    }

    private void restoreSelection(String key) {
        if (this.noSelection && (key == null || key.isEmpty())) {
            this.browserSelected = -1;
            return;
        }
        if (key != null && !key.isEmpty()) {
            for (int i = 0; i < this.browserRows.size(); i++) {
                if (key.equals(this.browserRows.get(i).selectionKey())) {
                    this.browserSelected = i;
                    return;
                }
            }
        }
        this.browserSelected = this.firstSelectableRow();
    }

    /**
     * 把檔案清單建成樹：先資料夾（遞迴）再檔案，全部依名稱排序。
     * @param pathPrefix    要從 entry.path() 去掉的前綴（內建來源是 {@code apidoc/}）
     * @param collapsePrefix 收合鍵前綴（{@code dir:} 或 {@code dir:apidoc/}）
     * @param extraDirs     沒有檔案的資料夾（腳本目錄才需要）
     */
    private void addTree(List<BrowserRow> rows, List<LuaScriptFiles.Entry> entries,
                         String pathPrefix, String collapsePrefix, int depth,
                         List<String> extraDirs) {
        Map<String, List<String>> childDirs = new LinkedHashMap<>();
        Map<String, List<String>> childNames = new LinkedHashMap<>();
        Map<String, List<LuaScriptFiles.Entry>> childFiles = new LinkedHashMap<>();

        List<String> dirs = new ArrayList<>(extraDirs);
        for (LuaScriptFiles.Entry entry : entries) {
            String relative = entry.path().substring(Math.min(pathPrefix.length(), entry.path().length()));
            int slash = relative.lastIndexOf('/');
            if (slash > 0) {
                String dir = relative.substring(0, slash);
                // 補上所有上層目錄
                while (!dir.isEmpty() && !dirs.contains(dir)) {
                    dirs.add(dir);
                    int up = dir.lastIndexOf('/');
                    dir = up > 0 ? dir.substring(0, up) : "";
                }
            }
            String parent = slash > 0 ? relative.substring(0, slash) : "";
            childFiles.computeIfAbsent(parent, key -> new ArrayList<>()).add(entry);
            childNames.computeIfAbsent(parent, key -> new ArrayList<>())
                    .add(relative.substring(slash + 1));
        }
        for (String dir : dirs) {
            int slash = dir.lastIndexOf('/');
            String parent = slash > 0 ? dir.substring(0, slash) : "";
            childDirs.computeIfAbsent(parent, key -> new ArrayList<>()).add(dir);
        }
        for (List<String> list : childDirs.values()) {
            list.sort(String::compareToIgnoreCase);
        }
        this.addNode(rows, "", pathPrefix, collapsePrefix, childDirs, childFiles, childNames, depth);
    }

    private void addNode(List<BrowserRow> rows, String parentDir, String pathPrefix, String collapsePrefix,
                         Map<String, List<String>> childDirs,
                         Map<String, List<LuaScriptFiles.Entry>> childFiles,
                         Map<String, List<String>> childNames, int depth) {
        for (String dir : childDirs.getOrDefault(parentDir, List.of())) {
            String relative = dir.substring(parentDir.isEmpty() ? 0 : parentDir.length() + 1);
            String full = pathPrefix + dir;
            boolean expanded = this.expandedDirs.contains(full);
            String marker = expanded ? "\u25BC " : "\u25B6 ";
            rows.add(new BrowserRow(marker + relative + "/", null, null, full, depth, null));
            if (expanded) {
                this.addNode(rows, dir, pathPrefix, collapsePrefix, childDirs, childFiles, childNames, depth + 1);
            }
        }
        List<LuaScriptFiles.Entry> files = childFiles.getOrDefault(parentDir, List.of());
        List<String> names = childNames.getOrDefault(parentDir, List.of());
        for (int i = 0; i < files.size(); i++) {
            rows.add(new BrowserRow(null, files.get(i), null, null, depth, names.get(i)));
        }
    }

    /** 以樹狀方式把腳本與資料夾塞進列清單。 */




    /** 分類標題預設收合，所以這裡切換的是「展開」狀態。 */
    private void toggleGroup(String group) {
        if (!this.expandedSections.remove(group)) {
            this.expandedSections.add(group);
        }
        this.refreshFileList();
    }

    /** 資料夾預設收合，所以這裡切換的是「展開」狀態。 */
    private void toggleFolder(String directory) {
        if (!this.expandedDirs.remove(directory)) {
            this.expandedDirs.add(directory);
        }
        this.refreshFileList();
    }

    /** 游標預設停在目前開啟的檔案，找不到就停在第一個可選列。 */
    private int firstSelectableRow() {
        String current = this.relativeName();
        for (int i = 0; i < this.browserRows.size(); i++) {
            BrowserRow row = this.browserRows.get(i);
            if (row.entry() != null && row.entry().path().equals(current)) {
                return i;
            }
        }
        for (int i = 0; i < this.browserRows.size(); i++) {
            if (this.browserRows.get(i).isSelectable()) {
                return i;
            }
        }
        return 0;
    }

    /** 內建來源（apidoc / lib）底下的路徑一律唯讀。 */
    private boolean isBuiltinPath(String path) {
        if (path == null) {
            return false;
        }
        for (LuaScriptFiles.BuiltinRoot root : LuaScriptFiles.BUILTIN_ROOTS) {
            if (path.equals(root.name()) || path.startsWith(root.name() + "/")) {
                return true;
            }
        }
        return false;
    }

    /** 目前選取的資料夾（相對路徑），不是資料夾就回傳 null。 */
    private String currentFolder() {
        if (this.browserSelected < 0 || this.browserSelected >= this.browserRows.size()) {
            return null;
        }
        return this.browserRows.get(this.browserSelected).directory();
    }

    private LuaScriptFiles.Entry currentSelection() {
        if (this.browserSelected < 0 || this.browserSelected >= this.browserRows.size()) {
            return null;
        }
        BrowserRow row = this.browserRows.get(this.browserSelected);
        return row.entry();
    }

    /** 上下移動時跳過分類標題列。 */
    /** 上下移動時跳過分類標題列（資料夾可以選）。 */
    private int moveSelection(int delta) {
        int index = this.browserSelected;
        for (int step = 0; step < this.browserRows.size(); step++) {
            index += delta;
            if (index < 0) {
                index = this.browserRows.size() - 1;
            } else if (index >= this.browserRows.size()) {
                index = 0;
            }
            if (this.browserRows.get(index).isSelectable()) {
                return index;
            }
        }
        return this.browserSelected;
    }

    private String builtinTitle(String category) {
        return switch (category) {
            case "apidoc" -> "內建 API";
            case "lib" -> "內建工具庫";
            case "lua53" -> "Lua 5.3 標準庫";
            default -> category;
        };
    }

    private int countEntries(boolean builtin) {
        int count = 0;
        for (BrowserRow row : this.browserRows) {
            if (row.entry() != null && row.entry().builtin() == builtin) {
                count++;
            }
        }
        return count;
    }

    private void toggleBrowser() {
        this.browserOpen = !this.browserOpen;
        this.browserFocused = this.browserOpen;
        if (this.browserOpen) {
            this.refreshFileList();
        }
    }

    /** 新增檔案／資料夾要放進哪個目錄（選到資料夾就用它，選到檔案就用它所在目錄）。 */
    private String targetDirectory() {
        if (this.browserSelected < 0 || this.browserSelected >= this.browserRows.size()) {
            return "";
        }
        BrowserRow row = this.browserRows.get(this.browserSelected);
        if (row.isFolder()) {
            String dir = row.directory();
            for (LuaScriptFiles.BuiltinRoot root : LuaScriptFiles.BUILTIN_ROOTS) {
                if (dir.startsWith(root.name() + "/")) {
                    return "";   // 唯讀來源不能新增
                }
            }
            return dir;
        }
        if (row.entry() != null && !row.entry().builtin()) {
            String path = row.entry().path();
            int slash = path.lastIndexOf('/');
            return slash > 0 ? path.substring(0, slash) : "";
        }
        return "";
    }

    /** 建立新腳本緩衝（先不寫檔，Ctrl+S 才落地）。 */
    private void newScript(String name) {
        String normalized = name.trim().replace('\\', '/');
        if (normalized.isEmpty()) {
            return;
        }
        String directory = this.targetDirectory();
        if (!directory.isEmpty() && !normalized.contains("/")) {
            normalized = directory + "/" + normalized;
        }
        if (!normalized.endsWith(".lua")) {
            normalized = normalized + ".lua";
        }
        if (Files.exists(scriptsDir().resolve(normalized))) {
            this.openFile(normalized);
            //this.browserOpen = false;
            this.setStatus("檔案已存在，已開啟 " + normalized, 0xFFFFD479);
            return;
        }
        try {
            // 先落地（空模板），這樣檔案管理器立刻就能看到它
            LuaScriptFiles.write(LuaScriptFiles.scriptsDir().resolve(normalized), TEMPLATE);
        } catch (IOException exception) {
            this.setStatus("建立失敗：" + exception.getMessage(), 0xFFFF6B6B);
            return;
        }
        this.openFile(normalized);
        this.cursorLine = 0;
        this.cursorColumn = 0;
        this.scroll = 0;
        this.selectionLine = -1;
        this.dirty = false;   // 模板已寫入磁碟，內容與檔案一致
        this.pendingSelectionKey = "file:" + normalized;
        this.refreshFileList();
        //this.browserOpen = false;
        this.setStatus("已建立 " + this.fileName + "（可直接編輯，Ctrl+S 儲存）", 0xFF7BD88F);
    }

    /** 內容區左緣（瀏覽器開啟時往右讓開）。 */
    private int contentLeft() {
        return PADDING + this.gutterWidth() + (this.browserOpen ? BROWSER_WIDTH + 6 : 0);
    }

    private String content() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.lines.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(this.lines.get(i));
        }
        return builder.toString();
    }

    // ------------------------------------------------------------------ 畫面

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.font == null) {
            this.ensureInitialized();
            return;
        }
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        this.frameId++;
        //super.render(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, this.height, BG);

        int gutter = this.gutterWidth();
        int textLeft = this.contentLeft();
        int statusTop = this.height - STATUS_HEIGHT;
        int visibleLines = Math.max(1, (statusTop - textTop()) / LINE_HEIGHT);

        // 目前行高亮
        int cursorScreenLine = this.cursorLine - this.scroll;
        if (cursorScreenLine >= 0 && cursorScreenLine < visibleLines) {
            int y = textTop() + cursorScreenLine * LINE_HEIGHT;
            graphics.fill(PADDING, y, this.width - PADDING, y + LINE_HEIGHT, CURRENT_LINE);
        }

        graphics.enableScissor(PADDING, textTop(), this.width - PADDING, statusTop);
        for (int i = 0; i < visibleLines; i++) {
            int lineIndex = this.scroll + i;
            if (lineIndex >= this.lines.size()) {
                break;
            }
            int y = textTop() + i * LINE_HEIGHT;

            boolean errorLine = this.syntaxErrorLine > 0 && lineIndex == this.syntaxErrorLine - 1;
            if (errorLine) {
                graphics.fill(PADDING, y, this.width - PADDING, y + LINE_HEIGHT, 0x33FF5555);
            }
            String gutterText = this.lines.get(lineIndex).toString();
            LuaSnippetMarkers.Mapped mapped = this.displayOf(gutterText);
            LuaSnippetMarkers.Marker startMarker =
                    LuaSnippetMarkers.startMarker(gutterText, this.pairedSnippetIndices());
            String snippetIcon = startMarker == null ? null : startMarker.icon();
            int gutterX = this.gutterLeft();
            if (snippetIcon != null) {
                // 樣板起始行：行號位置改畫樣板圖示
                int iconSize = LINE_HEIGHT;
                graphics.blit(this.gutterIcon(snippetIcon), gutterX + 1, y, iconSize, iconSize,
                        0.0F, 0.0F, 24, 24, 24, 24);
            } else {
                String number = String.valueOf(lineIndex + 1);
                graphics.drawString(this.font, number,
                        gutterX + gutter - 4 - this.font.width(number),
                        y + 1, errorLine ? 0xFFFF6B6B : LINE_NO, false);
            }

            this.drawSelection(graphics, lineIndex, y, textLeft);
            this.drawLine(graphics, mapped.display(), textLeft, y);
        }
        graphics.disableScissor();

        Scrollbar editorBar = this.editorScrollbar();
        this.drawScrollbar(graphics, editorBar, this.scrollbarDrag == DRAG_EDITOR
                || this.hitScrollbar(editorBar, mouseX, mouseY));

        this.drawCursor(graphics, textLeft, visibleLines, statusTop);
        this.renderCompletion(graphics, textLeft);
        if (this.browserOpen) {
            this.renderBrowser(graphics, mouseX, mouseY);
        }
        if (this.draggingFile && this.dragCandidate != null) {
            String name = this.dragCandidate.substring(this.dragCandidate.lastIndexOf('/') + 1);
            int gx = mouseX + 8;
            int gy = mouseY - 4;
            int gw = this.font.width(name) + 6;
            graphics.fill(gx - 1, gy - 1, gx + gw + 1, gy + LINE_HEIGHT + 1, 0xFF3B4252);
            graphics.fill(gx, gy, gx + gw, gy + LINE_HEIGHT, 0xF01B1F2A);
            graphics.drawString(this.font, name, gx + 3, gy + 1, 0xFFFFD479, false);
        }
        this.drawTopBar(graphics, gutter);
        this.renderTaskBar(graphics, mouseX, mouseY);
        this.renderSnippetBar(graphics, mouseX, mouseY);
        if (this.dialog != Dialog.NONE) {
            this.renderDialog(graphics);
        }
        this.drawStatusBar(graphics, statusTop);
        this.renderActionTooltip(graphics);
        this.renderSnippetTooltip(graphics);
        if (this.dialog == Dialog.PARAMETERS) {
            this.renderSwitchDropdown(graphics);
        }
    }

    // ---- 檔案管理器版面 ----

    private static final String[] BROWSER_ACTIONS = {
            "＋ 新增腳本 (Ctrl+N)", "＋ 新增資料夾", "✎ 重新命名", "✕ 刪除", "⟳ 重新整理"
    };

    private int browserTop() {
        //return textTop();
        return BROWSER_TOP + SNIPPET_CATEGORY_HEIGHT + SNIPPET_TOOL_HEIGHT;
    }

    private int browserHeight() {
        return Math.max(80, (this.height - STATUS_HEIGHT) - browserTop() - 4);
    }

    private int browserActionTop() {
        return browserTop() + browserHeight() - BROWSER_ACTIONS.length * (LINE_HEIGHT + 2) - 2;
    }

    private int browserListRows() {
        return Math.max(1, (browserActionTop() - (browserTop() + 20)) / BROWSER_ROW_HEIGHT);
    }

    /** 讓選取項保持在可視範圍內，並回傳實際的第一列。 */
    private int browserListStart(int rows) {
        if (this.browserSelected < this.browserScroll) {
            this.browserScroll = this.browserSelected;
        } else if (this.browserSelected >= this.browserScroll + rows) {
            this.browserScroll = this.browserSelected - rows + 1;
        }
        int max = Math.max(0, this.browserRows.size() - rows);
        this.browserScroll = Math.max(0, Math.min(this.browserScroll, max));
        return this.browserScroll;
    }

    private static final int rowTopOffset = 20;

    private void renderBrowser(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = PADDING;
        int y = browserTop();
        int w = BROWSER_WIDTH;
        int h = browserHeight();
        int actionTop = browserActionTop();
        boolean hoverInside = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        graphics.fill(x, y, x + w, y + h, 0xF21B1F2A);
        graphics.fill(x, y, x + w, y + 1, 0xFF3B4252);
        graphics.drawString(this.font, "檔案管理器", x + 4, y + 4, 0xFFFFFFFF, false);

        int rowTop = y + rowTopOffset;
        int rows = browserListRows();
        if (this.browserSelected >= this.browserRows.size()) {
            this.browserSelected = Math.max(0, this.browserRows.size() - 1);
        }
        int start = browserListStart(rows);
        for (int i = 0; i < rows && start + i < this.browserRows.size(); i++) {
            BrowserRow row = this.browserRows.get(start + i);
            int rowY = rowTop + i * (BROWSER_ROW_HEIGHT);
            int indent = x + 4 + Math.max(0, row.depth()) * 8;
            boolean hovered = hoverInside && mouseY >= rowY - 1 && mouseY < rowY + LINE_HEIGHT;
            if (this.draggingFile && row.isFolder() && row.directory().equals(this.dropTargetDir)) {
                graphics.fill(x + 1, rowY - 1, x + w - 1, rowY + LINE_HEIGHT-1, 0x6640C074);
            }
            if (row.isSection()) {
                boolean collapsible = row.isCollapsible();
                graphics.fill(x + 1, rowY - 1, x + w - 1, rowY + LINE_HEIGHT-1, 0x40000000);
                if (collapsible && hovered) {
                    graphics.fill(x + 1, rowY - 1, x + w - 1, rowY + LINE_HEIGHT-1, 0x333B82F6);
                }
                graphics.drawString(this.font, row.header(), x + 4, rowY,
                        collapsible ? 0xFF8A93A6 : 0xFFCFD8E6, false);
                continue;
            }
            if (row.isFolder()) {
                if (start + i == this.browserSelected) {
                    graphics.fill(x + 1, rowY - 1, x + w - 1, rowY + LINE_HEIGHT-1, 0x663B82F6);
                } else if (hovered) {
                    graphics.fill(x + 1, rowY - 1, x + w - 1, rowY + LINE_HEIGHT-1, 0x333B82F6);
                }
                String header = row.header() == null ? "" : row.header();
                String marker = header.length() >= 2 ? header.substring(0, 2) : "";
                String folderName = header.substring(marker.length());
                int textY = rowY + (BROWSER_ROW_HEIGHT - 8) / 2;
                graphics.drawString(this.font, marker, indent, textY, 0xFF8A93A6, false);
                int iconX = indent + this.font.width(marker) + 2;
                this.drawBrowserIcon(graphics, ICON_FOLDER, iconX, rowY + (BROWSER_ROW_HEIGHT - BROWSER_ICON_SIZE) / 2);
                int textX = iconX + BROWSER_ICON_SIZE + 3;
                graphics.drawString(this.font,
                        this.font.plainSubstrByWidth(folderName, w - (textX - x) - 4),
                        textX, textY, 0xFFFFD479, false);
                continue;
            }
            LuaScriptFiles.Entry entry = row.entry();
            boolean current = entry.path().equals(this.relativeName());
            if (start + i == this.browserSelected) {
                graphics.fill(x + 1, rowY - 1, x + w - 1, rowY + LINE_HEIGHT-1, 0x663B82F6);
            } else if (hovered) {
                graphics.fill(x + 1, rowY - 1, x + w - 1, rowY + LINE_HEIGHT-1, 0x333B82F6);
            }
            boolean dirtyHere = this.isDirty(entry.path());
            int rowColor = dirtyHere ? 0xFFFFD479
                    : (current ? 0xFF7BD88F : (entry.builtin() ? 0xFF7A8699 : 0xFFD4D4D4));
            String label = row.label() == null ? entry.path() : row.label();
            if (dirtyHere) {
                label = label + " *";
            }
            this.drawBrowserIcon(graphics, label.equals("main.lua") ? ICON_CODE_MAIN : ICON_CODE, indent,
                    rowY + (BROWSER_ROW_HEIGHT - BROWSER_ICON_SIZE) / 2);
            int labelX = indent + BROWSER_ICON_SIZE + 3;
            graphics.drawString(this.font, this.font.plainSubstrByWidth(label, w - (labelX - x) - 4),
                    labelX, rowY + (BROWSER_ROW_HEIGHT - 8) / 2, rowColor, false);
        }


        Scrollbar browserBar = this.browserScrollbar();
        this.drawScrollbar(graphics, browserBar, this.scrollbarDrag == DRAG_BROWSER
                || this.hitScrollbar(browserBar, mouseX, mouseY));

        graphics.fill(x + 1, actionTop - 3, x + w - 1, actionTop - 2, 0xFF3B4252);
        for (int i = 0; i < BROWSER_ACTIONS.length; i++) {
            int rowY = actionTop + i * (LINE_HEIGHT + 2);
            boolean hovered = hoverInside && mouseY >= rowY && mouseY < rowY + LINE_HEIGHT + 2;
            if (hovered) {
                graphics.fill(x + 1, rowY, x + w - 1, rowY + LINE_HEIGHT + 1, 0x553B82F6);
            }
            int color = switch (i) {
                case 0, 1 -> 0xFFFFD479;
                case 3 -> 0xFFFF8080;
                default -> 0xFFD4D4D4;
            };
            graphics.drawString(this.font, BROWSER_ACTIONS[i], x + 4, rowY + 1, color, false);
        }
    }


    private void renderDialog(GuiGraphics graphics) {
        if (this.dialog == Dialog.PARAMETERS) {
            this.renderParameterDialog(graphics);
            return;
        }
        int w = 280;
        int h = this.dialog == Dialog.DELETE ? 46 : 48;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF3B4252);
        graphics.fill(x, y, x + w, y + h, 0xF21B1F2A);



        if (this.dialog == Dialog.DELETE) {
            boolean folder = this.dialogSource.startsWith("dir:");
            String label = folder ? "刪除資料夾 " + this.dialogInput + "（含內容）?"
                    : "刪除 " + this.dialogSource + " ?";
            graphics.drawString(this.font, label, x + 6, y + 8, 0xFFFF8080, false);
            graphics.drawString(this.font, "Enter 確認刪除 · Esc 取消", x + 6, y + 24, LINE_NO, false);
            return;
        }
        String title = switch (this.dialog) {
            case RENAME -> "重新命名（Enter 確認 · Esc 取消）";
            case NEW_FOLDER -> "新資料夾名稱（Enter 建立 · Esc 取消）";
            default -> "新腳本檔名（Enter 建立 · Esc 取消）";
        };
        String target = this.targetDirectory();
        if ((this.dialog == Dialog.NEW_FILE || this.dialog == Dialog.NEW_FOLDER) && !target.isEmpty()) {
            title = title + "  →  " + target + "/";
        }
        graphics.drawString(this.font, title, x + 6, y + 6, 0xFFD4D4D4, false);
        String text = this.dialogInput + "_";
        graphics.drawString(this.font, this.font.plainSubstrByWidth(text, w - 12), x + 6, y + 24, 0xFFFFFFFF, false);
    }

    private static final int parameterRowOffset = 40;
    /** 縱向排列的參數表單：一列一個參數，Tab/↑↓ 換欄位。 */
    private void renderParameterDialog(GuiGraphics graphics) {
        int w = 300;
        int h = this.parameterDialogHeight();
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF3B4252);
        graphics.fill(x, y, x + w, y + h, 0xF21B1F2A);
        graphics.drawString(this.font, this.pendingSnippet == null ? "" : this.pendingSnippet.label(),
                x + 8, y + 6, 0xFFFFFFFF, false);
        graphics.drawString(this.font, "Tab/↑↓ 換欄位 · ←→ 或點擊切換選項 · Enter 插入 · Esc 取消",
                x + 8, y + 18, 0xFF6B7280, false);

        for (int i = 0; i < this.pendingParams.size(); i++) {
            LuaSnippetLibrary.Parameter parameter = this.pendingParams.get(i);
            int rowY = y + parameterRowOffset + i * PARAM_ROW_HEIGHT;
            boolean focused = i == this.pendingFocus;
            if (focused) {
                graphics.fill(x + 4, rowY - 2, x + w - 4, rowY + PARAM_ROW_HEIGHT - 5, 0x553B82F6);
            }
            String label = this.font.plainSubstrByWidth(parameter.label(), 110);
            graphics.drawString(this.font, label, x + 8, rowY + 2,
                    focused ? 0xFFFFFFFF : 0xFF9AA4BC, false);
            String value = this.pendingFieldValues.size() > i ? this.pendingFieldValues.get(i) : "";
            int valueX = x + 124;
            graphics.fill(valueX - 4, rowY - 1, x + w - 8, rowY + LINE_HEIGHT + 3, 0x66000000);
            if (parameter.isBoolean()) {
                boolean on = "true".equalsIgnoreCase(value);
                int boxColor = on ? 0xFF4CAF50 : 0xFF6B7280;
                graphics.fill(valueX, rowY, valueX + LINE_HEIGHT, rowY + LINE_HEIGHT, boxColor);
                if (on) {
                    graphics.drawString(this.font, "v", valueX + 2, rowY + 1, 0xFF0B0D10, false);
                }
                graphics.drawString(this.font, on ? "true" : "false", valueX + LINE_HEIGHT + 4,
                        rowY + 1, focused ? 0xFFFFD479 : 0xFFD4D4D4, false);
            } else if (parameter.isSwitch()) {
                List<String> options = parameter.options();
                int at = Math.max(0, options.indexOf(value));
                String shown = this.font.plainSubstrByWidth(value, w - 190);
                graphics.drawString(this.font, shown, valueX, rowY + 1,
                        focused ? 0xFFFFD479 : 0xFFD4D4D4, false);
                int arrowX = valueX + this.font.width(shown) + 6;
                boolean open = this.switchDropdownOpen && this.switchDropdownRow == i;
                graphics.drawString(this.font, open ? "^" : "v", arrowX, rowY + 1,
                        focused ? 0xFFFFFFFF : 0xFF7A8699, false);
                graphics.drawString(this.font, (at + 1) + "/" + options.size(), arrowX + 10,
                        rowY + 1, 0xFF6B7280, false);
            } else {
                if (focused && this.fieldHasSelection()) {
                    String full = value;
                    int from = Math.max(0, Math.min(this.fieldSelectionFrom(), full.length()));
                    int to = Math.max(0, Math.min(this.fieldSelectionTo(), full.length()));
                    int selX1 = valueX + this.font.width(full.substring(0, from));
                    int selX2 = valueX + this.font.width(full.substring(0, to));
                    graphics.fill(selX1, rowY - 1, Math.max(selX2, selX1 + 1), rowY + LINE_HEIGHT + 1,
                            0x883B82F6);
                }
                String shown = this.font.plainSubstrByWidth(value, w - 132);
                graphics.drawString(this.font, shown, valueX, rowY + 1,
                        focused ? 0xFFFFD479 : 0xFFD4D4D4, false);
                if (focused) {
                    int caretColumn = value.length();
                    if (this.fieldSelectionCaret >= 0) {
                        caretColumn = Math.max(0, Math.min(this.fieldSelectionCaret, value.length()));
                    }
                    int caret = valueX + this.font.width(value.substring(0, caretColumn));
                    graphics.fill(caret, rowY + 1, caret + 1, rowY + 1 + LINE_HEIGHT, 0xFFFFFFFF);
                }
            }
        }
    }

    /** 展開中的選項清單（可滾動）。 */
    private void renderSwitchDropdown(GuiGraphics graphics) {
        int[] rect = this.switchDropdownRect();
        if (rect == null) {
            return;
        }
        LuaSnippetLibrary.Parameter parameter = this.parameterAt(this.switchDropdownRow);
        List<String> options = parameter.options();
        int visible = this.switchDropdownVisible();
        graphics.fill(rect[0] - 1, rect[1] - 1, rect[0] + rect[2] + 1, rect[1] + rect[3] + 1, 0xFF3B4252);
        graphics.fill(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3], 0xF21B1F2A);
        for (int i = 0; i < visible; i++) {
            int index = this.switchDropdownScroll + i;
            if (index >= options.size()) {
                break;
            }
            int rowY = rect[1] + 2 + i * LINE_HEIGHT;
            boolean highlight = index == this.switchDropdownHighlight;
            if (highlight) {
                graphics.fill(rect[0], rowY - 1, rect[0] + rect[2], rowY + LINE_HEIGHT, 0xFF2D6CDF);
            }
            String label = this.font.plainSubstrByWidth(options.get(index), rect[2] - 16);
            graphics.drawString(this.font, label, rect[0] + 5, rowY,
                    highlight ? 0xFFFFFFFF : 0xFFD4D4D4, false);
        }
        int[] bar = this.switchScrollbarRect();
        if (bar != null) {
            graphics.fill(bar[0], bar[1], bar[0] + bar[2], bar[1] + bar[3], 0x40000000);
            graphics.fill(bar[0], this.switchThumbTop(bar), bar[0] + bar[2],
                    this.switchThumbTop(bar) + this.switchThumbHeight(bar),
                    this.switchScrollbarDragging ? 0xFFB8C2D4 : 0xFF7C8798);
        }
    }

    private void drawTopBar(GuiGraphics graphics, int gutter) {
        graphics.fill(0, 0, this.width, TOP_BAR_HEIGHT, BAR_BG);

        // 標題
        String title = EDITOR_TITLE;
        graphics.drawString(this.font, title, PADDING, PADDING + 2, 0xFFFFFFFF, false);

        int x = PADDING + this.font.width(title) + 5;
        int hovered = -1;
        TopAction[] actions = TopAction.values();
        for (int i = 0; i < actions.length; i++) {
            TopAction action = actions[i];
            if (action.isSeparator()) {
                graphics.drawString(this.font, "|", x + 2, PADDING + 4, 0xFF4A5568, false);
                this.actionX[i] = x;
                this.actionW[i] = 6;
                x += 6;
                continue;
            }
            boolean enabled = this.topActionEnabled(action);
            boolean hover = enabled && this.inRect(this.lastMouseX, this.lastMouseY, x, 2, ICON_SIZE, ICON_SIZE);
            if (hover) {
                graphics.fill(x, 2, x + ICON_SIZE, 2 + ICON_SIZE, 0x553B82F6);
                hovered = i;
            } else if (action == TopAction.OPEN && this.browserOpen) {
                graphics.fill(x, 2, x + ICON_SIZE, 2 + ICON_SIZE, 0xFF2D6CDF);
            }
            graphics.blit(action.texture(), x, 2, ICON_SIZE, ICON_SIZE, 0.0F, 0.0F, 24, 24, 24, 24);
            if (!enabled) {
                graphics.fill(x, 2, x + ICON_SIZE, 2 + ICON_SIZE, 0x99000000);
            }
            this.actionX[i] = x;
            this.actionW[i] = ICON_SIZE;
            x += ICON_SIZE + 1;
        }

        long now = System.currentTimeMillis();
        if (hovered != this.hoveredAction) {
            this.hoveredAction = hovered;
            this.hoverActionSince = now;
        }

        // 右上角：關閉按鈕（和 ESC 同功能，滑鼠移上去變紅）
        int closeX = this.width - PADDING - ICON_SIZE;
        boolean closeHover = this.inRect(this.lastMouseX, this.lastMouseY, closeX, 2, ICON_SIZE, ICON_SIZE);
        if (closeHover) {
            graphics.fill(closeX, 2, closeX + ICON_SIZE, 2 + ICON_SIZE, 0x55FF5555);
        }
        int closeColor = closeHover ? 0xFFFF5555 : 0xFFB8C2D4;
        for (int i = 0; i < 9; i++) {
            int px = closeX + 3 + i;
            int py = 3 + i;
            graphics.fill(px, py, px + 1, py + 1, closeColor);
            graphics.fill(closeX + 11 - i, py, closeX + 12 - i, py + 1, closeColor);
        }

        if (this.syntaxErrorLine > 0) {
            String badge = "⚠ 第 " + this.syntaxErrorLine + " 行";
            graphics.drawString(this.font, badge, closeX - this.font.width(badge) - 8,
                    PADDING + 4, 0xFFFF6B6B, false);
        }
    }

    private boolean topActionEnabled(TopAction action) {
        return switch (action) {
            case OPEN -> true;
            case SAVE -> !this.readOnly;
            case SAVE_ALL -> this.anyDirtyBuffer();
            case UNDO -> !this.undoStack.isEmpty();
            case REDO -> !this.redoStack.isEmpty();
            case CUT, COPY -> this.hasSelection();
            case PASTE -> !this.readOnly;
            default -> false;
        };
    }

    private void runTopAction(TopAction action) {
        switch (action) {
            case OPEN -> this.toggleBrowser();
            case SAVE -> this.save();
            case SAVE_ALL -> this.saveAll();
            case UNDO -> this.undo();
            case REDO -> this.redo();
            case CUT -> this.cutSelection();
            case COPY -> this.copySelection();
            case PASTE -> this.pasteFromClipboard();
            default -> {
            }
        }
    }

    /** 滑鼠停留 1 秒後顯示的說明（含快捷鍵）。 */
    private void renderActionTooltip(GuiGraphics graphics) {
        TopAction[] actions = TopAction.values();
        if (this.hoveredAction < 0 || this.hoveredAction >= actions.length) {
            return;
        }
        TopAction action = actions[this.hoveredAction];
        if (action.isSeparator()) {
            return;
        }
        if (System.currentTimeMillis() - this.hoverActionSince < 1000L) {
            return;
        }
        String text = action.label + "   " + action.hotkey;
        int width = this.font.width(text) + 8;
        int x = Math.min(Math.max(PADDING, this.actionX[this.hoveredAction]), this.width - width - PADDING);
        int y = TOP_BAR_HEIGHT + 2;
        graphics.fill(x - 1, y - 1, x + width + 1, y + LINE_HEIGHT + 3, 0xFF3B4252);
        graphics.fill(x, y, x + width, y + LINE_HEIGHT + 2, 0xF01B1F2A);
        graphics.drawString(this.font, text, x + 4, y + 2, 0xFFD4D4D4, false);
    }

    private boolean inRect(double x, double y, int rx, int ry, int rw, int rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    /** 按鈕是否可用（沒有可做的事就變灰且點不動）。 */
    private boolean topActionEnabled(int index) {
        return switch (index) {
            case 0 -> !this.readOnly;
            case 1 -> this.anyDirtyBuffer();
            case 2 -> !this.undoStack.isEmpty();
            case 3 -> !this.redoStack.isEmpty();
            default -> false;
        };
    }

    private boolean anyDirtyBuffer() {
        if (this.dirty) {
            return true;
        }
        for (Buffer buffer : this.buffers.values()) {
            if (buffer.dirty && !buffer.readOnly) {
                return true;
            }
        }
        return false;
    }

    private static final int taskBarOffset = 3;
    /** 工作欄左緣：檔案管理器打開時往右縮，對齊文字區。 */
    private int taskBarLeft() {
        return PADDING + (this.browserOpen ? BROWSER_WIDTH + taskBarOffset : 0);
    }

    /** 依目前寬度把分頁排成多列（每幀只算一次）。 */
    private void layoutTaskBar() {
        if (this.layoutFrame == this.frameId) {
            return;
        }
        this.layoutFrame = this.frameId;
        List<TaskRect> layout = new ArrayList<>();
        int left = this.taskBarLeft();
        int maxRight = Math.max(left + 40, this.width - PADDING);
        int x = left;
        int row = 0;
        for (int i = 0; i < this.taskBar.size(); i++) {
            int width = Math.min(this.taskWidth(i), maxRight - left);
            if (x + width > maxRight && x > left) {
                row++;
                x = left;
            }
            layout.add(new TaskRect(x, this.taskBarTop() + row * TASK_BAR_ROW_HEIGHT, width));
            x += width + 2;
        }
        this.taskLayout = layout;
        this.taskRowCount = Math.max(1, row + 1);
    }

    /** 文字區頂端：工作欄佔幾列就往下推多少。 */
    private int textTop() {
        this.layoutTaskBar();
        return this.taskBarTop() + this.taskRowCount * TASK_BAR_ROW_HEIGHT;
    }

    /** 工作欄頂端：在代碼工具列下方。 */
    private int taskBarTop() {
        return TOP_BAR_HEIGHT + SNIPPET_CATEGORY_HEIGHT + SNIPPET_TOOL_HEIGHT;
    }

    /** 代碼工具列頂端（工作欄下方）。 */
    private int snippetBarTop() {
        return TOP_BAR_HEIGHT;
    }

    /** 工作欄：分頁太多時換行往下排。 */
    private void renderTaskBar(GuiGraphics graphics, int mouseX, int mouseY) {
        this.layoutTaskBar();
        int left = this.taskBarLeft();
        int bottom = this.textTop();
        int top = this.taskBarTop();
        graphics.fill(left-taskBarOffset, top, this.width, bottom, 0xFF14161A);
        if (this.taskBar.isEmpty()) {
            graphics.drawString(this.font, "雙擊檔案可加入工作欄", left, top + 3, LINE_NO, false);
            return;
        }
        for (int i = 0; i < this.taskLayout.size(); i++) {
            TaskRect rect = this.taskLayout.get(i);
            String path = this.taskBar.get(i);
            String label = this.taskLabel(i);
            boolean active = path.equals(this.relativeName());
            graphics.fill(rect.x(), rect.y() + 1, rect.x() + rect.width(),
                    rect.y() + TASK_BAR_ROW_HEIGHT - 1, active ? 0xFF2D6CDF : 0xFF23262E);
            graphics.drawString(this.font, this.font.plainSubstrByWidth(label, rect.width() - 22),
                    rect.x() + 5, rect.y() + 3, active ? 0xFFFFFFFF : 0xFFB8C2D4, false);
            boolean hoverClose = mouseX >= rect.x() + rect.width() - 10
                    && mouseX <= rect.x() + rect.width()
                    && mouseY >= rect.y() && mouseY < rect.y() + TASK_BAR_ROW_HEIGHT;
            if (hoverClose) {
                graphics.fill(rect.x() + rect.width() - 10, rect.y() + 1,
                        rect.x() + rect.width(), rect.y() + TASK_BAR_ROW_HEIGHT - 1, 0x55FF5555);
            }
            graphics.drawString(this.font, "x", rect.x() + rect.width() - 8, rect.y() + 3,
                    hoverClose ? 0xFFFF5555 : (active ? 0xFFCFD8E6 : 0xFF7A8699), false);
        }
    }

    /** 代碼工具列：上排分類、下排樣板按鈕。 */
    private void renderSnippetBar(GuiGraphics graphics, int mouseX, int mouseY) {
        int top = this.snippetBarTop();
        int catBottom = top + SNIPPET_CATEGORY_HEIGHT;
        graphics.fill(0, top, this.width, catBottom, 0xFF101216);

        int x = PADDING;
        SnippetCategory[] categories = SnippetCategory.values();
        for (int i = 0; i < categories.length; i++) {
            String label = categories[i].label;
            int width = this.font.width(label) + 12;
            boolean active = categories[i] == this.snippetCategory;
            boolean hover = this.inRect(mouseX, mouseY, x, top, width, SNIPPET_CATEGORY_HEIGHT);
            graphics.fill(x, top, x + width, catBottom,
                    active ? 0xFF2D6CDF : (hover ? 0xFF262A33 : 0xFF16181D));
            graphics.drawString(this.font, label, x + 6, top + 2,
                    active ? 0xFFFFFFFF : 0xFFB8C2D4, false);
            this.categoryX[i] = x;
            this.categoryW[i] = width;
            x += width + 2;
        }

        int toolTop = catBottom;
        graphics.fill(0, toolTop, this.width, toolTop + SNIPPET_TOOL_HEIGHT, 0xFF0D0F13);
        List<Snippet> snippets = this.snippetsFor(this.snippetCategory);
        this.snippetCount = snippets.size();
        int iconX = PADDING;
        for (int i = 0; i < snippets.size(); i++) {
            int iconY = toolTop + (SNIPPET_TOOL_HEIGHT - SNIPPET_ICON) / 2;
            boolean hover = this.inRect(mouseX, mouseY, iconX, iconY, SNIPPET_ICON, SNIPPET_ICON);
            if (hover) {
                graphics.fill(iconX, iconY, iconX + SNIPPET_ICON, iconY + SNIPPET_ICON, 0x553B82F6);
            }
            graphics.blit(snippets.get(i).icon(), iconX, iconY, SNIPPET_ICON, SNIPPET_ICON,
                    0.0F, 0.0F, 24, 24, 24, 24);
            this.snippetX[i] = iconX;
            iconX += SNIPPET_ICON + 2;
        }

        int hovered = this.snippetHit(mouseX, mouseY);
        long now = System.currentTimeMillis();
        if (hovered != this.hoveredSnippet) {
            this.hoveredSnippet = hovered;
            this.hoverSnippetSince = now;
        }
    }

    /** 回傳滑鼠所在的分類索引，沒有就 -1。 */
    private int categoryHit(double mouseX, double mouseY) {
        int top = this.snippetBarTop();
        if (mouseY < top || mouseY > top + SNIPPET_CATEGORY_HEIGHT) {
            return -1;
        }
        for (int i = 0; i < SnippetCategory.values().length; i++) {
            if (mouseX >= this.categoryX[i] && mouseX <= this.categoryX[i] + this.categoryW[i]) {
                return i;
            }
        }
        return -1;
    }

    /** 回傳滑鼠所在的樣板按鈕索引，沒有就 -1。 */
    private int snippetHit(double mouseX, double mouseY) {
        int top = this.snippetBarTop() + SNIPPET_CATEGORY_HEIGHT;
        int iconY = top + (SNIPPET_TOOL_HEIGHT - SNIPPET_ICON) / 2;
        if (mouseY < iconY || mouseY > iconY + SNIPPET_ICON) {
            return -1;
        }
        List<Snippet> snippets = this.snippetsFor(this.snippetCategory);
        for (int i = 0; i < snippets.size(); i++) {
            if (mouseX >= this.snippetX[i] && mouseX <= this.snippetX[i] + SNIPPET_ICON) {
                return i;
            }
        }
        return -1;
    }

    /** 把樣板程式碼插入游標處。 */
    private void insertSnippet(int index) {
        List<Snippet> snippets = this.snippetsFor(this.snippetCategory);
        if (index < 0 || index >= snippets.size()) {
            return;
        }
        Snippet snippet = snippets.get(index);
        if (!snippet.hasParameters()) {
            this.insertSnippetCode(snippet, Map.of());
            return;
        }
        if (this.readOnly) {
            this.setStatus("內建 API 文件是唯讀的", 0xFFFFD479);
            return;
        }
        this.pendingSnippet = snippet;
        this.pendingParams = snippet.parameters();
        this.pendingFocus = 0;
        this.pendingFieldValues.clear();
        for (LuaSnippetLibrary.Parameter parameter : this.pendingParams) {
            this.pendingFieldValues.add(parameter.defaultValue());
        }
        this.dialog = Dialog.PARAMETERS;
    }

    /** 把 ${name} 佔位符換成使用者輸入後插入。 */
    private void insertSnippetCode(Snippet snippet, Map<String, String> values) {
        String code = snippet.code();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            code = code.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        // 行內標記：Start 貼在第一行程式碼前、End 貼在最後一行後，都不額外換行
        // index 讓「樣板裡再插樣板」時內外兩層不會配錯
        String body = code.endsWith("\n") ? code.substring(0, code.length() - 1) : code;
        String[] bodyLines = body.split("\n", -1);
        int snippetIndex = this.nextSnippetIndex();
        bodyLines[0] = LuaSnippetMarkers.START_PREFIX + snippetIndex + " " + snippet.iconFile()
                + " ]] " + bodyLines[0];
        int last = bodyLines.length - 1;
        bodyLines[last] = bodyLines[last] + " " + LuaSnippetMarkers.END_PREFIX + " "
                + snippetIndex + "]]";
        String block = String.join("\n", bodyLines) + "\n";
        // 目前這一行如果有內容（不是空白），先斷行，讓區塊從新的一行開始
        boolean needBreak = !this.lines.get(this.cursorLine).toString().trim().isEmpty();
        int blockStart = this.cursorLine + (needBreak ? 1 : 0);
        if (needBreak) {
            block = "\n" + block;
        }
        this.insert(block);
        // 游標停在區塊第一行程式碼（不是 End 之後的空白行）
        this.setCursor(Math.min(blockStart + 1, this.lines.size() - 1), 0);
        this.setStatus("已插入：" + snippet.label(), 0xFF7BD88F);
    }

    /** 確認整個表單：所有欄位一次套用。 */
    private void confirmParameters() {
        if (this.pendingSnippet == null) {
            this.dialog = Dialog.NONE;
            return;
        }
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < this.pendingParams.size(); i++) {
            String value = i < this.pendingFieldValues.size() ? this.pendingFieldValues.get(i) : "";
            values.put(this.pendingParams.get(i).name(), value);
        }
        Snippet snippet = this.pendingSnippet;
        this.resetParameterDialog();
        this.insertSnippetCode(snippet, values);
    }

    private void resetParameterDialog() {
        this.closeSwitchDropdown();
        this.fieldSelectionAnchor = -1;
        this.fieldSelectionCaret = -1;
        this.fieldSelecting = false;
        this.dialog = Dialog.NONE;
        this.pendingSnippet = null;
        this.pendingParams = List.of();
        this.pendingFocus = 0;
        this.pendingFieldValues.clear();
    }

    /** 表單鍵盤操作：Tab/↑↓ 換欄位、Enter 確定、Esc 取消。 */
    private void handleParameterDialogKey(int keyCode, boolean control, boolean shift) {
        int size = this.pendingParams.size();
        if (control && this.isTextFieldFocused()) {
            if (keyCode == GLFW.GLFW_KEY_A) {
                this.fieldSelectionAnchor = 0;
                this.fieldSelectionCaret = this.fieldValue(this.pendingFocus).length();
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_C) {
                String selected = this.fieldSelectedText();
                if (selected.isEmpty()) {
                    selected = this.fieldValue(this.pendingFocus);
                }
                if (!selected.isEmpty()) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(selected);
                    this.setStatus("已複製參數內容", STATUS_TEXT);
                }
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_X) {
                String selected = this.fieldSelectedText();
                if (selected.isEmpty()) {
                    selected = this.fieldValue(this.pendingFocus);
                    this.fieldSelectionAnchor = 0;
                    this.fieldSelectionCaret = selected.length();
                }
                if (!selected.isEmpty()) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(selected);
                    this.fieldDeleteSelection();
                }
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_V) {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                clip = clip.replace("\r", "").replace("\n", " ");
                if (!clip.isEmpty()) {
                    this.fieldInsert(clip);
                }
                return;
            }
        }
        if (this.switchDropdownOpen) {
            LuaSnippetLibrary.Parameter parameter = this.parameterAt(this.switchDropdownRow);
            int optionCount = parameter.options().size();
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.switchDropdownHighlight = Math.min(optionCount - 1, this.switchDropdownHighlight + 1);
                this.ensureSwitchHighlightVisible();
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_UP) {
                this.switchDropdownHighlight = Math.max(0, this.switchDropdownHighlight - 1);
                this.ensureSwitchHighlightVisible();
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.pendingFieldValues.set(this.switchDropdownRow,
                        parameter.options().get(this.switchDropdownHighlight));
                this.closeSwitchDropdown();
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.closeSwitchDropdown();
                return;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.confirmParameters();
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.resetParameterDialog();
            this.setStatus("已取消插入樣板", LINE_NO);
        } else if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_DOWN) {
            this.pendingFocus = size == 0 ? 0 : (this.pendingFocus + 1) % size;
            this.closeSwitchDropdown();
            this.fieldSelectionAnchor = -1;
            this.fieldSelectionCaret = -1;
        } else if (keyCode == GLFW.GLFW_KEY_UP) {
            this.pendingFocus = size == 0 ? 0 : (this.pendingFocus - 1 + size) % size;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            this.cycleParameter(this.pendingFocus, -1);
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_SPACE) {
            this.cycleParameter(this.pendingFocus, 1);
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (this.isTextFieldFocused()) {
                if (this.fieldHasSelection()) {
                    this.fieldDeleteSelection();
                } else {
                    String value = this.fieldValue(this.pendingFocus);
                    if (!value.isEmpty()) {
                        this.pendingFieldValues.set(this.pendingFocus,
                                value.substring(0, value.length() - 1));
                    }
                }
            }
        }
    }

    private static final int SWITCH_DROPDOWN_MAX_ROWS = 6;
    private static final int SWITCH_DROPDOWN_WIDTH = 110;

    /** 下拉選單的可見列數。 */
    private int switchDropdownVisible() {
        LuaSnippetLibrary.Parameter parameter = this.parameterAt(this.switchDropdownRow);
        return Math.max(1, Math.min(parameter.options().size(), SWITCH_DROPDOWN_MAX_ROWS));
    }

    /** 下拉選單的矩形（x, y, w, h）；沒開就 null。 */
    private int[] switchDropdownRect() {
        if (!this.switchDropdownOpen || this.switchDropdownRow < 0) {
            return null;
        }
        int w = 200;
        int h = this.parameterDialogHeight();
        int left = (this.width - w) / 2;
        int top = (this.height - h) / 2;
        int rowY = top + parameterRowOffset + this.switchDropdownRow * PARAM_ROW_HEIGHT;
        int ddHeight = this.switchDropdownVisible() * LINE_HEIGHT + 4;
        return new int[]{left + 124, rowY + PARAM_ROW_HEIGHT, SWITCH_DROPDOWN_WIDTH, ddHeight};
    }

    /** 滾動條矩形（x, y, w, h）；不需要滾動時回傳 null。 */
    private int[] switchScrollbarRect() {
        int[] rect = this.switchDropdownRect();
        if (rect == null) {
            return null;
        }
        LuaSnippetLibrary.Parameter parameter = this.parameterAt(this.switchDropdownRow);
        if (parameter.options().size() <= this.switchDropdownVisible()) {
            return null;
        }
        int width = 4;
        return new int[]{rect[0] + rect[2] - width - 2, rect[1] + 2, width, rect[3] - 4};
    }

    private int switchThumbHeight(int[] bar) {
        LuaSnippetLibrary.Parameter parameter = this.parameterAt(this.switchDropdownRow);
        int total = parameter.options().size();
        int visible = this.switchDropdownVisible();
        return Math.max(10, (int) ((long) bar[3] * visible / Math.max(1, total)));
    }

    private int switchThumbTop(int[] bar) {
        LuaSnippetLibrary.Parameter parameter = this.parameterAt(this.switchDropdownRow);
        int maxScroll = Math.max(1, parameter.options().size() - this.switchDropdownVisible());
        int travel = Math.max(1, bar[3] - this.switchThumbHeight(bar));
        return bar[1] + (int) ((long) travel * Math.min(this.switchDropdownScroll, maxScroll) / maxScroll);
    }

    /** 依滑鼠 y 換算 scroll（拖曳或點軌道）。 */
    private int switchScrollFromMouse(double mouseY) {
        int[] bar = this.switchScrollbarRect();
        if (bar == null) {
            return 0;
        }
        LuaSnippetLibrary.Parameter parameter = this.parameterAt(this.switchDropdownRow);
        int maxScroll = Math.max(0, parameter.options().size() - this.switchDropdownVisible());
        int thumb = this.switchThumbHeight(bar);
        int travel = bar[3] - thumb;
        if (travel <= 0) {
            return 0;
        }
        double offset = mouseY - bar[1] - thumb / 2.0;
        offset = Math.max(0, Math.min(offset, travel));
        return (int) Math.round(offset * maxScroll / travel);
    }

    private boolean onSwitchScrollbar(double mouseX, double mouseY) {
        int[] bar = this.switchScrollbarRect();
        return bar != null && mouseX >= bar[0] - 2 && mouseX <= bar[0] + bar[2] + 2
                && mouseY >= bar[1] && mouseY <= bar[1] + bar[3];
    }

    private void openSwitchDropdown(int row) {
        LuaSnippetLibrary.Parameter parameter = this.parameterAt(row);
        if (!parameter.isSwitch()) {
            return;
        }
        this.switchDropdownOpen = true;
        this.switchDropdownRow = row;
        this.switchDropdownHighlight = Math.max(0, parameter.options().indexOf(
                this.pendingFieldValues.get(row)));
        this.ensureSwitchHighlightVisible();
    }

    private void closeSwitchDropdown() {
        this.switchDropdownOpen = false;
        this.switchScrollbarDragging = false;
        this.fieldSelecting = false;
        this.switchDropdownRow = -1;
        this.switchDropdownScroll = 0;
    }

    private void ensureSwitchHighlightVisible() {
        int visible = this.switchDropdownVisible();
        if (this.switchDropdownHighlight < this.switchDropdownScroll) {
            this.switchDropdownScroll = this.switchDropdownHighlight;
        } else if (this.switchDropdownHighlight >= this.switchDropdownScroll + visible) {
            this.switchDropdownScroll = this.switchDropdownHighlight - visible + 1;
        }
        this.switchDropdownScroll = Math.max(0, this.switchDropdownScroll);
    }

    /** 滑鼠在下拉選單的哪一個選項上；沒有回傳 -1。 */
    private int switchOptionAt(double mouseX, double mouseY) {
        int[] rect = this.switchDropdownRect();
        if (rect == null) {
            return -1;
        }
        if (mouseX < rect[0] || mouseX > rect[0] + rect[2]
                || mouseY < rect[1] + 2 || mouseY > rect[1] + rect[3] - 2) {
            return -1;
        }
        int index = this.switchDropdownScroll + (int) ((mouseY - rect[1] - 2) / LINE_HEIGHT);
        return index >= 0 && index < this.parameterAt(this.switchDropdownRow).options().size()
                ? index : -1;
    }

    // ---- 參數欄位：選取 / 插入 / 刪除 ----

    private boolean isTextFieldFocused() {
        return this.dialog == Dialog.PARAMETERS
                && !this.pendingParams.isEmpty()
                && this.parameterAt(this.pendingFocus).kind()
                        == LuaSnippetLibrary.ParameterKind.TEXT
                && !this.pendingFieldValues.isEmpty();
    }

    private String fieldValue(int index) {
        return index >= 0 && index < this.pendingFieldValues.size()
                ? this.pendingFieldValues.get(index) : "";
    }

    private boolean fieldHasSelection() {
        return this.fieldSelectionAnchor >= 0 && this.fieldSelectionCaret >= 0
                && this.fieldSelectionAnchor != this.fieldSelectionCaret;
    }

    private int fieldSelectionFrom() {
        return Math.min(this.fieldSelectionAnchor, this.fieldSelectionCaret);
    }

    private int fieldSelectionTo() {
        return Math.max(this.fieldSelectionAnchor, this.fieldSelectionCaret);
    }

    private String fieldSelectedText() {
        if (!this.fieldHasSelection() || !this.isTextFieldFocused()) {
            return "";
        }
        String value = this.fieldValue(this.pendingFocus);
        int from = Math.max(0, Math.min(this.fieldSelectionFrom(), value.length()));
        int to = Math.max(0, Math.min(this.fieldSelectionTo(), value.length()));
        return value.substring(from, to);
    }

    private void fieldDeleteSelection() {
        if (!this.fieldHasSelection() || !this.isTextFieldFocused()) {
            return;
        }
        String value = this.fieldValue(this.pendingFocus);
        int from = Math.max(0, Math.min(this.fieldSelectionFrom(), value.length()));
        int to = Math.max(0, Math.min(this.fieldSelectionTo(), value.length()));
        this.pendingFieldValues.set(this.pendingFocus, value.substring(0, from) + value.substring(to));
        this.fieldSelectionAnchor = from;
        this.fieldSelectionCaret = from;
    }

    /** 在有選取時取代選取，否則接在字尾。 */
    private void fieldInsert(String text) {
        if (!this.isTextFieldFocused()) {
            return;
        }
        this.fieldDeleteSelection();
        String value = this.fieldValue(this.pendingFocus);
        if (this.fieldHasSelection()) {
            return;
        }
        int at = this.fieldSelectionCaret >= 0
                ? Math.max(0, Math.min(this.fieldSelectionCaret, value.length())) : value.length();
        this.pendingFieldValues.set(this.pendingFocus,
                value.substring(0, at) + text + value.substring(at));
        int caret = at + text.length();
        this.fieldSelectionAnchor = caret;
        this.fieldSelectionCaret = caret;
    }

    private int fieldCharAt(double mouseX) {
        String value = this.fieldValue(this.pendingFocus);
        int valueX = this.parameterValueX();
        int target = (int) mouseX - valueX;
        int column = 0;
        while (column < value.length() && this.font.width(value.substring(0, column + 1)) <= target) {
            column++;
        }
        return column;
    }

    /** 參數值欄位的左緣（繪製與點擊共用）。 */
    private int parameterValueX() {
        return (this.width - 300) / 2 + 124;
    }

    private LuaSnippetLibrary.Parameter parameterAt(int index) {
        return this.pendingParams.get(Math.max(0, Math.min(index, this.pendingParams.size() - 1)));
    }

    /** BOOLEAN 切換 true/false；SWITCH 在選項間循環。 */
    private void cycleParameter(int index, int direction) {
        if (index < 0 || index >= this.pendingParams.size()) {
            return;
        }
        LuaSnippetLibrary.Parameter parameter = this.pendingParams.get(index);
        String current = this.pendingFieldValues.get(index);
        if (parameter.isBoolean()) {
            this.pendingFieldValues.set(index, "true".equalsIgnoreCase(current) ? "false" : "true");
        } else if (parameter.isSwitch()) {
            List<String> options = parameter.options();
            int at = options.indexOf(current);
            at = at < 0 ? 0 : Math.floorMod(at + direction, options.size());
            this.pendingFieldValues.set(index, options.get(at));
        }
    }

    private int parameterDialogHeight() {
        return 30 + this.pendingParams.size() * PARAM_ROW_HEIGHT + 16;
    }

    /** 滑鼠點在表單的哪一列，沒有就 -1。 */
    private int parameterRowAt(double mouseX, double mouseY) {
        if (this.dialog != Dialog.PARAMETERS) {
            return -1;
        }
        int w = 300;
        int h = this.parameterDialogHeight();
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        if (mouseX < x || mouseX > x + w) {
            return -1;
        }
        int index = (int) ((mouseY - (y + parameterRowOffset)) / PARAM_ROW_HEIGHT);
        return index >= 0 && index < this.pendingParams.size() ? index : -1;
    }



    /** 樣板按鈕的說明（停留 1 秒）。 */
    private void renderSnippetTooltip(GuiGraphics graphics) {
        if (this.hoveredSnippet < 0 || System.currentTimeMillis() - this.hoverSnippetSince < 1000L) {
            return;
        }
        List<Snippet> snippets = this.snippetsFor(this.snippetCategory);
        if (this.hoveredSnippet >= snippets.size()) {
            return;
        }
        Snippet snippet = snippets.get(this.hoveredSnippet);
        String title = snippet.label();
        List<String> lines = new ArrayList<>();
        lines.add(title);
        if (snippet.description() != null && !snippet.description().isEmpty()) {
            int wrapWidth = 200;
            for (String raw : this.wrapText(snippet.description(), wrapWidth)) {
                lines.add(raw);
                if (lines.size() >= 6) {
                    break;
                }
            }
        }
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, this.font.width(line));
        }
        width += 10;
        int height = lines.size() * LINE_HEIGHT + 6;
        int x = Math.min(Math.max(PADDING, this.snippetX[this.hoveredSnippet]), this.width - width - PADDING);
        int y = Math.min(this.taskBarTop() + 2, this.height - STATUS_HEIGHT - height - 2);
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF3B4252);
        graphics.fill(x, y, x + width, y + height, 0xF01B1F2A);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(this.font, lines.get(i), x + 5, y + 3 + i * LINE_HEIGHT,
                    i == 0 ? 0xFFFFFFFF : 0xFF9AA4BC, false);
        }
    }

    private record TaskHit(int index, boolean close) {
        boolean found() {
            return this.index >= 0;
        }
    }

    /** 工作欄命中測試；每一格右端 10px 是關閉用的 x。 */
    private TaskHit taskBarHit(double mouseX, double mouseY) {
        this.layoutTaskBar();
        for (int i = 0; i < this.taskLayout.size(); i++) {
            TaskRect rect = this.taskLayout.get(i);
            if (mouseX >= rect.x() && mouseX <= rect.x() + rect.width()
                    && mouseY >= rect.y() && mouseY < rect.y() + TASK_BAR_ROW_HEIGHT) {
                return new TaskHit(i, mouseX >= rect.x() + rect.width() - 10);
            }
        }
        return new TaskHit(-1, false);
    }

    private String taskLabel(int index) {
        String path = this.taskBar.get(index);
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name + (this.isDirty(path) ? " *" : "");
    }

    private int taskWidth(int index) {
        return this.font.width(this.taskLabel(index)) + 12 + 12;
    }

    /** 目前檔案加入工作欄（已存在就不動）。 */
    private void addTask(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        if (this.taskBar.contains(path)) {
            this.setStatus(path + " 已在工作欄中", LINE_NO);
            return;
        }
        this.taskBar.add(path);
        this.setStatus("已加入工作欄：" + path, 0xFF7BD88F);
    }

    private void removeTask(int index) {
        if (index < 0 || index >= this.taskBar.size()) {
            return;
        }
        this.setStatus("已從工作欄移除：" + this.taskBar.remove(index), STATUS_TEXT);
    }

    private void drawStatusBar(GuiGraphics graphics, int statusTop) {
        graphics.fill(0, statusTop, this.width, this.height, BAR_BG);
        String position = "行 " + (this.cursorLine + 1) + " 列 " + (this.cursorColumn + 1)
                + " · " + this.lines.size() + " 行";
        graphics.drawString(this.font, position, PADDING, statusTop + 3, STATUS_TEXT, false);
        if (!this.status.isEmpty()) {
            int x = PADDING + this.font.width(position) + 12;
            graphics.drawString(this.font, this.status, x, statusTop + 3, this.statusColor, false);
        }
    }

    private void drawSelection(GuiGraphics graphics, int lineIndex, int y, int textLeft) {
        if (this.selectionLine < 0) {
            return;
        }
        int[] range = this.selectionRangeOn(lineIndex);
        if (range == null) {
            return;
        }
        String line = this.lines.get(lineIndex).toString();
        LuaSnippetMarkers.Mapped mapped = this.displayOf(line);
        int from = Math.min(mapped.toDisplay(Math.min(range[0], line.length())),
                mapped.display().length());
        int to = Math.min(mapped.toDisplay(Math.min(range[1], line.length())),
                mapped.display().length());
        int x1 = textLeft + this.font.width(mapped.display().substring(0, from));
        int x2 = textLeft + this.font.width(mapped.display().substring(0, to));
        graphics.fill(x1, y, Math.max(x2, x1 + 1), y + LINE_HEIGHT, SELECTION);
    }

    private void drawCursor(GuiGraphics graphics, int textLeft, int visibleLines, int statusTop) {
        if ((System.currentTimeMillis() / 500L) % 2L != 0L) {
            return;
        }
        int screenLine = this.cursorLine - this.scroll;
        if (screenLine < 0 || screenLine >= visibleLines) {
            return;
        }
        String line = this.lines.get(this.cursorLine).toString();
        int column = Math.min(this.cursorColumn, line.length());
        LuaSnippetMarkers.Mapped mapped = this.displayOf(line);
        int displayColumn = Math.min(mapped.toDisplay(column), mapped.display().length());
        int x = textLeft + this.font.width(mapped.display().substring(0, displayColumn));
        int y = textTop() + screenLine * LINE_HEIGHT;
        graphics.fill(x, y, x + 1, Math.min(y + LINE_HEIGHT, statusTop), CURSOR);
    }

    /** 逐段上色繪製一行（關鍵字 / 字串 / 數字 / 註解）。 */
    private void drawLine(GuiGraphics graphics, String line, int x, int y) {
        int i = 0;
        int length = line.length();
        int drawX = x;
        while (i < length) {
            char c = line.charAt(i);
            int start = i;
            int color = TEXT;

            if (c == '-' && i + 1 < length && line.charAt(i + 1) == '-') {
                int color2 = (i + 2 < length && line.charAt(i + 2) == '@') ? ANNOTATION : COMMENT;
                graphics.drawString(this.font, line.substring(i), drawX, y + 1, color2, false);
                return;
            } else if (c == '"' || c == '\'') {
                i++;
                while (i < length) {
                    char current = line.charAt(i);
                    if (current == '\\' && i + 1 < length) {
                        i += 2;
                        continue;
                    }
                    i++;
                    if (current == c) {
                        break;
                    }
                }
                color = STRING;
            } else if (Character.isDigit(c)) {
                while (i < length && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '.'
                        || line.charAt(i) == 'x' || Character.isLetter(line.charAt(i)))) {
                    i++;
                }
                color = NUMBER;
            } else if (Character.isLetter(c) || c == '_') {
                while (i < length && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
                    i++;
                }
                String word = line.substring(start, i);
                if (KEYWORDS.contains(word)) {
                    color = KEYWORD;
                } else {
                    // 後面接 ( 的識別字視為函式呼叫，和 IntelliJ 一樣上色
                    int probe = i;
                    while (probe < length && line.charAt(probe) == ' ') {
                        probe++;
                    }
                    if (probe < length && line.charAt(probe) == '(') {
                        color = FUNCTION;
                    }
                }
            } else {
                i++;
            }
            if (i <= start) {
                i = start + 1;
            }
            String token = line.substring(start, i);
            graphics.drawString(this.font, token, drawX, y + 1, color, false);
            drawX += this.font.width(token);
        }
    }

    /** 滑鼠 x 對應到某一行的第幾個字元。 */
    private int columnAt(int lineIndex, double mouseX) {
        String line = this.lines.get(lineIndex).toString();
        LuaSnippetMarkers.Mapped mapped = this.displayOf(line);
        String display = mapped.display();
        int textLeft = this.contentLeft();
        int target = (int) mouseX - textLeft;
        int column = 0;
        while (column < display.length() && this.font.width(display.substring(0, column + 1)) <= target) {
            column++;
        }
        return mapped.toBuffer()[Math.min(column, mapped.toBuffer().length - 1)];
    }

    /** 畫管理器列上的小圖示（來源 24x24，縮到 12x12）。 */
    private void drawBrowserIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        graphics.blit(icon, x, y, BROWSER_ICON_SIZE, BROWSER_ICON_SIZE,
                0.0F, 0.0F, 24, 24, 24, 24);
    }

    private int gutterWidth() {
        int digits = String.valueOf(Math.max(1, this.lines.size())).length();
        return 10 + digits * 6;
    }

    // ------------------------------------------------------------------ 輸入

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.font == null) {
            return false;
        }
        if (this.inRect(mouseX, mouseY, this.width - PADDING - ICON_SIZE, 2, ICON_SIZE, ICON_SIZE)) {
            this.onClose();
            return true;
        }
        if (this.dialog == Dialog.PARAMETERS) {
            // 下拉開著時：點選項 = 選取；點別處 = 關閉
            if (this.switchDropdownOpen) {
                if (this.onSwitchScrollbar(mouseX, mouseY)) {
                    this.switchScrollbarDragging = true;
                    this.switchDropdownScroll = this.switchScrollFromMouse(mouseY);
                    return true;
                }
                int option = this.switchOptionAt(mouseX, mouseY);
                if (option >= 0) {
                    this.pendingFieldValues.set(this.switchDropdownRow,
                            this.parameterAt(this.switchDropdownRow).options().get(option));
                    this.closeSwitchDropdown();
                    return true;
                }
                this.closeSwitchDropdown();
            }
            int row = this.parameterRowAt(mouseX, mouseY);
            if (row >= 0) {
                this.pendingFocus = row;
                LuaSnippetLibrary.Parameter parameter = this.parameterAt(row);
                if (parameter.kind() == LuaSnippetLibrary.ParameterKind.TEXT) {
                    if (mouseX >= this.parameterValueX() - 4) {
                        int at = this.fieldCharAt(mouseX);
                        this.fieldSelectionAnchor = at;
                        this.fieldSelectionCaret = at;
                        this.fieldSelecting = true;
                    } else {
                        this.fieldSelectionAnchor = -1;
                        this.fieldSelectionCaret = -1;
                    }
                } else {
                    this.fieldSelectionAnchor = -1;
                    this.fieldSelectionCaret = -1;
                }
                if (parameter.isSwitch()) {
                    // 點一次展開選單（點在選單上方的設定列才會到這裡）
                    this.openSwitchDropdown(row);
                } else if (parameter.isBoolean()) {
                    this.cycleParameter(row, 1);
                }
            }
            return true;
        }
        if (this.dialog != Dialog.NONE) {
            return true;
        }
        if (button == 0) {
            int category = this.categoryHit(mouseX, mouseY);
            if (category >= 0) {
                this.snippetCategory = SnippetCategory.values()[category];
                this.hoveredSnippet = -1;
                return true;
            }
            int snippet = this.snippetHit(mouseX, mouseY);
            if (snippet >= 0) {
                this.insertSnippet(snippet);
                return true;
            }
        }
        Scrollbar editorBar = this.editorScrollbar();
        if (editorBar.maxScroll() > 0 && this.hitScrollbar(editorBar, mouseX, mouseY)) {
            this.scrollbarDrag = DRAG_EDITOR;
            this.scroll = this.scrollForMouse(editorBar, mouseY);
            return true;
        }
        if (this.browserOpen) {
            Scrollbar bar = this.browserScrollbar();
            if (bar.maxScroll() > 0 && this.hitScrollbar(bar, mouseX, mouseY)) {
                this.scrollbarDrag = DRAG_BROWSER;
                this.browserScroll = this.scrollForMouse(bar, mouseY);
                return true;
            }
        }

        TaskHit taskHit = this.taskBarHit(mouseX, mouseY);
        if (taskHit.found()) {
            String path = this.taskBar.get(taskHit.index());
            if (button == 1 || taskHit.close()) {
                this.removeTask(taskHit.index());
            } else if (this.isBuiltinPath(path)) {
                this.openBuiltin(path);
            } else {
                this.openFile(path);
            }
            return true;
        }

        if (this.handleTopBarClick(mouseX, mouseY)) {
            return true;
        }
        if (button == 0 && this.browserOpen && this.handleBrowserClick(mouseX, mouseY)) {
            return true;
        }
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int statusTop = this.height - STATUS_HEIGHT;
        if (mouseY < textTop() || mouseY > statusTop) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        this.browserFocused = false;
        int textLeft = this.contentLeft();
        int clickedLine = this.scroll + (int) ((mouseY - textTop()) / LINE_HEIGHT);
        if (clickedLine >= 0 && clickedLine < this.lines.size()
                && mouseX >= this.gutterLeft() && mouseX < textLeft
                && LuaSnippetMarkers.anyStartMarker(this.lines.get(clickedLine).toString()) != null) {
            this.selectSnippetBlock(clickedLine);
            return true;
        }
        int lineIndex = this.scroll + (int) ((mouseY - textTop()) / LINE_HEIGHT);
        lineIndex = Math.max(0, Math.min(lineIndex, this.lines.size() - 1));
        int column = this.columnAt(lineIndex, mouseX);
        if (hasShiftDown()) {
            if (this.selectionLine < 0) {
                this.selectionLine = this.cursorLine;
                this.selectionColumn = this.cursorColumn;
            }
        } else {
            this.selectionLine = -1;
        }
        this.setCursor(lineIndex, column);
        // 準備拖曳選取：錨點記在按下的位置
        this.textSelecting = true;
        this.textAnchorLine = lineIndex;
        this.textAnchorColumn = column;
        return true;
    }

    private boolean handleBrowserClick(double mouseX, double mouseY) {
        int x = PADDING;
        int y = browserTop();
        int w = BROWSER_WIDTH;
        int h = browserHeight();
        if (mouseX < x || mouseX > x + w || mouseY < y || mouseY > y + h) {
            return false;
        }
        this.browserFocused = true;

        // 動作列
        int actionTop = browserActionTop();
        if (mouseY >= actionTop) {
            int action = (int) ((mouseY - actionTop) / (LINE_HEIGHT + 2));
            if (action >= 0 && action < BROWSER_ACTIONS.length) {
                switch (action) {
                    case 0 -> {
                        this.dialog = Dialog.NEW_FILE;
                        this.dialogSource = "";
                        this.dialogInput = "";
                    }
                    case 1 -> {
                        this.dialog = Dialog.NEW_FOLDER;
                        this.dialogSource = "";
                        this.dialogInput = "";
                    }
                    case 2 -> this.openRenameDialog();
                    case 3 -> this.openDeleteDialog();
                    default -> {
                        this.refreshFileList();
                        this.setStatus("已重新整理，共 " + this.countEntries(false) + " 個腳本、"
                                + this.countEntries(true) + " 個內建", STATUS_TEXT);
                    }
                }
            }
            return true;
        }

        // 檔案列（分類標題不可選）
        int rowTop = y + rowTopOffset;
        int rows = browserListRows();
        int start = browserListStart(rows);
        int index = start + (int) ((mouseY - rowTop) / BROWSER_ROW_HEIGHT);
        if (mouseY >= rowTop && index >= 0 && index < this.browserRows.size()) {
            BrowserRow row = this.browserRows.get(index);
            if (row.isSection()) {
                if (row.isCollapsible()) {
                    this.toggleGroup(row.group());
                } else {
                    // 「腳本」標題：取消選取，新增就會落在 scripts/ 根目錄
                    this.browserSelected = -1;
                    this.noSelection = true;
                    this.setStatus("已取消選取：新增的檔案會建在 scripts/ 根目錄", STATUS_TEXT);
                }
            } else if (row.isFolder()) {
                this.browserSelected = index;
                this.noSelection = false;
                this.toggleFolder(row.directory());
            } else {
                String path = row.entry().path();
                boolean doubleClick = path.equals(this.lastClickedPath)
                        && System.currentTimeMillis() - this.lastClickAt < 300L;
                this.lastClickedPath = path;
                this.lastClickAt = System.currentTimeMillis();
                this.browserSelected = index;
                this.noSelection = false;
                this.dragCandidate = path;
                this.dragBuiltin = row.entry().builtin();
                this.dragStartX = mouseX;
                this.dragStartY = mouseY;
                if (doubleClick) {
                    this.addTask(path);
                    this.openEntry(row.entry());
                    this.refreshFileList();
                }
            }
        }
        return true;
    }


    /** 重新命名目前選取的檔案。 */
    private void openRenameDialog() {
        String folder = this.currentFolder();
        if (folder != null) {
            this.setStatus(this.isBuiltinPath(folder)
                    ? "內建資料夾是唯讀的" : "資料夾不能改名（可刪除後重建）", 0xFFFFD479);
            return;
        }
        LuaScriptFiles.Entry entry = this.currentSelection();
        if (entry == null) {
            this.setStatus("沒有可改名的檔案", LINE_NO);
            return;
        }
        if (entry.builtin()) {
            this.setStatus("內建 API 文件不能改名", 0xFFFFD479);
            return;
        }
        this.dialog = Dialog.RENAME;
        this.dialogSource = entry.path();
        this.dialogInput = this.dialogSource;
    }


    /** 刪除目前選取的檔案（需確認）。 */
    private void openDeleteDialog() {
        String folder = this.currentFolder();
        if (folder != null) {
            if (this.isBuiltinPath(folder)) {
                this.setStatus("內建資料夾是唯讀的，不能刪除", 0xFFFFD479);
                return;
            }
            this.dialog = Dialog.DELETE;
            this.dialogSource = "dir:" + folder;
            this.dialogInput = folder;
            return;
        }
        LuaScriptFiles.Entry entry = this.currentSelection();
        if (entry == null) {
            this.setStatus("沒有可刪除的檔案", LINE_NO);
            return;
        }
        if (entry.builtin()) {
            this.setStatus("內建 API 文件不能刪除", 0xFFFFD479);
            return;
        }
        this.dialog = Dialog.DELETE;
        this.dialogSource = entry.path();
        this.dialogInput = this.dialogSource;
    }


    /** 建立資料夾（可打 a/b 建立多層）。 */
    /** 建立資料夾（可打 a/b 建立多層）；選到資料夾時建在它裡面。 */
    private void createFolder(String name) {
        String normalized = name.trim().replace('\\', '/');
        if (normalized.isEmpty()) {
            this.setStatus("資料夾名稱不能為空", 0xFFFF6B6B);
            return;
        }
        String directory = this.targetDirectory();
        if (!directory.isEmpty() && !normalized.contains("/")) {
            normalized = directory + "/" + normalized;
        }
        try {
            LuaScriptFiles.createDirectory(LuaScriptFiles.scriptsDir(), normalized);
            this.refreshFileList();
            this.setStatus("已建立資料夾 " + normalized, 0xFF7BD88F);
        } catch (IOException exception) {
            this.setStatus("建立失敗：" + exception.getMessage(), 0xFFFF6B6B);
        }
    }

    private void confirmRename() {
        String target = LuaScriptFiles.normalizeName(this.dialogInput);
        if (target.isEmpty()) {
            this.setStatus("檔名不能為空", 0xFFFF6B6B);
            return;
        }
        boolean renamingCurrent = this.dialogSource.equals(this.relativeName());
        try {
            java.nio.file.Path from = LuaScriptFiles.scriptsDir().resolve(this.dialogSource);
            java.nio.file.Path to = LuaScriptFiles.scriptsDir().resolve(target);
            if (!LuaScriptFiles.rename(from, to)) {
                this.setStatus("改名失敗：目標已存在 " + target, 0xFFFF6B6B);
                return;
            }
            Buffer moved = this.buffers.remove(this.dialogSource);
            if (moved != null) {
                this.buffers.put(target, moved);
            }
            if (renamingCurrent) {
                this.fileName = target;
                this.filePath = to;
            }
            this.refreshFileList();
            this.setStatus("已改名為 " + target, 0xFF7BD88F);
        } catch (IOException exception) {
            this.setStatus("改名失敗：" + exception.getMessage(), 0xFFFF6B6B);
        }
    }

    private void confirmDelete() {
        try {
            if (this.dialogSource.startsWith("dir:")) {
                String folder = this.dialogSource.substring(4);
                java.nio.file.Path dir = LuaScriptFiles.scriptsDir().resolve(folder);
                if (!LuaScriptFiles.deleteDirectory(dir)) {
                    this.setStatus("資料夾不存在：" + folder, 0xFFFF6B6B);
                    return;
                }
                this.buffers.keySet().removeIf(key -> key.equals(folder) || key.startsWith(folder + "/"));
                this.refreshFileList();
                this.setStatus("已刪除資料夾 " + folder, 0xFF7BD88F);
                return;
            }
            java.nio.file.Path file = LuaScriptFiles.scriptsDir().resolve(this.dialogSource);
            if (!LuaScriptFiles.delete(file)) {
                this.setStatus("檔案不存在：" + this.dialogInput, 0xFFFF6B6B);
                return;
            }
            boolean deletingCurrent = this.dialogSource.equals(this.relativeName());
            if (deletingCurrent) {
                this.openFile("untitled.lua");
            }
            this.buffers.remove(this.dialogSource);
            this.refreshFileList();
            this.setStatus("已刪除 " + this.dialogSource, 0xFF7BD88F);
        } catch (IOException exception) {
            this.setStatus("刪除失敗：" + exception.getMessage(), 0xFFFF6B6B);
        }
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.font == null) {
            return false;
        }
        if (this.switchDropdownOpen) {
            LuaSnippetLibrary.Parameter parameter = this.parameterAt(this.switchDropdownRow);
            int maxScroll = Math.max(0, parameter.options().size() - this.switchDropdownVisible());
            this.switchDropdownScroll = Math.max(0, Math.min(maxScroll,
                    this.switchDropdownScroll - (int) Math.signum(scrollY)));
            return true;
        }
        if (this.browserOpen && mouseX >= PADDING && mouseX <= PADDING + BROWSER_WIDTH
                && mouseY >= browserTop() && mouseY <= browserActionTop()) {
            int visible = browserListRows();
            int max = Math.max(0, this.browserRows.size() - visible);
            this.browserScroll = Math.max(0, Math.min(max,
                    this.browserScroll - (int) Math.signum(scrollY) * 2));
            return true;
        }
        int statusTop = this.height - STATUS_HEIGHT;
        int visibleLines = Math.max(1, (statusTop - textTop()) / LINE_HEIGHT);
        int maxScroll = Math.max(0, this.lines.size() - visibleLines);
        this.scroll = Math.max(0, Math.min(maxScroll, this.scroll - (int) Math.signum(scrollY) * 2));
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.dialog == Dialog.PARAMETERS) {
            boolean text = this.isTextFieldFocused();
            if (codePoint >= 32 && text) {
                this.fieldInsert(String.valueOf(codePoint));
            }
            return true;
        }
        if (this.dialog != Dialog.NONE) {
            boolean editable = this.dialog == Dialog.NEW_FILE || this.dialog == Dialog.NEW_FOLDER
                    || this.dialog == Dialog.RENAME;
            if (editable && codePoint >= 32
                    && "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-./".indexOf(codePoint) >= 0) {
                this.dialogInput += codePoint;
            }
            return true;
        }
        if (codePoint < 32 || codePoint == 127) {
            return false;
        }
        if (this.skipClosing(codePoint)) {
            return true;
        }
        switch (codePoint) {
            case '(', '[', '{', '"', '\'' -> {
                this.insertPair(codePoint);
                return true;
            }
            default -> {
            }
        }
        this.insert(String.valueOf(codePoint));
        if (codePoint == ':' || codePoint == '.') {
            // 開頭是空白（例如剛打完 object:）也要列出該類別成員
            this.completionShowAll = true;
            this.updateCompletion();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean control = hasControlDown();
        boolean shift = hasShiftDown();

        if (this.dialog == Dialog.PARAMETERS) {
            this.handleParameterDialogKey(keyCode, control, shift);
            return true;
        }

        if (this.dialog != Dialog.NONE) {
            boolean isDelete = this.dialog == Dialog.DELETE;
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                Dialog active = this.dialog;
                this.dialog = Dialog.NONE;
                switch (active) {
                    case NEW_FILE -> this.newScript(this.dialogInput);
                    case NEW_FOLDER -> this.createFolder(this.dialogInput);
                    case RENAME -> this.confirmRename();
                    case DELETE -> this.confirmDelete();
                    default -> {
                    }
                }
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.dialog = Dialog.NONE;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !isDelete && !this.dialogInput.isEmpty()) {
                this.dialogInput = this.dialogInput.substring(0, this.dialogInput.length() - 1);
            }
            return true;
        }

        if (this.browserOpen && !this.completionActive) {
            if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_UP
                    || keyCode == GLFW.GLFW_KEY_DELETE) {
                this.browserFocused = true;
                this.noSelection = false;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                this.openDeleteDialog();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                if (!this.browserRows.isEmpty()) {
                    this.browserSelected = this.moveSelection(1);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_UP) {
                if (!this.browserRows.isEmpty()) {
                    this.browserSelected = this.moveSelection(-1);
                }
                return true;
            }
            /*if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                LuaScriptFiles.Entry entry = this.currentSelection();
                if (entry != null) {
                    this.openEntry(entry);
                    this.refreshFileList();
                    this.browserOpen = false;
                }
                return true;
            }*/
        }

        if (this.completionActive) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_UP -> {
                    this.completionSelected = (this.completionSelected - 1 + this.completionItems.size())
                            % this.completionItems.size();
                    return true;
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    this.completionSelected = (this.completionSelected + 1) % this.completionItems.size();
                    return true;
                }
                case GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    this.acceptCompletion();
                    return true;
                }
                case GLFW.GLFW_KEY_ESCAPE -> {
                    this.closeCompletion();
                    return true;
                }
                default -> {
                }
            }
        }

        if (control && shift && keyCode == GLFW.GLFW_KEY_S) {
            this.saveAll();
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_S) {
            this.save();
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_A) {
            this.selectionLine = 0;
            this.selectionColumn = 0;
            this.setCursor(this.lines.size() - 1, this.lines.get(this.lines.size() - 1).length());
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_C) {
            this.copySelection();
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_X) {
            this.cutSelection();
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_V) {
            this.pasteFromClipboard();
            return true;
        }

        if (control && keyCode == GLFW.GLFW_KEY_SLASH) {
            this.toggleComment();
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_N) {
            this.browserOpen = true;
            this.refreshFileList();
            this.dialog = Dialog.NEW_FILE;
            this.dialogSource = "";
            this.dialogInput = "";
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_O) {
            this.toggleBrowser();
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_SPACE) {
            this.openCompletion(true);
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_Z) {
            if (shift) {
                this.redo();
            } else {
                this.undo();
            }
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_Y) {
            this.redo();
            return true;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> this.insert("\n");
            case GLFW.GLFW_KEY_TAB -> this.insert("    ");
            case GLFW.GLFW_KEY_BACKSPACE -> this.backspace(control);
            case GLFW.GLFW_KEY_DELETE -> this.deleteForward();
            case GLFW.GLFW_KEY_LEFT -> this.moveHorizontal(-1, shift, control);
            case GLFW.GLFW_KEY_RIGHT -> this.moveHorizontal(1, shift, control);
            case GLFW.GLFW_KEY_UP -> this.moveVertical(-1, shift);
            case GLFW.GLFW_KEY_DOWN -> this.moveVertical(1, shift);
            case GLFW.GLFW_KEY_HOME -> this.setCursor(this.cursorLine, 0, shift);
            case GLFW.GLFW_KEY_END -> this.setCursor(this.cursorLine, this.lines.get(this.cursorLine).length(), shift);
            case GLFW.GLFW_KEY_PAGE_UP -> this.moveVertical(-this.visibleLines(), shift);
            case GLFW.GLFW_KEY_PAGE_DOWN -> this.moveVertical(this.visibleLines(), shift);
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    private int visibleLines() {
        return Math.max(1, (this.height - STATUS_HEIGHT - textTop()) / LINE_HEIGHT);
    }

    // ------------------------------------------------------------------ 編輯操作

    private void insert(String text) {
        if (this.readOnly) {
            this.setStatus("內建 API 文件是唯讀的", 0xFFFFD479);
            return;
        }
        this.clearSyntaxError();
        if (text == null || text.isEmpty()) {
            return;
        }
        this.recordUndo(text.length() <= 1 && !text.contains("\n"));
        this.deleteSelection();
        String[] parts = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        StringBuilder current = this.lines.get(this.cursorLine);
        String tail = current.substring(Math.min(this.cursorColumn, current.length()));
        current.setLength(Math.min(this.cursorColumn, current.length()));
        current.append(parts[0]);

        if (parts.length > 1) {
            String indent = leadingWhitespace(this.lines.get(this.cursorLine).toString());
            for (int i = 1; i < parts.length; i++) {
                this.lines.add(this.cursorLine + i, new StringBuilder(parts[i]));
            }
            int last = this.cursorLine + parts.length - 1;
            this.lines.get(last).append(tail);
            this.cursorLine = last;
            this.cursorColumn = parts[parts.length - 1].length();
            if (indent.isEmpty()) {
                // 保持簡單：不自動加縮排
            } else {
                this.indentCurrentLine(indent);
            }
        } else {
            current.append(tail);
            this.cursorColumn += parts[0].length();
        }
        this.dirty = true;
        this.updateCompletion();
        this.ensureCursorVisible();
    }

    private void indentCurrentLine(String indent) {
        StringBuilder line = this.lines.get(this.cursorLine);
        line.insert(0, indent);
        this.cursorColumn += indent.length();
    }

    private void backspace(boolean byWord) {
        if (this.readOnly) {
            return;
        }
        if (this.handleSnippetMarkerDelete(true)) {
            return;
        }
        this.clearSyntaxError();
        this.recordUndo(true);
        if (this.hasSelection()) {
            this.deleteSelection();
            return;
        }
        if (this.cursorColumn > 0) {
            StringBuilder line = this.lines.get(this.cursorLine);
            int from = this.cursorColumn;
            if (byWord) {
                while (from > 0 && Character.isWhitespace(line.charAt(from - 1))) {
                    from--;
                }
                while (from > 0 && !Character.isWhitespace(line.charAt(from - 1))) {
                    from--;
                }
            } else {
                from--;
            }
            line.delete(from, this.cursorColumn);
            this.cursorColumn = from;
        } else if (this.cursorLine > 0) {
            StringBuilder previous = this.lines.get(this.cursorLine - 1);
            int previousLength = previous.length();
            previous.append(this.lines.get(this.cursorLine));
            this.lines.remove(this.cursorLine);
            this.cursorLine--;
            this.cursorColumn = previousLength;
        }
        this.dirty = true;
        this.updateCompletion();
        this.ensureCursorVisible();
    }

    private void deleteForward() {
        if (this.readOnly) {
            return;
        }
        if (this.handleSnippetMarkerDelete(false)) {
            return;
        }
        this.clearSyntaxError();
        this.recordUndo(true);
        if (this.hasSelection()) {
            this.deleteSelection();
            return;
        }
        StringBuilder line = this.lines.get(this.cursorLine);
        if (this.cursorColumn < line.length()) {
            line.deleteCharAt(this.cursorColumn);
        } else if (this.cursorLine < this.lines.size() - 1) {
            line.append(this.lines.get(this.cursorLine + 1));
            this.lines.remove(this.cursorLine + 1);
        }
        this.dirty = true;
        this.updateCompletion();
    }

    private void moveHorizontal(int direction, boolean shift, boolean byWord) {
        if (!shift && this.hasSelection() && direction < 0) {
            int[] start = this.selectionStart();
            this.selectionLine = -1;
            this.setCursor(start[0], start[1]);
            return;
        }
        int line = this.cursorLine;
        int column = this.cursorColumn;
        if (direction < 0) {
            if (column > 0) {
                String text = this.lines.get(line).toString();
                if (byWord) {
                    while (column > 0 && Character.isWhitespace(text.charAt(column - 1))) {
                        column--;
                    }
                    while (column > 0 && !Character.isWhitespace(text.charAt(column - 1))) {
                        column--;
                    }
                } else {
                    column--;
                }
            } else if (line > 0) {
                line--;
                column = this.lines.get(line).length();
            }
        } else {
            if (column < this.lines.get(line).length()) {
                String text = this.lines.get(line).toString();
                if (byWord) {
                    while (column < text.length() && !Character.isWhitespace(text.charAt(column))) {
                        column++;
                    }
                    while (column < text.length() && Character.isWhitespace(text.charAt(column))) {
                        column++;
                    }
                } else {
                    column++;
                }
            } else if (line < this.lines.size() - 1) {
                line++;
                column = 0;
            }
        }
        this.setCursor(line, column, shift);
    }

    private void moveVertical(int delta, boolean shift) {
        int line = Math.max(0, Math.min(this.cursorLine + delta, this.lines.size() - 1));
        int column = Math.min(this.cursorColumn, this.lines.get(line).length());
        this.setCursor(line, column, shift);
    }

    private void setCursor(int line, int column) {
        this.setCursor(line, column, false);
    }

    private void setCursor(int line, int column, boolean keepSelection) {
        if (!keepSelection) {
            this.selectionLine = -1;
        } else if (this.selectionLine < 0) {
            this.selectionLine = this.cursorLine;
            this.selectionColumn = this.cursorColumn;
        }
        this.cursorLine = Math.max(0, Math.min(line, this.lines.size() - 1));
        this.cursorColumn = Math.max(0, Math.min(column, this.lines.get(this.cursorLine).length()));
        this.ensureCursorVisible();
    }

    private void ensureCursorVisible() {
        int visible = this.visibleLines();
        if (this.cursorLine < this.scroll) {
            this.scroll = this.cursorLine;
        } else if (this.cursorLine >= this.scroll + visible) {
            this.scroll = this.cursorLine - visible + 1;
        }
        this.scroll = Math.max(0, this.scroll);
    }

    private boolean hasSelection() {
        if (this.selectionLine < 0 || this.lines.isEmpty()) {
            return false;
        }
        if (this.selectionLine >= this.lines.size() || this.cursorLine >= this.lines.size()) {
            return false;   // 座標過期（例如復原後內容變短）
        }
        return this.selectionLine != this.cursorLine || this.selectionColumn != this.cursorColumn;
    }

    private int[] selectionStart() {
        int anchorLine = this.clampLine(this.selectionLine);
        int anchorColumn = this.clampColumn(anchorLine, this.selectionColumn);
        int cursorLine = this.clampLine(this.cursorLine);
        int cursorColumn = this.clampColumn(cursorLine, this.cursorColumn);
        if (anchorLine < cursorLine || (anchorLine == cursorLine && anchorColumn < cursorColumn)) {
            return new int[]{anchorLine, anchorColumn};
        }
        return new int[]{cursorLine, cursorColumn};
    }

    private int[] selectionEnd() {
        int anchorLine = this.clampLine(this.selectionLine);
        int anchorColumn = this.clampColumn(anchorLine, this.selectionColumn);
        int cursorLine = this.clampLine(this.cursorLine);
        int cursorColumn = this.clampColumn(cursorLine, this.cursorColumn);
        if (anchorLine < cursorLine || (anchorLine == cursorLine && anchorColumn < cursorColumn)) {
            return new int[]{cursorLine, cursorColumn};
        }
        return new int[]{anchorLine, anchorColumn};
    }

    /** 某一行在選取範圍內的字元區間；不在範圍內回傳 null。 */
    private int[] selectionRangeOn(int lineIndex) {
        if (!this.hasSelection()) {
            return null;
        }
        int[] start = this.selectionStart();
        int[] end = this.selectionEnd();
        if (lineIndex < start[0] || lineIndex > end[0]) {
            return null;
        }
        int from = lineIndex == start[0] ? start[1] : 0;
        int to = lineIndex == end[0] ? end[1] : this.lines.get(lineIndex).length();
        if (from >= to) {
            return null;
        }
        return new int[]{from, to};
    }

    private String selectedText() {
        if (!this.hasSelection()) {
            return "";
        }
        int[] start = this.selectionStart();
        int[] end = this.selectionEnd();
        StringBuilder builder = new StringBuilder();
        for (int line = start[0]; line <= end[0]; line++) {
            String text = this.lines.get(line).toString();
            int from = line == start[0] ? Math.min(start[1], text.length()) : 0;
            int to = line == end[0] ? Math.min(end[1], text.length()) : text.length();
            builder.append(text, from, to);
            if (line < end[0]) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    /** 把行號夾進目前內容範圍（選取座標可能因為復原／切換緩衝而過期）。 */
    private int clampLine(int line) {
        if (this.lines.isEmpty()) {
            return 0;
        }
        return Math.max(0, Math.min(line, this.lines.size() - 1));
    }

    private int clampColumn(int line, int column) {
        if (this.lines.isEmpty()) {
            return 0;
        }
        int target = clampLine(line);
        return Math.max(0, Math.min(column, this.lines.get(target).length()));
    }

    private void deleteSelection() {
        if (!this.hasSelection()) {
            return;
        }
        this.selectionLine = this.clampLine(this.selectionLine);
        this.selectionColumn = this.clampColumn(this.selectionLine, this.selectionColumn);
        this.cursorLine = this.clampLine(this.cursorLine);
        this.cursorColumn = this.clampColumn(this.cursorLine, this.cursorColumn);
        int[] start = this.selectionStart();
        int[] end = this.selectionEnd();
        StringBuilder first = this.lines.get(start[0]);
        String firstText = first.toString();
        String lastText = this.lines.get(end[0]).toString();
        String head = firstText.substring(0, Math.min(start[1], firstText.length()));
        String tail = lastText.substring(Math.min(end[1], lastText.length()));
        first.setLength(0);
        first.append(head).append(tail);
        for (int line = end[0]; line > start[0]; line--) {
            this.lines.remove(line);
        }
        this.selectionLine = -1;
        this.cursorLine = start[0];
        this.cursorColumn = head.length();
        this.dirty = true;
        this.ensureCursorVisible();
    }

    // ------------------------------------------------------------------ 自動完成

    /** 游標前的識別字起始欄位。 */
    private int wordStartColumn() {
        String line = this.lines.get(this.cursorLine).toString();
        int start = Math.min(this.cursorColumn, line.length());
        while (start > 0) {
            char c = line.charAt(start - 1);
            if (!(Character.isLetterOrDigit(c) || c == '_')) {
                break;
            }
            start--;
        }
        return start;
    }

    private void openCompletion(boolean showAll) {
        this.completionReceiver = "";
        this.completionShowAll = showAll || this.completionActive;
        this.updateCompletion();
        if (this.completionItems.isEmpty()) {
            this.closeCompletion();
            this.setStatus("沒有符合的補全", LINE_NO);
        }
    }

    private void closeCompletion() {
        this.completionActive = false;
        this.completionReceiver = "";
        this.completionShowAll = false;
        this.completionItems = List.of();
        this.completionSelected = 0;
    }

    /** 目前緩衝的自訂符號；內容沒變就直接用快取。 */
    private List<LuaSymbolIndex.Symbol> documentSymbols() {
        String source = this.content();
        int hash = source.hashCode();
        if (hash != this.symbolCacheHash) {
            this.symbolCacheHash = hash;
            this.symbolCache = LuaSymbolIndex.parse(source);
        }
        return this.symbolCache;
    }

    /** 依游標前的識別字重新篩選；沒有前綴且不是手動開啟時自動關閉。 */
    private void updateCompletion() {
        if (!this.completionActive && !this.completionShowAll) {
            return;
        }
        if (this.cursorLine >= this.lines.size()) {
            this.closeCompletion();
            return;
        }
        String line = this.lines.get(this.cursorLine).toString();
        int start = this.wordStartColumn();
        String prefix = line.substring(start, Math.min(this.cursorColumn, line.length()));

        // 依「.」或「:」前後的文字推算接收者，這樣物件成員補全才不會列出全部 API
        this.completionReceiver = "";
        if (start > 0) {
            char separator = line.charAt(start - 1);
            if (separator == ':' || separator == '.') {
                int wordStart = start - 1;
                while (wordStart > 0 && isIdentifierChar(line.charAt(wordStart - 1))) {
                    wordStart--;
                }
                this.completionReceiver = line.substring(wordStart, start - 1);
            }
        }
        if (prefix.isEmpty() && !this.completionShowAll) {
            this.closeCompletion();
            return;
        }
        List<LuaApiIndex.Entry> matches = new ArrayList<>();
        if (this.completionReceiver.isEmpty()) {
            // 自己寫的 local / 全域 / 參數優先，排在最前面
            for (LuaSymbolIndex.Symbol symbol : this.documentSymbols()) {
                if (prefix.isEmpty() || symbol.name().startsWith(prefix)) {
                    matches.add(new LuaApiIndex.Entry(symbol.name(), symbol.detail(), "", ""));
                    if (matches.size() >= COMPLETION_ROWS) {
                        break;
                    }
                }
            }
        }
        List<LuaApiIndex.Entry> candidates = this.completionReceiver.isEmpty()
                ? LuaApiIndex.entries()
                : LuaApiIndex.entriesFor(this.completionReceiver);
        for (LuaApiIndex.Entry completion : candidates) {
            if (prefix.isEmpty() || completion.name().startsWith(prefix)) {
                matches.add(completion);
                if (matches.size() >= COMPLETION_ROWS) {
                    break;
                }
            }
        }
        if (matches.isEmpty()) {
            this.closeCompletion();
            return;
        }
        this.completionItems = matches;
        this.completionStartColumn = start;
        this.completionSelected = Math.min(this.completionSelected, matches.size() - 1);
        this.completionActive = true;
    }

    private void acceptCompletion() {
        if (!this.completionActive || this.completionItems.isEmpty()) {
            return;
        }
        LuaApiIndex.Entry completion = this.completionItems.get(this.completionSelected);
        StringBuilder line = this.lines.get(this.cursorLine);
        int end = Math.min(this.cursorColumn, line.length());
        boolean isFunction = completion.isFunction();
        String insert = completion.name() + (isFunction ? "()" : "");
        line.replace(this.completionStartColumn, end, insert);
        this.cursorColumn = this.completionStartColumn + insert.length() - (isFunction ? 1 : 0);
        this.dirty = true;
        this.closeCompletion();
        this.ensureCursorVisible();
    }

    private void renderCompletion(GuiGraphics graphics, int textLeft) {
        if (!this.completionActive || this.completionItems.isEmpty()) {
            return;
        }
        String line = this.lines.get(this.cursorLine).toString();
        int prefixWidth = this.font.width(line.substring(0, Math.min(this.completionStartColumn, line.length())));
        int x = textLeft + prefixWidth;
        int y = textTop() + (this.cursorLine - this.scroll + 1) * LINE_HEIGHT;
        int rowWidth = 0;
        for (LuaApiIndex.Entry completion : this.completionItems) {
            rowWidth = Math.max(rowWidth, this.font.width(completion.name()) + 8 + this.font.width(completion.detail()));
        }
        int width = Math.min(320, rowWidth + 10);
        int height = this.completionItems.size() * LINE_HEIGHT + 4;
        int maxX = this.width - PADDING - width;
        int maxY = this.height - STATUS_HEIGHT - height;
        x = Math.max(PADDING, Math.min(x, maxX));
        y = Math.max(textTop(), Math.min(y, maxY));
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF3B4252);
        graphics.fill(x, y, x + width, y + height, 0xF01B1F2A);
        for (int i = 0; i < this.completionItems.size(); i++) {
            LuaApiIndex.Entry completion = this.completionItems.get(i);
            int rowY = y + 2 + i * LINE_HEIGHT;
            if (i == this.completionSelected) {
                graphics.fill(x, rowY, x + width, rowY + LINE_HEIGHT, 0xFF2D6CDF);
            }
            graphics.drawString(this.font, completion.name(), x + 4, rowY + 1, 0xFFFFFFFF, false);
            int detailX = x + 4 + this.font.width(completion.name()) + 6;
            graphics.drawString(this.font, completion.detail(), detailX, rowY + 1,
                    i == this.completionSelected ? 0xFFD8E2FF : 0xFF7C8798, false);
        }

        // 快速文件（對應 IntelliJ 的 Quick Documentation）
        LuaApiIndex.Entry selected = this.completionItems.get(this.completionSelected);
        if (!selected.doc().isEmpty()) {
            int docWidth = 220;
            List<String> docLines = this.wrapText(selected.doc(), docWidth - 8);
            int visible = Math.min(8, docLines.size());
            int docHeight = visible * LINE_HEIGHT + 6;
            int docX = x + width + 4;
            if (docX + docWidth > this.width - PADDING) {
                docX = Math.max(PADDING, x - docWidth - 4);
            }
            int docY = Math.max(textTop(), Math.min(y, this.height - STATUS_HEIGHT - docHeight));
            graphics.fill(docX - 1, docY - 1, docX + docWidth + 1, docY + docHeight + 1, 0xFF3B4252);
            graphics.fill(docX, docY, docX + docWidth, docY + docHeight, 0xF01B1F2A);
            for (int i = 0; i < visible; i++) {
                graphics.drawString(this.font, docLines.get(i), docX + 4, docY + 3 + i * LINE_HEIGHT,
                        0xFFA9B7C6, false);
            }
        }
    }

    /** 依字寬硬換行（中日韓沒有空白，用字元累積最穩）。 */
    private List<String> wrapText(String text, int maxWidth) {
        List<String> result = new ArrayList<>();
        for (String raw : text.split("\n", -1)) {
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);
                current.append(c);
                if (this.font.width(current.toString()) > maxWidth) {
                    current.setLength(current.length() - 1);
                    result.add(current.toString());
                    current = new StringBuilder().append(c);
                }
            }
            result.add(current.toString());
        }
        return result;
    }

    // ------------------------------------------------------------------ 復原／重做

    /** 完整文字 + 游標位置。腳本通常只有幾 KB，用快照比命令式 undo 簡單且不會有狀態漂移。 */
    private record Snapshot(String[] lines, int line, int column) {
    }

    private Snapshot snapshot() {
        String[] copy = new String[this.lines.size()];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = this.lines.get(i).toString();
        }
        return new Snapshot(copy, this.cursorLine, this.cursorColumn);
    }

    /**
     * 記錄一個復原點。
     * @param coalesce true 表示連續打字要合併成一步（避免每個字元一個 undo）
     */
    private void recordUndo(boolean coalesce) {
        long now = System.currentTimeMillis();
        this.redoStack.clear();
        if (coalesce && now - this.lastUndoAt < TYPING_COALESCE_MS && !this.undoStack.isEmpty()) {
            this.lastUndoAt = now;
            return;
        }
        this.undoStack.push(this.snapshot());
        while (this.undoStack.size() > MAX_UNDO) {
            this.undoStack.removeLast();
        }
        this.lastUndoAt = now;
    }

    private void undo() {
        if (this.undoStack.isEmpty()) {
            this.setStatus("沒有可復原的操作", LINE_NO);
            return;
        }
        this.redoStack.push(this.snapshot());
        this.restore(this.undoStack.pop());
        this.setStatus("復原（剩餘 " + this.undoStack.size() + "）", STATUS_TEXT);
    }

    private void redo() {
        if (this.redoStack.isEmpty()) {
            this.setStatus("沒有可重做的操作", LINE_NO);
            return;
        }
        this.undoStack.push(this.snapshot());
        this.restore(this.redoStack.pop());
        this.setStatus("重做（剩餘 " + this.redoStack.size() + "）", STATUS_TEXT);
    }

    private void restore(Snapshot snapshot) {
        this.lines.clear();
        for (String line : snapshot.lines()) {
            this.lines.add(new StringBuilder(line));
        }
        if (this.lines.isEmpty()) {
            this.lines.add(new StringBuilder());
        }
        this.selectionLine = -1;
        this.cursorLine = Math.max(0, Math.min(snapshot.line(), this.lines.size() - 1));
        this.cursorColumn = Math.max(0, Math.min(snapshot.column(), this.lines.get(this.cursorLine).length()));
        this.dirty = true;
        this.updateCompletion();
        this.ensureCursorVisible();
    }

    private static boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /** 自動配對：打 ( [ { " ' 時補上結尾並把游標放中間。 */
    private void insertPair(char open) {
        char close = switch (open) {
            case '(' -> ')';
            case '[' -> ']';
            case '{' -> '}';
            default -> open;
        };
        this.insert(String.valueOf(open) + close);
        this.cursorColumn--;
    }

    /** 下一個字元就是同一個結尾時直接跳過（不要重複插入）。 */
    private boolean skipClosing(char typed) {
        if (typed != ')' && typed != ']' && typed != '}' && typed != '"' && typed != '\'') {
            return false;
        }
        String line = this.lines.get(this.cursorLine).toString();
        if (this.cursorColumn < line.length() && line.charAt(this.cursorColumn) == typed) {
            this.cursorColumn++;
            return true;
        }
        return false;
    }

    /** Ctrl+/：切換整行或選取範圍的 -- 註解。 */
    private void toggleComment() {
        if (this.readOnly) {
            this.setStatus("內建 API 文件是唯讀的", 0xFFFFD479);
            return;
        }
        int from = this.cursorLine;
        int to = this.cursorLine;
        if (this.hasSelection()) {
            from = this.selectionStart()[0];
            to = this.selectionEnd()[0];
        }
        this.recordUndo(false);
        boolean allCommented = true;
        for (int i = from; i <= to; i++) {
            String text = this.lines.get(i).toString().trim();
            if (!text.isEmpty() && !text.startsWith("--")) {
                allCommented = false;
                break;
            }
        }
        for (int i = from; i <= to; i++) {
            StringBuilder line = this.lines.get(i);
            String text = line.toString();
            int indent = 0;
            while (indent < text.length() && Character.isWhitespace(text.charAt(indent))) {
                indent++;
            }
            if (allCommented) {
                if (text.startsWith("-- ", indent)) {
                    line.delete(indent, indent + 3);
                } else if (text.startsWith("--", indent)) {
                    line.delete(indent, indent + 2);
                }
            } else if (!text.trim().isEmpty()) {
                line.insert(indent, "-- ");
            }
        }
        this.dirty = true;
        this.updateCompletion();
    }

    /** 套用存檔時的檢查結果：有錯就標紅並跳到該行。 */
    private void applySyntaxResult(LuaSyntaxChecker.Result result) {
        if (result.ok()) {
            this.clearSyntaxError();
            return;
        }
        this.syntaxErrorLine = Math.max(1, result.line());
        this.syntaxErrorMessage = result.message();
        this.gotoLine(this.syntaxErrorLine - 1);
    }

    private void clearSyntaxError() {
        this.syntaxErrorLine = 0;
        this.syntaxErrorMessage = "";
    }

    /** 把游標移到指定行（0 起算），用於跳到語法錯誤。 */
    private void gotoLine(int lineIndex) {
        if (this.lines.isEmpty()) {
            return;
        }
        int target = Math.max(0, Math.min(lineIndex, this.lines.size() - 1));
        this.selectionLine = -1;
        this.cursorLine = target;
        this.cursorColumn = 0;
        this.ensureCursorVisible();
    }

    // ------------------------------------------------------------------ 捲軸

    private record Scrollbar(int left, int top, int height, int thumbTop, int thumbHeight, int maxScroll) {
    }

    private Scrollbar editorScrollbar() {
        int statusTop = this.height - STATUS_HEIGHT;
        int visible = Math.max(1, (statusTop - textTop()) / LINE_HEIGHT);
        return this.computeScrollbar(this.width - PADDING - SCROLLBAR_WIDTH, textTop(),
                Math.max(1, statusTop - textTop()), this.lines.size(), visible, this.scroll);
    }

    private Scrollbar browserScrollbar() {
        int rowTop = browserTop() + 20;
        int height = Math.max(1, browserActionTop() - rowTop - 6);
        return this.computeScrollbar(PADDING + BROWSER_WIDTH - SCROLLBAR_WIDTH - 1, rowTop,
                height, this.browserRows.size(), browserListRows(), this.browserScroll);
    }

    private Scrollbar computeScrollbar(int left, int top, int height, int total, int visible, int scroll) {
        if (total <= visible || height <= 0) {
            return new Scrollbar(left, top, height, top, height, 0);
        }
        int maxScroll = total - visible;
        int thumbHeight = Math.max(10, (int) ((long) height * visible / total));
        int travel = Math.max(1, height - thumbHeight);
        int clamped = Math.max(0, Math.min(scroll, maxScroll));
        int thumbTop = top + (int) ((long) travel * clamped / maxScroll);
        return new Scrollbar(left, top, height, thumbTop, thumbHeight, maxScroll);
    }

    private boolean hitScrollbar(Scrollbar bar, double mouseX, double mouseY) {
        return mouseX >= bar.left() - 2 && mouseX <= bar.left() + SCROLLBAR_WIDTH + 2
                && mouseY >= bar.top() && mouseY <= bar.top() + bar.height();
    }

    private int scrollForMouse(Scrollbar bar, double mouseY) {
        if (bar.maxScroll() <= 0) {
            return 0;
        }
        int travel = bar.height() - bar.thumbHeight();
        if (travel <= 0) {
            return 0;
        }
        double offset = mouseY - bar.top() - bar.thumbHeight() / 2.0;
        offset = Math.max(0, Math.min(offset, travel));
        return (int) Math.round(offset * bar.maxScroll() / travel);
    }

    private void drawScrollbar(GuiGraphics graphics, Scrollbar bar, boolean highlighted) {
        if (bar.maxScroll() <= 0) {
            return;
        }
        graphics.fill(bar.left(), bar.top(), bar.left() + SCROLLBAR_WIDTH, bar.top() + bar.height(),
                0x40000000);
        graphics.fill(bar.left(), bar.thumbTop(), bar.left() + SCROLLBAR_WIDTH,
                bar.thumbTop() + bar.thumbHeight(), highlighted ? 0xFFB8C2D4 : 0xFF7C8798);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.fieldSelecting && this.dialog == Dialog.PARAMETERS && this.isTextFieldFocused()) {
            this.fieldSelectionCaret = this.fieldCharAt(mouseX);
            return true;
        }
        if (this.switchScrollbarDragging) {
            this.switchDropdownScroll = this.switchScrollFromMouse(mouseY);
            return true;
        }
        if (this.dragCandidate != null) {
            if (!this.draggingFile
                    && Math.abs(mouseX - this.dragStartX) + Math.abs(mouseY - this.dragStartY) > 4.0) {
                this.draggingFile = true;
            }
            if (this.draggingFile) {
                if (this.isInTextArea(mouseX, mouseY)) {
                    // 放開就會插入 core.doFile("...")，不要顯示資料夾高亮
                    this.dropTargetDir = null;
                    if (this.dragCandidate != null) {
                        String name = this.dragCandidate.substring(this.dragCandidate.lastIndexOf('/') + 1);
                        this.setStatus("放開以插入 core.doFile(\"" + name + "\")", 0xFF7BD88F);
                    }
                } else {
                    this.dropTargetDir = this.folderAt(mouseX, mouseY);
                }
                return true;
            }
        }
        if (this.textSelecting) {
            int statusTop = this.height - STATUS_HEIGHT;
            // 拖出文字區時自動捲動
            if (mouseY < textTop()) {
                this.scroll = Math.max(0, this.scroll - 1);
            } else if (mouseY > statusTop) {
                int max = Math.max(0, this.lines.size() - this.visibleLines());
                this.scroll = Math.min(max, this.scroll + 1);
            }
            int lineIndex = this.scroll + (int) ((mouseY - textTop()) / LINE_HEIGHT);
            lineIndex = Math.max(0, Math.min(lineIndex, this.lines.size() - 1));
            int column = this.columnAt(lineIndex, mouseX);
            if (this.selectionLine < 0) {
                this.selectionLine = this.textAnchorLine;
                this.selectionColumn = this.textAnchorColumn;
            }
            this.setCursor(lineIndex, column, true);
            return true;
        }
        if (this.scrollbarDrag == DRAG_EDITOR) {
            this.scroll = this.scrollForMouse(this.editorScrollbar(), mouseY);
            return true;
        }
        if (this.scrollbarDrag == DRAG_BROWSER) {
            this.browserScroll = this.scrollForMouse(this.browserScrollbar(), mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.switchScrollbarDragging = false;
        if (this.draggingFile && this.dragCandidate != null) {
            String source = this.dragCandidate;
            boolean builtin = this.dragBuiltin;
            String target = this.dropTargetDir;
            boolean intoTextArea = this.isInTextArea(mouseX, mouseY);
            this.resetDrag();
            if (intoTextArea) {
                this.insertDoFile(mouseX, mouseY, source);
            } else if (!builtin && target != null && !target.equals(parentOf(source))) {
                this.moveFileInto(source, target);
            }
            return true;
        }
        this.resetDrag();
        this.textSelecting = false;
        this.scrollbarDrag = DRAG_NONE;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void resetDrag() {
        this.dragCandidate = null;
        this.draggingFile = false;
        this.dragBuiltin = false;
        this.dropTargetDir = null;
    }

    private static String parentOf(String path) {
        int slash = path.lastIndexOf('/');
        return slash > 0 ? path.substring(0, slash) : "";
    }

    /** 滑鼠底下的放置目標；回傳 null 表示不能放，空字串表示放到 scripts 根目錄。 */
    /** 滑鼠是不是在文字工作區裡（行號欄右側、狀態列上方）。 */
    private boolean isInTextArea(double mouseX, double mouseY) {
        int textLeft = this.contentLeft();
        int statusTop = this.height - STATUS_HEIGHT;
        return mouseX >= textLeft && mouseX <= this.width - PADDING
                && mouseY >= textTop() && mouseY < statusTop;
    }

    /** 把瀏覽器拖過來的檔案，在放開的位置插入 {@code core.doFile("path")}。 */
    private void insertDoFile(double mouseX, double mouseY, String path) {
        int line = this.scroll + (int) ((mouseY - textTop()) / LINE_HEIGHT);
        line = Math.max(0, Math.min(line, this.lines.size() - 1));
        int column = this.columnAt(line, mouseX);
        this.setCursor(line, column);
        this.insert("core.doFile(\"" + path + "\")");
        this.setStatus("已插入 core.doFile(\"" + path + "\")", 0xFF7BD88F);
    }

    private String folderAt(double mouseX, double mouseY) {
        if (!this.browserOpen) {
            return null;
        }
        int x = PADDING;
        int y = browserTop();
        int w = BROWSER_WIDTH;
        int actionTop = browserActionTop();
        if (mouseX < x || mouseX > x + w || mouseY < y + 20 || mouseY > actionTop) {
            return null;
        }
        int rowTop = y + 20;
        int rows = browserListRows();
        int start = this.browserScroll;
        int index = start + (int) ((mouseY - rowTop) / BROWSER_ROW_HEIGHT);
        if (index < 0 || index >= this.browserRows.size()) {
            return null;
        }
        BrowserRow row = this.browserRows.get(index);
        if (row.isFolder()) {
            return this.isBuiltinPath(row.directory()) ? null : row.directory();
        }
        if (row.isSection() && row.group() == null) {
            return "";   // 「腳本」分類標題 = 放到根目錄
        }
        return null;
    }

    /** 把檔案搬到資料夾（拖曳放開時呼叫）。 */
    private void moveFileInto(String sourcePath, String targetDir) {
        String name = sourcePath.substring(sourcePath.lastIndexOf('/') + 1);
        try {
            java.nio.file.Path from = LuaScriptFiles.scriptsDir().resolve(sourcePath);
            java.nio.file.Path toDir = LuaScriptFiles.scriptsDir().resolve(targetDir);
            if (!LuaScriptFiles.move(from, toDir)) {
                this.setStatus("目標資料夾已有同名檔案：" + name, 0xFFFF6B6B);
                return;
            }
            this.pendingSelectionKey = "file:" + (targetDir.isEmpty() ? name : targetDir + "/" + name);
            this.refreshFileList();
            this.setStatus("已移動 " + name + " → " + (targetDir.isEmpty() ? "/" : targetDir + "/"),
                    0xFF7BD88F);
        } catch (IOException exception) {
            this.setStatus("移動失敗：" + exception.getMessage(), 0xFFFF6B6B);
        }
    }

    private void setStatus(String message, int color) {
        this.status = message;
        this.statusColor = color;
    }

    private static String leadingWhitespace(String text) {
        int i = 0;
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return text.substring(0, i);
    }

    /**
     * Minecraft.setScreen 是「先掛上畫面、後 init()」；若中間（BufferUploader.reset）出錯，
     * 畫面會停在半初始化狀態，font 為 null，render 就會 NPE。這裡補一次 setScreen 讓它重新 init。
     */
    private void ensureInitialized() {
        if (this.reinitRequested) {
            return;
        }
        this.reinitRequested = true;
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.screen == this) {
                minecraft.setScreen(this);
            }
        });
    }

    private boolean handleTopBarClick(double mouseX, double mouseY) {
        TopAction[] actions = TopAction.values();
        for (int i = 0; i < actions.length; i++) {
            TopAction action = actions[i];
            if (action.isSeparator() || this.actionW[i] <= 0) {
                continue;
            }
            if (!this.inRect(mouseX, mouseY, this.actionX[i], 2, ICON_SIZE, ICON_SIZE)) {
                continue;
            }
            if (!this.topActionEnabled(action)) {
                this.setStatus(action.label + " 目前不可用", LINE_NO);
                return true;
            }
            this.runTopAction(action);
            return true;
        }
        return false;
    }

    private void copySelection() {
        String selected = this.selectedText();
        if (selected.isEmpty()) {
            return;
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(selected);
        this.setStatus("已複製 " + selected.length() + " 個字元", STATUS_TEXT);
    }

    private void cutSelection() {
        if (this.readOnly) {
            this.setStatus("內建 API 文件是唯讀的", 0xFFFFD479);
            return;
        }
        String selected = this.selectedText();
        if (selected.isEmpty()) {
            return;
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(selected);
        this.recordUndo(false);
        this.deleteSelection();
        this.setStatus("已剪下 " + selected.length() + " 個字元", STATUS_TEXT);
    }

    private void pasteFromClipboard() {
        if (this.readOnly) {
            this.setStatus("內建 API 文件是唯讀的", 0xFFFFD479);
            return;
        }
        this.insert(Minecraft.getInstance().keyboardHandler.getClipboard());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
