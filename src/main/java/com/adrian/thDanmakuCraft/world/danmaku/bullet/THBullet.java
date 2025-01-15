package com.adrian.thDanmakuCraft.world.danmaku.bullet;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.danmaku.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class THBullet extends THObject {
    protected DefaultBulletStyle style;
    protected BULLET_COLOR bulletColor;

    public THBullet(THObjectType<THBullet> type, THObjectContainer container) {
        super(type, container);
        //this.luaValueForm = this.ofLuaValue();
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

    public void setBulletColor(int color){
        this.bulletColor = BULLET_COLOR.getColorByIndex(color);
    }

    public BULLET_COLOR getBulletColor() {
        return bulletColor;
    }

    public int getBulletIndex(){
        return bulletColor.getIndex();
    }

    public void setStyle(DefaultBulletStyle style) {
        this.style = style;
        this.size = style.getSize();
    }

    public void setStyle(String style) {
        this.setStyle(DefaultBulletStyle.valueOf(style));
        /*
        this.style = DefaultBulletStyle.valueOf(style);
        this.size = this.style.getSize();
         */
    }

    public DefaultBulletStyle getStyle() {
        return this.style;
    }

    public String getStyleName() {
        return this.style.name();
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
    public Image getImage() {
        return this.style.getImage(this.bulletColor.index);
    }

    public enum BULLET_COLOR {
        COLOR_DEEP_RED(0,       Color(200,20,20)),
        COLOR_RED(1,            Color(255,0,0)),
        COLOR_DEEP_PURPLE(2,    Color(190,0,255)),
        COLOR_PURPLE(3,         Color(255,0,255)),
        COLOR_DEEP_BLUE(4,      Color(107,0,255)),
        COLOR_BLUE(5,           Color(0,0,255)),
        COLOR_ROYAL_BLUE(6,     Color(40,84,145)),
        COLOR_CYAN (7,          Color(0,255,255)),
        COLOR_DEEP_GREEN(8,     Color(62,207,112)),
        COLOR_GREEN(9,         Color(0,255,0)),
        COLOR_CHARTREUSE(10,    Color(223,255,0)),
        COLOR_YELLOW (11,       Color(255,255,0)),
        COLOR_GOLDEN_YELLOW(12, Color(255,229,88)),
        COLOR_ORANGE(13,        Color(255,145,37)),
        COLOR_GRAY(14,          Color(199,199,199)),
        COLOR_DEEP_GRAY(15,     Color(137,137,137));

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

    public static final Image IMAGE_WHITE = new Image(TEXTURE_WHITE,0.0f,0.0f,1.0f,1.0f);
    //public static final Image IMAGE_BALL_MID = new Image(new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/danmaku/ball_mid.png"),0.0f,0.0f,1.0f,1.0f);
    //public static final Image IMAGE_ARROW_BIG = new Image(new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/danmaku/arrow_big.png"),0.0f,0.0f,1.0f,1.0f);
    public static final Image.ImageGroup IMAGE_BALL_MID = new Image.ImageGroup(new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/danmaku/ball_mid.png"),
            0.0f,0.0f,1.0f,1.0f/16,1,16);
    public static final Image.ImageGroup IMAGE_ARROW_BIG = new Image.ImageGroup(new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/danmaku/arrow_big.png"),
            0.0f,0.0f,1.0f,1.0f/16,1,16);
    private static final Vec3 DEFAULT_SIZE = new Vec3(0.5f,0.5f,0.5f);
    public interface IBulletStyle{
        boolean is3D();
        boolean shouldFaceCamera();
        Vec3 getSize();
        CollisionType getCollisionType();
        Image getImage(int index);

        void writeData(FriendlyByteBuf buffer);
        IBulletStyle readData(FriendlyByteBuf buffer);
        void save(CompoundTag tag);
        IBulletStyle load(CompoundTag tag);
    }

    /*
    public static void main(String[] args){
        String[] styleNames;
        DefaultBulletStyle[] styles = DefaultBulletStyle.values();
        styleNames = new String[styles.length];
        for(DefaultBulletStyle style:styles){
            styleNames[style.ordinal()] = style.name();
        }

        for(String styleName : styleNames){
            System.out.println("\"" + styleName + "\",");
        }
    }*/

    public enum DefaultBulletStyle implements IBulletStyle{
        arrow_big(IMAGE_ARROW_BIG,new Vec3(0.15f,0.15f,0.15f),false, CollisionType.SPHERE, true),
        arrow_mid,
        arrow_small,
        gun_bullet,
        butterfly,
        square,
        ball_small(IMAGE_ARROW_BIG,new Vec3(0.15f,0.15f,0.15f),false, CollisionType.SPHERE, true),
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

        private final IImage image;
        private final Vec3 size;
        private final boolean faceCam;
        private final boolean is3D;
        private final CollisionType collisionType;

        DefaultBulletStyle(IImage image, Vec3 size, boolean faceCam, CollisionType collisionType, boolean is3D){
            this.image = image;
            this.size = size;
            this.faceCam = faceCam;
            this.collisionType = collisionType;
            this.is3D = is3D;
        }

        DefaultBulletStyle(IImage image, Vec3 size, CollisionType collisionType){
            this.image = image;
            this.size = size;
            this.faceCam = false;
            this.collisionType = collisionType;
            this.is3D = false;
        }

        DefaultBulletStyle(){
            this(IMAGE_WHITE,DEFAULT_SIZE,CollisionType.AABB);
        }

        DefaultBulletStyle(IImage image){
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

        public Image getImage(int index){
            return this.image.getImage(index);
        }

        @Override
        public void writeData(FriendlyByteBuf buffer) {
            buffer.writeEnum(this);
        }

        @Override
        public IBulletStyle readData(FriendlyByteBuf buffer) {
            return buffer.readEnum(DefaultBulletStyle.class);
        }

        @Override
        public void save(CompoundTag tag) {
            tag.putInt("Style",this.ordinal());
        }

        @Override
        public IBulletStyle load(CompoundTag tag) {
            return DefaultBulletStyle.class.getEnumConstants()[tag.getInt("Style")];
        }

        public static DefaultBulletStyle getStyleByIndex(int index){
            return DefaultBulletStyle.class.getEnumConstants()[index];
        }
    }

    public static class UserBulletStyle{
    }

    private static THBullet checkTHBullet(LuaValue luaValue) {
        if (luaValue.get("source").checkuserdata() instanceof THBullet bullet) {
            return bullet;
        }
        throw new NullPointerException();
    }

    private static final LibFunction setStyle = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            checkTHBullet(luaValue0).setStyle(luaValue.checkjstring());
            return LuaValue.NIL;
        }
    };

    private static final LibFunction setBulletColor = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            checkTHBullet(luaValue0).setBulletColor(luaValue.checkint());
            return LuaValue.NIL;
        }
    };

    private static final LibFunction getStyle = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0) {
            return LuaValue.valueOf(checkTHBullet(luaValue0).getStyle().toString());
        }
    };

    private static final LibFunction getBulletColor = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0) {
            return LuaValue.valueOf(checkTHBullet(luaValue0).getBulletColor().getIndex());
        }
    };

    @Override
    public LuaValue ofLuaClass(){
        LuaValue library = super.ofLuaClass();
        return library;
    }

    @Override
    public LuaValue getMeta(){
        return meta;
    }

    public static final LuaValue meta = LuaValue.tableOf();
    static {
        meta.set("__index", luaClassFunctions());
    }
    public static LuaValue luaClassFunctions(){
        LuaValue library = THObject.luaClassFunctions();
        library.set("setStyle",       setStyle);
        library.set("setBulletColor", setBulletColor);
        library.set("getStyle",       getStyle);
        library.set("getBulletColor", getBulletColor);
        return library;
    }
}