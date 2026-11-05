package com.adrian.thDanmakuCraft.client.gui.editor;

import com.adrian.thDanmakuCraft.client.gui.editor.LuaSnippetLibrary.Parameter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 樣板參數表單（原本塞在 {@link LuaScriptEditorScreen} 裡的 700 行）。
 *
 * <p>負責：所有參數一次列出、TEXT / BOOLEAN / SWITCH 三種控件、下拉選單（含拖曳滾動條）、
 * 欄位的選取與剪貼簿。確認後直接呼叫 {@code screen.insertSnippetCode(...)}。</p>
 */
final class LuaParameterDialog {

    private static final int LINE_HEIGHT = 10;
    private static final int PARAM_ROW_HEIGHT = LINE_HEIGHT + 8;
    private static final int parameterRowOffset = 40;
    private static final int SWITCH_DROPDOWN_MAX_ROWS = 6;
    private static final int SWITCH_DROPDOWN_WIDTH = 110;
    private static final int STATUS_TEXT = 0xFF9AA4BC;
    private static final int LINE_NO = 0xFF5A6478;

    private final LuaScriptEditorScreen screen;

    private LuaSnippet snippet;
    private List<Parameter> params = List.of();
    private int focus;
    private final List<String> values = new ArrayList<>();

    private boolean switchDropdownOpen;
    private int switchDropdownRow = -1;
    private int switchDropdownScroll;
    private int switchDropdownHighlight;
    private boolean switchScrollbarDragging;

    private int fieldSelectionAnchor = -1;
    private int fieldSelectionCaret = -1;
    private boolean fieldSelecting;

    LuaParameterDialog(LuaScriptEditorScreen screen) {
        this.screen = screen;
    }

    boolean isOpen() {
        return this.snippet != null;
    }

    void open(LuaSnippet snippet) {
        this.snippet = snippet;
        this.params = snippet.parameters();
        this.focus = 0;
        this.values.clear();
        for (Parameter parameter : this.params) {
            this.values.add(parameter.defaultValue());
        }
        this.fieldSelectionAnchor = -1;
        this.fieldSelectionCaret = -1;
        this.fieldSelecting = false;
        this.closeSwitchDropdown();
    }

    void close() {
        this.closeSwitchDropdown();
        this.fieldSelectionAnchor = -1;
        this.fieldSelectionCaret = -1;
        this.fieldSelecting = false;
        this.snippet = null;
        this.params = List.of();
        this.focus = 0;
        this.values.clear();
    }

    public void render(GuiGraphics graphics) {
        int w = 300;
        int h = this.parameterDialogHeight();
        int x = (this.screen.width - w) / 2;
        int y = (this.screen.height - h) / 2;
        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF3B4252);
        graphics.fill(x, y, x + w, y + h, 0xF21B1F2A);
        graphics.drawString(this.screen.font(), this.snippet == null ? "" : this.snippet.label(),
                x + 8, y + 6, 0xFFFFFFFF, false);
        graphics.drawString(this.screen.font(), "Tab/↑↓ 換欄位 · ←→ 或點擊切換選項 · Enter 插入 · Esc 取消",
                x + 8, y + 18, 0xFF6B7280, false);

