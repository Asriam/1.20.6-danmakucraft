package com.adrian.thDanmakuCraft.client.renderer.buffer;

import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.client.renderer.compat.LegacyBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.adrian.thDanmakuCraft.client.renderer.compat.LegacyBufferBuilder.normalIntValue;

@OnlyIn(Dist.CLIENT)
public class VertexBuilder {

    private final LegacyBufferBuilder builder;

    public VertexBuilder(VertexConsumer builder){
        this.builder = (LegacyBufferBuilder) builder;
    }

    public VertexBuilder vertex(double x, double y, double z){
        //builder.vertex(x,y,z);
        builder.putFloat(0, (float)x);
        builder.putFloat(4, (float)y);
        builder.putFloat(8, (float)z);
        builder.nextElement();
        return this;
    }

    public VertexBuilder vec4(double x, double y, double z, double w){
        //builder.vertex(x,y,z);
        builder.putFloat(0, (float)x);
        builder.putFloat(4, (float)y);
        builder.putFloat(8, (float)z);
        builder.putFloat(12, (float)w);
        builder.nextElement();
        return this;
    }

    public VertexBuilder vertex(Matrix4f pose, float x, float y, float z){
        Vector3f pos = pose.transformPosition(x, y, z, new Vector3f());
        //Vector3f pos = new Vector3f(x,y,z);
        return this.vertex(pos.x, pos.y, pos.z);
    }

    public VertexBuilder vertex(Vec3 vec3){
        return this.vertex(vec3.x,vec3.y,vec3.z);
    }

    public VertexBuilder vertex(Vector3f vec3){
        return this.vertex(vec3.x,vec3.y,vec3.z);
    }

    public VertexBuilder vertex(Matrix4f pose, Vector3f pos){
        return this.vertex(pose,pos.x,pos.y,pos.z);
    }

    public VertexBuilder color(int r, int g, int b, int a){
        //builder.color(r,g,b,a);
        builder.putByte(0, (byte)r);
        builder.putByte(1, (byte)g);
        builder.putByte(2, (byte)b);
        builder.putByte(3, (byte)a);
        builder.nextElement();
        return this;
    }

    public VertexBuilder color(float r, float g, float b, float a){
        builder.color(r,g,b,a);
        return this;
    }

    public VertexBuilder color(Color color){
        return this.color(color.r,color.g,color.b,color.a);
    }

    public VertexBuilder uv(float u, float v){
        //builder.uv(u,v);
        builder.putFloat(0, u);
        builder.putFloat(4, v);
        builder.nextElement();
        return this;
    }

    public VertexBuilder normal(float nx, float ny, float nz){
        //builder.normal(nx,ny,nz);
        builder.putByte(0, normalIntValue(nx));
        builder.putByte(1, normalIntValue(ny));
        builder.putByte(2, normalIntValue(nz));
        builder.nextElement();
        return this;
    }

    public VertexBuilder normal(Vector3f normal){
        return this.normal(normal.x, normal.y, normal.z);
    }

    public VertexBuilder normal(Matrix3f pose_normal, float nx, float ny, float nz){
        Vector3f nom = transformNormal(pose_normal, nx, ny, nz);
        return this.normal(nom.x,nom.y,nom.z);
    }

    public VertexBuilder normal(Matrix3f pose_normal, Vector3f normal){
        Vector3f nom = transformNormal(pose_normal, normal);
        //Vector3f nom = new Vector3f(normal);
        return this.normal(nom.x,nom.y,nom.z);
    }

    public void endVertex(){
        builder.endVertex();
    }
    public void positionColor(
            Matrix4f pose, float x, float y, float z,
            int r, int g, int b, int a){
        this.vertex(pose,x,y,z).color(r,g,b,a).endVertex();
    }

    public void positionColor(
            Matrix4f pose, Vector3f pos,
            Color color){
        positionColor(pose, pos.x, pos.y, pos.z,color.r,color.g,color.b,color.a);
    }

    public void positionColorUV(
            Matrix4f pose, float x, float y, float z,
            int r, int g, int b, int a,
            float u, float v){
        this.vertex(pose,x,y,z).color(r,g,b,a).uv(u,v).endVertex();
    }

    public void positionColorUV(
            Matrix4f pose, float x, float y, float z,
            Color color,
            float u, float v) {
        positionColorUV(pose, x, y, z, color.r, color.g, color.b, color.a, u, v);
    }

