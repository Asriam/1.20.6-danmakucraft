package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.script.IScript;
import com.adrian.thDanmakuCraft.lua.LuaCore;
import com.adrian.thDanmakuCraft.lua.LuaManager;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.*;

import javax.annotation.Nullable;
import java.util.*;

import static com.adrian.thDanmakuCraft.world.LuaValueHelper.*;

public class THObjectContainer implements ITHObjectContainer, IScript, ILuaValue {
    public static final List<THObjectContainer> allContainers = new ArrayList<>();

    private Entity hostEntity;
    private int maxObjectAmount = 2000;
    protected final TargetUserManager targetUserManager;
    protected final THObjectManager objectManager;
    //protected final LuaManager scriptManager;
    //protected final THTasker.THTaskerManager taskerManager;
    protected final AdditionalParameterManager parameterManager;
    protected final RandomSource random = RandomSource.create();
    private int timer = 0;
    public AABB aabb = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    public AABB bound = new AABB(-60.0D,-60.0D,-60.0D,60.0D,60.0D,60.0D);
    public boolean autoRemove = true;
    public int autoRemoveLife = 60;
    private List<Entity> entitiesInBound;
    private LuaValue luaValueForm;
    private LuaValue luaClass;
    private String luaClassKey = "";
    private final LuaValueStorageHelper luaValueStorageHelper;

    public THObjectContainer(Entity hostEntity) {
        allContainers.add(this);
        this.hostEntity = hostEntity;
        this.parameterManager  = new AdditionalParameterManager(this);
        this.targetUserManager = new TargetUserManager(this);
        this.objectManager     = new THObjectManager(this);
        //this.taskerManager     = new THTasker.THTaskerManager(this);
        this.luaValueStorageHelper = new LuaValueStorageHelper(this);
        //this.scriptManager     = new LuaManager();
        this.entitiesInBound   = new ArrayList<>();
        this.setMaxObjectAmount(10000);
        //this.luaValueForm = this.ofLuaClass();
    }

    public THObjectContainer(Entity hostEntity, String luaClassKey){
        this(hostEntity);
        this.luaClassKey = luaClassKey;
        this.scriptEvent("onInit",this.ofLuaValue());
    }

    public void onAddToWorld(){
        //this.scriptInit();
    }

    public int getMaxObjectAmount() {
        return this.maxObjectAmount;
    }

