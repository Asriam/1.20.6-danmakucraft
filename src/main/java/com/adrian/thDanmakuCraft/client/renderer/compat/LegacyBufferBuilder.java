package com.adrian.thDanmakuCraft.client.renderer.compat;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * 1.20.6 風格的頂點緩衝建構器（相容層）。
 *
 * <p>Minecraft 1.21 移除了 {@code com.mojang.blaze3d.vertex.BufferBuilder} 的低階寫入 API
 * （{@code begin/end/putFloat/putByte/nextElement/endVertex} 與 {@code (int)} 建構子），改為
 * {@code ByteBufferBuilder + MeshData}。本類別在 1.21.1 上重現舊介面，讓既有的彈幕渲染程式碼
 * 可以原樣運作，特別是「同一個頂點格式中包含兩個 COLOR 與多組 UV」這種官方 VertexConsumer
 * 無法表達的自訂配置。</p>
 *
 * <p>產出的 {@link MeshData} 可直接交給 {@code BufferUploader.drawWithShader(...)}，行為與舊版
 * {@code BufferBuilder.end()} 回傳的 RenderedBuffer 相同。</p>
 */
public class LegacyBufferBuilder implements VertexConsumer {

    private static final LegacyBufferBuilder THREAD_BUILDER = new LegacyBufferBuilder(4096);

    private ByteBuffer data;
    private int capacity;

    private VertexFormat.Mode mode = VertexFormat.Mode.QUADS;
    private VertexFormat format;
    private int vertexSize = 4;
    private List<VertexFormatElement> elements;
    private int[] elementOffsets;

    private int vertexCount;
    private int vertexStart;
    private int elementIndex;
    private int elementCount;

    public LegacyBufferBuilder(int initialCapacityBytes) {
        this.capacity = Math.max(256, initialCapacityBytes);
        this.data = ByteBuffer.allocate(this.capacity);
        this.data.order(ByteOrder.nativeOrder());
    }

    /** 取得渲染執行緒共用的實例（對應舊版的 Tesselator#getBuilder）。 */
    public static LegacyBufferBuilder threadBuilder() {
        return THREAD_BUILDER;
    }

    public LegacyBufferBuilder getBuilder() {
        return this;
    }

    public void begin(VertexFormat.Mode mode, VertexFormat format) {
        this.mode = mode;
        this.format = format;
        this.elements = format.getElements();
        this.vertexSize = format.getVertexSize();
        this.elementCount = this.elements.size();
        this.elementOffsets = new int[this.elementCount];
        int offset = 0;
        for (int i = 0; i < this.elements.size(); i++) {
            this.elementOffsets[i] = offset;
            offset += this.elements.get(i).byteSize();
        }
        this.vertexCount = 0;
        this.vertexStart = 0;
        this.elementIndex = 0;
        this.data.clear();
    }

    private void ensureCapacity(int needed) {
        if (needed <= this.capacity) {
            return;
        }
        int newCapacity = this.capacity;
        while (newCapacity < needed) {
            newCapacity *= 2;
        }
        ByteBuffer grown = ByteBuffer.allocate(newCapacity);
        grown.order(ByteOrder.nativeOrder());
        this.data.position(0);
        this.data.limit(this.capacity);
        grown.put(this.data);
        this.data = grown;
        this.capacity = newCapacity;
    }

    private int elementOffset(int index) {
        if (this.elementOffsets == null || index < 0 || index >= this.elementOffsets.length) {
            return 0;
        }
        return this.elementOffsets[index];
    }

    private int offsetOf(VertexFormatElement element) {
        if (this.elements == null) {
            return 0;
        }
        for (int i = 0; i < this.elements.size(); i++) {
            if (this.elements.get(i).equals(element)) {
                return this.elementOffsets[i];
            }
        }
        return 0;
    }

    private void ensureVertex() {
        ensureCapacity(this.vertexStart + this.vertexSize);
    }

    private void putFloatAt(int absoluteOffset, float value) {
        ensureVertex();
        this.data.putFloat(absoluteOffset, value);
    }

    private void putByteAt(int absoluteOffset, byte value) {
        ensureVertex();
        this.data.put(absoluteOffset, value);
    }

    // ------------------------------------------------------------------ 舊版低階 API

    public LegacyBufferBuilder putFloat(int index, float value) {
        if (this.elementIndex >= this.elementCount) {
            warnOverrun();
            return this;
        }
        putFloatAt(this.vertexStart + elementOffset(this.elementIndex) + index, value);
        return this;
    }

    public LegacyBufferBuilder putByte(int index, byte value) {
        if (this.elementIndex >= this.elementCount) {
            warnOverrun();
            return this;
        }
        putByteAt(this.vertexStart + elementOffset(this.elementIndex) + index, value);
        return this;
    }

    public void nextElement() {
        if (this.elementIndex < this.elementCount) {
            this.elementIndex++;
        }
    }

    /**
     * 結束一個頂點：把「這個格式有、但呼叫端沒寫到」的元素補成 0。
     * 緩衝是跨幀重複使用的，不補零會把上一顆子彈殘留的位元組（例如 UV）留在顏色通道裡。
     */
    public void endVertex() {
        if (this.elementIndex < this.elementCount) {
            int from = this.vertexStart + elementOffset(this.elementIndex);
            int to = this.vertexStart + this.vertexSize;
            for (int i = from; i < to; i++) {
                this.data.put(i, (byte) 0);
            }
            this.elementIndex = this.elementCount;
        }
        this.vertexCount++;
        this.vertexStart += this.vertexSize;
        this.elementIndex = 0;
    }

    private static boolean warnedOverrun;

