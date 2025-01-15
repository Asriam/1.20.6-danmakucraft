package com.adrian.thDanmakuCraft.client.renderer;

public class OcclusionCulling {
    private final double camX;
    private final double camY;
    private final double camZ;

    public OcclusionCulling(double camX, double camY, double camZ) {
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;
    }
    public boolean isVisible(RenderUtil.Quad quad) {


        return false;
    }
}