    public void setMaxObjectAmount(int maxObjectAmount) {
        this.maxObjectAmount = maxObjectAmount;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void task(){
        boolean flag = false;

        if(this.objectManager.isEmpty() && !flag) {
            for (int j = 0; j< THBullet.DefaultBulletStyle.class.getEnumConstants().length; j++) {
                for (int i = 0; i < 16; i++) {
                    THObject a = (THObject) new THBullet(this, THBullet.DefaultBulletStyle.getStyleByIndex(j), THBullet.BULLET_INDEX_COLOR.getColorByIndex(i + 1))
                            .initPosition(this.position().add(i*2, 0.0d, j*2))
                            .shoot(
                                    0.0f,
                                    Vec3.ZERO
                            );
                    a.setLifetime(100);
                    a.setBlend(THObject.Blend.add);
                    //a.setBlend(THObject.Blend.class.getEnumConstants()[(int) ((THObject.Blend.class.getEnumConstants().length)*random.nextFloat())]);
                    //a.blend = THObject.BlendMode.add;
                }
            }
        }
    }

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

    private final Map<String,LuaValue> scriptEventCache = Maps.newHashMap();
    public void scriptEvent(String eventName,LuaValue... args){
        if(this.luaClass == null || this.luaClass.isnil()) {
            LuaValue luaClass1 = LuaCore.getInstance().getLuaClass(this.getLuaClassKey());
            if (luaClass1 == null) {
                return;
            }
            this.luaClass = luaClass1;
        }
        LuaValue event;
        if(scriptEventCache.containsKey(eventName)){
            event = scriptEventCache.get(eventName);
        }else {
            event = this.luaClass.get(eventName);
            scriptEventCache.put(eventName,event);
        }
        //LuaValue event = this.luaClass.get(eventName);
        if(!event.isnil() && event.isfunction()){
            try {
                event.checkfunction().invoke(args);
            }catch (Exception e){
                THDanmakuCraftCore.LOGGER.error("Failed invoke script!", e);
                this.discard();
            }
        }
    }

    public void tick() {
        if(this.hostEntity == null){
            THDanmakuCraftCore.LOGGER.warn("Host Entity is null!");
            return;
        }

        this.targetUserManager.loadUserAndTarget(level());

        if (timer == 0){
            this.scriptEvent("onInit",this.ofLuaValue());
        }
        this.setBound(this.position(),this.bound);
        //this.task();
        this.entitiesInBound = this.level().getEntities(this.hostEntity,this.getContainerBound()).stream().filter((entity -> !(entity.equals(this.hostEntity)) && !(entity instanceof EntityTHObjectContainer))).toList();

        this.scriptEvent("onTick",this.ofLuaValue());

        this.objectManager.tickTHObjects();

        if(this.autoRemove) {
            if (this.objectManager.isEmpty() && --this.autoRemoveLife < 0) {
                this.getHostEntity().remove(Entity.RemovalReason.DISCARDED);
            }
        }

        this.timer++;
    }

    public void clearObjects(){
        this.getObjectManager().clearStorage();
    }

    public Vec3 position() {
        return this.hostEntity.position();
    }

    public Level level(){
        return this.hostEntity.level();
    }

    public final void setBound(AABB boundingBox) {
        this.aabb = boundingBox;
    }

    public final void setBound(Vec3 pos, Vec3 size) {
        setBound(new AABB(
                pos.x - size.x / 2, pos.y - size.y / 2, pos.z - size.z / 2,
                pos.x + size.x / 2, pos.y + size.y / 2, pos.z + size.z / 2
        ));
    }

    public final void setBound(Vec3 pos, AABB aabb) {
        setBound(new AABB(
                pos.x + aabb.minX, pos.y + aabb.minY, pos.z + aabb.minZ,
                pos.x + aabb.maxX, pos.y + aabb.maxY, pos.z + aabb.maxZ
        ));
    }

    public final AABB getContainerBound(){
        return this.aabb;
    }

    public THObjectManager getObjectManager(){
        return this.objectManager;
    }

    public void setUser(Entity entity){
        this.targetUserManager.setUser(entity);
    }

    @Nullable
    public Entity getUser(){
        //THDanmakuCraftCore.LOGGER.info(""+this.targetUserManager.safeGetUser());
        return this.targetUserManager.unsafeGetUser();
    }

    public void setTarget(Entity target) {
        this.targetUserManager.setTarget(target);
    }

    @Nullable
    public Entity getTarget() {
        return this.targetUserManager.unsafeGetTarget();
    }

    public List<Entity> getEntitiesInBound(){
        return this.entitiesInBound;//this.level().getEntities(this,this.getAabb()).stream().filter((entity -> !(entity instanceof EntityTHObjectContainer))).toList();
    }

    public Entity getHostEntity(){
        return this.hostEntity;
    }

    private void setHostEntity(Entity entity){
        this.hostEntity = entity;
    }

    public void initHostEntity(Entity entity){
        if(this.hostEntity == null){
            this.setHostEntity(entity);
        }
    }

    public THObject createTHObject(Vec3 pos) {
        return new THObject(this,pos);
    }

    public THBullet createTHBullet(Vec3 pos, String style, int color) {
        return new THBullet(this, THBullet.DefaultBulletStyle.valueOf(style), THBullet.BULLET_INDEX_COLOR.getColorByIndex(color));
    }

    public THCurvedLaser createTHCurvedLaser(Vec3 pos, int color, int length, float width) {
        return new THCurvedLaser(this, THBullet.BULLET_INDEX_COLOR.getColorByIndex(color),length,width);
    }

    public void spawnTHObject(THObject object){
        object.isSpawned = true;
        if (!this.getObjectManager().contains(object)) {
            object.setContainer(this);
            this.getObjectManager().addTHObject(object);
        }
    }

    public void discard(){
        this.hostEntity.remove(Entity.RemovalReason.DISCARDED);
    }

    public <T extends THObject> T getObjectFromUUID(UUID uuid) {
        for(THObject object:this.getObjectManager().getTHObjects()){
            if (object.getUUID().equals(uuid)){
                return (T) object;
            }
        }
        return null;
    }

    public <T extends THObject> T getObjectFromUUID(String uuid) {
        return this.getObjectFromUUID(UUID.fromString(uuid));
    }

    @Deprecated
    public void setLuaClassKey(String className) {
        this.luaClassKey = className;
    }

    public void setLuaClass(String className){
        this.luaClassKey = className;
        LuaValue luaClass1 = LuaCore.getInstance().getLuaClass(className);
        this.luaClass = luaClass1 == null ? LuaValue.NIL : luaClass1;
    }

    public String getLuaClassKey() {
        return this.luaClassKey;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.maxObjectAmount);
        buffer.writeInt(this.timer);
        buffer.writeUtf(this.luaClassKey);
        this.targetUserManager.encode(buffer);
        this.objectManager.encode(buffer);
        //this.scriptManager.encode(buffer);
        this.parameterManager.encode(buffer);
        //this.taskerManager.writeData(buffer);
        LuaValue params = this.ofLuaValue().get("params");
        if(params.istable()) {
            luaValueStorageHelper.writeLuaTable(buffer, params.checktable());
        }else {
            buffer.writeShort(0);
        }
   }

