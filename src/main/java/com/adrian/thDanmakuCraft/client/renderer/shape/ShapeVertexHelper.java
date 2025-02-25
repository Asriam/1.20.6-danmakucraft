package com.adrian.thDanmakuCraft.client.renderer.shape;

import com.adrian.thDanmakuCraft.util.Color;
import org.joml.Vector3f;

public abstract class ShapeVertexHelper {

    public abstract void vertex(VertexHelper helper);

    public interface VertexHelper {
        void vertex(Vector3f vertexPos, Vector3f normal, Color color);
    }
}
