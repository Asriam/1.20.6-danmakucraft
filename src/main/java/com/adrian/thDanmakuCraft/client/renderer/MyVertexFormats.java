package com.adrian.thDanmakuCraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class MyVertexFormats {
    public static final VertexFormat POSITION_COLOR_COLOR_TEX = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                    .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
                    .put("Color2", DefaultVertexFormat.ELEMENT_COLOR)
                    .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
                    .build()
    );

    public static final VertexFormat POSITION_NORMAL = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                    .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
                    .build()
    );

    public static final VertexFormat POSITION_NORMAL_COLOR = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                    .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
                    .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
                    .build()
    );
}