    public void decode(FriendlyByteBuf buffer) {
        this.maxObjectAmount = buffer.readInt();
        this.timer = buffer.readInt();
        this.luaClassKey = buffer.readUtf();
        this.targetUserManager.decode(buffer);
        this.objectManager.decode(buffer);
        //this.scriptManager.decode(buffer);
        this.parameterManager.decode(buffer);
        //this.taskerManager.readData(additionalData);
        this.setBound(this.position(),this.bound);
        this.ofLuaValue().set("params", luaValueStorageHelper.readLuaTable(buffer));
    }

    public void save(CompoundTag tag) {
        tag.putInt("Timer",this.timer);
        tag.putInt("MaxObjectAmount",this.maxObjectAmount);
        tag.putString("LuaClassKey", luaClassKey);
        tag.put("object_storage", this.objectManager.save(new CompoundTag()));
        //tag.put("script",this.scriptManager.save(new CompoundTag()));
        tag.put("user_target", this.targetUserManager.save(new CompoundTag()));
        tag.put("parameters", this.parameterManager.save(new CompoundTag()));
        tag.put("params", luaValueStorageHelper.saveLuaTable(this.ofLuaValue().get("params")));
    }

    public void load(CompoundTag tag) {
        this.timer = tag.getInt("Timer");
        this.maxObjectAmount = tag.getInt("MaxObjectAmount");
        this.luaClassKey = tag.getString("LuaClassKey");
        this.objectManager.load(tag.getCompound("object_storage"));
        //this.scriptManager.load(tag.getCompound("script"));
        this.targetUserManager.load(tag.getCompound("user_target"));
        this.parameterManager.load(tag.getCompound("parameters"));
        this.ofLuaValue().set("params", luaValueStorageHelper.loadLuaTable(tag.getCompound("params")));
    }

    public void injectScript(String script) {
        //this.scriptManager.setScript(script);
    }

    @Override
    public LuaManager getScriptManager() {
        //return this.scriptManager;
        return null;
    }

    public RandomSource getRandomSource() {
        return this.random;
    }

