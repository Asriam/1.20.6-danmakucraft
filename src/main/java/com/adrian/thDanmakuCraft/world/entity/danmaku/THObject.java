package com.adrian.thDanmakuCraft.world.entity.danmaku;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.api.script.IScriptTHObjectAPI;
import com.adrian.thDanmakuCraft.client.renderer.THObjectRenderHelper;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHObjectContainerRenderer;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.script.IScript;
import com.adrian.thDanmakuCraft.script.ScriptManager;
import com.adrian.thDanmakuCraft.util.CollisionHelper;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.script.js.JSManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import java.lang.Math;
import java.util.List;

public class THObject implements IScript, IScriptTHObjectAPI {
    private final THObjectType<? extends THObject> type;
    private final ScriptManager scriptManager;
    protected final RandomSource random;
    private final Level level;
    private EntityTHObjectContainer container;
    public static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/white.png");
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
    protected float xRot = 0.0f;
    protected float yRot = 0.0f;
    protected float zRot = 0.0f;
    protected float damage = 1.0f;
    protected int timer = 0;
    protected int lifetime = 120;
    private int deathLastingTime = 10;
    public boolean shouldSave = true;
    public boolean deathAnimation = true;
    public boolean spawnAnimation = true;
    public boolean colli = true;
    public boolean navi = false;
    //public boolean bound = true;
    public boolean noCulling = false;
    public boolean shouldTick = true;
    public boolean isDead = false;
    public boolean removeFlag = false;
    protected boolean faceCamera = true;
    protected boolean canHitUser = false;

    public int layer = 0;

    public Color color = Color(255,255,255,255);
    public THRenderType.BLEND blend = THRenderType.BLEND.LIGHTEN;
    protected CollisionType collisionType = CollisionType.AABB;

    public THObject(THObjectType<? extends THObject> type,EntityTHObjectContainer container) {
        this.type = type;
        this.container = container;
        this.random = container.random;
        this.level = container.level();
        this.scriptManager = new JSManager();
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
        this.setVelocityFromDirection(speed,vectorRotation,true);
        this.spawn();
        return this;
    }

    public THObject shoot(Vec3 velocity){
        this.setVelocity(velocity,true);
        this.spawn();
        return this;
    }

    public THObject shoot(float speed, Vec2 rotation, boolean isDeg){
        this.setVelocityFromRotation(speed, rotation, isDeg,true);
        this.spawn();
        return this;
    }

    public void injectScript(String script){
        this.scriptManager.setScript(script);
    }

    public void spawn(){
        if (!this.container.getObjectManager().contains(this)) {
            this.container.getObjectManager().addTHObject(this);
        }
    }

    public void setDead() {
        this.isDead = true;
        //this.onDead();
    }

    public void remove() {
        this.removeFlag = true;
    }

    public void setPosition(Vec3 pos) {
        this.positionX = pos.x;
        this.positionY = pos.y;
        this.positionZ = pos.z;
    }