    private static void warnOverrun() {
        if (!warnedOverrun) {
            warnedOverrun = true;
            org.slf4j.LoggerFactory.getLogger("thdanmakucraft/compat")
                    .warn("[thdanmakucraft] 有頂點寫入超出目前 VertexFormat 的元素數量，已忽略（避免覆蓋 Position）。");
        }
    }

    public LegacyBufferBuilder vertex(double x, double y, double z) {
        putFloat(0, (float) x);
        putFloat(4, (float) y);
        putFloat(8, (float) z);
        nextElement();
        return this;
    }

    public LegacyBufferBuilder vertex(float x, float y, float z) {
        return vertex((double) x, (double) y, (double) z);
    }

    public LegacyBufferBuilder vertex(Matrix4f pose, float x, float y, float z) {
        Vector3f pos = pose.transformPosition(x, y, z, new Vector3f());
        return vertex(pos.x, pos.y, pos.z);
    }

    public LegacyBufferBuilder color(int r, int g, int b, int a) {
        putByte(0, (byte) r);
        putByte(1, (byte) g);
        putByte(2, (byte) b);
        putByte(3, (byte) a);
        nextElement();
        return this;
    }

    public LegacyBufferBuilder color(float r, float g, float b, float a) {
        return color((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F), (int) (a * 255.0F));
    }

    public LegacyBufferBuilder uv(float u, float v) {
        putFloat(0, u);
        putFloat(4, v);
        nextElement();
        return this;
    }

    public LegacyBufferBuilder normal(float nx, float ny, float nz) {
        putByte(0, normalIntValue(nx));
        putByte(1, normalIntValue(ny));
        putByte(2, normalIntValue(nz));
        nextElement();
        return this;
    }

    public LegacyBufferBuilder normal(Matrix3f pose, float nx, float ny, float nz) {
        Vector3f n = pose.transform(new Vector3f(nx, ny, nz));
        return normal(n.x, n.y, n.z);
    }

    /** 與 1.20.6 的 {@code BufferVertexConsumer.normalIntValue} 等價。 */
    public static byte normalIntValue(float value) {
        return (byte) ((int) (value * 127.0F) & 0xFF);
    }

    // ------------------------------------------------------------------ VertexConsumer

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        putFloat(0, x);
        putFloat(4, y);
        putFloat(8, z);
        nextElement();
        return this;
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int packedLight, int packedOverlay, float nx, float ny, float nz) {
        addVertex(x, y, z);
        setColor(color);
        setUv(u, v);
        setUv1(packedLight & 0xFFFF, packedLight >> 16 & 0xFFFF);
        setUv2(packedOverlay & 0xFFFF, packedOverlay >> 16 & 0xFFFF);
        setNormal(nx, ny, nz);
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        int offset = this.vertexStart + offsetOf(VertexFormatElement.COLOR);
        putByteAt(offset, (byte) r);
        putByteAt(offset + 1, (byte) g);
        putByteAt(offset + 2, (byte) b);
        putByteAt(offset + 3, (byte) a);
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        return setColor(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        int offset = this.vertexStart + offsetOf(VertexFormatElement.UV0);
        putFloatAt(offset, u);
        putFloatAt(offset + 4, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        int offset = this.vertexStart + offsetOf(VertexFormatElement.UV1);
        putByteAt(offset, (byte) u);
        putByteAt(offset + 1, (byte) (u >> 8));
        putByteAt(offset + 2, (byte) v);
        putByteAt(offset + 3, (byte) (v >> 8));
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        int offset = this.vertexStart + offsetOf(VertexFormatElement.UV2);
        putByteAt(offset, (byte) u);
        putByteAt(offset + 1, (byte) (u >> 8));
        putByteAt(offset + 2, (byte) v);
        putByteAt(offset + 3, (byte) (v >> 8));
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        int offset = this.vertexStart + offsetOf(VertexFormatElement.NORMAL);
        putByteAt(offset, normalIntValue(x));
        putByteAt(offset + 1, normalIntValue(y));
        putByteAt(offset + 2, normalIntValue(z));
        return this;
    }

    @Override
    public VertexConsumer setOverlay(int packedOverlay) {
        return setUv1(packedOverlay & 0xFFFF, packedOverlay >> 16 & 0xFFFF);
    }

    @Override
    public VertexConsumer setLight(int packedLight) {
        return setUv2(packedLight & 0xFFFF, packedLight >> 16 & 0xFFFF);
    }

    /** 目前緩衝內已完成的頂點數（診斷用）。 */
    public int vertexCount() {
        return this.vertexCount;
    }

    // ------------------------------------------------------------------ 產出

    /** 對應舊版 {@code end()}：把目前的頂點資料封裝成可直接上傳的 {@link MeshData}。 */
    public MeshData end() {
        int bytes = this.vertexCount * this.vertexSize;
        ByteBufferBuilder builder = new ByteBufferBuilder(Math.max(bytes, 256));
        if (bytes > 0) {
            long pointer = builder.reserve(bytes);
            ByteBuffer destination = MemoryUtil.memByteBuffer(pointer, bytes);
            ByteBuffer source = this.data.duplicate();
            source.position(0);
            source.limit(bytes);
            destination.put(source);
        } else {
            // 1.21.1 的 ByteBufferBuilder#build() 在「自上次 build 後沒有任何寫入」時會回傳 null，
            // 直接塞進 MeshData 會讓 vertexBuffer 為 null，繪製時就 NPE。
            // 空批次（例如整批被視錐剔除）在這裡保留一個頂點的空間，讓它變成合法但不繪製的空網格。
            builder.reserve(Math.max(4, this.vertexSize));
        }
        MeshData.DrawState state = new MeshData.DrawState(this.format, this.vertexCount,
                this.mode.indexCount(this.vertexCount), this.mode, VertexFormat.IndexType.least(this.vertexCount));
        return new MeshData(builder.build(), state);
    }
}