    public Vec3 getPosition(){
        return this.position();
    }

    public AdditionalParameterManager getParameterManager() {
        return this.parameterManager;
    }

    public static THObjectContainer checkTHObjectContainer(LuaValue luaValue) {
        if (luaValue.get("source").checkuserdata() instanceof THObjectContainer container){
            return container;
        }
        throw new NullPointerException();
        //return null;
    }

    private static String getLuaClassName(LuaValue luaValue){
        if(luaValue.isstring()){
            return luaValue.checkjstring();
        }else if(luaValue.istable()){
            return luaValue.get("className").checkjstring();
        }
        return "";
    }

    public static void initTHObject(THObject object, String luaClassKey, Varargs args) {
        object.setLuaClassKey(luaClassKey);
        object.initLuaValue();
        LuaValue luaObject = object.ofLuaValue();
        if(args == LuaValue.NIL){
            object.invokeScriptEvent("onInit",luaObject);
        }else {
            object.invokeScriptEvent("onInit", LuaValue.varargsOf(luaObject, args));
        }
        object.spawn();
    }

    private static final LibFunction getMaxObjectAmount = new OneArgFunction(){

        @Override
        public LuaValue call(LuaValue luaValue) {
            return LuaValue.valueOf(checkTHObjectContainer(luaValue).getMaxObjectAmount());
        }
    };

    private static final LibFunction getPosition = new OneArgFunction(){
        @Override
        public LuaValue call(LuaValue luaValue) {
            return Vec3ToLuaValue(checkTHObjectContainer(luaValue).getPosition());
        }
    };

    private static final LibFunction getTimer = new OneArgFunction(){
        @Override
        public LuaValue call(LuaValue luaValue) {
            return LuaValue.valueOf(checkTHObjectContainer(luaValue).getTimer());
        }
    };

