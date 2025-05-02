package com.adrian.thDanmakuCraft.client.renderer.shape;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CylinderVertexHelper implements ShapeVertexHelper{

    private final Matrix4f pose;
    private final Matrix3f normal;
    private final Vector3f offsetPosition;
    private final float radius;
    private final float length;
    private final int edge;
    private final boolean isClosed;

    public CylinderVertexHelper(Matrix4f pose, Matrix3f normal, Vector3f offsetPosition, float radius, float length, int edge, boolean isClosed) {
        this.pose = pose;
        this.normal = normal;
        this.offsetPosition = offsetPosition;
        this.radius = radius;
        this.length = length;
        this.edge = edge;
        this.isClosed = isClosed;
    }

    @Override
    public void vertexs(VertexHelper helper1, VertexHelper helper2, VertexHelper helper3, VertexHelper helper4) {

    }
}
