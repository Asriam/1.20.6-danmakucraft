package com.adrian.thDanmakuCraft.client.gui.editor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.compiler.LuaC;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用 luaj 的編譯器做語法檢查（只解析、不執行）。
 *
 * <p>抽成獨立類別的理由和 {@link LuaApiIndex} 一樣：可以在沒有 Minecraft 的環境測試。
 * {@code LuaC.compile()} 對語法錯誤丟 {@link LuaError}，訊息形如
 * {@code chunkName:12: 'end' expected (to close 'function' at line 5)}，
 * 這裡把行號抽出來給編輯器標紅。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class LuaSyntaxChecker {

    /** @param line 1 起算的行號；-1 表示無法從訊息取得 */
    public record Result(boolean ok, int line, String message) {
        public static Result success() {
            return new Result(true, -1, "");
        }
    }

    private static final Pattern LINE_PATTERN = Pattern.compile(":(\\d+):");
    /** 訊息前綴，例如 {@code [string "demo.lua"]:4: } 或 {@code demo.lua:4: }。 */
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^.*?:\\d+:\\s*");

    private LuaSyntaxChecker() {
    }

    public static Result check(String source, String chunkName) {
        if (source == null || source.isEmpty()) {
            return Result.success();
        }
        String name = chunkName == null || chunkName.isEmpty() ? "script" : chunkName;
        try (InputStream in = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8))) {
            LuaC.instance.compile(in, name);
            return Result.success();
        } catch (LuaError error) {
            return new Result(false, extractLine(error.getMessage(), name), cleanMessage(error.getMessage(), name));
        } catch (IOException | RuntimeException error) {
            return new Result(false, -1, String.valueOf(error.getMessage()));
        }
    }

    private static int extractLine(String message, String name) {
        if (message == null) {
            return -1;
        }
        Matcher matcher = LINE_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        return -1;
    }

    /** 去掉訊息前面的 {@code [string "x.lua"]:4:} 這類前綴，狀態列才不會太長。 */
    private static String cleanMessage(String message, String name) {
        if (message == null) {
            return "";
        }
        String text = message;
        Matcher prefix = PREFIX_PATTERN.matcher(text);
        if (prefix.find()) {
            text = text.substring(prefix.end());
        }
        return text.replace('\n', ' ');
    }
}