    private static final LibFunction setTimer = new TwoArgFunction(){
        @Override
        public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
            checkTHObjectContainer(luaValue).setTimer(luaValue1.checkint());
            return LuaValue.NIL;
        }
    };

    private static final LibFunction clearObjects = new OneArgFunction(){
        @Override
        public LuaValue call(LuaValue luaValue) {
            checkTHObjectContainer(luaValue).clearObjects();
            return LuaValue.NIL;
        }
    };

    private static final LibFunction createTHObject = new VarArgFunction(){
        @Override
        public Varargs invoke(Varargs varargs) {
            String luaClassKey = getLuaClassName(varargs.arg(2));
            THObject object = checkTHObjectContainer(varargs.arg(1)).createTHObject(LuaValueToVec3(varargs.arg(4)));
            initTHObject(object, luaClassKey, varargs.arg(3).checktable().unpack());
            return object.ofLuaValue();
        }
    };

    private static final LibFunction createTHBullet = new VarArgFunction(){
        @Override
        public Varargs invoke(Varargs varargs) {
            String luaClassKey = getLuaClassName(varargs.arg(2));
            THBullet bullet = checkTHObjectContainer(varargs.arg(1)).createTHBullet(
                    LuaValueToVec3(varargs.arg(4)),
                    varargs.arg(5).checkjstring(),
                    varargs.arg(6).checkint());
            initTHObject(bullet, luaClassKey, varargs.arg(3).checktable().unpack());
            return bullet.ofLuaValue();
        }
    };

    private static final LibFunction createTHCurvedLaser = new VarArgFunction(){
        @Override
        public Varargs invoke(Varargs varargs) {
            String luaClassKey = getLuaClassName(varargs.arg(2));
            Vec3 pos = LuaValueToVec3(varargs.arg(4));
            int colorIndex = varargs.arg(5).checkint();
            int length = varargs.arg(6).checkint();
            float width = varargs.arg(7).tofloat();
            THCurvedLaser laser = checkTHObjectContainer(varargs.arg(1)).createTHCurvedLaser(pos,colorIndex,length,width);
            initTHObject(laser, luaClassKey, varargs.arg(3).checktable().unpack());
            return laser.ofLuaValue();
        }
    };

    private static final LibFunction getUser = new OneArgFunction(){
        @Override
        public LuaValue call(LuaValue luaValue) {
            return EntityToLuaValue(checkTHObjectContainer(luaValue).getUser());
        }
    };

    private static final LibFunction getTarget = new OneArgFunction(){
        @Override
        public LuaValue call(LuaValue luaValue) {
            return EntityToLuaValue(checkTHObjectContainer(luaValue).getTarget());
        }
    };

    private static final LibFunction getParameterManager = new OneArgFunction(){
        @Override
        public LuaValue call(LuaValue luaValue) {
            THObjectContainer container = checkTHObjectContainer(luaValue);
            return container.getParameterManager().ofLuaValue();
        }
    };

    private static final LibFunction discard = new OneArgFunction(){
        @Override
        public LuaValue call(LuaValue luaValue) {
            checkTHObjectContainer(luaValue).discard();
            return LuaValue.NIL;
        }
    };

    public static THObjectType<?> checkObjectType(LuaValue luaValue){
        if (luaValue.isuserdata()){
            return (THObjectType<?>) luaValue.checkuserdata();
        }

        if (luaValue.isstring()){
            return THObjectType.getValue(new ResourceLocation(luaValue.checkjstring()));
        }

        return null;
    }

    private static final LibFunction newTHObject = new VarArgFunction() {
        @Override
        public Varargs invoke(Varargs varargs) {
            LuaValue luaClass = varargs.arg(2);
            String luaClassKey = getLuaClassName(luaClass);
            THObjectType<?> thobject_type = checkObjectType(luaClass.get("thobject_type"));
            if(thobject_type == null){
                thobject_type = THObjectInit.TH_OBJECT.get();
            }
            THObject object = thobject_type.create(checkTHObjectContainer(varargs.arg(1)));
            initTHObject(object, luaClassKey, varargs.arg(3).checktable().unpack());
            return object.ofLuaValue();
        }
    };

    public void initLuaValue() {
        this.luaValueForm = this.ofLuaClass();
    }

    /*
    public static final LuaValue meta = LuaValue.tableOf();
    static {
        meta.set("__index", functions());
    }*/
    public static LuaValue setMeta(LuaValue luaValue){
        LuaValue meta = LuaValue.tableOf();
        meta.set("__index", luaValue);
        return meta;
    }

    public static final LuaValue meta = setMeta(functions());

    @Override
    public LuaValue ofLuaClass(){
        LuaValue library = LuaValue.tableOf();
        library.setmetatable(this.getMeta());
        //fields
        library.set( "class", this.getLuaClass());
        library.set( "type", "thobject_container");
        library.set( "source", LuaValue.userdataOf(this));
        library.set( "parameterManager", this.getParameterManager().ofLuaValue());
        library.set( "params", LuaValue.tableOf());
        return library;
    }

    public LuaValue getMeta(){
        return meta;
    }

    private static LuaValue functions(){
        LuaValue library = LuaValue.tableOf();
        library.set( "getMaxObjectAmount", getMaxObjectAmount);
        library.set( "getPosition", getPosition);
        library.set( "setTimer", setTimer);
        library.set( "getTimer", getTimer);
        library.set( "getUser", getUser);
        library.set( "getTarget", getTarget);
        library.set( "clearObjects", clearObjects);
        library.set( "createTHObject", createTHObject);
        library.set( "createTHBullet", createTHBullet);
        library.set( "createTHCurvedLaser", createTHCurvedLaser);
        library.set( "getParameterManager", getParameterManager);
        library.set( "discard", discard);
        library.set( "newTHObject", newTHObject);
        return library;
    }

    @Override
    public LuaValue ofLuaValue() {
        if (this.luaValueForm == null) {
            this.initLuaValue();
        }
        return luaValueForm;
    }
}
