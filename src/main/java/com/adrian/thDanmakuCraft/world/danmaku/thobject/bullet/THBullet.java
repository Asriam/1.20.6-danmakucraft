package com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.CompoundTagUtil;
import com.adrian.thDanmakuCraft.util.FriendlyByteBufUtil;
import com.adrian.thDanmakuCraft.util.IImage;
import com.adrian.thDanmakuCraft.world.LuaValueHelper;
import com.adrian.thDanmakuCraft.world.danmaku.ITHObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.*;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class THBullet extends THObject {
    protected DefaultBulletStyle style = DefaultBulletStyle.arrow_big;
    //protected BULLET_INDEX_COLOR bulletIndexColor = BULLET_INDEX_COLOR.COLOR_DEEP_RED;
    protected Color bulletColor = BULLET_INDEX_COLOR.COLOR_DEEP_RED.color;

    public THBullet(THObjectType<THBullet> type, ITHObjectContainer container) {
        super(type, container);
    }

    public THBullet(THObjectContainer container, DefaultBulletStyle style, BULLET_INDEX_COLOR bulletColor) {
        this(THObjectInit.TH_BULLET.get(), container);
        this.style         = style;
        this.size          = style.size;
        this.collisionType = style.collisionType;
        this.setBulletColorByIndex(bulletColor);
    }

    public void setBulletColorByIndex(BULLET_INDEX_COLOR bulletIndexColor){
        //this.bulletIndexColor = bulletIndexColor;
        this.setBulletColor(bulletIndexColor.getColor());
    }

    public void setBulletColorByIndex(int index){
        BULLET_INDEX_COLOR bulletIndexColor = BULLET_INDEX_COLOR.getColorByIndex(index);
        this.setBulletColor(bulletIndexColor.getColor());
    }


    public void setBulletColor(Color color){
        this.bulletColor = color;
    }

    public void setBulletColor(int r, int g, int b, int a){
        this.setBulletColor(new Color(r,g,b,a));
    }

    public Color getBulletColor(){
        return this.bulletColor;
    }
    /*
    public BULLET_INDEX_COLOR getBulletIndexColor() {
        return bulletIndexColor;
    }

    public int getBulletIndex(){
        return bulletIndexColor.getIndex();
    }*/

    @Override
    public float getDamage(){
        return this.style.damage * super.getDamage();
    }

    public void setStyle(DefaultBulletStyle style) {
        this.style = style;
        this.size = style.getSize();
    }

    public void setStyle(String style) {
        this.setStyle(DefaultBulletStyle.valueOf(style));
    }

    public DefaultBulletStyle getStyle() {
        return this.style;
    }

    public String getStyleName() {
        return this.style.name();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
        //buffer.writeEnum(this.bulletIndexColor);
        buffer.writeEnum(this.style);
        /*Color c = this.bulletColor;
        buffer.writeInt(c.r);
        buffer.writeInt(c.g);
        buffer.writeInt(c.b);
        buffer.writeInt(c.a);*/
        FriendlyByteBufUtil.writeColor(buffer,this.bulletColor);
    }

    @Override
    public void decode(FriendlyByteBuf buffer){
        super.decode(buffer);
        //this.bulletIndexColor = buffer.readEnum(BULLET_INDEX_COLOR.class);
        this.style = buffer.readEnum(DefaultBulletStyle.class);
        /*this.setBulletColor(
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt()
        );*/
        this.bulletColor = FriendlyByteBufUtil.readColor(buffer);
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        //nbt.putInt("BulletIndexColor",this.bulletIndexColor.ordinal());
        tag.putInt("Style",this.style.ordinal());
        Color c = this.bulletColor;
        tag.put("BulletColor", CompoundTagUtil.newIntList(c.r, c.g, c.b, c.a));
        //return tag;
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        //this.bulletIndexColor = BULLET_INDEX_COLOR.class.getEnumConstants()[tag.getInt("BulletIndexColor")];
        this.style       = DefaultBulletStyle.class.getEnumConstants()[tag.getInt("Style")];
        ListTag colorTag = tag.getList("BulletColor", Tag.TAG_INT);
        this.setBulletColor(colorTag.getInt(0),colorTag.getInt(1),colorTag.getInt(2),colorTag.getInt(3));
    }

    @Override
    public IImage.Image getImage() {
        return this.style.getImage(1);
    }

    public enum BULLET_INDEX_COLOR {
        COLOR_DEEP_RED(0,       Color(200,20,20)),
        COLOR_RED(1,            Color(255,0,0)),
        COLOR_DEEP_PURPLE(2,    Color(190,0,255)),
        COLOR_PURPLE(3,         Color(255,0,255)),
        COLOR_DEEP_BLUE(4,      Color(107,0,255)),
        COLOR_BLUE(5,           Color(0,0,255)),
        COLOR_ROYAL_BLUE(6,     Color(40,84,145)),
        COLOR_CYAN (7,          Color(0,255,255)),
        COLOR_DEEP_GREEN(8,     Color(62,207,112)),
        COLOR_GREEN(9,          Color(0,255,0)),
        COLOR_CHARTREUSE(10,    Color(223,255,0)),
        COLOR_YELLOW (11,       Color(255,255,0)),
        COLOR_GOLDEN_YELLOW(12, Color(255,229,88)),
        COLOR_ORANGE(13,        Color(255,145,37)),
        COLOR_GRAY(14,          Color(199,199,199)),
        COLOR_DEEP_GRAY(15,     Color(137,137,137));

        private final int index;
        private final Color color;
        BULLET_INDEX_COLOR(int index, Color color){
            this.index = index;
            this.color = color;
        }

        public int getIndex(){
            return this.index;
        }

        public Color getColor(){
            return this.color;
        }

        public static BULLET_INDEX_COLOR getColorByIndex(int index){
            return BULLET_INDEX_COLOR.class.getEnumConstants()[Mth.clamp(index,1,16)-1];
        }
    }

    public static final IImage.Image IMAGE_WHITE = new IImage.Image(TEXTURE_WHITE,0.0f,0.0f,1.0f,1.0f);
    public static final IImage.Image.ImageGroup IMAGE_BALL_MID = new IImage.Image.ImageGroup(new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/danmaku/ball_mid.png"),
            0.0f,0.0f,1.0f,1.0f/16,1,16);
    public static final IImage.Image.ImageGroup IMAGE_ARROW_BIG = new IImage.Image.ImageGroup(new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/danmaku/arrow_big.png"),
            0.0f,0.0f,1.0f,1.0f/16,1,16);
    private static final Vec3 DEFAULT_SIZE = new Vec3(0.5f,0.5f,0.5f);
    public interface IBulletStyle{
        boolean is3D();
        boolean shouldFaceCamera();
        Vec3 getSize();
        CollisionType getCollisionType();
        IImage.Image getImage(int index);
        String getName();

        void writeData(FriendlyByteBuf buffer);
        IBulletStyle readData(FriendlyByteBuf buffer);
        void save(CompoundTag tag);
        IBulletStyle load(CompoundTag tag);
        boolean isDefaultBulletStyle();
    }

    public enum DefaultBulletStyle implements IBulletStyle{
        arrow_big(IMAGE_ARROW_BIG,2.0f,new Vec3(0.15f,0.15f,0.15f),false, CollisionType.SPHERE, true),
        arrow_mid,
        arrow_small,
        gun_bullet,
        butterfly,
        square,
        ball_small(IMAGE_BALL_MID,2.0f,new Vec3(0.15f,0.15f,0.15f),false, CollisionType.SPHERE, false),
        ball_mid(IMAGE_BALL_MID,2.6f,new Vec3(0.3f,0.3f,0.3f),false, CollisionType.SPHERE,true),
        ball_mid_c(IMAGE_BALL_MID),
        ball_big(IMAGE_BALL_MID,3.0f,new Vec3(0.5f,0.5f,0.5f),false, CollisionType.SPHERE, true),
        ball_huge,
        ball_light,
        star_small,
        star_big,
        grain_a(IMAGE_WHITE,2.0f,new Vec3(0.15f,0.15f,0.15f),false, CollisionType.AABB, true),
        grain_b(IMAGE_WHITE,2.0f,new Vec3(0.15f,0.15f,0.15f),false, CollisionType.AABB, true),
        grain_c, kite, knife, knife_b,
        water_drop, mildew,
        ellipse(IMAGE_WHITE,2.6f,new Vec3(0.4f,0.4f,0.5f),false, CollisionType.ELLIPSOID, true),
        heart, money, music, silence,
        water_drop_dark, ball_huge_dark, ball_light_dark;

        private final IImage image;
        private final Vec3 size;
        private final boolean faceCam;
        private final boolean is3D;
        private final CollisionType collisionType;
        private final float damage;

        DefaultBulletStyle(IImage image,float damage, Vec3 size, boolean faceCam, CollisionType collisionType, boolean is3D){
            this.image = image;
            this.size = size;
            this.faceCam = faceCam;
            this.collisionType = collisionType;
            this.is3D = is3D;
            this.damage = damage;
        }

        DefaultBulletStyle(IImage image, Vec3 size, CollisionType collisionType){
            this.image = image;
            this.size = size;
            this.faceCam = false;
            this.collisionType = collisionType;
            this.is3D = false;
            this.damage = 1.0f;
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

        public IImage.Image getImage(int index){
            return this.image.getImage(index);
        }

        public String getName(){
            return this.name();
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

        @Override
        public boolean isDefaultBulletStyle(){
            return true;
        }
    }

    public static void printAllBulletStyles(){
        String bulletStyles = "";
        for(THBullet.DefaultBulletStyle style : THBullet.DefaultBulletStyle.values()){
            bulletStyles += style.getName() + "=\""+ style.getName() +"\",\n";
        }
        System.out.print(bulletStyles);
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

    private static final LibFunction setBulletColorByIndex = new TwoArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
            checkTHBullet(luaValue0).setBulletColorByIndex(luaValue.checkint());
            return LuaValue.NIL;
        }
    };

    private static final LibFunction setBulletColor = new VarArgFunction() {
        @Override
        public Varargs invoke(Varargs varargs){
            checkTHBullet(varargs.arg1()).setBulletColor(
                    varargs.arg(1).checkint(),
                    varargs.arg(3).checkint(),
                    varargs.arg(4).checkint(),
                    varargs.arg(5).checkint()
            );
            return LuaValue.NIL;
        }
    };

    private static final LibFunction getStyle = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0) {
            return LuaValue.valueOf(checkTHBullet(luaValue0).getStyle().toString());
        }
    };

    private static final LibFunction getBulletIndex = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0) {
            //return LuaValue.valueOf(checkTHBullet(luaValue0).getBulletIndexColor().getIndex());
            return LuaValue.NIL;
        }
    };

    private static final LibFunction getBulletColor = new OneArgFunction() {
        @Override
        public LuaValue call(LuaValue luaValue0) {
            return LuaValueHelper.ColorToLuaValue(checkTHBullet(luaValue0).getBulletColor());
        }
    };

    public static final LuaValue meta = THObjectContainer.setMeta(functions());
    @Override
    public LuaValue ofLuaClass(){
        LuaValue library = super.ofLuaClass();
        return library;
    }

    @Override
    public LuaValue getMeta(){
        return meta;
    }

    /*public static final LuaValue meta = LuaValue.tableOf();
    static {
        meta.set("__index", functions());
    }*/
    
    public static LuaValue functions(){
        LuaValue library = THObject.functions();
        library.set("setStyle",       setStyle);
        library.set("setBulletColorByIndex", setBulletColorByIndex);
        library.set("setBulletColor", setBulletColor);
        library.set("getStyle",       getStyle);
        library.set("getBulletColor", getBulletColor);
        return library;
    }
}