    public void setPosition(double x, double y, double z){
        this.positionX = x;
        this.positionY = y;
        this.positionZ = z;
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

    public void setVelocityFromDirection(float speed, Vec3 direction, boolean setRotation) {
        this.setVelocity(direction.normalize().multiply(speed,speed,speed),setRotation);
    }

    public void setVelocityFromRotation(float speed, Vec2 rotation, boolean isDeg, boolean setRotation) {
        this.setVelocityFromDirection(speed, Vec3.directionFromRotation(isDeg ? rotation : rotation.scale(Mth.RAD_TO_DEG)), false);

        if (setRotation){
            this.setRotation(isDeg? rotation.scale(Mth.DEG_TO_RAD) : rotation);
        }
    }

    public void setAcceleration(Vec3 acceleration){
        this.acceleration = acceleration;
    }

    public void setAccelerationFromDirection(float acceleration, Vec3 direction){
        this.setAcceleration(direction.normalize().multiply(acceleration,acceleration,acceleration));
    }

    public void setAccelerationFromRotation(float acceleration, Vec2 rotation, boolean isDeg){
        this.setAccelerationFromDirection(acceleration, Vec3.directionFromRotation(isDeg ? rotation : rotation.scale(Mth.RAD_TO_DEG)));
    }

    public void setRotation(float xRot,float yRot,float zRot) {
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
    }

    public void setRotation(Vector3f rotation) {
        this.setRotation(rotation.x,rotation.y,rotation.z);
    }

    public void setRotation(Vec2 rotation){
        this.xRot = rotation.x;
        this.yRot = rotation.y;
    }

    public void setRotationByDirectionalVector(Vec3 vectorRotation) {
        this.setRotation(VectorAngleToRadAngle(vectorRotation));
    }

    public static Vec2 VectorAngleToRadAngle(Vec3 formDir){
        float y = (float) Mth.atan2(formDir.x,formDir.z);
        float x = (float) Mth.atan2(formDir.y,Mth.sqrt((float) (formDir.x*formDir.x+formDir.z*formDir.z)));
        return new Vec2(x,y);
    }

    public static Vec2 VectorAngleToRadAngleInverseX(Vec3 formDir){
        Vec2 vec2 = VectorAngleToRadAngle(formDir);
        return new Vec2(-vec2.x,vec2.y);
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

    public CollisionType getCollisionType(){
        return this.collisionType;
    }

    public void setCollisionType(CollisionType type){
        this.collisionType = type;
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
        this.setBoundingBox(this.getPosition(), this.size);
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

        if (--this.lifetime < 0 || (/*this.bound &&*/ !this.getContainer().getAabb().contains(this.getPosition()))){
            this.setDead();
        }

        if (this.isDead){
            this.onDead();
        }

        this.scriptManager.invokeScript("onTick", (exception)-> {
            if(this.container != null){
                this.container.remove(Entity.RemovalReason.DISCARDED);
            }
            this.remove();
        }, this);

        this.timer += 1;
    }

    public void collision(){
        List<Entity> entitiesInBound = this.container.getEntitiesInBound();
        if(entitiesInBound.isEmpty()){
            return;
        }

        this.collisionType = CollisionType.ELLIPSOID;
        //this.setSize(new Vec3(0.5f,0.5f,2.0f));

        entitiesInBound.forEach(entity -> {
            if (this.collisionType == CollisionType.AABB) {
                if (entity.getBoundingBox().intersects(this.getBoundingBox())) {
                    this.onHit(new EntityHitResult(entity, this.getPosition()));
                }
            } else if (this.collisionType == CollisionType.SPHERE) {
                if(CollisionHelper.isCollidingSphereBox(this.getPosition(), this.size.x,entity.getBoundingBox())){
                    this.onHit(new EntityHitResult(entity, this.getPosition()));
                }
            } else if (this.collisionType == CollisionType.ELLIPSOID) {
                if(CollisionHelper.isCollidingOrientedEllipsoidBox(this.getPosition(), this.size, this.getRotation(),entity.getBoundingBox())){
                    this.onHit(new EntityHitResult(entity, this.getPosition()));
                }
            } else if (this.collisionType == CollisionType.CUBOID) {
                // TODO cuboid collision
            }
        });

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
        HitResult.Type type = result.getType();

        if (type == HitResult.Type.MISS) {
            return;
        }

        if (type == HitResult.Type.ENTITY && result instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (!this.canHitUser && entity.equals(this.container.getUser()))
                return;
            this.onHitEntity(entityHitResult);
        }

        if (type == HitResult.Type.BLOCK && result instanceof BlockHitResult blockHitResult) {
            this.onHitBlock(blockHitResult);
        }

        this.setDead();
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
        /*
        THTasker tasker = this.container.taskerManager.create();
        for(int i=0;i<10;i++) {
            tasker.add(() -> {
                this.color.a -= 255 / 10;
            });
            tasker.wait(1);
        }
        tasker.add(this::remove);
        tasker.lock();
         */
        this.setVelocity(Vec3.ZERO,false);
        if (this.deathAnimation) {
            this.deathLastingTime--;
            if (this.deathLastingTime <= 0) {
                this.remove();
            }
            this.color.a -= 255/10;
        } else {
            this.remove();
        }
    }

    public void onRemove(){

    }

    public void setBlend(THRenderType.BLEND blend){
        this.blend = blend;
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
        buffer.writeVec3(this.lastPosition);
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
        buffer.writeInt(this.deathLastingTime);
        buffer.writeEnum(this.blend);
        buffer.writeBoolean(this.isDead);
        buffer.writeBoolean(this.colli);
        buffer.writeEnum(this.collisionType);
        //buffer.writeBoolean(this.bound);
        buffer.writeBoolean(this.shouldSave);
        this.scriptManager.writeData(buffer);
    }

    public void readData(FriendlyByteBuf buffer){
        this.positionX = buffer.readDouble();
        this.positionY = buffer.readDouble();
        this.positionZ = buffer.readDouble();
        this.lastPosition = buffer.readVec3();
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
        this.deathLastingTime = buffer.readInt();
        this.blend = buffer.readEnum(THRenderType.BLEND.class);
        this.isDead = buffer.readBoolean();
        this.colli = buffer.readBoolean();
        this.collisionType = buffer.readEnum(THObject.CollisionType.class);
        //this.bound = buffer.readBoolean();
        this.shouldSave = buffer.readBoolean();
        this.scriptManager.readData(buffer);
        this.setBoundingBox(this.getPosition(), this.size);
    }

    public CompoundTag save(CompoundTag tag) {
        //tag.putString("type", this.getType().getKey().toString());
        tag.put("Pos", newDoubleList(this.positionX, this.positionY, this.positionZ));
        tag.put("PrePos", newVec3(this.prePosition));
        tag.put("Rotation", newVector3f(this.getRotation()));
        tag.put("Velocity", newVec3(this.velocity));
        tag.put("Acceleration", newVec3(this.acceleration));
        tag.put("Scale", newVector3f(this.scale));
        tag.put("Size", newVec3(this.size));
        Color c = this.color;
        tag.put("Color", newIntList(c.r,c.g,c.b,c.a));
        tag.put("Timers", newIntList(this.timer,this.lifetime,this.deathLastingTime));
        tag.putInt("Blend",this.blend.ordinal());
        tag.putBoolean("IsDead", this.isDead);
        tag.putBoolean("Collision",this.colli);
        tag.putInt("CollisionType",this.collisionType.ordinal());
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
        this.deathLastingTime = timerTag.getInt(2);
        this.blend = THRenderType.BLEND.class.getEnumConstants()[tag.getInt("Blend")];
        this.isDead = tag.getBoolean("IsDead");
        this.colli = tag.getBoolean("Collision");
        this.collisionType = THObject.CollisionType.class.getEnumConstants()[tag.getInt("CollisionType")];
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

    @OnlyIn(value = Dist.CLIENT)
    public void onRender(EntityTHObjectContainerRenderer renderer, Vec3 objectPos, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay) {
        if(this.color.a <= 0){
            return;
        }

        poseStack.pushPose();
        if(this.faceCamera) {
            poseStack.mulPose(renderer.getRenderDispatcher().cameraOrientation());
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

        VertexConsumer vertexconsumer = bufferSource.getBuffer(this.blend.renderType.apply(this.getTexture()));

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

    public enum CollisionType{
        AABB,
        SPHERE,
        ELLIPSOID,
        CUBOID
    }

    public static class Color{
        public int r,g,b,a;

        /*
        private static final Color WHITE =   new Color(255,255,255,255);
        private static final Color GRAY =    new Color(255,255,255,255).multiply(0.5f);
        private static final Color BLACK =   new Color(0,0,0,255);
        private static final Color VOID =    new Color(0,0,0,0);
        */

        public static Color WHITE(){
            return new Color(255,255,255,255);
        }
        public static Color GRAY(){
            return new Color(255,255,255,255).multiply(0.5f);
        }
        public static Color BLACK(){
            return new Color(0,0,0,255);
        }
        public static Color VOID(){
            return new Color(0,0,0,0);
        }

        Color(int r, int g, int b, int a){
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        Color(Color color){
            this.r = color.r;
            this.g = color.g;
            this.b = color.b;
            this.a = color.a;
        }

        public Color normalize(){
            int r = Mth.clamp(this.r,0,255);
            int g = Mth.clamp(this.g,0,255);
            int b = Mth.clamp(this.b,0,255);
            int a = Mth.clamp(this.a,0,255);
            return new Color(r,g,b,a);
        }

        public Color add(int r, int g, int b, int a){
            return new Color(this.r+r,this.g+g,this.b+b,this.a+a);
        }

        public Color subtract(int r, int g, int b, int a){
            return new Color(this.r-r,this.g-g,this.b-b,this.a-a);
        }

        public Color subtract(Color color){
            return new Color(this.r-color.r,this.g-color.g,this.b-color.b,this.a-color.a);
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

        public int[] of(){
            return new int[] {r,g,b,a};
        }
    }

    public static Color Color(int r, int g, int b, int a){
        return new Color(r,g,b,a);
    }

    public static Color Color(int r, int g, int b){
        return Color(r,g,b,255);
    }
}