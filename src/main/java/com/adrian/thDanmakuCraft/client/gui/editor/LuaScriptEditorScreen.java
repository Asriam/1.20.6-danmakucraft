package com.adrian.thDanmakuCraft.client.gui.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
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

    // ---- 版面 ----
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_TOP = 26;
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

    private static final String TEMPLATE = """
            ---@type Class|THObjectContainer
            local container = core.defineSpellCardClass("test1")
            function container:onInit()
                for i = 1,60 do
                    self:createTHBullet(testBullet2, {i}, self:getPosition(),bullet_styles.ball_big,1)
                end
            end

            function container:onTick()
            end
            
            function container:onRegisterTasks(taskManager)
            end
            
            ---@type Class|THBullet
            local testBullet = core.defineClass()
            function testBullet:onInit(i)
                self:setVelocity(math.vec3.new(0.2,0.0,0.0):yRot(Mth.PI*2/32*i),true)
                self:setNavi(true)
            end
            
            function testBullet:onTick()
                self:move(0.0,0.1,0.0)
            end
            """;

    // ---- 檔案 ----
    private String fileName;
    private Path filePath;

    // ---- 檔案瀏覽 ----
    private static final int BROWSER_WIDTH = 156;
    private boolean browserOpen;
    /** 檔案管理器的一列：header 非 null 代表分類標題。 */
    private record BrowserRow(String header, LuaScriptFiles.Entry entry) {
        boolean isHeader() {
            return this.entry == null;
        }
    }

    /** 分成「腳本」與「內建 API」兩個分類，標題列插在中間。 */
    private List<BrowserRow> browserRows = new ArrayList<>();
    private int browserSelected;
    /** 是否已嘗試補做 init()，避免每幀重試。 */
    private boolean reinitRequested;
    private boolean dirty;

    /** 目前開的是不是 jar 內的唯讀 apidoc。 */
    private boolean readOnly;

    /** 存檔時檢查到的語法錯誤行（1 起算，0 = 沒有錯誤）。 */
    private int syntaxErrorLine;
    private String syntaxErrorMessage = "";


    private enum Dialog { NONE, NEW_FILE, RENAME, DELETE }

    private Dialog dialog = Dialog.NONE;

    /** 上一幀滑鼠位置（給頂部按鈕的 hover 用）。 */
    private int lastMouseX;
    private int lastMouseY;
    private int fileButtonX;
    private int fileButtonW;
    private String dialogInput = "";
    /** 對話框開啟時的原始檔名（改名／刪除的來源，不受輸入內容影響）。 */
    private String dialogSource = "";

    // ---- 編輯狀態 ----
    private final List<StringBuilder> lines = new ArrayList<>();
    private int cursorLine;
    private int cursorColumn;
    private int selectionLine = -1;
    private int selectionColumn;
    private int scroll;
    private String status = "";
    private int statusColor = STATUS_TEXT;

    // ---- 復原／重做 ----
    private static final int MAX_UNDO = 200;
    private static final long TYPING_COALESCE_MS = 600L;
    private final Deque<Snapshot> undoStack = new ArrayDeque<>();
    private final Deque<Snapshot> redoStack = new ArrayDeque<>();
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
        this.readOnly = false;
        this.fileName = name;
        this.filePath = scriptsDir().resolve(this.fileName);
        this.undoStack.clear();
        this.redoStack.clear();
        this.closeCompletion();
        this.lines.clear();
        try {
            if (Files.exists(this.filePath)) {
                String content = LuaScriptFiles.read(this.filePath);
                for (String line : content.split("\n", -1)) {
                    this.lines.add(new StringBuilder(line));
                }
                this.setStatus("已載入 " + this.fileName, STATUS_TEXT);
            } else {
                for (String line : TEMPLATE.split("\n", -1)) {
                    this.lines.add(new StringBuilder(line));
                }
                this.setStatus("新檔案：" + this.fileName, STATUS_TEXT);
            }
        } catch (IOException exception) {
            for (String line : TEMPLATE.split("\n", -1)) {
                this.lines.add(new StringBuilder(line));
            }
            this.setStatus("讀取失敗：" + exception.getMessage(), 0xFFFF6B6B);
        }
        if (this.lines.isEmpty()) {
            this.lines.add(new StringBuilder());
        }
        this.cursorLine = 0;
        this.cursorColumn = 0;
        this.scroll = 0;
        this.selectionLine = -1;
        this.dirty = false;
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
        this.readOnly = true;
        this.fileName = displayPath;
        this.filePath = null;
        this.undoStack.clear();
        this.redoStack.clear();
        this.closeCompletion();
        this.lines.clear();
        try {
            for (String line : LuaScriptFiles.readBuiltin(displayPath).split("\n", -1)) {
                this.lines.add(new StringBuilder(line));
            }
            this.setStatus("內建 API 文件（唯讀，不能修改或儲存）", 0xFF7BD88F);
        } catch (IOException exception) {
            this.lines.add(new StringBuilder());
            this.setStatus("讀取內建文件失敗：" + exception.getMessage(), 0xFFFF6B6B);
        }
        if (this.lines.isEmpty()) {
            this.lines.add(new StringBuilder());
        }
        this.cursorLine = 0;
        this.cursorColumn = 0;
        this.scroll = 0;
        this.selectionLine = -1;
        this.dirty = false;
        this.clearSyntaxError();
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

    /** 掃描 scripts 目錄（最多兩層）列出 *.lua。 */
    /** 重新掃描，並把腳本與內建 API 分成兩個分類。 */
    private void refreshFileList() {
        List<LuaScriptFiles.Entry> scripts = new ArrayList<>();
        List<LuaScriptFiles.Entry> builtins = new ArrayList<>();
        for (LuaScriptFiles.Entry entry : LuaScriptFiles.listAll()) {
            if (entry.builtin()) {
                builtins.add(entry);
            } else {
                scripts.add(entry);
            }
        }
        List<BrowserRow> rows = new ArrayList<>();
        rows.add(new BrowserRow("腳本 (" + scripts.size() + ")", null));
        for (LuaScriptFiles.Entry entry : scripts) {
            rows.add(new BrowserRow(null, entry));
        }
        // 唯讀來源各自一個分類（apidoc / lib）
        for (LuaScriptFiles.BuiltinRoot root : LuaScriptFiles.BUILTIN_ROOTS) {
            int count = 0;
            for (LuaScriptFiles.Entry entry : builtins) {
                if (entry.category().equals(root.name())) {
                    count++;
                }
            }
            rows.add(new BrowserRow(this.builtinTitle(root.name()) + " (" + count + ") · 唯讀", null));
            for (LuaScriptFiles.Entry entry : builtins) {
                if (entry.category().equals(root.name())) {
                    rows.add(new BrowserRow(null, entry));
                }
            }
        }
        this.browserRows = rows;
        this.browserSelected = this.firstSelectableRow();
    }

    /** 游標預設停在目前開啟的檔案，找不到就停在第一個可選列。 */
    private int firstSelectableRow() {
        String current = this.relativeName();
        for (int i = 0; i < this.browserRows.size(); i++) {
            BrowserRow row = this.browserRows.get(i);
            if (!row.isHeader() && row.entry().path().equals(current)) {
                return i;
            }
        }
        for (int i = 0; i < this.browserRows.size(); i++) {
            if (!this.browserRows.get(i).isHeader()) {
                return i;
            }
        }
        return 0;
    }

    private LuaScriptFiles.Entry currentSelection() {
        if (this.browserSelected < 0 || this.browserSelected >= this.browserRows.size()) {
            return null;
        }
        return this.browserRows.get(this.browserSelected).entry();
    }

    /** 上下移動時跳過分類標題列。 */
    private int moveSelection(int delta) {
        int index = this.browserSelected;
        for (int step = 0; step < this.browserRows.size(); step++) {
            index += delta;
            if (index < 0) {
                index = this.browserRows.size() - 1;
            } else if (index >= this.browserRows.size()) {
                index = 0;
            }
            if (!this.browserRows.get(index).isHeader()) {
                return index;
            }
        }
        return this.browserSelected;
    }

    private String builtinTitle(String category) {
        return switch (category) {
            case "apidoc" -> "內建 API";
            case "lib" -> "內建工具庫";
            default -> category;
        };
    }

    private int countEntries(boolean builtin) {
        int count = 0;
        for (BrowserRow row : this.browserRows) {
            if (!row.isHeader() && row.entry().builtin() == builtin) {
                count++;
            }
        }
        return count;
    }

    private void toggleBrowser() {
        this.browserOpen = !this.browserOpen;
        if (this.browserOpen) {
            this.refreshFileList();
        }
    }

    /** 建立新腳本緩衝（先不寫檔，Ctrl+S 才落地）。 */
    private void newScript(String name) {
        String normalized = name.trim().replace('\\', '/');
        if (normalized.isEmpty()) {
            return;
        }
        if (!normalized.endsWith(".lua")) {
            normalized = normalized + ".lua";
        }
        if (Files.exists(scriptsDir().resolve(normalized))) {
            this.openFile(normalized);
            this.browserOpen = false;
            this.setStatus("檔案已存在，已開啟 " + normalized, 0xFFFFD479);
            return;
        }
        this.openFile(normalized);
        this.lines.clear();
        for (String line : TEMPLATE.split("\n", -1)) {
            this.lines.add(new StringBuilder(line));
        }
        this.cursorLine = 0;
        this.cursorColumn = 0;
        this.scroll = 0;
        this.selectionLine = -1;
        this.dirty = true;
        this.browserOpen = false;
        this.setStatus("新腳本 " + this.fileName + "（Ctrl+S 儲存）", 0xFF7BD88F);
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
        //super.render(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, this.height, BG);

        int gutter = this.gutterWidth();
        int textLeft = this.contentLeft();
        int statusTop = this.height - STATUS_HEIGHT;
        int visibleLines = Math.max(1, (statusTop - TEXT_TOP) / LINE_HEIGHT);

        // 目前行高亮
        int cursorScreenLine = this.cursorLine - this.scroll;
        if (cursorScreenLine >= 0 && cursorScreenLine < visibleLines) {
            int y = TEXT_TOP + cursorScreenLine * LINE_HEIGHT;
            graphics.fill(PADDING, y, this.width - PADDING, y + LINE_HEIGHT, CURRENT_LINE);
        }

        graphics.enableScissor(PADDING, TEXT_TOP, this.width - PADDING, statusTop);
        for (int i = 0; i < visibleLines; i++) {
            int lineIndex = this.scroll + i;
            if (lineIndex >= this.lines.size()) {
                break;
            }
            int y = TEXT_TOP + i * LINE_HEIGHT;

            boolean errorLine = this.syntaxErrorLine > 0 && lineIndex == this.syntaxErrorLine - 1;
            if (errorLine) {
                graphics.fill(PADDING, y, this.width - PADDING, y + LINE_HEIGHT, 0x33FF5555);
            }
            String number = String.valueOf(lineIndex + 1);
            graphics.drawString(this.font, number, PADDING + gutter - 4 - this.font.width(number),
                    y + 1, errorLine ? 0xFFFF6B6B : LINE_NO, false);

            this.drawSelection(graphics, lineIndex, y, textLeft);
            this.drawLine(graphics, this.lines.get(lineIndex).toString(), textLeft, y);
        }
        graphics.disableScissor();

        this.drawCursor(graphics, textLeft, visibleLines, statusTop);
        this.renderCompletion(graphics, textLeft);
        if (this.browserOpen) {
            this.renderBrowser(graphics, mouseX, mouseY);
        }
        this.drawTopBar(graphics, gutter);
        if (this.dialog != Dialog.NONE) {
            this.renderDialog(graphics);
        }
        this.drawStatusBar(graphics, statusTop);
    }

    // ---- 檔案管理器版面 ----

    private static final String[] BROWSER_ACTIONS = {
            "＋ 新增 (Ctrl+N)", "✎ 重新命名", "✕ 刪除", "⟳ 重新整理"
    };

    private int browserTop() {
        return TEXT_TOP - 4;
    }

    private int browserHeight() {
        return Math.max(80, (this.height - STATUS_HEIGHT) - browserTop() - 4);
    }

    private int browserActionTop() {
        return browserTop() + browserHeight() - BROWSER_ACTIONS.length * (LINE_HEIGHT + 2) - 2;
    }

    private int browserListRows() {
        return Math.max(1, (browserActionTop() - (browserTop() + 20)) / (LINE_HEIGHT + 1));
    }

    private int browserListStart(int rows) {
        return Math.max(0, Math.min(this.browserSelected - rows + 1,
                Math.max(0, this.browserRows.size() - rows)));
    }

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

        int rowTop = y + 20;
        int rows = browserListRows();
        if (this.browserSelected >= this.browserRows.size()) {
            this.browserSelected = Math.max(0, this.browserRows.size() - 1);
        }
        int start = browserListStart(rows);
        for (int i = 0; i < rows && start + i < this.browserRows.size(); i++) {
            BrowserRow row = this.browserRows.get(start + i);
            int rowY = rowTop + i * (LINE_HEIGHT + 1);
            if (row.isHeader()) {
                graphics.fill(x + 1, rowY - 1, x + w - 1, rowY + LINE_HEIGHT, 0x40000000);
                graphics.drawString(this.font, row.header(), x + 4, rowY,
                        row.header().startsWith("內建") ? 0xFF8A93A6 : 0xFFCFD8E6, false);
                continue;
            }
            LuaScriptFiles.Entry entry = row.entry();
            boolean current = entry.path().equals(this.relativeName());
            boolean hovered = hoverInside && mouseY >= rowY - 1 && mouseY < rowY + LINE_HEIGHT;
            if (start + i == this.browserSelected) {
                graphics.fill(x + 1, rowY - 1, x + w - 1, rowY + LINE_HEIGHT, 0x663B82F6);
            } else if (hovered) {
                graphics.fill(x + 1, rowY - 1, x + w - 1, rowY + LINE_HEIGHT, 0x333B82F6);
            }
            int rowColor = current ? 0xFF7BD88F : (entry.builtin() ? 0xFF7A8699 : 0xFFD4D4D4);
            graphics.drawString(this.font, this.font.plainSubstrByWidth(entry.path(), w - 10),
                    x + 4, rowY, rowColor, false);
        }

        if (this.countEntries(false) == 0) {
            graphics.drawString(this.font, "（尚無腳本）", x + 4, rowTop + LINE_HEIGHT + 1, LINE_NO, false);
        }

        graphics.fill(x + 1, actionTop - 3, x + w - 1, actionTop - 2, 0xFF3B4252);
        for (int i = 0; i < BROWSER_ACTIONS.length; i++) {
            int rowY = actionTop + i * (LINE_HEIGHT + 2);
            boolean hovered = hoverInside && mouseY >= rowY && mouseY < rowY + LINE_HEIGHT + 2;
            if (hovered) {
                graphics.fill(x + 1, rowY, x + w - 1, rowY + LINE_HEIGHT + 1, 0x553B82F6);
            }
            int color = switch (i) {
                case 0 -> 0xFFFFD479;
                case 2 -> 0xFFFF8080;
                default -> 0xFFD4D4D4;
            };
            graphics.drawString(this.font, BROWSER_ACTIONS[i], x + 4, rowY + 1, color, false);
        }
    }


    private void renderDialog(GuiGraphics graphics) {
        int w = 280;
        int h = this.dialog == Dialog.DELETE ? 46 : 48;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF3B4252);
        graphics.fill(x, y, x + w, y + h, 0xF21B1F2A);

        if (this.dialog == Dialog.DELETE) {
            graphics.drawString(this.font, "刪除 " + this.dialogSource + " ?", x + 6, y + 8, 0xFFFF8080, false);
            graphics.drawString(this.font, "Enter 確認刪除 · Esc 取消", x + 6, y + 24, LINE_NO, false);
            return;
        }
        String title = this.dialog == Dialog.RENAME ? "重新命名（Enter 確認 · Esc 取消）"
                : "新腳本檔名（Enter 建立 · Esc 取消）";
        graphics.drawString(this.font, title, x + 6, y + 6, 0xFFD4D4D4, false);
        String text = this.dialogInput + "_";
        graphics.drawString(this.font, this.font.plainSubstrByWidth(text, w - 12), x + 6, y + 24, 0xFFFFFFFF, false);
    }
    private void drawTopBar(GuiGraphics graphics, int gutter) {
        graphics.fill(0, 0, this.width, TEXT_TOP - 2, BAR_BG);
        graphics.drawString(this.font, "Lua Script Editor", PADDING, PADDING + 1, 0xFFFFFFFF, false);

        // 「檔案」按鈕（讓檔案管理器看得到，不必先知道 Ctrl+O）
        int titleWidth = this.font.width("Lua Script Editor");
        this.fileButtonX = PADDING + titleWidth + 12;
        this.fileButtonW = this.font.width("檔案") + 10;
        boolean hover = this.lastMouseX >= this.fileButtonX && this.lastMouseX <= this.fileButtonX + this.fileButtonW
                && this.lastMouseY >= PADDING && this.lastMouseY <= PADDING + LINE_HEIGHT + 2;
        if (this.browserOpen || hover) {
            graphics.fill(this.fileButtonX, PADDING, this.fileButtonX + this.fileButtonW, PADDING + LINE_HEIGHT + 2,
                    this.browserOpen ? 0xFF2D6CDF : 0x553B82F6);
        }
        graphics.drawString(this.font, "檔案", this.fileButtonX + 5, PADDING + 2, 0xFFFFFFFF, false);

        String name = this.fileName + (this.dirty ? " *" : "") + (this.readOnly ? "  [唯讀]" : "");
        graphics.drawString(this.font, name, this.fileButtonX + this.fileButtonW + 10, PADDING + 2,
                this.dirty ? 0xFFFFD479 : STATUS_TEXT, false);
        if (this.syntaxErrorLine > 0) {
            String badge = "⚠ 第 " + this.syntaxErrorLine + " 行";
            int hintY = PADDING + 2;
            int hintX = this.width - PADDING - this.font.width("Ctrl+O 檔案 · Ctrl+S 儲存 · Esc 關閉");
            graphics.drawString(this.font, badge, hintX - this.font.width(badge) - 10, hintY,
                    0xFFFF6B6B, false);
        }
        String hint = "Ctrl+O 檔案 · Ctrl+S 儲存 · Esc 關閉";
        graphics.drawString(this.font, hint, this.width - PADDING - this.font.width(hint), PADDING + 1, LINE_NO, false);
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
        int from = Math.min(range[0], line.length());
        int to = Math.min(range[1], line.length());
        int x1 = textLeft + this.font.width(line.substring(0, from));
        int x2 = textLeft + this.font.width(line.substring(0, to));
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
        int x = textLeft + this.font.width(line.substring(0, column));
        int y = TEXT_TOP + screenLine * LINE_HEIGHT;
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
        if (this.dialog != Dialog.NONE) {
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
        if (mouseY < TEXT_TOP || mouseY > statusTop) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int lineIndex = this.scroll + (int) ((mouseY - TEXT_TOP) / LINE_HEIGHT);
        lineIndex = Math.max(0, Math.min(lineIndex, this.lines.size() - 1));
        String line = this.lines.get(lineIndex).toString();
        int textLeft = this.contentLeft();
        int column = 0;
        int target = (int) mouseX - textLeft;
        while (column < line.length() && this.font.width(line.substring(0, column + 1)) <= target) {
            column++;
        }
        if (hasShiftDown()) {
            if (this.selectionLine < 0) {
                this.selectionLine = this.cursorLine;
                this.selectionColumn = this.cursorColumn;
            }
        } else {
            this.selectionLine = -1;
        }
        this.setCursor(lineIndex, column);
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
                    case 1 -> this.openRenameDialog();
                    case 2 -> this.openDeleteDialog();
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
        int rowTop = y + 20;
        int rows = browserListRows();
        int start = browserListStart(rows);
        int index = start + (int) ((mouseY - rowTop) / (LINE_HEIGHT + 1));
        if (mouseY >= rowTop && index >= 0 && index < this.browserRows.size()) {
            BrowserRow row = this.browserRows.get(index);
            if (!row.isHeader()) {
                this.browserSelected = index;
                this.openEntry(row.entry());
                this.refreshFileList();
            }
        }
        return true;
    }


    /** 重新命名目前選取的檔案。 */
    private void openRenameDialog() {
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
            java.nio.file.Path file = LuaScriptFiles.scriptsDir().resolve(this.dialogSource);
            if (!LuaScriptFiles.delete(file)) {
                this.setStatus("檔案不存在：" + this.dialogInput, 0xFFFF6B6B);
                return;
            }
            boolean deletingCurrent = this.dialogSource.equals(this.relativeName());
            this.refreshFileList();
            if (deletingCurrent) {
                this.openFile("untitled.lua");
            }
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
        int statusTop = this.height - STATUS_HEIGHT;
        int visibleLines = Math.max(1, (statusTop - TEXT_TOP) / LINE_HEIGHT);
        int maxScroll = Math.max(0, this.lines.size() - visibleLines);
        this.scroll = Math.max(0, Math.min(maxScroll, this.scroll - (int) Math.signum(scrollY) * 2));
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.dialog != Dialog.NONE) {
            boolean editable = this.dialog == Dialog.NEW_FILE || this.dialog == Dialog.RENAME;
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

        if (this.dialog != Dialog.NONE) {
            boolean isDelete = this.dialog == Dialog.DELETE;
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                Dialog active = this.dialog;
                this.dialog = Dialog.NONE;
                switch (active) {
                    case NEW_FILE -> this.newScript(this.dialogInput);
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
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                LuaScriptFiles.Entry entry = this.currentSelection();
                if (entry != null) {
                    this.openEntry(entry);
                    this.refreshFileList();
                    this.browserOpen = false;
                }
                return true;
            }
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
            String selected = this.selectedText();
            if (!selected.isEmpty()) {
                Minecraft.getInstance().keyboardHandler.setClipboard(selected);
            }
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_X) {
            String selected = this.selectedText();
            if (!selected.isEmpty()) {
                Minecraft.getInstance().keyboardHandler.setClipboard(selected);
                this.recordUndo(false);
                this.deleteSelection();
            }
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_V) {
            this.insert(Minecraft.getInstance().keyboardHandler.getClipboard());
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
        return Math.max(1, (this.height - STATUS_HEIGHT - TEXT_TOP) / LINE_HEIGHT);
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
        return this.selectionLine >= 0
                && (this.selectionLine != this.cursorLine || this.selectionColumn != this.cursorColumn);
    }

    private int[] selectionStart() {
        if (this.selectionLine < this.cursorLine
                || (this.selectionLine == this.cursorLine && this.selectionColumn < this.cursorColumn)) {
            return new int[]{this.selectionLine, this.selectionColumn};
        }
        return new int[]{this.cursorLine, this.cursorColumn};
    }

    private int[] selectionEnd() {
        int[] start = this.selectionStart();
        if (start[0] == this.selectionLine && start[1] == this.selectionColumn) {
            return new int[]{this.cursorLine, this.cursorColumn};
        }
        return new int[]{this.selectionLine, this.selectionColumn};
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

    private void deleteSelection() {
        if (!this.hasSelection()) {
            return;
        }
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
        int y = TEXT_TOP + (this.cursorLine - this.scroll + 1) * LINE_HEIGHT;
        int rowWidth = 0;
        for (LuaApiIndex.Entry completion : this.completionItems) {
            rowWidth = Math.max(rowWidth, this.font.width(completion.name()) + 8 + this.font.width(completion.detail()));
        }
        int width = Math.min(320, rowWidth + 10);
        int height = this.completionItems.size() * LINE_HEIGHT + 4;
        int maxX = this.width - PADDING - width;
        int maxY = this.height - STATUS_HEIGHT - height;
        x = Math.max(PADDING, Math.min(x, maxX));
        y = Math.max(TEXT_TOP, Math.min(y, maxY));
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
            int docY = Math.max(TEXT_TOP, Math.min(y, this.height - STATUS_HEIGHT - docHeight));
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
        if (mouseY >= PADDING && mouseY <= PADDING + LINE_HEIGHT + 2
                && mouseX >= this.fileButtonX && mouseX <= this.fileButtonX + this.fileButtonW) {
            this.toggleBrowser();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
