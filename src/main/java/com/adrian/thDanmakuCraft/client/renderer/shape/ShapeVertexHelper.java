package com.adrian.thDanmakuCraft.client.renderer.shape;

import com.adrian.thDanmakuCraft.util.Color;
import org.joml.Vector3f;

public interface ShapeVertexHelper {

    void vertex(VertexHelper helper);

    void vertexs(
            ShapeVertexHelper.VertexHelper helper1,
            ShapeVertexHelper.VertexHelper helper2,
            ShapeVertexHelper.VertexHelper helper3,
            ShapeVertexHelper.VertexHelper helper4);

    public interface VertexHelper {
        void vertex(Vector3f vertexPos, Vector3f normal, Color color);
    }
}
