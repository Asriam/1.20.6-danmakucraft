package com.adrian.thDanmakuCraft.client.renderer;


import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class MyShaderInstance extends ShaderInstance {
    private BlendMode blendMode;

    public MyShaderInstance(ResourceProvider resourceProvider, ResourceLocation shaderLocation, VertexFormat vertexFormat) throws IOException {
        super(resourceProvider, shaderLocation, vertexFormat);
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    @Override
    public void apply() {
        super.apply();
        if (this.blendMode != null) {
            this.blendMode.apply();
        }
    }
}