package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.script.IScript;
import com.adrian.thDanmakuCraft.script.ScriptManager;
import com.adrian.thDanmakuCraft.script.lua.LuaManager;
import com.adrian.thDanmakuCraft.world.AdditionalParameterManager;
import com.adrian.thDanmakuCraft.world.THObjectContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.joml.*;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

import java.lang.Math;
import java.util.List;
import java.util.UUID;

public class THObject implements IScript{
    private final THObjectType<? extends THObject> type;
    private final ScriptManager scriptManager;
    private final AdditionalParameterManager parameterManager;
    private final Level level;
    protected final RandomSource random;
    protected THObjectContainer container;
    protected static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(THDanmakuCraftCore.MOD_ID, "textures/white.png");
    protected THImage image = new THImage(TEXTURE_WHITE,0.0f,0.0f,1.0f,1.0f);
    protected static final AABB INITIAL_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    protected AABB bb = INITIAL_AABB;
    protected double positionX;                    //Object Position
    protected double positionY;
    protected double positionZ;
    protected Vec3 prePosition;
    protected Vec3 lastPosition;
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
    protected int deathLastingTime = 10;
    public boolean shouldSave = true;
    public boolean deathAnimation = true;
    public boolean spawnAnimation = true;
    public boolean collision = true;
    public boolean shouldCollingWithBlock = true;
    public boolean navi = false;
    //public boolean bound = true;
    public boolean noCulling = false;
    public boolean shouldTick = true;
    public boolean isDead = false;
    public boolean removeFlag = false;
    protected boolean faceCamera = true;
    protected boolean canHitUser = false;
    protected boolean shouldSetDeadWhenCollision = true;
    public UUID uuid;

    public int layer = 0;

    public Color color = Color(255, 255, 255, 255);
    //public THRenderType.BLEND blend = THRenderType.BLEND.LIGHTEN;
    protected Blend blend = Blend.add;
    protected CollisionType collisionType = CollisionType.AABB;

    public THObject(THObjectType<? extends THObject> type, THObjectContainer container) {
        this.type = type;
        this.container = container;
        this.random = container.getRandomSource();
        this.level = container.level();
        this.scriptManager = new LuaManager();
        this.parameterManager = new AdditionalParameterManager(this.container);
        this.uuid = Mth.createInsecureUUID(this.random);
        this.initPosition(container.getPosition());
    }

    public THObject(THObjectContainer container, Vec3 position) {
        this(THObjectInit.TH_OBJECT.get(), container);
    }

    public <T extends THObject> T initPosition(Vec3 position) {
        this.setPosition(position);
        this.lastPosition = position;
        this.prePosition = position;
        return (T) this;
    }

    public <T extends THObject> T shoot(float speed, Vec3 vectorRotation) {
        this.setVelocityFromDirection(speed, vectorRotation, true);
        this.spawn();
        return (T) this;
    }

    public <T extends THObject> T shoot(Vec3 velocity) {
        this.setVelocity(velocity, true);
        this.spawn();
        return (T) this;
    }

    public <T extends THObject> T shoot(float speed, Vec2 rotation, boolean isDeg) {
        this.setVelocityFromRotation(speed, rotation, isDeg, true);
        this.spawn();
        return (T) this;
    }

    public void injectScript(String script) {
        this.scriptManager.setScript(script);
    }

