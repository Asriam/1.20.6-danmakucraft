# 專案結構重構計畫

> 對象：`thdanmakucraft`（Minecraft 1.21.1 / Forge 52.1.20）
> 現況：127 個 Java 類別、13,965 行、`gradle build jarJar` 可通過
> 原則：**每個階段結束都必須能編譯並能進遊戲**；行為不變，只改結構。

---

## 0. 現況快照

| 指標 | 數值 |
|---|---|
| Java 類別 | 127 |
| 總行數 | 13,965 |
| 最大檔案 | `THObject.java` 1,365 行 |
| 前四巨型類別 | THObject 1365、THObjectContainer 727、AdditionalParameterManager 582、THCurvyLaser 450 |
| 最深套件 | 7 層（`client.renderer.danmaku.thobject.bullet`） |
| `util` 類別數 | 18（職責橫跨數學、NBT、資源、Lua 轉換） |
| 區塊註解死碼 | 262 行（5 個檔案） |
| 沒有任何引用的類別 | 28（其中多數是事件監聽器，屬正常） |
| 空類／殘留 | `BufferVertexBuilder`（6 行空類）、`handler/*`（各 4 行）、`ObjectToBytesTest`（測試類在 main） |

---

## 1. 診斷：五個結構問題

### 1.1 巨型類別把 5 種職責綁在一起
`THObject`（1,365 行）= 狀態資料 + 運動積分 + 碰撞判定 + 生命週期 + 序列化 + Lua API（927–1365 行）。
`THObjectContainer`（727 行）同理，Lua API 佔 491–727。

**後果**：任何一行改動都要重編譯整類；Lua API 一改就可能碰到物理邏輯；單元測試不可能。

### 1.2 套件邊界與架構分層不一致
- `world.danmaku` 同時放：領域物件、管理器、序列化 helper、Lua 輔助
- `init` 與 `registries` 職責重疊（一個註冊方塊/物品，一個註冊彈幕型別）
- `util` 是 18 個類的雜物袋，且被領域層反向依賴
- 渲染類別散在 `client.renderer.danmaku.thobject.{bullet,laser}` 四層深處

### 1.3 死碼與殘留物
被 `/* */` 包住的舊實作 262 行（`THObjectContainerRenderer` 86、`SmoothSetValueManager` 55、`MyEntityDataSerializers` 45、`THCurvyLaser` 41、`Events` 35）。
`TaskManagerDeprecated`、`MyByteBuffer`、`OcclusionCulling`、`RadixSort` 等無人引用。

### 1.4 命名沒有語意
`MyRenderTypes`、`MyShaderInstance`、`MyVertexFormats`、`MyByteBuffer`、`MyEntityDataSerializers`、`MyLayeredDraw` — 「My」不傳達任何資訊，且與原版類別只差一個前綴，容易誤用。

### 1.5 建置與工作區衛生
`build.gradle` 有兩個 `repositories {}`（第二個空的）、根目錄有 `hs_err_pid*.log`、`src/test/java` 是沙盒（`tsasda`、`jsTest`）、`.gitignore` 沒蓋到 `.port/`、`run/logs`、`crash-reports`。

---

## 2. 目標結構

```
com.adrian.thDanmakuCraft
├── THDanmakuCraftMod
├── registry/                    ← init + registries 合併
│   ├── ModBlocks / ModItems / ModEntities / ModTabs
│   └── THObjectTypes
├── danmaku/                     ← 領域核心（原本 world.danmaku）
│   ├── container/               THObjectContainer、ObjectManager、TargetManager、TaskScheduler
│   ├── object/                  THObject、THBullet、THLaser、THCurvyLaser、THParticle
│   ├── spellcard/
│   ├── physics/                 collision + motion（純計算，無 Minecraft 依賴）
│   └── io/                      序列化（ISerializable 實作、ByteBuffer、NBT 轉換）
├── script/                      LuaCore、LuaLoader、LuaManager
│   └── api/                     由巨型類別抽出的 LuaAPI（物件 / 容器 / 粒子）
├── client/
│   ├── render/                  RenderTypes、VertexFormats、ShaderLoader、LegacyBufferBuilder
│   ├── render/object/           各 THObject 渲染器
│   ├── render/entity/
│   ├── gui/                     overlay + editor
│   └── model/
├── network/                     packets + syncher
├── event/                       7 個事件類
├── mixin/
└── util/                        只剩純函式：math/、nbt/、resource/
```

