package com.adrian.thDanmakuCraft.client.gui.editor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * 腳本檔案的存取工具（與 GUI 分離，方便單獨測試）。
 *
 * <p>三種來源：</p>
 * <ul>
 *   <li>{@code config/thdanmakucraft/scripts/} — 可編輯的使用者腳本</li>
 *   <li>{@code data/thdanmakucraft/lua/apidoc/} — jar 內的 API 文件（唯讀）</li>
 *   <li>{@code data/thdanmakucraft/lua/lib/} — jar 內的 Lua 工具庫（唯讀）</li>
 * </ul>
 *
 * <p>唯讀來源的檔案清單是<b>掃描出來的</b>，不是硬編碼；jar 與開發目錄兩種形式都支援。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class LuaScriptFiles {

    private static final int MAX_DEPTH = 6;

    /**
     * 一個唯讀來源。
     * @param name 顯示前綴（同時是檔案管理器的分類名稱）
     * @param dir  相對於 classpath 根的路徑
     */
    public record BuiltinRoot(String name, String dir) {
    }

    /** 唯讀來源，順序決定檔案管理器裡的分類順序。 */
    public static final List<BuiltinRoot> BUILTIN_ROOTS = List.of(
            new BuiltinRoot("apidoc", "data/thdanmakucraft/lua/apidoc"),
            new BuiltinRoot("lib", "data/thdanmakucraft/lua/lib"),
            new BuiltinRoot("lua53", "data/thdanmakucraft/lua/lua53")
    );

    private static List<String> cachedBuiltinFiles;

    private LuaScriptFiles() {
    }

    public static Path scriptsDir() {
        return FMLPaths.CONFIGDIR.get().resolve("thdanmakucraft").resolve("scripts");
    }

    /**
     * 檔案管理器的一列。
     * @param path    顯示路徑（唯讀來源帶 {@code apidoc/} 或 {@code lib/} 前綴）
     * @param builtin true 表示 jar 內的唯讀檔案
     */
    public record Entry(String path, boolean builtin) {
        /** 分類鍵：腳本回傳空字串，唯讀檔回傳前綴（apidoc / lib）。 */
        public String category() {
            if (!this.builtin) {
                return "";
            }
            int slash = this.path.indexOf('/');
            return slash > 0 ? this.path.substring(0, slash) : this.path;
        }
    }

    // ------------------------------------------------------------------ 唯讀來源

    /** 所有唯讀檔案的顯示路徑，依 {@link #BUILTIN_ROOTS} 順序。 */
    public static synchronized List<String> builtinFiles() {
        if (cachedBuiltinFiles != null) {
            return cachedBuiltinFiles;
        }
        List<String> all = new ArrayList<>();
        for (BuiltinRoot root : BUILTIN_ROOTS) {
            for (String relative : filesUnder(root)) {
                all.add(root.name() + "/" + relative);
            }
        }
        cachedBuiltinFiles = Collections.unmodifiableList(all);
        return cachedBuiltinFiles;
    }

    /** 列出某個唯讀來源底下的 .lua（相對路徑，已排序）。 */
    public static List<String> filesUnder(BuiltinRoot root) {
        List<String> found = new ArrayList<>();
        Path dir = resolveBuiltinDir(root);
        if (dir != null) {
            collectLua(dir, found);
        }
        if (found.isEmpty()) {
            collectFromClasspath(root.dir(), found);
        }
        found.sort(String::compareToIgnoreCase);
        return found;
    }

    /** 讀取唯讀檔案；{@code displayPath} 需帶前綴（例如 {@code lib/math/vec3.lua}）。 */
    public static String readBuiltin(String displayPath) throws IOException {
        for (BuiltinRoot root : BUILTIN_ROOTS) {
            String prefix = root.name() + "/";
            if (displayPath.startsWith(prefix)) {
                return readBuiltin(root, displayPath.substring(prefix.length()));
            }
        }
        return readResource(BUILTIN_ROOTS.get(0).dir() + "/" + displayPath);
    }

    public static String readBuiltin(BuiltinRoot root, String relative) throws IOException {
        return readResource(root.dir() + "/" + relative);
    }

    private static String readResource(String classpathPath) throws IOException {
        String resource = "/" + classpathPath;
        try (java.io.InputStream in = LuaScriptFiles.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("找不到內建檔案 " + classpathPath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** 開發環境走目錄、正式環境走 jar，都交給 Forge 的 mod 檔案介面。 */
    private static Path resolveBuiltinDir(BuiltinRoot root) {
        try {
            return net.neoforged.fml.ModList.get()
                    .getModFileById("thdanmakucraft").getFile()
                    .findResource(root.dir().split("/"));
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void collectLua(Path dir, List<String> out) {
        if (dir == null || !Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(dir, MAX_DEPTH)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isLua(path.getFileName().toString()))
                    .forEach(path -> out.add(dir.relativize(path).toString().replace('\\', '/')));
        } catch (IOException | RuntimeException ignored) {
            // 讀不到目錄就當作沒有檔案
        }
    }

    /** classpath 後備（單元測試 / 非 Forge 環境），支援目錄與 jar 兩種 URL。 */
    private static void collectFromClasspath(String classpathDir, List<String> out) {
        java.net.URL url = LuaScriptFiles.class.getClassLoader().getResource(classpathDir);
        if (url == null) {
            return;
        }
        try {
            if ("file".equals(url.getProtocol())) {
                collectLua(Path.of(url.toURI()), out);
            } else if ("jar".equals(url.getProtocol())) {
                String spec = url.getPath();
                int separator = spec.indexOf("!/");
                java.net.URI jarUri = java.net.URI.create(spec.substring(0, separator));
                try (java.nio.file.FileSystem fs = java.nio.file.FileSystems.newFileSystem(jarUri,
                        Collections.emptyMap())) {
                    collectLua(fs.getPath(classpathDir), out);
                }
            }
        } catch (Exception ignored) {
            // 掃不到就留空，編輯器會顯示 0 個
        }
    }

    // ------------------------------------------------------------------ 檔案清單

    /** 可編輯的腳本 + 唯讀來源，腳本在前。 */
    public static List<Entry> listAll() {
        List<Entry> entries = new ArrayList<>();
        for (String path : list(scriptsDir())) {
            entries.add(new Entry(path, false));
        }
        for (String path : builtinFiles()) {
            entries.add(new Entry(path, true));
        }
        return entries;
    }

    /** 列出 {@code root} 下所有 .lua，排序並以 / 分隔。 */
    public static List<String> list(Path root) {
        List<String> found = new ArrayList<>();
        if (!Files.isDirectory(root)) {
            return found;
        }
        try (Stream<Path> stream = Files.walk(root, MAX_DEPTH)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isLua(path.getFileName().toString()))
                    .forEach(path -> found.add(root.relativize(path).toString().replace('\\', '/')));
        } catch (IOException ignored) {
            // 讀不到就當作空清單
        }
        found.sort(String::compareToIgnoreCase);
        return found;
    }

    // ------------------------------------------------------------------ 一般檔案

    /** 列出 {@code root} 下所有子目錄（相對路徑，已排序）。 */
    public static List<String> listDirs(Path root) {
        List<String> found = new ArrayList<>();
        if (!Files.isDirectory(root)) {
            return found;
        }
        try (Stream<Path> stream = Files.walk(root, MAX_DEPTH)) {
            stream.filter(Files::isDirectory)
                    .filter(path -> !path.equals(root))
                    .forEach(path -> found.add(root.relativize(path).toString().replace('\\', '/')));
        } catch (IOException ignored) {
            // 讀不到就當作空清單
        }
        found.sort(String::compareToIgnoreCase);
        return found;
    }

    /** 在 {@code root} 下建立資料夾（可多層，例如 a/b）。 */
    public static Path createDirectory(Path root, String name) throws IOException {
        Path target = root.resolve(name);
        Files.createDirectories(target);
        return target;
    }

    /** 遞迴刪除資料夾（含內容）；回傳是否真的刪掉。 */
    public static boolean deleteDirectory(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return false;
        }
        try (Stream<Path> stream = Files.walk(dir)) {
            List<Path> paths = stream.sorted(java.util.Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
        return !Files.exists(dir);
    }

    /** 把使用者輸入正規化成檔名：去空白、統一斜線、補上 .lua。 */
    public static String normalizeName(String input) {
        if (input == null) {
            return "";
        }
        String name = input.trim().replace('\\', '/');
        if (name.isEmpty()) {
            return "";
        }
        if (!isLua(name)) {
            name = name + ".lua";
        }
        return name;
    }

    /** 副檔名判斷不分大小寫（Windows 上 x.Lua 也是 lua 檔）。 */
    public static boolean isLua(String name) {
        return name.toLowerCase(Locale.ROOT).endsWith(".lua");
    }

    /** 改名／搬移；目標已存在時不動並回傳 false。 */
    public static boolean rename(Path from, Path to) throws IOException {
        if (Files.exists(to)) {
            return false;
        }
        Path parent = to.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.move(from, to);
        return true;
    }

    /** 把檔案搬到某個資料夾（同名已存在就不動）。 */
    public static boolean move(Path file, Path targetDir) throws IOException {
        Path target = targetDir.resolve(file.getFileName().toString());
        if (Files.exists(target)) {
            return false;
        }
        Files.createDirectories(targetDir);
        Files.move(file, target);
        return true;
    }

    /** 刪除檔案；回傳是否真的刪掉。 */
    public static boolean delete(Path file) throws IOException {
        return Files.deleteIfExists(file);
    }

    public static String read(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    public static void write(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }
}