        for (int i = 0; i < this.params.size(); i++) {
            LuaSnippetLibrary.Parameter parameter = this.params.get(i);
            int rowY = y + parameterRowOffset + i * PARAM_ROW_HEIGHT;
            boolean focused = i == this.focus;
            if (focused) {
                graphics.fill(x + 4, rowY - 2, x + w - 4, rowY + PARAM_ROW_HEIGHT - 5, 0x553B82F6);
            }
            String label = this.screen.font().plainSubstrByWidth(parameter.label(), 110);
            graphics.drawString(this.screen.font(), label, x + 8, rowY + 2,
                    focused ? 0xFFFFFFFF : 0xFF9AA4BC, false);
            String value = this.values.size() > i ? this.values.get(i) : "";
            int valueX = x + 124;
            graphics.fill(valueX - 4, rowY - 1, x + w - 8, rowY + LINE_HEIGHT + 3, 0x66000000);
            if (parameter.isBoolean()) {
                boolean on = "true".equalsIgnoreCase(value);
                int boxColor = on ? 0xFF4CAF50 : 0xFF6B7280;
                graphics.fill(valueX, rowY, valueX + LINE_HEIGHT, rowY + LINE_HEIGHT, boxColor);
                if (on) {
                    graphics.drawString(this.screen.font(), "v", valueX + 2, rowY + 1, 0xFF0B0D10, false);
                }
                graphics.drawString(this.screen.font(), on ? "true" : "false", valueX + LINE_HEIGHT + 4,
                        rowY + 1, focused ? 0xFFFFD479 : 0xFFD4D4D4, false);
            } else if (parameter.isSwitch()) {
                List<String> options = parameter.options();
                int at = Math.max(0, options.indexOf(value));
                String shown = this.screen.font().plainSubstrByWidth(value, w - 190);
                graphics.drawString(this.screen.font(), shown, valueX, rowY + 1,
                        focused ? 0xFFFFD479 : 0xFFD4D4D4, false);
                int arrowX = valueX + this.screen.font().width(shown) + 6;
                boolean open = this.switchDropdownOpen && this.switchDropdownRow == i;
                graphics.drawString(this.screen.font(), open ? "^" : "v", arrowX, rowY + 1,
                        focused ? 0xFFFFFFFF : 0xFF7A8699, false);
                graphics.drawString(this.screen.font(), (at + 1) + "/" + options.size(), arrowX + 10,
                        rowY + 1, 0xFF6B7280, false);
            } else {
                if (focused && this.fieldHasSelection()) {
                    String full = value;
                    int from = Math.max(0, Math.min(this.fieldSelectionFrom(), full.length()));
                    int to = Math.max(0, Math.min(this.fieldSelectionTo(), full.length()));
                    int selX1 = valueX + this.screen.font().width(full.substring(0, from));
                    int selX2 = valueX + this.screen.font().width(full.substring(0, to));
                    graphics.fill(selX1, rowY - 1, Math.max(selX2, selX1 + 1), rowY + LINE_HEIGHT + 1,
                            0x883B82F6);
                }
                String shown = this.screen.font().plainSubstrByWidth(value, w - 132);
                graphics.drawString(this.screen.font(), shown, valueX, rowY + 1,
                        focused ? 0xFFFFD479 : 0xFFD4D4D4, false);
                if (focused) {
                    int caretColumn = value.length();
                    if (this.fieldSelectionCaret >= 0) {
                        caretColumn = Math.max(0, Math.min(this.fieldSelectionCaret, value.length()));
                    }
                    int caret = valueX + this.screen.font().width(value.substring(0, caretColumn));
                    graphics.fill(caret, rowY + 1, caret + 1, rowY + 1 + LINE_HEIGHT, 0xFFFFFFFF);
                }
            }
        }
        // 下拉選單畫在最上層
        this.renderSwitchDropdown(graphics);
    }

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
            String label = this.screen.font().plainSubstrByWidth(options.get(index), rect[2] - 16);
            graphics.drawString(this.screen.font(), label, rect[0] + 5, rowY,
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
    private void confirmParameters() {
        if (this.snippet == null) {
            this.close();
            return;
        }
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < this.params.size(); i++) {
            String value = i < this.values.size() ? this.values.get(i) : "";
            values.put(this.params.get(i).name(), value);
        }
        LuaSnippet snippet = this.snippet;
        this.close();
        this.screen.insertSnippetCode(snippet, values);
    }

    private void handleParameterDialogKey(int keyCode, boolean control, boolean shift) {
        int size = this.params.size();
        if (control && this.isTextFieldFocused()) {
            if (keyCode == GLFW.GLFW_KEY_A) {
                this.fieldSelectionAnchor = 0;
                this.fieldSelectionCaret = this.fieldValue(this.focus).length();
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_C) {
                String selected = this.fieldSelectedText();
                if (selected.isEmpty()) {
                    selected = this.fieldValue(this.focus);
                }
                if (!selected.isEmpty()) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(selected);
                    this.screen.setStatus("已複製參數內容", STATUS_TEXT);
                }
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_X) {
                String selected = this.fieldSelectedText();
                if (selected.isEmpty()) {
                    selected = this.fieldValue(this.focus);
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
                this.values.set(this.switchDropdownRow,
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
            this.close();
            this.screen.setStatus("已取消插入樣板", LINE_NO);
        } else if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_DOWN) {
            this.focus = size == 0 ? 0 : (this.focus + 1) % size;
            this.closeSwitchDropdown();
            this.fieldSelectionAnchor = -1;
            this.fieldSelectionCaret = -1;
        } else if (keyCode == GLFW.GLFW_KEY_UP) {
            this.focus = size == 0 ? 0 : (this.focus - 1 + size) % size;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            this.cycleParameter(this.focus, -1);
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_SPACE) {
            this.cycleParameter(this.focus, 1);
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (this.isTextFieldFocused()) {
                if (this.fieldHasSelection()) {
                    this.fieldDeleteSelection();
                } else {
                    String value = this.fieldValue(this.focus);
                    if (!value.isEmpty()) {
                        this.values.set(this.focus,
                                value.substring(0, value.length() - 1));
                    }
                }
            }
        }
    }
    private int switchDropdownVisible() {
        LuaSnippetLibrary.Parameter parameter = this.parameterAt(this.switchDropdownRow);
        return Math.max(1, Math.min(parameter.options().size(), SWITCH_DROPDOWN_MAX_ROWS));
    }
    private int[] switchDropdownRect() {
        if (!this.switchDropdownOpen || this.switchDropdownRow < 0) {
            return null;
        }
        int w = 200;
        int h = this.parameterDialogHeight();
        int left = (this.screen.width - w) / 2;
        int top = (this.screen.height - h) / 2;
        int rowY = top + parameterRowOffset + this.switchDropdownRow * PARAM_ROW_HEIGHT;
        int ddHeight = this.switchDropdownVisible() * LINE_HEIGHT + 4;
        return new int[]{left + 124, rowY + PARAM_ROW_HEIGHT, SWITCH_DROPDOWN_WIDTH, ddHeight};
    }
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
                this.values.get(row)));
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
    private boolean isTextFieldFocused() {
        return this.isOpen()
                && !this.params.isEmpty()
                && this.parameterAt(this.focus).kind()
                        == LuaSnippetLibrary.ParameterKind.TEXT
                && !this.values.isEmpty();
    }
    private String fieldValue(int index) {
        return index >= 0 && index < this.values.size()
                ? this.values.get(index) : "";
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
        String value = this.fieldValue(this.focus);
        int from = Math.max(0, Math.min(this.fieldSelectionFrom(), value.length()));
        int to = Math.max(0, Math.min(this.fieldSelectionTo(), value.length()));
        return value.substring(from, to);
    }
    private void fieldDeleteSelection() {
        if (!this.fieldHasSelection() || !this.isTextFieldFocused()) {
            return;
        }
        String value = this.fieldValue(this.focus);
        int from = Math.max(0, Math.min(this.fieldSelectionFrom(), value.length()));
        int to = Math.max(0, Math.min(this.fieldSelectionTo(), value.length()));
        this.values.set(this.focus, value.substring(0, from) + value.substring(to));
        this.fieldSelectionAnchor = from;
        this.fieldSelectionCaret = from;
    }
    private void fieldInsert(String text) {
        if (!this.isTextFieldFocused()) {
            return;
        }
        this.fieldDeleteSelection();
        String value = this.fieldValue(this.focus);
        if (this.fieldHasSelection()) {
            return;
        }
        int at = this.fieldSelectionCaret >= 0
                ? Math.max(0, Math.min(this.fieldSelectionCaret, value.length())) : value.length();
        this.values.set(this.focus,
                value.substring(0, at) + text + value.substring(at));
        int caret = at + text.length();
        this.fieldSelectionAnchor = caret;
        this.fieldSelectionCaret = caret;
    }
    private int fieldCharAt(double mouseX) {
        String value = this.fieldValue(this.focus);
        int valueX = this.parameterValueX();
        int target = (int) mouseX - valueX;
        int column = 0;
        while (column < value.length() && this.screen.font().width(value.substring(0, column + 1)) <= target) {
            column++;
        }
        return column;
    }
    private int parameterValueX() {
        return (this.screen.width - 300) / 2 + 124;
    }
    private LuaSnippetLibrary.Parameter parameterAt(int index) {
        return this.params.get(Math.max(0, Math.min(index, this.params.size() - 1)));
    }
    private void cycleParameter(int index, int direction) {
        if (index < 0 || index >= this.params.size()) {
            return;
        }
        LuaSnippetLibrary.Parameter parameter = this.params.get(index);
        String current = this.values.get(index);
        if (parameter.isBoolean()) {
            this.values.set(index, "true".equalsIgnoreCase(current) ? "false" : "true");
        } else if (parameter.isSwitch()) {
            List<String> options = parameter.options();
            int at = options.indexOf(current);
            at = at < 0 ? 0 : Math.floorMod(at + direction, options.size());
            this.values.set(index, options.get(at));
        }
    }
    private int parameterDialogHeight() {
        return 30 + this.params.size() * PARAM_ROW_HEIGHT + 16;
    }
    private int parameterRowAt(double mouseX, double mouseY) {
        if (!this.isOpen()) {
            return -1;
        }
        int w = 300;
        int h = this.parameterDialogHeight();
        int x = (this.screen.width - w) / 2;
        int y = (this.screen.height - h) / 2;
        if (mouseX < x || mouseX > x + w) {
            return -1;
        }
        int index = (int) ((mouseY - (y + parameterRowOffset)) / PARAM_ROW_HEIGHT);
        return index >= 0 && index < this.params.size() ? index : -1;
    }
    // ---- 供 screen 轉發的輸入事件 ----

    boolean keyPressed(int keyCode, boolean control, boolean shift) {
        if (!this.isOpen()) {
            return false;
        }
        this.handleParameterDialogKey(keyCode, control, shift);
        return true;
    }

    boolean charTyped(char codePoint) {
        if (!this.isOpen()) {
            return false;
        }
        if (codePoint >= 32 && this.isTextFieldFocused()) {
            this.fieldInsert(String.valueOf(codePoint));
        }
        return true;
    }

    boolean mouseClicked(double mouseX, double mouseY) {
        return this.isOpen() && this.handleClick(mouseX, mouseY);
    }

    boolean mouseDragged(double mouseX, double mouseY) {
        if (!this.isOpen()) {
            return false;
        }
        if (this.fieldSelecting && this.isTextFieldFocused()) {
            this.fieldSelectionCaret = this.fieldCharAt(mouseX);
            return true;
        }
        if (this.switchScrollbarDragging) {
            this.switchDropdownScroll = this.switchScrollFromMouse(mouseY);
            return true;
        }
        return false;
    }

    void mouseReleased() {
        this.fieldSelecting = false;
        this.switchScrollbarDragging = false;
    }

    boolean mouseScrolled(double scrollY) {
        if (!this.isOpen() || !this.switchDropdownOpen) {
            return false;
        }
        LuaSnippetLibrary.Parameter parameter = this.parameterAt(this.switchDropdownRow);
        int maxScroll = Math.max(0, parameter.options().size() - this.switchDropdownVisible());
        this.switchDropdownScroll = Math.max(0, Math.min(maxScroll,
                this.switchDropdownScroll - (int) Math.signum(scrollY)));
        return true;
    }

    /** 原本在 mouseClicked 裡的參數表單點擊邏輯。 */
    private boolean handleClick(double mouseX, double mouseY) {
        if (this.switchDropdownOpen) {
            if (this.onSwitchScrollbar(mouseX, mouseY)) {
                this.switchScrollbarDragging = true;
                this.switchDropdownScroll = this.switchScrollFromMouse(mouseY);
                return true;
            }
            int option = this.switchOptionAt(mouseX, mouseY);
            if (option >= 0) {
                this.values.set(this.switchDropdownRow,
                        this.parameterAt(this.switchDropdownRow).options().get(option));
                this.closeSwitchDropdown();
                return true;
            }
            this.closeSwitchDropdown();
        }
        int row = this.parameterRowAt(mouseX, mouseY);
        if (row >= 0) {
            this.focus = row;
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
                this.openSwitchDropdown(row);
            } else if (parameter.isBoolean()) {
                this.cycleParameter(row, 1);
            }
        }
        return true;
    }
}
