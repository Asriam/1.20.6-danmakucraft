package com.adrian.thDanmakuCraft.world.danmaku.thobject;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.lua.LuaCore;
import com.adrian.thDanmakuCraft.util.*;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import com.adrian.thDanmakuCraft.world.danmaku.*;
import com.google.common.collect.Maps;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.joml.Vector3f;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.adrian.thDanmakuCraft.world.LuaValueHelper.*;
import static com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject.api.*;

public class THObject implements ILuaValue {
    //private static final Logger log = LoggerFactory.getLogger(THObject.class);
    private final THObjectType<? extends THObject> type;
    private final AdditionalParameterManager parameterManager;
    //private final LuaTaskManager luaTaskManager;
    //private final Level level;
    protected final RandomSource random = RandomSource.create();
    protected ITHObjectContainer container;
    protected static final ResourceLocation TEXTURE_WHITE = ResourceLocationUtil.mod("textures/white.png");
    protected IImage.Image image = new IImage.Image(TEXTURE_WHITE, 0.0f, 0.0f, 1.0f, 1.0f);
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
    private UUID uuid;
    public boolean isSpawned = false;
    private LuaValue luaClass;
    private String luaClassKey = "";
    public int index = 0;

    public Color color = Color(255, 255, 255, 255);
    protected Blend blend = Blend.add;
    protected CollisionType collisionType = CollisionType.AABB;
    protected LuaValue luaValueForm;
    private final LuaValueStorageHelper luaValueStorageHelper;

    public THObject(THObjectType<? extends THObject> type, ITHObjectContainer container) {
        this.type = type;
        this.container = container;
        //this.level = container.level();
        this.parameterManager = new AdditionalParameterManager(this.container);
        //this.luaTaskManager = new LuaTaskManager(this);
        this.luaValueStorageHelper = new LuaValueStorageHelper(this.container);
        this.uuid = Mth.createInsecureUUID(this.random);
        this.initPosition(container.getPosition());
        //this.initLuaValue();
        //this.addTasks();
    }

    public THObject(THObjectContainer container, Vec3 position) {
        this(THObjectInit.TH_OBJECT.get(), container);
        this.initPosition(position);
    }

    public THObject(THObjectContainer container, Vec3 position, String luaClassKey) {
        this(container,position);
        this.init(luaClassKey,LuaValue.NIL);
    }