    public <T extends  THObject> T spawn() {
        if (!this.container.getObjectManager().contains(this)) {
            this.container.getObjectManager().addTHObject(this);
        }
        return (T) this;
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

    public void setPosition(double x, double y, double z) {
        this.positionX = x;
        this.positionY = y;
        this.positionZ = z;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public void setScale(float x, float y, float z) {
        this.scale = new Vector3f(x, y, z);
    }

    public void setScale(Vec3 scale) {
        this.scale = scale.toVector3f();
    }

    public void setSize(Vec3 size) {
        this.size = size;
    }

    public void setSize(float x, float y, float z) {
        this.size = new Vec3(x, y, z);
    }

    public void setVelocity(Vec3 velocity, boolean setRotation) {
        this.velocity = velocity;
        if (setRotation) {
            this.setRotationByDirectionalVector(velocity);
        }
    }

    public void setVelocityFromDirection(float speed, Vec3 direction, boolean setRotation) {
        this.setVelocity(direction.normalize().multiply(speed, speed, speed), setRotation);
    }

    public void setVelocityFromRotation(float speed, Vec2 rotation, boolean isDeg, boolean setRotation) {
        this.setVelocityFromDirection(speed, Vec3.directionFromRotation(isDeg ? rotation : rotation.scale(Mth.RAD_TO_DEG)), false);

        if (setRotation) {
            this.setRotation(isDeg ? rotation.scale(Mth.DEG_TO_RAD) : rotation);
        }
    }

    public void setAcceleration(Vec3 acceleration) {
        this.acceleration = acceleration;
    }

    public void setAccelerationFromDirection(float acceleration, Vec3 direction) {
        this.setAcceleration(direction.normalize().multiply(acceleration, acceleration, acceleration));
    }

    public void setAccelerationFromRotation(float acceleration, Vec2 rotation, boolean isDeg) {
        this.setAccelerationFromDirection(acceleration, Vec3.directionFromRotation(isDeg ? rotation : rotation.scale(Mth.RAD_TO_DEG)));
    }

    public void setRotation(float xRot, float yRot, float zRot) {
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
    }

    public void setRotation(Vector3f rotation) {
        this.setRotation(rotation.x, rotation.y, rotation.z);
    }

    public void setRotation(Vec3 vec3){
        this.setRotation(vec3.toVector3f());
    }

    public void setRotation(Vec2 rotation) {
        this.xRot = rotation.x;
        this.yRot = rotation.y;
    }

    public void setRotationByDirectionalVector(Vec3 vectorRotation) {
        this.setRotation(VectorAngleToRadAngle(vectorRotation));
    }

    public static Vec2 VectorAngleToRadAngle(Vec3 formDir) {
        float y = (float) Mth.atan2(formDir.x, formDir.z);
        float x = (float) Mth.atan2(formDir.y, Mth.sqrt((float) (formDir.x * formDir.x + formDir.z * formDir.z)));
        return new Vec2(x, y);
    }

    public static Vec2 VectorAngleToRadAngleInverseX(Vec3 formDir) {
        Vec2 vec2 = VectorAngleToRadAngle(formDir);
        return new Vec2(-vec2.x, vec2.y);
    }

    public static Vec2 VectorAngleToEulerDegAngle(Vec3 formDir) {
        return VectorAngleToRadAngle(formDir).scale(Mth.RAD_TO_DEG);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setColor(int r, int g, int b, int a) {
        this.setColor(Color(r, g, b, a));
    }

    public void setLifetime(int time) {
        this.lifetime = time;
    }

    public int getTimer() {
        return this.timer;
    }

    public Color getColor() {
        return this.color;
    }

    public boolean getShouldFaceCamera(){
        return this.faceCamera;
    }

    public Vec3 getPosition() {
        return new Vec3(positionX, positionY, positionZ);
    }

    public double[] getPositionArray(){
        return new double[]{positionX, positionY, positionZ};
    }

    public Vec3 getPrePosition() {
        return this.prePosition;
    }

    public Vec3 getOffsetPosition(float partialTicks) {
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

    public Vec3 getVelocity() {
        return this.velocity;
    }

    public Vec3 getMotionDirection() {
        return this.lastPosition.vectorTo(this.getPosition()).normalize();
    }

    public Vector3f getRotation() {
        return new Vector3f(this.xRot, this.yRot, this.zRot);
    }

    public final float getXRot() {
        return this.xRot;
    }

    public final float getYRot() {
        return this.yRot;
    }

    public final float getZRot() {
        return this.zRot;
    }

    public Vec3 getAcceleration() {
        return this.acceleration;
    }

    public Vector3f getScale() {
        return this.scale;
    }

    public Vec3 getSize() {
        return this.size;
    }

    public Blend getBlend(){
        return this.blend;
    }

    public String getBlendName(){
        return this.blend.name();
    }

    public boolean getIsDead() {
        return this.isDead;
    }

    public boolean getIsAlive() {
        return !this.isDead;
    }

    public void setCollisionType(CollisionType type) {
        this.collisionType = type;
    }

    public void setCollisionType(int type) {
        this.collisionType = CollisionType.values()[type];
    }

    public void setCollisionType(String type) {
        this.collisionType = CollisionType.valueOf(type);
    }

    public CollisionType getCollisionType() {
        return this.collisionType;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getUUIDasString() {
        return this.uuid.toString();
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

    public THObjectContainer getContainer() {
        return this.container;
    }

    public void setContainer(THObjectContainer container) {
        this.container = container;
    }

    public void setNewContainer(THObjectContainer container) {
        if (container.equals(this.container)) {
            return;
        }

        this.container.getObjectManager().removeTHObject(this);
        if (!container.getObjectManager().contains(this)) {
            container.getObjectManager().addTHObject(this);
        }

        this.setContainer(container);
    }

    public void move(double x, double y, double z) {
        this.positionX += x;
        this.positionY += y;
        this.positionZ += z;
    }

    public void move(Vec3 pos) {
        this.move(pos.x, pos.y, pos.z);
    }

    public void onTick() {
        /*
        if(!this.getContainer().getObjectFromUUID(this.getUUID()).equals(this)){
            this.remove();
        }*/

        this.lastPosition = new Vec3(this.positionX, this.positionY, this.positionZ);

        if (!this.shouldTick) {
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

        if (this.collision) {
            this.collisionLogic();
        }

        if (this.navi && !this.isDead) {
            this.setRotation(VectorAngleToRadAngle(this.getMotionDirection()));
        }

        if (--this.lifetime < 0 || (/*this.bound &&*/ !this.getContainer().getAabb().contains(this.getPosition()))) {
            this.setDead();
        }

        if (this.isDead) {
            this.onDead();
        }

        this.scriptManager.invokeScript("onTick", (exception) -> {
            THDanmakuCraftCore.LOGGER.error("Failed invoke script!", exception);
            if (this.container != null) {
                this.container.getHostEntity().remove(Entity.RemovalReason.DISCARDED);
            }
            this.remove();
        }, this);

        this.timer++;
    }

    public void collisionLogic() {
        List<Entity> entitiesInBound = this.container.getEntitiesInBound();
        if (entitiesInBound.isEmpty()) {
            return;
        }

        /*
        this.collisionType = CollisionType.ELLIPSOID;
        this.setSize(new Vec3(0.5f,0.5f,2.0f));
        this.setRotation(this.timer/40.0f,this.timer/10.0f,0.0f);

         */

        entitiesInBound.forEach(entity -> {
            if(!this.canHitUser && entity.equals(this.getContainer().getUser())){
                return;
            }
            if (this.collisionType == CollisionType.AABB) {
                AABB aabb = this.getBoundingBox();
                if (entity.getBoundingBox().intersects(aabb)) {
                    var result = new EntityHitResult(entity, this.getPosition());
                    //this.onHitEntity(result);
                    this.onHit(result);
                }
            }else if (this.collisionType.collisionEntity(this,entity)) {
                var result = new EntityHitResult(entity, this.getPosition());
                //this.onHitEntity(result);
                this.onHit(result);
            }
        });

        if(this.shouldCollingWithBlock) {
            if (this.collisionType == CollisionType.AABB) {
                AABB aabb = this.getBoundingBox();
                BlockHitResult result = this.level.clip(new ClipContext(
                        new Vec3(aabb.minX, aabb.minY, aabb.minZ),
                        new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        this.container.getHostEntity()));
                if (result.getType() != HitResult.Type.MISS) {
                    //this.onHitBlock(result);
                    this.onHit(result);
                }
            } else {
                double length = Mth.absMax(Mth.absMax(size.x, size.y), size.z);
                AABB box = new AABB(
                        this.getPosition().subtract(length, length, length),
                        this.getPosition().add(length, length, length)
                );
                for (double z = box.minZ; z <= box.maxZ; z += 1) {
                    for (double y = box.minY; y <= box.maxY; y += 1) {
                        for (double x = box.minX; x <= box.maxX; x += 1) {
                            BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
                            if (!this.level.getBlockState(pos).isAir()) {
                                if (this.collisionType.collisionBlock(this, pos)) {
                                    BlockHitResult result = new BlockHitResult(
                                            new Vec3(box.maxX, box.maxY, box.maxZ),
                                            Direction.getNearest(new Vec3(box.minX, box.minY, box.minZ)),
                                            pos, true);
                                    //this.onHitBlock(result);
                                    this.onHit(result);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void onHit(HitResult result) {
        if(this.shouldSetDeadWhenCollision) {
            this.setDead();
        }

        if(result.getType() == HitResult.Type.ENTITY && result instanceof EntityHitResult entityHitResult) {
            this.onHitEntity(entityHitResult);
        }

        if(result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult blockHitResult) {
            this.onHitBlock(blockHitResult);
        }
    }

    public void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if (this.damage <= 0.0f) {
            return;
        }
        entity.hurt(this.level.damageSources().magic(), this.damage);
    }

    public void onHitBlock(BlockHitResult result) {
        this.level.removeBlock(result.getBlockPos(),true);
    }

    public void onDead() {
        this.collision = false;
        this.setVelocity(Vec3.ZERO, false);
        if (this.deathAnimation) {
            this.deathLastingTime--;
            if (this.deathLastingTime <= 0) {
                this.remove();
            }
            this.color.a -= 255 / 10;
        } else {
            this.remove();
        }
    }

    public void onRemove() {

    }

    public void setBlend(Blend blend) {
        this.blend = blend;
    }

    public void setBlend(String blend){
        this.blend = Blend.valueOf(blend);
    }

    public boolean hasContainer() {
        return this.container != null && !this.container.getHostEntity().isRemoved();
    }

    public THObjectType<? extends THObject> getType() {
        return this.type;
    }

    public void writeData(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.uuid);
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
        buffer.writeBoolean(this.collision);
        buffer.writeEnum(this.collisionType);
        //buffer.writeBoolean(this.bound);
        buffer.writeBoolean(this.shouldSave);
        //this.blend.writeData(buffer);
        this.scriptManager.writeData(buffer);
        this.parameterManager.writeData(buffer);
    }

    public void readData(FriendlyByteBuf buffer) {
        this.uuid = buffer.readUUID();
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
        int r, g, b, a;
        r = buffer.readInt();
        g = buffer.readInt();
        b = buffer.readInt();
        a = buffer.readInt();
        this.color = Color(r, g, b, a);
        this.timer = buffer.readInt();
        this.lifetime = buffer.readInt();
        this.deathLastingTime = buffer.readInt();
        this.blend = buffer.readEnum(Blend.class);
        this.isDead = buffer.readBoolean();
        this.collision = buffer.readBoolean();
        this.collisionType = buffer.readEnum(THObject.CollisionType.class);
        //this.bound = buffer.readBoolean();
        this.shouldSave = buffer.readBoolean();
        //this.blend.readData(buffer);
        this.scriptManager.readData(buffer);
        this.parameterManager.readData(buffer);
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
        tag.put("Color", newIntList(c.r, c.g, c.b, c.a));
        tag.put("Timers", newIntList(this.timer, this.lifetime, this.deathLastingTime));
        tag.putInt("Blend", this.blend.ordinal());
        tag.putBoolean("IsDead", this.isDead);
        tag.putBoolean("Collision", this.collision);
        tag.putInt("CollisionType", this.collisionType.ordinal());
        tag.putUUID("UUID", this.uuid);
        this.scriptManager.save(tag);
        tag.put("parameters", this.parameterManager.save(new CompoundTag()));
        //this.blend.save(tag);
        return tag;
    }

    public void load(CompoundTag tag) {
        ListTag posTag = tag.getList("Pos", Tag.TAG_DOUBLE);
        ListTag prePosTag = tag.getList("PrePos", Tag.TAG_DOUBLE);
        ListTag rotationTag = tag.getList("Rotation", Tag.TAG_FLOAT);
        ListTag velocityTag = tag.getList("Velocity", Tag.TAG_DOUBLE);
        ListTag accelerationTag = tag.getList("Acceleration", Tag.TAG_DOUBLE);
        ListTag scaleTag = tag.getList("Scale", Tag.TAG_FLOAT);
        ListTag sizeTag = tag.getList("Size", Tag.TAG_DOUBLE);
        ListTag colorTag = tag.getList("Color", Tag.TAG_INT);
        ListTag timerTag = tag.getList("Timers", Tag.TAG_INT);
        this.setPosition(new Vec3(posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2)));
        this.prePosition = new Vec3(prePosTag.getDouble(0), prePosTag.getDouble(1), prePosTag.getDouble(2));
        this.velocity = new Vec3(velocityTag.getDouble(0), velocityTag.getDouble(1), velocityTag.getDouble(2));
        this.setRotation(rotationTag.getFloat(0), rotationTag.getFloat(1), rotationTag.getFloat(2));
        this.acceleration = new Vec3(accelerationTag.getDouble(0), accelerationTag.getDouble(1), accelerationTag.getDouble(2));
        this.scale = new Vector3f(scaleTag.getFloat(0), scaleTag.getFloat(1), scaleTag.getFloat(2));
        this.size = new Vec3(sizeTag.getDouble(0), sizeTag.getDouble(1), sizeTag.getDouble(2));
        this.setColor(colorTag.getInt(0), colorTag.getInt(1), colorTag.getInt(2), colorTag.getInt(3));
        this.timer = timerTag.getInt(0);
        this.lifetime = timerTag.getInt(1);
        this.deathLastingTime = timerTag.getInt(2);
        this.blend = Blend.class.getEnumConstants()[tag.getInt("Blend")];
        this.isDead = tag.getBoolean("IsDead");
        this.collision = tag.getBoolean("Collision");
        this.collisionType = THObject.CollisionType.class.getEnumConstants()[tag.getInt("CollisionType")];
        this.scriptManager.load(tag);
        this.parameterManager.load(tag.getCompound("parameters"));
        this.uuid = tag.getUUID("UUID");
        //this.blend.load(tag);
    }

    public AdditionalParameterManager getParameterManager() {
        return this.parameterManager;
    }

    public boolean shouldRender(double camX, double camY, double camZ) {
        double d0 = this.positionX - camX;
        double d1 = this.positionY - camY;
        double d2 = this.positionZ - camZ;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        return this.shouldRenderAtSqrDistance(d3, 80.0D);
    }

    public boolean shouldRenderAtSqrDistance(double sqrDist, double distance) {
        double d0 = (Math.max(this.getBoundingBox().getSize(), 1.0D)) * 4.0D;
        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }

        d0 *= distance;
        return sqrDist < d0 * d0;
    }

    public void setImage(THImage image) {
        this.image = image;
    }
    
    public THImage getImage() {
        return this.image;
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

    protected static ListTag newStringList(String... value) {
        ListTag listtag = new ListTag();
        for(String i : value) {
            listtag.add(StringTag.valueOf(i));
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

    public LuaValue ofLuaValue(){
        LuaValue library = LuaValue.tableOf();
        library.set( "setPosition", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue luaValue) {
                THObject.this.setPosition(luaValue.get(1).checkdouble(),luaValue.get(2).checkdouble(),luaValue.get(3).checkdouble());
                return LuaValue.NIL;
            }
        });
        return library;
    }

    public enum CollisionType{
        AABB(CollisionType::AABB),
        SPHERE(CollisionType::SPHERE),
        ELLIPSOID(CollisionType::Ellipsoid),
        CUBOID(CollisionType::CUBOID),;

        private final CollisionFactory factory;
        CollisionType(CollisionFactory factory){
            this.factory = factory;
        }

        public void collisionEntity(THObject object, Entity entity, Runnable whenColling){
            if (this.factory.collision(object.getPosition(),object.getSize(),object.getRotation(),entity.getBoundingBox())){
                whenColling.run();
            }
        }

        public boolean collision(THObject object, AABB aabb){
            return this.factory.collision(object.getPosition(),object.getSize(),object.getRotation(),aabb);
        }

        public boolean collisionEntity(THObject object, Entity entity){
            return this.factory.collision(object.getPosition(),object.getSize(),object.getRotation(),entity.getBoundingBox());
        }

        public boolean collisionBlock(THObject object, BlockPos pos){
            return this.factory.collision(object.getPosition(),object.getSize(),object.getRotation(),
                    new AABB(pos.getX(),pos.getY(),pos.getZ(),
                            pos.getX()+1,pos.getY()+1,pos.getZ()+1));
        }

        public static boolean AABB(Vec3 center, Vec3 size, Vector3f rotation, AABB aabb){
            return CollisionHelper.isCollidingAABB(center,size,aabb);
        }

        public static boolean SPHERE(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb){
            return CollisionHelper.isCollidingSphereBox(center,scale.x,aabb);
        }

        public static boolean Ellipsoid(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb){
            return CollisionHelper.isCollidingOrientedEllipsoidBox(center,scale,rotation,aabb);
        }

        public static boolean CUBOID(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb){
            return CollisionHelper.isCollidingAABB(center,scale,aabb);
        }

        public

        interface CollisionFactory{
            boolean collision(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb);
        }
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

        public int[] getAll(){
            return new int[] {r,g,b,a};
        }
    }

    public static Color Color(int r, int g, int b, int a){
        return new Color(r,g,b,a);
    }

    public static Color Color(int r, int g, int b){
        return Color(r,g,b,255);
    }

    public enum Blend {
        /*
        public static int ADD = 32774;
        public static int SUBTRACT = 32778;
        public static int REVERSE_SUBTRACT = 32779;
        public static int MIN = 32775;
        public static int MAX = 32776;

        public static int ZERO = 0;
        public static int ONE = 1;
        public static int SRC_COLOR = 768;
        public static int ONE_MINUS_SRC_COLOR = 769;
        public static int DST_COLOR = 774;
        public static int ONE_MINUS_DST_COLOR = 775;
        public static int SRC_ALPHA = 770;
        public static int ONE_MINUS_SRC_ALPHA = 771;
        public static int DST_ALPHA = 772;
        public static int ONE_MINUS_DST_ALPHA = 773;
         */
        normal("add","src_alpha","one_minus_src_alpha","one","one_minus_src_alpha"),
        add("add","src_alpha","one"),
        sub("subtract","src_alpha","one_minus_src_alpha"),
        max("max","src_alpha","one_minus_src_alpha"),
        min("min","src_alpha","one_minus_src_alpha"),
        mul_add("add","dst_color","1-srcalpha","one","1-srcalpha"),
        mul_rev("reverse_subtract","dstcolor","1-srcalpha","one","1-srcalpha"),
        mul_rev2("reverse_subtract","src_alpha","one_minus_src_alpha","zero","one");

        private final String blendFunc,
                srcColorFactor,
                dstColorFactor,
                srcAlphaFactor,
                dstAlphaFactor;
        private final boolean separateBlend;

        Blend(boolean separateBlend,String blendFunc, String srcColor, String dstColor, String srcAlpha, String dstAlpha) {
            this.separateBlend = separateBlend;
            this.blendFunc = blendFunc;
            this.srcColorFactor = srcColor;
            this.dstColorFactor = dstColor;
            this.srcAlphaFactor = srcAlpha;
            this.dstAlphaFactor = dstAlpha;
        }

        Blend(String blendFunc, String src, String dst) {
            this(false,blendFunc,src,dst,src,dst);
        }

        Blend(String blendFunc, String srcColor, String dstColor, String srcAlpha, String dstAlpha) {
            this(true,blendFunc,srcColor,dstColor,srcAlpha,dstAlpha);
        }

        public String getBlendFunc() {
            return this.blendFunc;
        }

        public String getSrcColor() {
            return this.srcColorFactor;
        }

        public String getDstColor() {
            return this.dstColorFactor;
        }

        public String getSrcAlpha() {
            return this.srcAlphaFactor;
        }

        public String getDstAlpha() {
            return this.dstAlphaFactor;
        }

        public boolean isSeparateBlend() {
            return this.separateBlend;
        }

        public String[] getALL(){
            return new String[]{
                    blendFunc,
                    srcColorFactor,
                    dstColorFactor,
                    srcAlphaFactor,
                    dstAlphaFactor
            };
        }
    }
}