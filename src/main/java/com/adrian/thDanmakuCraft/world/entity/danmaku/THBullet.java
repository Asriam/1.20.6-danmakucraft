package com.adrian.thDanmakuCraft.world.entity.danmaku;

import com.adrian.thDanmakuCraft.client.renderer.danmaku.THBulletRenderers;
import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class THBullet extends THObject {
    protected BULLET_STYLE style;
    protected BULLET_COLOR bulletColor;

    public THBullet(THObjectType<THBullet> type, EntityTHObjectContainer container) {
        super(type, container);
    }

    public THBullet(EntityTHObjectContainer container, BULLET_STYLE style, BULLET_COLOR bulletColor) {
        this(THObjectInit.TH_BULLET.get(), container);
        this.style         = style;
        this.size          = style.size;
        this.collisionType = style.collisionType;
        this.bulletColor   = bulletColor;
    }

    public void setBulletColor(BULLET_COLOR bulletColor){
        this.bulletColor = bulletColor;
    }

    public BULLET_COLOR getBulletColor() {
        return bulletColor;
    }

    public void setStyle(BULLET_STYLE style) {
        this.style = style;
        this.size = style.getSize();
    }

    public BULLET_STYLE getStyle() {
        return style;
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeEnum(this.bulletColor);
        buffer.writeEnum(this.style);
    }

    @Override
    public void readData(FriendlyByteBuf buffer){
        super.readData(buffer);
        this.bulletColor = buffer.readEnum(BULLET_COLOR.class);
        this.style = buffer.readEnum(BULLET_STYLE.class);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag nbt = super.save(tag);
        nbt.putInt("BulletColor",this.bulletColor.ordinal());
        nbt.putInt("Style",this.style.ordinal());
        return nbt;
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        this.bulletColor = BULLET_COLOR.class.getEnumConstants()[tag.getInt("BulletColor")];
        this.style       = BULLET_STYLE.class.getEnumConstants()[tag.getInt("Style")];
    }

    @Override
    public ResourceLocation getTexture() {
        return this.style.getTexture();
    }

    public enum BULLET_COLOR {
        COLOR_DEEP_RED(1,       Color(200,20,20)),
        COLOR_RED(2,            Color(255,0,0)),
        COLOR_DEEP_PURPLE(3,    Color(190,0,255)),
        COLOR_PURPLE(4,         Color(255,0,255)),
        COLOR_DEEP_BLUE(5,      Color(107,0,255)),
        COLOR_BLUE(6,           Color(0,0,255)),
        COLOR_ROYAL_BLUE(7,     Color(40,84,145)),
        COLOR_CYAN (8,          Color(0,255,255)),
        COLOR_DEEP_GREEN(9,     Color(62,207,112)),
        COLOR_GREEN(10,         Color(0,255,0)),
        COLOR_CHARTREUSE(11,    Color(223,255,0)),
        COLOR_YELLOW (12,       Color(255,255,0)),
        COLOR_GOLDEN_YELLOW(13, Color(255,229,88)),
        COLOR_ORANGE(14,        Color(255,145,37)),
        COLOR_GRAY(15,          Color(199,199,199)),
        COLOR_DEEP_GRAY(16,     Color(137,137,137));

        private final int index;
        private final Color color;
        BULLET_COLOR(int index, Color color){
            this.index = index;
            this.color = color;
        }

        public int getIndex(){
            return this.index;
        }

        public Color getColor(){
            return this.color;
        }

        public static BULLET_COLOR getColorByIndex(int index){
            return BULLET_COLOR.class.getEnumConstants()[Mth.clamp(index,1,16)-1];
        }
    }

    public static final ResourceLocation TEXTURE_BALL_MID = new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/danmaku/ball_mid.png");
    public static final ResourceLocation TEXTURE_ARROW_BIG = new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/danmaku/arrow_big.png");
    private static final Vec3 DEFAULT_SIZE = new Vec3(0.5f,0.5f,0.5f);
    public enum BULLET_STYLE {
        arrow_big(TEXTURE_ARROW_BIG,new Vec3(0.15f,0.15f,0.15f),false, CollisionType.SPHERE, true),
        arrow_mid,
        arrow_small,
        gun_bullet,
        butterfly,
        square,
        ball_small,
        ball_mid(TEXTURE_BALL_MID,new Vec3(0.3f,0.3f,0.3f),false, CollisionType.SPHERE,true),
        ball_mid_c(TEXTURE_BALL_MID),
        ball_big(TEXTURE_BALL_MID,new Vec3(0.5f,0.5f,0.5f),false, CollisionType.SPHERE, true),
        ball_huge,
        ball_light,
        star_small,
        star_big,
        grain_a(TEXTURE_WHITE,new Vec3(0.1f,0.15f,0.15f),false, CollisionType.AABB, true),
        grain_b(TEXTURE_WHITE,new Vec3(0.1f,0.15f,0.15f),false, CollisionType.AABB, true),
        grain_c, kite, knife, knife_b,
        water_drop, mildew,
        ellipse(TEXTURE_WHITE,new Vec3(0.4f,0.4f,0.5f),false, CollisionType.ELLIPSOID, true),
        heart, money, music, silence,
        water_drop_dark, ball_huge_dark, ball_light_dark;

        private final ResourceLocation texture;
        private final Vec3 size;
        private final boolean faceCam;
        private final boolean is3D;
        private final CollisionType collisionType;

        BULLET_STYLE(ResourceLocation texture, Vec3 size, boolean faceCam, CollisionType collisionType, boolean is3D){
            this.texture = texture;
            this.size = size;
            this.faceCam = faceCam;
            this.collisionType = collisionType;
            this.is3D = is3D;
        }

        BULLET_STYLE(ResourceLocation texture, Vec3 size, CollisionType collisionType){
            this.texture = texture;
            this.size = size;
            this.faceCam = false;
            this.collisionType = collisionType;
            this.is3D = false;
        }

        BULLET_STYLE(){
            this(TEXTURE_WHITE,DEFAULT_SIZE,CollisionType.AABB);
        }

        BULLET_STYLE(ResourceLocation texture){
            this(texture,DEFAULT_SIZE,CollisionType.AABB);
        }

        public boolean getIs3D(){
            return this.is3D;
        }

        public Vec3 getSize(){
            return this.size;
        }

        public CollisionType getCollisionType(){
            return this.collisionType;
        }

        public boolean getShouldFaceCamera(){
            return this.faceCam;
        }

        public ResourceLocation getTexture(){
            return this.texture;
        }

        /*
        public THBulletRenderers.THBulletRenderFactory getRenderFactory(){
            return this.renderFactory;
        }*/

        public static BULLET_STYLE getStyleByIndex(int index){
            return BULLET_STYLE.class.getEnumConstants()[index];
        }
    }

}
