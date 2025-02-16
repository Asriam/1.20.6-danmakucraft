package com.adrian.thDanmakuCraft.client.renderer;

import com.adrian.thDanmakuCraft.util.Color;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class VertexBuilder {

    private final VertexConsumer builder;

    public VertexBuilder(VertexConsumer builder){
        this.builder = builder;
    }
    public void vertexPositionColor(
            Matrix4f pose, float x, float y, float z,
            int r, int g, int b, int a){
        builder.vertex(pose,x,y,z).color(r,g,b,a).endVertex();
    }

    public void vertexPositionColor(
            Matrix4f pose, Vector3f pos,
            Color color){
        vertexPositionColor(pose, pos.x, pos.y, pos.z,color.r,color.g,color.b,color.a);
    }

    public void vertexPositionColorUV(
            Matrix4f pose, float x, float y, float z,
            int r, int g, int b, int a,
            float u, float v){
        builder.vertex(pose,x,y,z).color(r,g,b,a).uv(u,v).endVertex();
    }

    public void vertexPositionColorUV(
            Matrix4f pose, float x, float y, float z,
            Color color,
            float u, float v) {
        vertexPositionColorUV(pose, x, y, z, color.r, color.g, color.b, color.a, u, v);
    }

    public void vertexPositionColorUV(
            Matrix4f pose, Vector3f pos,
            Color color,
            float u, float v){
        vertexPositionColorUV(pose, pos.x, pos.y, pos.z,color,u,v);
    }

    public void vertexPositionColorColorUV(
            Matrix4f pose, float x, float y, float z,
            int r, int g, int b, int a,
            int r2, int g2, int b2, int a2,
            float u, float v){
        builder.vertex(pose,x,y,z).color(r,g,b,a).color(r2,g2,b2,a2).uv(u,v).endVertex();
    }

    public void vertexPositionColorColorUV(
            Matrix4f pose, float x, float y, float z,
            Color color,
            Color color2,
            float u, float v){
        vertexPositionColorColorUV(pose, x, y, z,color.r,color.g,color.b,color.a,color2.r,color2.g,color2.b,color2.a,u,v);
    }

    public void vertexPositionColorColorUV(
            Matrix4f pose, Vector3f pos,
            Color color,
            Color color2,
            float u, float v){
        vertexPositionColorColorUV(pose, pos.x, pos.y, pos.z,color,color2,u,v);
    }
}