**依賴規則（可自動檢查）**
1. `util` 不得 import `danmaku`、`client`、`world`
2. `danmaku` 不得 import `client`
3. `client` 可依賴 `danmaku`、`util`
4. `registry` 只被入口與 `danmaku` 引用

---

## 3. 分階段計畫

每階段獨立可交付、可回滾；建議一個階段一個 commit（或一個 branch）。

### S1 — 建置與衛生（預估 0.5 天，風險：低）

| # | 工作 | 檔案 |
|---|---|---|
| 1 | 刪除重複的空 `repositories {}` | `build.gradle` |
| 2 | `.gitignore` 補 `.port/`、`run/`（保留 `run/config` 範本）、`hs_err_pid*.log` | `.gitignore` |
| 3 | 移除根目錄 `hs_err_pid137396.log` | 根目錄 |
| 4 | `ObjectToBytesTest` 移到 `src/test/java`（它本來就是測試） | `src/main/.../ObjectToBytesTest.java` |
| 5 | 刪除 `BufferVertexBuilder`（空類）、`handler/ClientForgeHandler`、`ClientModHandler`（各 4 行、無引用） | 對應檔案 |
| 6 | 刪除 `TaskManagerDeprecated` | `util/` |
| 7 | 沙盒檔 `src/test/java/tsasda.java`、`jsTest.java` 移到 `docs/sandbox/` 或刪除 | `src/test` |

**驗證**：`gradle build jarJar` + `runClient` 進一局。
**回滾**：`git revert` 單一 commit。

> 死碼註解（262 行）**不建議直接刪**：裡面有被停用的替代實作（例如 `THObjectContainerRenderer` 的批次繪製方案）。
> 建議改成 `docs/legacy/` 的片段存檔，或在對應行留 `// LEGACY: 見 docs/legacy/xxx.md`。

### S2 — 命名與套件整理（預估 1–1.5 天，風險：中）

只動套件宣告與 import，不動類別內容。

| # | 工作 | 說明 |
|---|---|---|
| 1 | `My*` 改名：`MyRenderTypes`→`DanmakuRenderTypes`、`MyShaderInstance`→`DanmakuShaderInstance`、`MyVertexFormats`→`DanmakuVertexFormats`、`MyByteBuffer`→`LuaByteBuffer`、`MyEntityDataSerializers`→`THDataSerializers`、`MyLayeredDraw`→`GuiLayerStack` | 純改名，IDE 一次完成 |
| 2 | `init` + `registries` → `registry` | 5 + 2 個類別 |
| 3 | `util` 拆成 `util/math`（MathUtil、Matrix3x3d、OBB、RadixSort）、`util/nbt`（CompoundTagUtil、SerializeUtil）、`util/resource`（ResourceLoader、FileLoader、GZIPUtil）、`util/lua`（LuaValueUtil、LuaValueHelper） | 18 → 4 個子套件 |
| 4 | `client.renderer.danmaku.thobject.*` 拉平為 `client.render/object/*` | 少 2 層 |
| 5 | `world.danmaku` → `danmaku`（見目標樹） | 需與 S3 一起做才划算 |

**驗證**：每個子步驟編譯一次；最後 `build` + `runClient` 放一發符卡。
**風險**：Lua 腳本以**字串**引用類別方法名（`data/thdanmakucraft/lua/**.lua`），改名時只改 Java 類別名沒問題，但**不要改 public 方法名**。

### S3 — 巨型類別拆分（預估 2–3 天，風險：中高）

先拆 `THObject`（1,365 行），模式確認後再套用到其他類。

| 目標 | 抽出的內容 | 型態 |
|---|---|---|
| `object/Transform` | position / lastPosition / velocity / acceleration / rotation / scale（約 35–90 行欄位 + 相關方法） | 組合（composition），`THObject` 持有 |
| `object/Motion` | `tick()` 中的積分、加速度、阻尼 | 類別，輸入 Transform |
| `object/Lifecycle` | timer / lifetime / deathLastingTime / spawnAnimation | 類別 |
| `object/ObjectRenderState` | image / color / blend / shouldRender* 旗標 | 類別 |
| `object/CollisionShape` | bb / size / collisionType / OBB 計算 | 類別 |
| `script/api/THObjectLuaAPI` | 927–1365 行整個 `LuaAPI` 靜態類 | 獨立檔案（保留方法簽名） |

