package com.adrian.thDanmakuCraft.client.renderer.type;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class ModVertexFormats {

    /**
     * 第二條顏色通道（shader 裡的 {@code Color2}）。
     *
     * <p>1.21.1 的 {@code VertexFormat#getOffset()} 是以「元素 id」查表（offsetsByElement[id]），
     * 所以同一個 builtin COLOR 元素用兩次時，兩個 attribute 會拿到相同位移，
     * 第二顏色永遠等於第一顏色（貼圖子彈因此變成純白）。
     * 這裡註冊一個 id 不同的 UBYTE×4 / COLOR 元素，位移才會各自獨立。</p>
     */
    public static final VertexFormatElement COLOR2 = VertexFormatElement.register(20, 0,
            VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4);

    public static final VertexFormat POSITION_COLOR_COLOR_TEX = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("Color2", COLOR2)
            .add("UV0", VertexFormatElement.UV0)
            .build();

    public static final VertexFormat POSITION_NORMAL = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Normal", VertexFormatElement.NORMAL)
            .build();

    public static final VertexFormat POSITION_NORMAL_COLOR = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Normal", VertexFormatElement.NORMAL)
            .add("Color", VertexFormatElement.COLOR)
            .build();

    /**
     * ellipsoid 用的三個額外 vec3 通道。
     *
     * <p>1.21.1 的 {@code VertexFormat#getOffset(element)} 是「以 element id 查表」，
     * 所以若四個通道都用同一個 {@link VertexFormatElement#POSITION}（id 0），
     * 四個 attribute 的 offset 都會變成 0，Position2／Rotation／Scale 會讀到 Position 的值。
     * 因此這裡替它們註冊獨立的 id（20 已被 {@link #COLOR2} 用掉）。</p>
     */
    public static final VertexFormatElement POSITION2 = VertexFormatElement.register(21, 0,
            VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
    public static final VertexFormatElement ROTATION = VertexFormatElement.register(22, 0,
            VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
    public static final VertexFormatElement SCALE = VertexFormatElement.register(23, 0,
            VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);

    public static final VertexFormat POSITION_POSITION2_ROTATION_SCALE = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Position2", POSITION2)
            .add("Rotation", ROTATION)
            .add("Scale", SCALE)
            .build();
}
