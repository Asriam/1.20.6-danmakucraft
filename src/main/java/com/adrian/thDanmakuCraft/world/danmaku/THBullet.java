package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.THObjectContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class THBullet extends THObject {
    protected DefaultBulletStyle style;
    protected BULLET_COLOR bulletColor;

    public THBullet(THObjectType<THBullet> type, THObjectContainer container) {
        super(type, container);
    }

    public THBullet(THObjectContainer container, DefaultBulletStyle style, BULLET_COLOR bulletColor) {
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

    public void setStyle(DefaultBulletStyle style) {
        this.style = style;
        this.size = style.getSize();
    }

    public DefaultBulletStyle getStyle() {
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
        this.style = buffer.readEnum(DefaultBulletStyle.class);
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
        this.style       = DefaultBulletStyle.class.getEnumConstants()[tag.getInt("Style")];
    }

    @Override
    public THImage getImage() {
        return this.style.getImage();
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

    public static final THImage IMAGE_WHITE = new THImage(TEXTURE_WHITE,0.0f,0.0f,1.0f,1.0f);
    public static final THImage IMAGE_BALL_MID = new THImage(new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/danmaku/ball_mid.png"),0.0f,0.0f,1.0f,1.0f);
    public static final THImage IMAGE_ARROW_BIG = new THImage(new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/danmaku/arrow_big.png"),0.0f,0.0f,1.0f,1.0f);
    private static final Vec3 DEFAULT_SIZE = new Vec3(0.5f,0.5f,0.5f);
    public interface IBulletStyle{
        boolean is3D();
        boolean shouldFaceCamera();
        Vec3 getSize();
        CollisionType getCollisionType();
        THImage getImage();
    }

    public enum DefaultBulletStyle implements IBulletStyle{
        arrow_big(IMAGE_ARROW_BIG,new Vec3(0.15f,0.15f,0.15f),false, CollisionType.SPHERE, true),
        arrow_mid,
        arrow_small,
        gun_bullet,
        butterfly,
        square,
        ball_small,
        ball_mid(IMAGE_BALL_MID,new Vec3(0.3f,0.3f,0.3f),false, CollisionType.SPHERE,true),
        ball_mid_c(IMAGE_BALL_MID),
        ball_big(IMAGE_BALL_MID,new Vec3(0.5f,0.5f,0.5f),false, CollisionType.SPHERE, true),
        ball_huge,
        ball_light,
        star_small,
        star_big,
        grain_a(IMAGE_WHITE,new Vec3(0.1f,0.15f,0.15f),false, CollisionType.AABB, true),
        grain_b(IMAGE_WHITE,new Vec3(0.1f,0.15f,0.15f),false, CollisionType.AABB, true),
        grain_c, kite, knife, knife_b,
        water_drop, mildew,
        ellipse(IMAGE_WHITE,new Vec3(0.4f,0.4f,0.5f),false, CollisionType.ELLIPSOID, true),
        heart, money, music, silence,
        water_drop_dark, ball_huge_dark, ball_light_dark;

        private final THImage image;
        private final Vec3 size;
        private final boolean faceCam;
        private final boolean is3D;
        private final CollisionType collisionType;

        DefaultBulletStyle(THImage image, Vec3 size, boolean faceCam, CollisionType collisionType, boolean is3D){
            this.image = image;
            this.size = size;
            this.faceCam = faceCam;
            this.collisionType = collisionType;
            this.is3D = is3D;
        }

        DefaultBulletStyle(THImage image, Vec3 size, CollisionType collisionType){
            this.image = image;
            this.size = size;
            this.faceCam = false;
            this.collisionType = collisionType;
            this.is3D = false;
        }

        DefaultBulletStyle(){
            this(IMAGE_WHITE,DEFAULT_SIZE,CollisionType.AABB);
        }

        DefaultBulletStyle(THImage image){
            this(image,DEFAULT_SIZE,CollisionType.AABB);
        }

        public boolean is3D(){
            return this.is3D;
        }

        public Vec3 getSize(){
            return this.size;
        }

        public CollisionType getCollisionType(){
            return this.collisionType;
        }

        public boolean shouldFaceCamera(){
            return this.faceCam;
        }

        public THImage getImage(){
            return this.image;
        }

        public static DefaultBulletStyle getStyleByIndex(int index){
            return DefaultBulletStyle.class.getEnumConstants()[index];
        }
    }

}