`THObjectContainer`（727 行）同理：
- 491–727 行的 `LuaAPI` → `script/api/THObjectContainerLuaAPI`
- `objectManager`、`targetUserManager`、`taskManager` 三者已是獨立類別，改成**建構子注入**而非直接 new，方便測試

`AdditionalParameterManager`（582 行）：157–350 的 `enum Type` 拆到 `parameter/ParameterType`，351–582 的 `Factory`/`Parameter` 拆到 `parameter/ParameterFactory`。

`THCurvyLaser`（450 行）：`NodeManager`（155–303）、`LaserNode`（304–397）本來就是獨立概念，直接提到 `object/laser/` 套件。

**驗證策略（關鍵）**：每拆一個類別就 `build`；拆 `LuaAPI` 時，用 `/spellcard test <key>` 跑既有 Lua 腳本回歸（`data/thdanmakucraft/lua/spellcard/*.lua`）。

### S4 — 架構護欄（預估 1 天，風險：低）

1. 在 `build.gradle` 加一個 `archCheck` task：掃描 import，違反第 2 節的 4 條規則就讓建置失敗。
2. 把「套件 → 允許依賴」寫成 `docs/architecture-rules.md`，與 `docs/architecture-spec.svg` 對齊。
3. 加一個簡單的 `check` 依賴：`build.dependsOn archCheck`。

範例（Gradle）：

```groovy
tasks.register('archCheck') {
    def src = fileTree('src/main/java') { include '**/*.java' }
    inputs.files(src)
    doLast {
        def violations = []
        src.each { f ->
            def pkg = f.path.replace('\\', '/')
            def text = f.getText('UTF-8')
            if (pkg.contains('/util/') && text =~ /import com\.adrian\.thDanmakuCraft\.(danmaku|client)\./)
                violations << "util 依賴上層: ${f.name}"
        }
        if (violations) throw new GradleException(violations.join('\n'))
    }
}
```

---

## 4. 驗收指標

| 指標 | 現在 | 目標 |
|---|---|---|
| 最大檔案行數 | 1,365 | ≤ 500 |
| 最深套件層數 | 7 | ≤ 5 |
| `util` 對上層的依賴 | 有（`Color` → `THObject`） | 0 |
| 巨型類別數（> 500 行） | 4 | 0 |
| 空類／死類 | 6 | 0 |
| 架構違規數 | 未量測 | 0（由 `archCheck` 把關） |

---

## 5. 風險與回滾

| 風險 | 影響 | 對策 |
|---|---|---|
| 拆 `LuaAPI` 時改到方法名 | Lua 腳本執行期才炸 | 只搬檔案、不改簽名；搬完跑 `/spellcard test` 回歸 |
| 套件搬遷漏改 import | 編譯失敗 | 每步 `gradle compileJava`；IDE 全域 rename |
| `@OnlyIn(Dist.CLIENT)` 類別被移到共用套件 | 伺服器端載入失敗 | 保留 `client/` 邊界，ArchCheck 禁止 `danmaku` 引用 `client` |
| mixin 目標類別被改動 | 執行期 mixin 失效 | `mixins` 套件不動；改動後確認 refmap 仍指向正確 SRG |
| 大規模重排導致 review 困難 | 難以回退 | 一階段一 branch、一類別一 commit |

---

## 6. 不建議動的地方

- **Lua API 的公開方法名**：被 `.lua` 腳本與 `apidoc/*.lua` 文件同時引用。
- **`mixins/` 套件位置**：`mixins.thdanmakucraft.json` 的 `package` 欄位綁死。
- **`client.renderer.compat.LegacyBufferBuilder`**：刻意模仿 1.20.6 的 API，命名保留 `Legacy` 是為了標示它是相容層。
- **shader 資源路徑**：`assets/thdanmakucraft/shaders/core/**` 被 shader JSON 的 `vertex`/`fragment` 欄位引用。
- **`data/thdanmakucraft/lua/**` 的目錄結構**：`LuaLoader` 以路徑掃描，改了要同步改載入器。

---

## 7. 建議執行順序

```
S1（半天，先做）→ 驗收 → S4 的 archCheck（先量基線）→ S2（分批）→ S3（逐類）
```

先做 S1 + S4 的度量，是因為它們**零行為風險**，而且能讓後續每一步都有自動護欄。
S3 建議從 `THCurvyLaser`（450 行、內部已是三個獨立概念）開始熱身，最後才動 `THObject`。