    public void positionColorUV(
            Matrix4f pose, Vector3f pos,
            Color color,
            float u, float v){
        positionColorUV(pose, pos.x, pos.y, pos.z,color,u,v);
    }

    public void positionUVColor(
            Matrix4f pose, float x, float y, float z,
            float u, float v,
            int r, int g, int b, int a){
        this.vertex(pose,x,y,z).uv(u,v).color(r,g,b,a).endVertex();
    }

    public void positionUVColor(
            Matrix4f pose, float x, float y, float z,
            float u, float v,
            Color color){
        positionUVColor(pose, x, y, z, u ,v ,color.r, color.g, color.b, color.a);
    }

    public void positionUVColor(
            Matrix4f pose, Vector3f pos,
            float u, float v,
            Color color){
        positionUVColor(pose, pos.x, pos.y, pos.z, u ,v ,color.r, color.g, color.b, color.a);
    }
    public void positionColorColorUV(
            Matrix4f pose, float x, float y, float z,
            int r, int g, int b, int a,
            int r2, int g2, int b2, int a2,
            float u, float v){
        this.vertex(pose,x,y,z).color(r,g,b,a).color(r2,g2,b2,a2).uv(u,v).endVertex();
    }

    public void positionColorColorUV(
            Matrix4f pose, float x, float y, float z,
            Color color,
            Color color2,
            float u, float v){
        positionColorColorUV(pose, x, y, z,color.r,color.g,color.b,color.a,color2.r,color2.g,color2.b,color2.a,u,v);
    }

    public void positionColorColorUV(
            Matrix4f pose, Vector3f pos,
            Color color,
            Color color2,
            float u, float v){
        positionColorColorUV(pose, pos.x, pos.y, pos.z,color,color2,u,v);
    }

    public void positionColorColorUvUvNormal(
            Matrix4f pose, float x, float y, float z,
            int r, int g, int b, int a,
            int r2, int g2, int b2, int a2,
            float u, float v,
            float u2, float v2,
            float nx, float ny, float nz){
        this.vertex(pose,x,y,z).color(r,g,b,a).color(r2,g2,b2,a2).uv(u,v).uv(u2,v2).normal(nx,ny,nz).endVertex();
    }

    public void positionColorColorUvUvNormal(
            Matrix4f pose, float x, float y, float z,
            Color color,
            Color color2,
            float u, float v,
            float u2, float v2,
            float nx, float ny, float nz){
        positionColorColorUvUvNormal(pose, x, y, z,color.r,color.g,color.b,color.a,color2.r,color2.g,color2.b,color2.a,u,v,u2,v2,nx,ny,nz);
    }

    public void positionColorColorUvUvNormal(
            Matrix4f pose, float x, float y, float z,
            Color color,
            Color color2,
            float u, float v,
            float u2, float v2,
            Matrix3f pose_normal, float nx, float ny, float nz){
        Vector3f nom = transformNormal(pose_normal, nx, ny, nz);
        positionColorColorUvUvNormal(pose, x, y, z,color,color2,u,v,u2,v2,nom.x,nom.y,nom.z);
    }

    public void positionColorColorUvUvNormal(
            Matrix4f pose, Vector3f pos,
            Color color,
            Color color2,
            float u, float v,
            float u2, float v2,
            Vector3f normal){
        positionColorColorUvUvNormal(pose, pos.x, pos.y, pos.z,color.r,color.g,color.b,color.a,color2.r,color2.g,color2.b,color2.a,u,v,u2,v2,normal.x,normal.y,normal.z);
    }
    public void positionColorColorUvUvNormal(
            Matrix4f pose, Vector3f pos,
            Color color,
            Color color2,
            float u, float v,
            float u2, float v2,
            Matrix3f pose_normal, Vector3f normal){
        Vector3f nom = transformNormal(pose_normal, normal);
        positionColorColorUvUvNormal(pose,pos,color,color2,u,v,u2,v2,nom);
    }

    public static Vector3f transformNormal(Matrix3f pose_normal, float x, float y, float z) {
        return pose_normal.transform(new Vector3f(x, y, z));
    }

    public static Vector3f transformNormal(Matrix3f pose_normal, Vector3f normal) {
        return pose_normal.transform(normal, new Vector3f());
    }
}
