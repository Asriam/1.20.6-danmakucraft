package com.adrian.thDanmakuCraft.world.danmaku;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.compress.utils.Lists;
import org.joml.Vector2f;

import java.util.List;

public class Image implements IImage {

    private final ResourceLocation textureLocation;
    private final Vec2 uvStart;
    private final Vec2 size;

    public Image(ResourceLocation textureLocation, Vec2 uvStart, Vec2 size) {
        this.textureLocation = textureLocation;
        this.uvStart = uvStart;
        this.size = size;
    }

    public Image(ResourceLocation textureLocation, float uStart, float vStart, float width, float height) {
        this(textureLocation, new Vec2(uStart, vStart), new Vec2(width, height));
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public Vec2 getUVStart() {
        return uvStart;
    }

    public Vec2 getUVEnd() {
        return uvStart.add(size);
    }

    public Vec2 getSize(){
        return size;
    }
    @Override
    public Image getImage(int index) {
        return this;
    }

    public static class ImageGroup implements IImage{
        private final List<Image> images = Lists.newArrayList();

        public ImageGroup(ResourceLocation resourceLocation,float startU, float startV, float width, float height, int cols, int rows) {
            for (int i=0;i<cols*rows;i++){
                images.add(new Image(resourceLocation,
                        startU+(i%cols)*width,
                        startV+((float) i /cols)*height,
                        width,
                        height
                ));
            }
        }

        public ImageGroup(ResourceLocation resourceLocation, int cols, int rows) {
            this(resourceLocation,0.0f,0.0f, (float) (1.0/cols), (float) (1.0/rows),cols,rows);
        }
        @Override
        public Image getImage(int index){
            return this.images.get(Mth.clamp(0,index,images.size()-1));
        }

        /*
        public Image[] getImages(){
            return this.images.toArray(new Image[0]);
        }*/
    }
}

