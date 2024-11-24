package com.asrian.thDanmakuCraft.world.entity.danmaku;

import com.asrian.thDanmakuCraft.THDanmakuCraftCore;
import com.asrian.thDanmakuCraft.client.renderer.THObjectRenderHelper;
import com.asrian.thDanmakuCraft.client.renderer.THRenderType;
import com.asrian.thDanmakuCraft.client.renderer.entity.EntityTHObjectContainerRenderer;
import com.asrian.thDanmakuCraft.init.THObjectInit;
import com.asrian.thDanmakuCraft.util.RunnableWithException;
import com.asrian.thDanmakuCraft.util.script.ScriptManager;
import com.asrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.asrian.thDanmakuCraft.util.script.IScript;
import com.asrian.thDanmakuCraft.util.script.JavaScriptManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import java.lang.Math;
import java.util.List;

public class THObject implements IScript, IScriptTHObjectAPI{
    private final THObjectType<? extends THObject> type;
    private final ScriptManager scriptManager;
    private Level level;
    private EntityTHObjectContainer container;
    public static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(THDanmakuCraftCore.MODID, "textures/white.png");
    protected ResourceLocation TEXTURE = TEXTURE_WHITE;
    protected static final AABB INITIAL_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    protected AABB bb = INITIAL_AABB;
    protected double positionX;                    //Object Position
    protected double positionY;
    protected double positionZ;
    protected Vec3 prePosition;
    public Vec3 lastPosition;
    protected Vec3 velocity = new Vec3(0.0d, 0.0d, 0.0d);
    protected Vec3 acceleration = new Vec3(0.0d, 0.0d, 0.0d);
    protected Vec3 size = new Vec3(0.5f, 0.5f, 0.5f);                  //Hitbox size
    protected Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);                             //Render scale
    //public Vector3f rotation = new Vector3f(0.0f,0.0f,0.0f);                                   //Euler Angles
    protected float damage = 0.0f;
    protected float xRot = 0.0f;
    protected float yRot = 0.0f;
    protected float zRot = 0.0f;
    protected int timer = 0;
    protected int lifetime = 120;
    private int deathLastsTime = 10;
    public boolean shouldSave = true;
    public boolean deathAnimation = true;
    public boolean inWater;
    public boolean colli = true;
    public boolean navi = false;
    public boolean bound = true;
    public boolean noCulling;
    public boolean shouldTick = true;
    public boolean isDead = false;
    public boolean removeFlag = false;
    protected boolean faceCamera = true;
    protected boolean canHitUser = false;

    public int layer = 0;

    public Color color = Color(255,255,255,255);
    public THRenderType.BLEND _blend = THRenderType.BLEND.LIGHTEN;


    public THObject(THObjectType<? extends THObject> type,EntityTHObjectContainer container) {
        this.type = type;
        this.container = container;
        this.level = container.level();
        this.scriptManager = container.getScriptManager();
        //this.scriptManager.disableScript();
        this.initPosition(container.position());
    }

    public THObject(EntityTHObjectContainer container, Vec3 position) {
        this(THObjectInit.TH_OBJECT.get(), container);
    }

    public THObject initPosition(Vec3 position){
        this.setPosition(position);
        this.lastPosition = position;
        this.prePosition = position;
        return this;
    }

    public THObject shoot(float speed, Vec3 vectorRotation) {
        this.setVelocity(speed,vectorRotation,true);
        this.spawn();
        return this;
    }

    public THObject shoot(Vec3 velocity){
        this.setVelocity(velocity,true);
        this.spawn();
        return this;
    }

    public THObject shoot(float speed, Vec2 rotation, boolean isDeg){
        this.setVelocity(speed, rotation, isDeg,true);
        this.spawn();
        return this;
    }

    public void injectScript(String script){
        this.scriptManager.setScript(script);
        this.scriptManager.enableScript();
    }

    public void spawn(){
        if (!this.container.getObjectManager().contains(this)) {
            this.container.getObjectManager().addTHObject(this);
        }
    }

    public void setDead() {
        this.isDead = true;
    }

    public void remove() {
        this.removeFlag = true;
    }

    public void setPosition(Vec3 pos) {
        this.positionX = pos.x;
        this.positionY = pos.y;
        this.positionZ = pos.z;
    }

    public void setScale(Vector3f scale){
        this.scale = scale;
    }

    public void setSize(Vec3 size) {
        this.size = size;
    }

    public void setVelocity(Vec3 velocity, boolean setRotation) {
        this.velocity = velocity;
        if(setRotation){
            this.setRotationByDirectionalVector(velocity);
        }
    }

    public void setVelocity(float speed, Vec3 rotation, boolean setRotation) {
        this.setVelocity(rotation.normalize().multiply(speed,speed,speed),setRotation);
    }

    public void setVelocity(float speed, Vec2 rotation, boolean isDeg, boolean setRotation) {
        this.setVelocity(speed, Vec3.directionFromRotation(isDeg ? rotation : rotation.scale(Mth.RAD_TO_DEG)), false);

        if (setRotation){
            this.setRotation(isDeg? rotation.scale(Mth.DEG_TO_RAD) : rotation);
        }
    }

    public void setAcceleration(Vec3 acceleration){
        this.acceleration = acceleration;
    }

    public void setAcceleration(float acceleration, Vec3 rotation){
        this.setAcceleration(rotation.normalize().multiply(acceleration,acceleration,acceleration));
    }

    public void setRotation(float x,float y,float z) {
        this.xRot = x;
        this.yRot = -y;
        this.zRot = z;
    }

    public void setRotation(Vector3f rotation) {
        this.setRotation(rotation.x,rotation.y,rotation.z);
    }

    public void setRotation(Vec2 rotation){
        this.xRot = rotation.x;
        this.yRot = -rotation.y;
    }

    public void setRotationByDirectionalVector(Vec3 vectorRotation) {
        this.setRotation(VectorAngleToRadAngle(vectorRotation));
    }

    public static Vec2 VectorAngleToRadAngle(Vec3 formDir){
        float y = (float) Mth.atan2(formDir.x,formDir.z);
        float x = (float) Mth.atan2(formDir.y,Mth.sqrt((float) (formDir.x*formDir.x+formDir.z*formDir.z)));
        return new Vec2(-x,y);
    }

    public static Vec2 VectorAngleToEulerDegAngle(Vec3 formDir){
        return VectorAngleToRadAngle(formDir).scale(Mth.RAD_TO_DEG);
    }

    public void setColor(Color color){
        this.color = color;
    }

    public void setColor(int r, int g, int b, int a) {
        this.setColor(Color(r,g,b,a));
    }

    public void setLifetime(int time){
        this.lifetime = time;
    }

    public int getTimer(){
        return this.timer;
    }
    public Color getColor(){
        return this.color;
    }

    public Vec3 getPosition() {
        return new Vec3(positionX, positionY, positionZ);
    }

    public Vec3 getPrePosition(){
        return this.prePosition;
    }

    public Vec3 getOffsetPosition(float partialTicks){
        double x = Mth.lerp(partialTicks, this.lastPosition.x, this.getX());
        double y = Mth.lerp(partialTicks, this.lastPosition.y, this.getY());
        double z = Mth.lerp(partialTicks, this.lastPosition.z, this.getZ());
        return new Vec3(x, y, z);
    }


    public final double getX() {
        return this.positionX;
    }

    public final double getY() {
        return this.positionY;
    }

    public final double getZ() {
        return this.positionZ;
    }

    public double getSpeed() {
        return this.velocity.length();
    }

    public Vec3 getVelocity(){
        return this.velocity;
    }

    public Vec3 getMotionDirection(){
        return this.lastPosition.vectorTo(this.getPosition()).normalize();
    }

    public Vector3f getRotation(){
        return new Vector3f(this.xRot,this.yRot,this.zRot);
    }

    public Vec3 getAcceleration(){
        return this.acceleration;
    }

    public Vector3f getScale(){
        return this.scale;
    }

    public Vec3 getSize(){
        return this.size;
    }

    public boolean getIsDead(){
        return this.isDead;
    }

    public boolean getIsAlive(){
        return !this.isDead;
    }

    public final void setBoundingBox(AABB boundingBox) {
        this.bb = boundingBox;
    }

    public final void setBoundingBox(Vec3 pos, Vec3 size) {
        setBoundingBox(new AABB(
                pos.x - size.x / 2, pos.y - size.y / 2, pos.z - size.z / 2,
                pos.x + size.x / 2, pos.y + size.y / 2, pos.z + size.z / 2
        ));
    }

    public final AABB getBoundingBox() {
        return this.bb;
    }

    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox();
    }

    public EntityTHObjectContainer getContainer(){
        return this.container;
    }

    public void setContainer(EntityTHObjectContainer container){
        this.container = container;
    }

    public void setNewContainer(EntityTHObjectContainer container){
        if (container.equals(this.container)){
            return;
        }

        this.container.getObjectManager().removeTHObject(this);
        if(!container.getObjectManager().contains(this)){
            container.getObjectManager().addTHObject(this);
        }

        this.setContainer(container);
    }

    public void move(double x, double y, double z){
        this.positionX += x;
        this.positionY += y;
        this.positionZ += z;
    }

    public void move(Vec3 pos){
        this.move(pos.x,pos.y,pos.z);
    }

    public void onTick() {
        this.lastPosition = new Vec3(this.positionX,this.positionY,this.positionZ);

        if(!this.shouldTick){
            return;
        }
        this.positionX += this.velocity.x;
        this.positionY += this.velocity.y;
        this.positionZ += this.velocity.z;
        setBoundingBox(new Vec3(this.positionX, this.positionY, this.positionZ), this.size);
        this.velocity = new Vec3(
                this.velocity.x + this.acceleration.x,
                this.velocity.y + this.acceleration.y,
                this.velocity.z + this.acceleration.z
        );

        if(this.colli){
            this.collision();
        }

        if(this.navi && !this.isDead){
            this.setRotation(VectorAngleToRadAngle(this.getMotionDirection()));
        }

        if (--this.lifetime < 0 || (this.bound && !this.getContainer().getAabb().contains(this.getPosition()))){
            this.setDead();
        }

        if (this.isDead){
            this.onDead();
        }

        this.timer += 1;


        this.scriptManager.invokeScript("onTick", (exception)-> {
            if(this.container != null){
                this.container.remove(Entity.RemovalReason.DISCARDED);
            }
            this.remove();
        }, this);
    }

    public void collision(){
        List<Entity> entities = this.level.getEntities(this.container,this.getBoundingBox());
        if(!entities.isEmpty()){
            for (Entity entity:entities) {
                this.onHit(new EntityHitResult(entity,this.getPosition()));
            }
        }

        //HitResult hitresult = this.level.clip(new ClipContext(this.lastPosition, this.getPosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.container));
        AABB aabb = this.getBoundingBox();
        HitResult hitresult = this.level.clip(new ClipContext(
                new Vec3(aabb.minX,aabb.minY,aabb.minZ),
                new Vec3(aabb.maxX,aabb.maxY,aabb.maxZ),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this.container));
        this.onHit(hitresult);
    }

    public void onHit(HitResult result) {
        if (result.getType() != HitResult.Type.MISS) {
            if (result.getType() == HitResult.Type.ENTITY && result instanceof EntityHitResult entityHitResult){
                Entity entity = entityHitResult.getEntity();
                if(entity instanceof EntityTHObjectContainer || !this.canHitUser && entity.equals(this.container.getUser()))
                    return;
                this.onHitEntity(entityHitResult);
            }

            if (result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult blockHitResult){
                this.onHitBlock(blockHitResult);
            }
            this.setDead();
        }
    }

    public void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if(this.damage <= 0.0f ){
            return;
        }
        entity.hurt(this.container.damageSources().magic(),this.damage);
    }

    public void onHitBlock(BlockHitResult result) {
        //this.level.removeBlock(result.getBlockPos(),true);
    }

    public void onDead(){
        this.colli = false;
        this.setVelocity(Vec3.ZERO,false);
        if (this.deathAnimation) {
            this.deathLastsTime--;
            if (this.deathLastsTime <= 0) {
                this.remove();
            }
            this.color.a -= 255/10;
        } else {
            this.remove();
        }
    }

    public void onRemove(){

    }

    public boolean hasContainer(){
        return this.container != null && !this.container.isRemoved();
    }

    public THObjectType<?> getType() {
        return this.type;
    }

    public void writeData(FriendlyByteBuf buffer){
        buffer.writeDouble(this.positionX);
        buffer.writeDouble(this.positionY);
        buffer.writeDouble(this.positionZ);
        buffer.writeVec3(this.prePosition);
        buffer.writeFloat(this.xRot);
        buffer.writeFloat(this.yRot);
        buffer.writeFloat(this.zRot);
        buffer.writeVec3(this.velocity);
        buffer.writeVec3(this.acceleration);
        buffer.writeVector3f(this.scale);
        buffer.writeVec3(this.size);
        Color c = this.color;
        buffer.writeInt(c.r);
        buffer.writeInt(c.g);
        buffer.writeInt(c.b);
        buffer.writeInt(c.a);
        buffer.writeInt(this.timer);
        buffer.writeInt(this.lifetime);
        buffer.writeInt(this.deathLastsTime);
        buffer.writeEnum(this._blend);
        buffer.writeBoolean(this.isDead);
        buffer.writeBoolean(this.colli);
        buffer.writeBoolean(this.bound);
        buffer.writeBoolean(this.shouldSave);
        this.scriptManager.writeData(buffer);
    }

    public void readData(FriendlyByteBuf buffer){
        this.positionX = buffer.readDouble();
        this.positionY = buffer.readDouble();
        this.positionZ = buffer.readDouble();
        this.lastPosition = this.getPosition();
        this.prePosition = buffer.readVec3();
        this.xRot = buffer.readFloat();
        this.yRot = buffer.readFloat();
        this.zRot = buffer.readFloat();
        this.velocity = buffer.readVec3();
        this.acceleration = buffer.readVec3();
        this.scale = buffer.readVector3f();
        this.size = buffer.readVec3();
        int r,g,b,a;
        r = buffer.readInt();
        g = buffer.readInt();
        b = buffer.readInt();
        a = buffer.readInt();
        this.color = Color(r,g,b,a);
        this.timer = buffer.readInt();
        this.lifetime = buffer.readInt();
        this.deathLastsTime = buffer.readInt();
        this._blend = buffer.readEnum(THRenderType.BLEND.class);
        this.isDead = buffer.readBoolean();
        this.colli = buffer.readBoolean();
        this.bound = buffer.readBoolean();
        this.shouldSave = buffer.readBoolean();
        this.scriptManager.readData(buffer);
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("type", this.getType().getKey().toString());
        tag.put("Pos", newDoubleList(this.positionX, this.positionY, this.positionZ));
        tag.put("PrePos", newVec3(this.prePosition));
        tag.put("Rotation", newVector3f(this.getRotation()));
        tag.put("Velocity", newVec3(this.velocity));
        tag.put("Acceleration", newVec3(this.acceleration));
        tag.put("Scale", newVector3f(this.scale));
        tag.put("Size", newVec3(this.size));
        Color c = this.color;
        tag.put("Color", newIntList(c.r,c.g,c.b,c.a));
        tag.put("Timers", newIntList(this.timer,this.lifetime,this.deathLastsTime));
        tag.putInt("Blend",this._blend.ordinal());
        tag.putBoolean("IsDead", this.isDead);
        tag.putBoolean("Collision",this.colli);
        this.scriptManager.save(tag);
        return tag;
    }

    public void load(CompoundTag tag){
        ListTag posTag = tag.getList("Pos", Tag.TAG_DOUBLE);
        ListTag prePosTag = tag.getList("PrePos", Tag.TAG_DOUBLE);
        ListTag rotationTag = tag.getList("Rotation", Tag.TAG_FLOAT);
        ListTag velocityTag = tag.getList("Velocity", Tag.TAG_DOUBLE);
        ListTag accelerationTag = tag.getList("Acceleration", Tag.TAG_DOUBLE);
        ListTag scaleTag = tag.getList("Scale", Tag.TAG_FLOAT);
        ListTag sizeTag = tag.getList("Size", Tag.TAG_DOUBLE);
        ListTag colorTag = tag.getList("Color", Tag.TAG_INT);
        ListTag timerTag = tag.getList("Timers", Tag.TAG_INT);
        this.setPosition(new Vec3(posTag.getDouble(0),posTag.getDouble(1),posTag.getDouble(2)));
        this.prePosition = new Vec3(prePosTag.getDouble(0),prePosTag.getDouble(1),prePosTag.getDouble(2));
        this.velocity = new Vec3(velocityTag.getDouble(0),velocityTag.getDouble(1),velocityTag.getDouble(2));
        this.setRotation(rotationTag.getFloat(0),rotationTag.getFloat(1),rotationTag.getFloat(2));
        this.acceleration = new Vec3(accelerationTag.getDouble(0),accelerationTag.getDouble(1),accelerationTag.getDouble(2));
        this.scale = new Vector3f(scaleTag.getFloat(0),scaleTag.getFloat(1),scaleTag.getFloat(2));
        this.size = new Vec3(sizeTag.getDouble(0),sizeTag.getDouble(1),sizeTag.getDouble(2));
        this.setColor(colorTag.getInt(0),colorTag.getInt(1),colorTag.getInt(2),colorTag.getInt(3));
        this.timer = timerTag.getInt(0);
        this.lifetime = timerTag.getInt(1);
        this.deathLastsTime = timerTag.getInt(2);
        this._blend = THRenderType.BLEND.class.getEnumConstants()[tag.getInt("Blend")];
        this.isDead = tag.getBoolean("IsDead");
        this.colli = tag.getBoolean("Collision");
        this.scriptManager.load(tag);
    }

    public boolean shouldRender(double camX, double camY, double camZ) {
        double d0 = this.positionX - camX;
        double d1 = this.positionY - camY;
        double d2 = this.positionZ - camZ;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        return this.shouldRenderAtSqrDistance(d3,80.0D);
    }

    public boolean shouldRenderAtSqrDistance(double sqrDist, double distance) {
        double d0 = (Math.max(this.getBoundingBox().getSize(), 1.0D))  * 4.0D;
        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }

        d0 *= distance;
        return sqrDist < d0 * d0;
    }

    public class renderer{
        private final THObject object;

        public renderer(THObject object) {
            this.object = object;
        }
    }

    @OnlyIn(value = Dist.CLIENT)
    public void onRender(EntityTHObjectContainerRenderer renderer, Vec3 objectPos,float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay) {
        if(this.color.a <= 0){
            return;
        }

        poseStack.pushPose();
        if(this.faceCamera) {
            poseStack.mulPose(renderer.dispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }else {
            poseStack.mulPose(new Quaternionf().rotationYXZ(this.yRot,this.xRot,this.zRot));
        }
        poseStack.scale(this.scale.x, this.scale.y,this.scale.z);
        PoseStack.Pose posestack$pose = poseStack.last();
        /*
        Matrix4f pose = posestack$pose.pose();
        Matrix3f normal = posestack$pose.normal();
         */

        VertexConsumer vertexconsumer = bufferSource.getBuffer(this._blend.renderType.apply(this.getTexture()));

        THObjectRenderHelper.renderTexture(vertexconsumer, posestack$pose, combinedOverlay,
                new Vector3f(-0.5f, -0.5f, 0.0f),   new Vector2f(0.0f, 1.0f),
                new Vector3f(0.5f, -0.5f, 0.0f),    new Vector2f(1.0f, 1.0f),
                new Vector3f(0.5f, 0.5f, 0.0f),     new Vector2f(1.0f, 0.0f),
                new Vector3f(-0.5f, 0.5f, 0.0f),    new Vector2f(0.0f, 0.0f),
                this.color);
        poseStack.popPose();
    }

    public void setTEXTURE(ResourceLocation texture) {
        this.TEXTURE = texture;
    }
    
    public ResourceLocation getTexture() {
        return this.TEXTURE;
    }

    protected static ListTag newDoubleList(double... value) {
        ListTag listtag = new ListTag();

        for(double d0 : value) {
            listtag.add(DoubleTag.valueOf(d0));
        }

        return listtag;
    }

    protected static ListTag newFloatList(float... value) {
        ListTag listtag = new ListTag();

        for(float f : value) {
            listtag.add(FloatTag.valueOf(f));
        }

        return listtag;
    }

    protected static ListTag newIntList(int... value) {
        ListTag listtag = new ListTag();

        for(int i : value) {
            listtag.add(IntTag.valueOf(i));
        }

        return listtag;
    }

    protected static ListTag newVec2(Vec2 vec2) {
        return newFloatList(vec2.x,vec2.y);
    }

    protected static ListTag newVec3(Vec3 vec3) {
        return newDoubleList(vec3.x,vec3.y,vec3.z);
    }

    protected static ListTag newVector3f(Vector3f vec3) {
        return newFloatList(vec3.x,vec3.y,vec3.z);
    }

    public static boolean IsValid(THObject object){
        return object != null;
    }

    @Override
    public ScriptManager getScriptManager() {
        return this.scriptManager;
    }

    public static class Color{
        public int r;
        public int g;
        public int b;
        public int a;

        public static final Color WHITE(){
            return new Color(255,255,255,255);
        }

        public static final Color BLACK(){
            return new Color(0,0,0,255);
        }

        public static final Color VOID(){
            return new Color(0,0,0,0);
        }

        Color(int r, int g, int b, int a){
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        public Color normalize(){
            int r = Mth.clamp(this.r,0,255);
            int g = Mth.clamp(this.g,0,255);
            int b = Mth.clamp(this.b,0,255);
            int a = Mth.clamp(this.a,0,255);
            return new Color(r,g,b,a);
        }

        public Color plus(int r, int g, int b, int a){
            return new Color(this.r+r,this.g+g,this.b+b,this.a+a);
        }

        public Color minus(int r, int g, int b, int a){
            return new Color(this.r-r,this.g-g,this.b-b,this.a-a);
        }

        public Color multiply(float r, float g, float b, float a){
            return new Color((int) (this.r*r),(int) (this.g*g),(int) (this.b*b),(int) (this.a*a));
        }

        public Color multiply(float factor){
            return this.multiply(factor,factor,factor,factor);
        }

        public Color divide(float r, float g, float b, float a){
            return new Color((int) (this.r/r),(int) (this.g/g),(int) (this.b/b),(int) (this.a/a));
        }

        public Color divide(float factor){
            return this.divide(factor,factor,factor,factor);
        }
    }

    public static Color Color(int r, int g, int b, int a){
        return new Color(r,g,b,a);
    }

    public static Color Color(int r, int g, int b){
        return Color(r,g,b,255);
    }
}