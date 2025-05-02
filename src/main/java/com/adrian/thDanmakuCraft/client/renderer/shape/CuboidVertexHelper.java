package com.adrian.thDanmakuCraft.client.renderer.shape;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CuboidVertexHelper implements ShapeVertexHelper{

    private final Matrix4f pose;
    private final Matrix3f normal;
    private final Vector3f offsetPosition;
    private final Vector3f size;

    public CuboidVertexHelper(Matrix4f pose, Matrix3f normal, Vector3f offsetPosition, Vector3f size) {
        this.pose = pose;
        this.normal = normal;
        this.offsetPosition = offsetPosition;
        this.size = size;
    }

    @Override
    public void vertexs(VertexHelper helper1, VertexHelper helper2, VertexHelper helper3, VertexHelper helper4) {

    }
}
