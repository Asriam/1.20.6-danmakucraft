package com.asrian.thDanmakuCraft.world.entity.danmaku;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;
import com.asrian.thDanmakuCraft.client.renderer.THBulletRenderer;
import com.asrian.thDanmakuCraft.client.renderer.entity.EntityTHObjectContainerRenderer;
import com.asrian.thDanmakuCraft.init.THObjectInit;
import com.asrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import javax.annotation.Nullable;

public class THBullet extends THObject {
    protected BULLET_STYLE style;
    protected BULLET_COLOR bulletColor;

    public THBullet(THObjectType<THBullet> type, EntityTHObjectContainer container) {
        super(type, container);
    }

    public THBullet(EntityTHObjectContainer container, BULLET_STYLE style, BULLET_COLOR bulletColor) {
        this(THObjectInit.TH_BULLET.get(), container);
        this.style = style;
        this.size = style.size;
        this.bulletColor = bulletColor;
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
        this.style = BULLET_STYLE.class.getEnumConstants()[tag.getInt("Style")];
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void onRender(EntityTHObjectContainerRenderer renderer, Vec3 bulletPos, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay) {
        if(this.color.a <= 0){
            return;
        }
        poseStack.pushPose();
        /**
        Vec3 offsetPos = this.getOffsetPosition(partialTicks);
        poseStack.translate(offsetPos.x(), offsetPos.y(), offsetPos.z());
         */

        this.style.render(renderer,this,partialTicks,poseStack,bufferSource,combinedOverlay);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTexture() {
        return this.style.texture;
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

    public static final ResourceLocation TEXTURE_BALL_MID = new ResourceLocation(THDanmakuCraftCore.MODID, "textures/danmaku/ball_mid.png");
    public static final ResourceLocation TEXTURE_ARROW_BIG = new ResourceLocation(THDanmakuCraftCore.MODID, "textures/danmaku/arrow_big.png");
    private static final Vec3 DEFAULT_SIZE = new Vec3(0.5f,0.5f,0.5f);
    public enum BULLET_STYLE {
        arrow_big(TEXTURE_ARROW_BIG,new Vec3(0.15f,0.15f,0.15f),false, THBulletRenderer.BulletRenderers::arrow_big),
        arrow_mid,
        arrow_small,
        gun_bullet,
        butterfly,
        square,
        ball_small,
        ball_mid(TEXTURE_BALL_MID,DEFAULT_SIZE,true, THBulletRenderer.BulletRenderers::ball_mid),
        ball_mid_c,
        ball_big(TEXTURE_BALL_MID,DEFAULT_SIZE,false, THBulletRenderer.BulletRenderers::ball_big),
        ball_huge,
        ball_light,
        star_small, star_big,
        grain_a(TEXTURE_WHITE,new Vec3(0.15f,0.15f,0.15f),false, THBulletRenderer.BulletRenderers::grain_a),
        grain_b(TEXTURE_WHITE,new Vec3(0.2f,0.2f,0.2f),false, THBulletRenderer.BulletRenderers::grain_b),
        grain_c, kite, knife, knife_b,
        water_drop, mildew,
        ellipse(TEXTURE_WHITE,DEFAULT_SIZE,false, THBulletRenderer.BulletRenderers::ellipse),
        heart, money, music, silence,
        water_drop_dark, ball_huge_dark, ball_light_dark;

        private final ResourceLocation texture;
        private final Vec3 size;
        private final boolean faceCam;
        private boolean is3D;
        @Nullable
        public final THBulletRenderer.THBulletRenderFactory renderFactory;

        BULLET_STYLE(ResourceLocation texture, Vec3 size, boolean faceCam, THBulletRenderer.THBulletRenderFactory factory){
            this.texture = texture;
            this.size = size;
            this.faceCam = faceCam;
            this.renderFactory = factory;
            this.is3D = factory != null;
        }

        BULLET_STYLE(ResourceLocation texture, Vec3 size){
            this.texture = texture;
            this.size = size;
            this.faceCam = false;
            this.renderFactory = null;
            this.is3D = false;
        }

        BULLET_STYLE(){
            this(TEXTURE_WHITE,DEFAULT_SIZE);
        }

        public void setIs3D(boolean is3D){
            this.is3D = is3D;
        }

        public Vec3 getSize(){
            return this.size;
        }

        public static BULLET_STYLE getStyleByIndex(int index){
            return BULLET_STYLE.class.getEnumConstants()[index];
        }

        @OnlyIn(Dist.CLIENT)
        public void render(EntityTHObjectContainerRenderer renderer,THBullet object, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_114710_){
            poseStack.pushPose();
            if(this.is3D) {
                if(this.faceCam) {
                    poseStack.mulPose(renderer.dispatcher.cameraOrientation());
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                }else {
                    poseStack.mulPose(new Quaternionf().rotationYXZ(object.yRot,object.xRot+Mth.DEG_TO_RAD*90.0f,object.zRot));
                }
                THBulletRenderer.render3DBullet(renderer, object,this.renderFactory,partialTicks, poseStack, bufferSource, p_114710_);

            }else {
                THBulletRenderer.render2DBullet(renderer, object,partialTicks, poseStack, bufferSource, p_114710_);
            }
            poseStack.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public enum BULLET_QUALITY_LEVEL {
        VERY_CLOSE(10,10,false),
        CLOSE(8,8,false),
        MEDIUM(6,6,false),
        FAR(6,5,false),
        VERY_FAR(4,4,true);

        public final int edgeANum;
        public final int edgeBNum;
        public boolean is2D;

        BULLET_QUALITY_LEVEL(int edgeA, int edgeB, boolean is2D){
            this.edgeANum = edgeA;
            this.edgeBNum = edgeB;
            this.is2D = is2D;
        }

        public static BULLET_QUALITY_LEVEL getLevel(THObject object, double camX, double camY, double camZ){
            double d0 = object.positionX - camX;
            double d1 = object.positionY - camY;
            double d2 = object.positionZ - camZ;
            double distSquare = (d0 * d0 + d1 * d1 + d2 * d2);

            double d4 = object.getBoundingBox().getSize() * 4.0D;
            if (Double.isNaN(d4)) {
                d4 = 4.0D;
            }
            d4 *= d4;

            double[] distOfLevel = {4.0d,8.0D,16.0D,32.0D,48.0D};

            if(distSquare < d4*distOfLevel[0]*distOfLevel[0]){
                return VERY_CLOSE;
            }else if(distSquare < d4*distOfLevel[1]*distOfLevel[1]){
                return CLOSE;
            }else if(distSquare < d4*distOfLevel[2]*distOfLevel[2]){
                return MEDIUM;
            }else if(distSquare < d4*distOfLevel[3]*distOfLevel[3]){
                return FAR;
            }
            return VERY_FAR;
        }
    }
}