    public void init(String luaClassKey, Varargs args) {
        this.setLuaClassKey(luaClassKey);
        this.initLuaValue();
        LuaValue luaObject = this.ofLuaValue();
        if(args == LuaValue.NIL){
            this.invokeScriptEvent("onInit",luaObject);
        }else {
            this.invokeScriptEvent("onInit", LuaValue.varargsOf(luaObject, args));
        }
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

    public void spawn() {
        /*
        if (!this.container.getObjectManager().contains(this)) {
            this.container.getObjectManager().addTHObject(this);
            this.isSpawned = true;
        }*/
        this.container.spawnTHObject(this);
    }

    public boolean isSpawned() {
        this.isSpawned = !this.removeFlag && (this.isSpawned || this.container.getObjectManager().contains(this));
        return this.isSpawned;
    }

    public void copy(THObject object){
        this.positionX = object.positionX;
        this.positionY = object.positionY;
        this.positionZ = object.positionZ;
        this.lastPosition = object.lastPosition;
        this.prePosition = object.prePosition;
        this.xRot = object.xRot;
        this.yRot = object.yRot;
        this.zRot = object.zRot;
    }

    public float getDamage(){
        return this.damage * (1.0f + this.level().getDifficulty().getId() * 0.11f);
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

    public void setSize(double x, double y, double z) {
        this.size = new Vec3(x, y, z);
    }

    public void setVelocity(Vec3 velocity, boolean setRotation) {
        this.velocity = velocity;
        if (setRotation) {
            this.setRotationByDirectionalVector(velocity);
        }
    }

    public void setNavi(boolean navi) {
        this.navi = navi;
    }

    public void setVelocityFromDirection(double speed, Vec3 direction, boolean setRotation) {
        this.setVelocity(direction.normalize().multiply(speed, speed, speed), setRotation);
    }

    public void setVelocityFromRotation(double speed, Vec2 rotation, boolean isDeg, boolean setRotation) {
        this.setVelocityFromDirection(speed, Vec3.directionFromRotation(isDeg ? rotation : rotation.scale(Mth.RAD_TO_DEG)), false);

        if (setRotation) {
            this.setRotation(isDeg ? rotation.scale(Mth.DEG_TO_RAD) : rotation);
        }
    }

    public void setAcceleration(Vec3 acceleration) {
        this.acceleration = acceleration;
    }

    public void setAccelerationFromDirection(double acceleration, Vec3 direction) {
        this.setAcceleration(direction.normalize().multiply(acceleration, acceleration, acceleration));
    }

    public void setAccelerationFromRotation(double acceleration, Vec2 rotation, boolean isDeg) {
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

    public void setRotation(Vec3 vec3) {
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

    public boolean getShouldFaceCamera() {
        return this.faceCamera;
    }

    public Vec3 getPosition() {
        return new Vec3(positionX, positionY, positionZ);
    }

    public double[] getPositionAsArray() {
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

    public Blend getBlend() {
        return this.blend;
    }

    public String getBlendName() {
        return this.blend.name();
    }

    public boolean isDead() {
        return this.isDead;
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

    public RandomSource getRandomSource() {
        return this.random;
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

    public ITHObjectContainer getContainer() {
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

    public void setShouldSetDeadWhenCollision(boolean shouldSetDeadWhenCollision) {
        this.shouldSetDeadWhenCollision = shouldSetDeadWhenCollision;
    }

    private final Map<String, LuaValue> scriptEventCache = Maps.newHashMap();

    public LuaValue getLuaClass(){
        if (this.luaClass == null || this.luaClass.isnil()) {
            LuaValue luaClass1 = LuaCore.getInstance().getLuaClass(this.getLuaClassKey());
            if (luaClass1 != null && !luaClass1.isnil()) {
                this.luaClass = luaClass1;
            }else {
                return LuaValue.NIL;
            }
        }
        return this.luaClass;
    }

    public void invokeScriptEvent(String eventName, Varargs args) {
        LuaValue luaClass = this.getLuaClass();
        //luaClass = this.ofLuaValue().get("class");

        if(luaClass == null || luaClass.isnil()) {
            return;
        }

        LuaValue event;
        if (scriptEventCache.containsKey(eventName)) {
            event = scriptEventCache.get(eventName);
        } else {
            event = luaClass.get(eventName);
            scriptEventCache.put(eventName, event);
        }

        if (!event.isnil() && event.isfunction()) {
            try {
                event.checkfunction().invoke(args);
            } catch (Exception e) {
                THDanmakuCraftCore.LOGGER.error("Failed invoke script!", e);
                this.remove();
            }
        }
    }

    public void invokeScriptEvent(String eventName, LuaValue... args){
        Varargs varargs = LuaValue.varargsOf(args);
        this.invokeScriptEvent(eventName, varargs);
    }

    public int getIndex() {
        return this.index;
    }

    public void onTick() {
        this.lastPosition = new Vec3(this.positionX, this.positionY, this.positionZ);
        //THDanmakuCraftCore.LOGGER.warn("tickTHObject");
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

        this.invokeScriptEvent("onTick", this.ofLuaValue());

        if (this.collision) {
            this.collisionLogic();
        }

        if (this.navi && !this.isDead) {
            this.setRotation(VectorAngleToRadAngle(this.getMotionDirection()));
        }

        if (this.timer > this.lifetime || (!this.getContainer().getContainerBound().contains(this.getPosition()))) {
            this.setDead();
        }

        if (this.isDead) {
            this.onDead();
        }

        this.timer++;
    }

    public Level level(){
        return this.container.level();
    }

    public void collisionLogic() {
        List<Entity> entitiesInBound = this.container.getEntitiesInBound();
        if (entitiesInBound.isEmpty()) {
            return;
        }

        for (Entity entity : entitiesInBound) {
            if (!this.canHitUser && entity.equals(this.getContainer().getUser())) {
                continue;
            }

            if (this.collisionType == CollisionType.AABB) {
                AABB aabb = this.getBoundingBox();
                if (entity.getBoundingBox().intersects(aabb)) {
                    onHit(new EntityHitResult(entity, this.getPosition()));
                }
            } else if (this.collisionType.collisionEntity(this, entity)) {
                onHit(new EntityHitResult(entity, this.getPosition()));
            }
        }

        if (this.shouldCollingWithBlock) {
            if (this.collisionType == CollisionType.AABB) {
                AABB aabb = this.getBoundingBox();
                BlockHitResult result = this.level().clip(new ClipContext(
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
                            BlockState blockState = this.level().getBlockState(pos);
                            if (!blockState.isAir() && blockState.isCollisionShapeFullBlock(this.level(), pos)) {
                                if (this.collisionType.collisionBlock(this, pos)) {
                                    BlockHitResult result = new BlockHitResult(
                                            new Vec3(box.maxX, box.maxY, box.maxZ),
                                            Direction.getNearest(new Vec3(box.minX, box.minY, box.minZ)),
                                            pos, true);
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
        this.setVelocity(Vec3.ZERO, false);
        this.invokeScriptEvent("onHit", this.ofLuaValue());
        if (this.shouldSetDeadWhenCollision) {
            this.setDead();
        }

        if (result.getType() == HitResult.Type.ENTITY && result instanceof EntityHitResult entityHitResult) {
            this.onHitEntity(entityHitResult);
        }

        if (result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult blockHitResult) {
            this.onHitBlock(blockHitResult);
        }
    }

    public void onHitEntity(EntityHitResult result) {
        if (this.damage <= 0.0f) {
            return;
        }
        Entity entity = result.getEntity();
        entity.hurt(this.level().damageSources().magic(), this.getDamage());
    }

    public void onHitBlock(BlockHitResult result) {
        //this.level.removeBlock(result.getBlockPos(), true);
    }

    public void onDead() {
        this.collision = false;
        //this.setVelocity(Vec3.ZERO, false);
        if (this.deathAnimation) {
            this.deathLastingTime--;
            if (this.deathLastingTime <= 0) {
                this.remove();
            }
            this.color.a -= 255 / 10;
        } else {
            this.remove();
        }

        this.invokeScriptEvent("onDead", this.ofLuaValue());
    }

    public void onRemove() {
        this.invokeScriptEvent("onRemove", this.ofLuaValue());
    }

    public void setBlend(Blend blend) {
        this.blend = blend;
    }

    public void setBlend(String blend) {
        this.blend = Blend.valueOf(blend);
    }

    public boolean hasContainer() {
        return this.container != null && !this.container.getHostEntity().isRemoved();
    }

    public THObjectType<? extends THObject> getType() {
        return this.type;
    }

    public void setLuaClassKey(String className) {
        this.luaClassKey = className;
    }

    public String getLuaClassKey() {
        return this.luaClassKey;
    }

    public void encode(FriendlyByteBuf buffer) {
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
        FriendlyByteBufUtil.writeColor(buffer, this.color);
        buffer.writeInt(this.timer);
        buffer.writeInt(this.lifetime);
        buffer.writeInt(this.deathLastingTime);
        buffer.writeEnum(this.blend);
        buffer.writeBoolean(this.isDead);
        buffer.writeBoolean(this.collision);
        buffer.writeEnum(this.collisionType);
        buffer.writeBoolean(this.shouldSave);
        buffer.writeUtf(this.luaClassKey);
        //this.scriptManager.writeData(buffer);
        this.parameterManager.encode(buffer);
        //this.luaTaskManager.encode(buffer);
        LuaValue params = this.ofLuaValue().get("params");
        if(params.istable()) {
            luaValueStorageHelper.writeLuaTable(buffer, params.checktable());
        }else {
            buffer.writeShort(0);
        }
    }

    public void decode(FriendlyByteBuf buffer) {
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
        this.color = FriendlyByteBufUtil.readColor(buffer);
        this.timer = buffer.readInt();
        this.lifetime = buffer.readInt();
        this.deathLastingTime = buffer.readInt();
        this.blend = buffer.readEnum(Blend.class);
        this.isDead = buffer.readBoolean();
        this.collision = buffer.readBoolean();
        this.collisionType = buffer.readEnum(CollisionType.class);
        this.shouldSave = buffer.readBoolean();
        this.luaClassKey = buffer.readUtf();
        //this.scriptManager.readData(buffer);
        this.parameterManager.decode(buffer);
        //this.luaTaskManager.decode(buffer);
        this.setBoundingBox(this.getPosition(), this.size);
        this.ofLuaValue().set("params", luaValueStorageHelper.readLuaTable(buffer));
    }

    public void save(CompoundTag tag) {
        tag.put("Pos", CompoundTagUtil.newDoubleList(this.positionX, this.positionY, this.positionZ));
        tag.put("PrePos", CompoundTagUtil.newVec3(this.prePosition));
        tag.put("Rotation", CompoundTagUtil.newVector3f(this.getRotation()));
        tag.put("Velocity", CompoundTagUtil.newVec3(this.velocity));
        tag.put("Acceleration", CompoundTagUtil.newVec3(this.acceleration));
        tag.put("Scale", CompoundTagUtil.newVector3f(this.scale));
        tag.put("Size", CompoundTagUtil.newVec3(this.size));
        Color c = this.color;
        tag.put("Color", CompoundTagUtil.newIntList(c.r, c.g, c.b, c.a));
        tag.put("Timers", CompoundTagUtil.newIntList(this.timer, this.lifetime, this.deathLastingTime));
        tag.putInt("Blend", this.blend.ordinal());
        tag.putBoolean("IsDead", this.isDead);
        tag.putBoolean("Collision", this.collision);
        tag.putInt("CollisionType", this.collisionType.ordinal());
        tag.putUUID("UUID", this.uuid);
        tag.putString("LuaClassKey", luaClassKey);
        //this.scriptManager.save(tag);
        tag.put("parameters", this.parameterManager.save(new CompoundTag()));
        tag.put("params", luaValueStorageHelper.saveLuaTable(this.ofLuaValue().get("params")));
        //return tag;
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
        this.collisionType = CollisionType.class.getEnumConstants()[tag.getInt("CollisionType")];
        this.luaClassKey = tag.getString("LuaClassKey");
        //this.scriptManager.load(tag);
        this.parameterManager.load(tag.getCompound("parameters"));
        this.uuid = tag.getUUID("UUID");
        this.ofLuaValue().set("params", luaValueStorageHelper.loadLuaTable(tag.getCompound("params")));
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

    public void setImage(IImage.Image image) {
        this.image = image;
    }

    public IImage.Image getImage() {
        return this.image;
    }

    public static boolean IsValid(THObject object) {
        return object != null;
    }

    public static Color Color(int r, int g, int b, int a) {
        return new Color(r, g, b, a);
    }

    public static Color Color(int r, int g, int b) {
        return Color(r, g, b, 255);
    }

    public enum CollisionType {
        AABB(CollisionType::AABB),
        SPHERE(CollisionType::SPHERE),
        ELLIPSOID(CollisionType::Ellipsoid),
        CUBOID(CollisionType::CUBOID),
        ;

        private final CollisionFactory factory;

        CollisionType(CollisionFactory factory) {
            this.factory = factory;
        }

        public void collisionEntity(THObject object, Entity entity, Runnable whenColling) {
            if (this.factory.collision(object.getPosition(), object.getSize(), object.getRotation(), entity.getBoundingBox())) {
                whenColling.run();
            }
        }

        public boolean collision(THObject object, net.minecraft.world.phys.AABB aabb) {
            return this.factory.collision(object.getPosition(), object.getSize(), object.getRotation(), aabb);
        }

        public boolean collisionEntity(THObject object, Entity entity) {
            return this.factory.collision(object.getPosition(), object.getSize(), object.getRotation(), entity.getBoundingBox());
        }

        public boolean collisionBlock(THObject object, BlockPos pos) {
            return this.factory.collision(object.getPosition(), object.getSize(), object.getRotation(),
                    new AABB(pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
        }

        public static boolean AABB(Vec3 center, Vec3 size, Vector3f rotation, AABB aabb) {
            return CollisionHelper.isCollidingAABB(center, size, aabb);
        }

        public static boolean SPHERE(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb) {
            return CollisionHelper.isCollidingSphereBox(center, scale.x, aabb);
        }

        public static boolean Ellipsoid(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb) {
            return CollisionHelper.isCollidingOrientedEllipsoidBox(center, scale, rotation, aabb);
        }

        public static boolean CUBOID(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb) {
            return CollisionHelper.isCollidingAABB(center, scale, aabb);
        }

        public
        interface CollisionFactory {
            boolean collision(Vec3 center, Vec3 scale, Vector3f rotation, AABB aabb);
        }
    }

    public enum Blend {
        normal("add", "src_alpha", "one_minus_src_alpha", "one", "one_minus_src_alpha"),
        add("add", "src_alpha", "one"),
        sub("subtract", "src_alpha", "one_minus_src_alpha"),
        max("max", "src_alpha", "one_minus_src_alpha"),
        min("min", "src_alpha", "one_minus_src_alpha"),
        mul_add("add", "dst_color", "1-srcalpha", "one", "1-srcalpha"),
        mul_rev("reverse_subtract", "dstcolor", "1-srcalpha", "one", "1-srcalpha"),
        mul_rev2("reverse_subtract", "src_alpha", "one_minus_src_alpha", "zero", "one");

        private final String blendFunc,
                srcColorFactor,
                dstColorFactor,
                srcAlphaFactor,
                dstAlphaFactor;
        private final boolean separateBlend;

        Blend(boolean separateBlend, String blendFunc, String srcColor, String dstColor, String srcAlpha, String dstAlpha) {
            this.separateBlend = separateBlend;
            this.blendFunc = blendFunc;
            this.srcColorFactor = srcColor;
            this.dstColorFactor = dstColor;
            this.srcAlphaFactor = srcAlpha;
            this.dstAlphaFactor = dstAlpha;
        }

        Blend(String blendFunc, String src, String dst) {
            this(false, blendFunc, src, dst, src, dst);
        }

        Blend(String blendFunc, String srcColor, String dstColor, String srcAlpha, String dstAlpha) {
            this(true, blendFunc, srcColor, dstColor, srcAlpha, dstAlpha);
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

        public String[] getALL() {
            return new String[]{
                    blendFunc,
                    srcColorFactor,
                    dstColorFactor,
                    srcAlphaFactor,
                    dstAlphaFactor
            };
        }
    }

    public static THObject checkTHObject(LuaValue luaValue) {
        if (luaValue.get("source").checkuserdata() instanceof THObject object) {
            return object;
        }

        throw new NullPointerException();
    }

    public static boolean isTHObject(LuaValue luaValue){
        LuaValue source = luaValue.get("source");
        if(source.isnil()){
            return false;
        }
        return source.checkuserdata() instanceof THObject;
    }

    public static class api {
        protected static final LibFunction setPosition = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHObject(varargs.arg(1)).setPosition(LuaValueToVec3(varargs.arg(2)));
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction initPosition = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHObject(varargs.arg(1)).initPosition(LuaValueToVec3(varargs.arg(2)));
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction setLifetime = new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
                checkTHObject(luaValue0).setLifetime(luaValue.checkint());
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction setScale = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHObject(varargs.arg(1)).setScale(LuaValueToVec3(varargs.arg(2)));
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction setSize = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHObject(varargs.arg(1)).setSize(LuaValueToVec3(varargs.arg(2)));
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction setVelocity = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                LuaValue velocity = varargs.arg(2);
                boolean shouldSetRotation = varargs.arg(3).checkboolean();
                checkTHObject(varargs.arg(1)).setVelocity(LuaValueToVec3(velocity), shouldSetRotation);
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction setVelocityFromDirection = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                double speed = varargs.arg(2).checkdouble();
                LuaValue direction = varargs.arg(3);
                boolean shouldSetRotation = varargs.arg(4).checkboolean();
                checkTHObject(varargs.arg(1)).setVelocityFromDirection(speed, LuaValueToVec3(direction), shouldSetRotation);
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction setVelocityFromRotation = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                double speed = varargs.arg(2).checkdouble();
                LuaValue rotation = varargs.arg(3);
                boolean isDeg = varargs.arg(4).checkboolean();
                boolean shouldSetRotation = varargs.arg(5).checkboolean();
                checkTHObject(varargs.arg(1)).setVelocityFromRotation(speed, LuaValueToVec2(rotation), isDeg, shouldSetRotation);
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction getParameterManager = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return checkTHObject(luaValue0).getParameterManager().ofLuaValue();
            }
        };

        protected static final LibFunction setAcceleration = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                LuaValue acc = varargs.arg(2);
                checkTHObject(varargs.arg(1)).setAcceleration(LuaValueToVec3(acc));
                return LuaValue.NIL;
            }
        };
        protected static final LibFunction setAccelerationFromDirection = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                double acc = varargs.arg(2).checkdouble();
                LuaValue direction = varargs.arg(3);
                checkTHObject(varargs.arg(1)).setAccelerationFromDirection(acc, LuaValueToVec3(direction));
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction setAccelerationFromRotation = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                double acc = varargs.arg(2).checkdouble();
                LuaValue rotation = varargs.arg(3);
                boolean isDeg = varargs.arg(4).checkboolean();
                checkTHObject(varargs.arg(1)).setAccelerationFromRotation(acc, LuaValueToVec2(rotation), isDeg);
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction setRotation = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHObject(varargs.arg(1)).setRotation(
                        varargs.arg(2).tofloat(),
                        varargs.arg(3).tofloat(),
                        varargs.arg(4).tofloat()
                );
                return LuaValue.NIL;
            }
        };

        protected static final LibFunction setRotationByDirectionalVector = new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
                checkTHObject(luaValue0).setRotationByDirectionalVector(LuaValueToVec3(luaValue));
                return LuaValue.NIL;
            }
        };
        protected static final LibFunction setColor = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHObject(varargs.arg(1)).setColor(
                        varargs.arg(2).checkint(),
                        varargs.arg(3).checkint(),
                        varargs.arg(4).checkint(),
                        varargs.arg(5).checkint()
                );
                return LuaValue.NIL;
            }
        };
        protected static final LibFunction setBlend = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHObject(varargs.arg(1)).setBlend(varargs.arg(2).checkjstring());
                return LuaValue.NIL;
            }
        };
        protected static final LibFunction setCollisionType = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHObject(varargs.arg(1)).setCollisionType(varargs.arg(2).checkjstring());
                return LuaValue.NIL;
            }
        };
        protected static final LibFunction getTimer = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return LuaValue.valueOf(checkTHObject(luaValue0).getTimer());
            }
        };
        protected static final LibFunction getContainer = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return checkTHObject(luaValue0).getContainer().ofLuaValue();
            }
        };
        protected static final LibFunction getPosition = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                THObject object = checkTHObject(luaValue0);
                return Vec3ToLuaValue(object.positionX, object.positionY, object.positionZ);
            }
        };
        protected static final LibFunction getPrePosition = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return Vec3ToLuaValue(checkTHObject(luaValue0).getPrePosition());
            }
        };
        protected static final LibFunction getSpeed = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return LuaValue.valueOf(checkTHObject(luaValue0).getSpeed());
            }
        };
        protected static final LibFunction getVelocity = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return Vec3ToLuaValue(checkTHObject(luaValue0).getVelocity());
            }
        };
        protected static final LibFunction getMotionDirection = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return Vec3ToLuaValue(checkTHObject(luaValue0).getMotionDirection());
            }
        };
        protected static final LibFunction getRotation = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                THObject object = checkTHObject(luaValue0);
                return Vector3fToLuaValue(object.xRot, object.yRot, object.zRot);
                //return Vector3fToLuaValue(checkTHObject(luaValue0).getRotation());
            }
        };
        protected static final LibFunction getXRot = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return LuaValue.valueOf(checkTHObject(luaValue0).getXRot());
            }
        };
        protected static final LibFunction getYRot = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return LuaValue.valueOf(checkTHObject(luaValue0).getYRot());
            }
        };
        protected static final LibFunction getZRot = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return LuaValue.valueOf(checkTHObject(luaValue0).getZRot());
            }
        };
        protected static final LibFunction getAcceleration = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return Vec3ToLuaValue(checkTHObject(luaValue0).getAcceleration());
            }
        };
        protected static final LibFunction getScale = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return Vector3fToLuaValue(checkTHObject(luaValue0).getScale());
            }
        };
        protected static final LibFunction getSize = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return Vec3ToLuaValue(checkTHObject(luaValue0).getSize());
            }
        };
        protected static final LibFunction move = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                LuaValue arg1 = varargs.arg(2);
                THObject object = checkTHObject(varargs.arg(1));
                if (arg1.isuserdata() || arg1.istable()) {
                    object.move(LuaValueToVec3(arg1));
                } else {
                    object.move(new Vec3(
                            arg1.checkdouble(),
                            varargs.arg(3).checkdouble(),
                            varargs.arg(4).checkdouble()
                    ));
                }
                return LuaValue.NIL;
            }
        };
        protected static final LibFunction setDead = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                checkTHObject(luaValue0).setDead();
                return LuaValue.NIL;
            }
        };
        protected static final LibFunction setShouldSetDeadWhenCollision = new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
                checkTHObject(luaValue0).setShouldSetDeadWhenCollision(luaValue.checkboolean());
                return LuaValue.NIL;
            }
        };
        protected static final LibFunction remove = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                checkTHObject(luaValue0).remove();
                return LuaValue.NIL;
            }
        };
        protected static final LibFunction spawn = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                checkTHObject(luaValue0).spawn();
                return LuaValue.NIL;
            }
        };
        protected static final LibFunction getIsSpawned = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0) {
                return LuaValue.valueOf(checkTHObject(luaValue0).isSpawned());
            }
        };
        protected static final LibFunction setNavi = new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
                checkTHObject(luaValue0).setNavi(luaValue.checkboolean());
                return LuaValue.NIL;
            }
        };
    }

    public void initLuaValue() {
        this.luaValueForm = this.ofLuaClass();
        this.addTasks();
    }

    @Override
    public LuaValue ofLuaClass() {
        LuaValue library = LuaValue.tableOf();
        //functions
        library.setmetatable(this.getMeta());
        //fields
        library.set("isTHObject", LuaValue.TRUE);
        library.set("class", this.getLuaClass());
        library.set("source", LuaValue.userdataOf(this));
        library.set("type", this.getType().getKey().toString());
        library.set("uuid", this.getUUIDasString());
        library.set("container", this.getContainer().ofLuaValue());
        library.set("parameterManager", this.getParameterManager().ofLuaValue());
        //library.set("taskManager", this.luaTaskManager.ofLuaValue());
        library.set("params", LuaValue.tableOf());
        return library;
    }

    /*public static final LuaValue meta = LuaValue.tableOf();
    static {
        meta.set("__index", functions());
    }*/

    public static final LuaValue meta = THObjectContainer.setMeta(functions());

    public LuaValue getMeta(){
        return meta;
    }

    public static LuaValue functions(){
        LuaValue library = LuaValue.tableOf();
        library.set("initPosition", initPosition);
        library.set("setPosition", setPosition);
        library.set("setLifetime", setLifetime);
        library.set("setScale", setScale);
        library.set("setSize", setSize);
        library.set("setVelocity", setVelocity);
        library.set("setVelocityFromDirection", setVelocityFromDirection);
        library.set("setVelocityFromRotation", setVelocityFromRotation);
        library.set("getParameterManager", getParameterManager);
        library.set("setAcceleration", setAcceleration);
        library.set("setAccelerationFromDirection", setAccelerationFromDirection);
        library.set("setAccelerationFromRotation", setAccelerationFromRotation);
        library.set("setRotation", setRotation);
        library.set("setRotationByDirectionalVector", setRotationByDirectionalVector);
        library.set("setColor", setColor);
        library.set("setBlend", setBlend);
        library.set("setCollisionType", setCollisionType);
        library.set("getTimer", getTimer);
        library.set("getContainer", getContainer);
        library.set("getPosition", getPosition);
        library.set("getPrePosition", getPrePosition);
        library.set("getSpeed", getSpeed);
        library.set("getVelocity", getVelocity);
        library.set("getMotionDirection", getMotionDirection);
        library.set("getRotation", getRotation);
        library.set("getXRot", getXRot);
        library.set("getYRot", getYRot);
        library.set("getZRot", getZRot);
        library.set("getAcceleration", getAcceleration);
        library.set("getScale", getScale);
        library.set("getSize", getSize);
        library.set("move", move);
        library.set("setDead", setDead);
        library.set("setShouldSetDeadWhenCollision", setShouldSetDeadWhenCollision);
        library.set("remove", remove);
        library.set("spawn", spawn);
        library.set("isSpawned", getIsSpawned);
        library.set("setNavi", setNavi);
        return library;
    }

    public void addTasks(){
        this.invokeScriptEvent("onAddTasks", this.ofLuaValue());
    }

    @Override
    public LuaValue ofLuaValue() {
        if (this.luaValueForm == null) {
            this.initLuaValue();
        }
        return luaValueForm;
    }

}