package com.adrian.thDanmakuCraft.world.danmaku;

import com.adrian.thDanmakuCraft.THDanmakuCraftCore;
import com.adrian.thDanmakuCraft.api.script.IScriptTHObjectContainerAPI;
import com.adrian.thDanmakuCraft.script.IScript;
import com.adrian.thDanmakuCraft.script.ScriptManager;
import com.adrian.thDanmakuCraft.script.lua.LuaCore;
import com.adrian.thDanmakuCraft.script.lua.LuaManager;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import com.adrian.thDanmakuCraft.world.THTasker;
import com.adrian.thDanmakuCraft.world.TargetUserManager;
import com.adrian.thDanmakuCraft.world.danmaku.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.laser.THCurvedLaser;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.*;

import javax.annotation.Nullable;
import java.security.Provider;
import java.util.*;
import java.util.function.Supplier;

import static com.adrian.thDanmakuCraft.world.LuaValueHelper.*;

public class THObjectContainer implements IScript, IScriptTHObjectContainerAPI, ILuaValue {
    public static final List<THObjectContainer> allContainers = new ArrayList<>();

    private final Entity hostEntity;
    private int maxObjectAmount = 2000;
    protected final TargetUserManager targetUserManager;
    protected final THObjectManager objectManager;
    protected final LuaManager scriptManager;
    protected final THTasker.THTaskerManager taskerManager;
    protected final AdditionalParameterManager parameterManager;
    protected final RandomSource random = RandomSource.create();
    private int timer = 0;
    public AABB aabb = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    public AABB bound = new AABB(-60.0D,-60.0D,-60.0D,60.0D,60.0D,60.0D);
    //public boolean bindingToUserPosition = false;
    public boolean autoRemove = true;
    public int autoRemoveLife = 60;
    private List<Entity> entitiesInBound;
    private final LuaValue luaValueForm;
    //private final static Globals globals = LuaCore.getGlobals();
    private LuaValue luaClass;
    private String luaClassKey = "";

    public THObjectContainer(Entity hostEntity) {
        allContainers.add(this);
        this.hostEntity = hostEntity;
        this.parameterManager  = new AdditionalParameterManager(this);
        this.targetUserManager = new TargetUserManager(this);
        this.objectManager     = new THObjectManager(this);
        this.taskerManager     = new THTasker.THTaskerManager(this);
        this.scriptManager     = new LuaManager();
        this.entitiesInBound   = new ArrayList<>();
        this.setMaxObjectAmount(2000);
        this.luaValueForm = this.ofLuaValue();
    }

    public THObjectContainer(Entity hostEntity, String luaClassKey){
        this(hostEntity);
        this.luaClassKey = luaClassKey;
    }
    /*
    public THObjectContainer(Entity hostEntity, String script){
        this(hostEntity);
        this.injectScript(script);
    }*/

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
                    THObject a = (THObject) new THBullet(this, THBullet.DefaultBulletStyle.getStyleByIndex(j),THBullet.BULLET_COLOR.getColorByIndex(i + 1))
                            .initPosition(this.position().add(i*2, 0.0d, j*1))
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

        if(flag) {
            Vec3 pos = this.position();
            Vec3 rotation = Vec3.directionFromRotation(0.0f,0.0f);
            Vec2 rotate = new Vec2(Mth.DEG_TO_RAD*((float) Math.pow(this.timer*0.1f,2)+360.0f/5),-Mth.DEG_TO_RAD*((float) Math.pow(this.timer*0.08f,2)+360.0f/5));

            Vec3 angle = rotation.xRot(Mth.DEG_TO_RAD*90.0f).normalize().xRot(rotate.x).yRot(rotate.y);
            THBullet.DefaultBulletStyle style = THBullet.DefaultBulletStyle.grain_a;
            THObject danmaku = new THBullet(this,style, THBullet.BULLET_COLOR.COLOR_PURPLE).initPosition(pos).shoot(
                    0.2f,
                    angle
            );

            danmaku.setAccelerationFromDirection(0.02f,angle);
            danmaku.setLifetime(120);

            //danmaku.getScriptManager().enableScript();

            int way = 8;
            for(int i=1;i<=3;i++){
                Vec3 angle2 = rotation.xRot(Mth.DEG_TO_RAD*90.0f-Mth.DEG_TO_RAD*60.0f*i).yRot(Mth.DEG_TO_RAD*(180.0f/way)*i);
                for(int j=0;j<way;j++) {
                    Vec3 angle3 = angle2.yRot(-Mth.DEG_TO_RAD * (360.0f/way)*j).normalize().xRot(rotate.x).yRot(rotate.y);
                    THObject danmaku2 = (THObject) new THBullet(this,style,
                            THBullet.BULLET_COLOR.COLOR_PURPLE).initPosition(pos).shoot(
                            0.2f,
                            angle3
                    );
                    danmaku2.setAccelerationFromDirection(0.02f, angle3);
                    danmaku2.setLifetime(120);
                    //danmaku2.setBlend(THObject.Blend.class.getEnumConstants()[(int) ((THObject.Blend.class.getEnumConstants().length)*random.nextFloat())]);
                }
            }

            Vec3 angle3 = rotation.xRot(Mth.DEG_TO_RAD*90.0f- Mth.DEG_TO_RAD * 180.0f).normalize().xRot(rotate.x).yRot(rotate.y);
            THBullet danmaku3 = (THBullet) new THBullet(this,style,
                    THBullet.BULLET_COLOR.COLOR_PURPLE).initPosition(pos).shoot(
                    0.2f,
                    angle3
            );
            danmaku3.setAccelerationFromDirection(0.02f, angle3);
            danmaku3.setLifetime(120);
        }
    }

    private final Map<String,LuaValue> scriptEventCache = Maps.newHashMap();
    public void scriptEvent(String eventName,LuaValue... args){
        if(this.luaClass == null || this.luaClass.isnil()){
            return;
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
        this.targetUserManager.loadUserAndTarget(level());
        if(luaClass == null || luaClass.isnil()) {
            this.luaClass = LuaCore.getInstance().getLuaClass(this.getLuaClassKey());
        }

        boolean flag = this.luaClass != null;
        if (timer == 0){
            this.scriptEvent("onInit",this.getLuaValue());
        }
        this.setBound(this.position(),this.bound);
        this.task();
        this.entitiesInBound = this.level().getEntities(this.hostEntity,this.getAabb()).stream().filter((entity -> !(entity.equals(this.hostEntity)) && !(entity instanceof EntityTHObjectContainer))).toList();

        if(flag) {
            this.scriptEvent("onTick",this.getLuaValue());
        }

        this.objectManager.tickTHObjects();

        if(this.autoRemove) {
            if (this.objectManager.isEmpty() && --this.autoRemoveLife < 0) {
                this.getHostEntity().remove(Entity.RemovalReason.DISCARDED);
            }
        }

        //this.scriptTick();

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

    public final AABB getAabb(){
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

    public THObject createTHObject(Vec3 pos) {
        return new THObject(this,pos);
    }

    public THBullet createTHBullet(Vec3 pos, String style, int color) {
        return new THBullet(this, THBullet.DefaultBulletStyle.valueOf(style),THBullet.BULLET_COLOR.getColorByIndex(color));
    }

    public THCurvedLaser createTHCurvedLaser(Vec3 pos, int color, int length, float width) {
        return new THCurvedLaser(this,THBullet.BULLET_COLOR.getColorByIndex(color),length,width);
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

    public void setLuaClassKey(String className) {
        this.luaClassKey = className;
    }

    public String getLuaClassKey() {
        return this.luaClassKey;
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.maxObjectAmount);
        buffer.writeInt(this.timer);
        //buffer.writeBoolean(this.bindingToUserPosition);
        buffer.writeUtf(this.luaClassKey);
        this.targetUserManager.writeData(buffer);
        this.objectManager.writeData(buffer);
        this.scriptManager.writeData(buffer);
        this.parameterManager.writeData(buffer);
        //this.taskerManager.writeData(buffer);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.maxObjectAmount = additionalData.readInt();
        this.timer = additionalData.readInt();
        //this.bindingToUserPosition = additionalData.readBoolean();
        this.luaClassKey = additionalData.readUtf();
        this.targetUserManager.readData(additionalData);
        this.objectManager.readData(additionalData);
        this.scriptManager.readData(additionalData);
        this.parameterManager.readData(additionalData);
        //this.taskerManager.readData(additionalData);
        this.setBound(this.position(),this.bound);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Timer",this.timer);
        tag.putInt("MaxObjectAmount",this.maxObjectAmount);
        //compoundTag.putBoolean("PositionBinding",this.bindingToUserPosition);
        tag.putString("LuaClassKey", luaClassKey);
        tag.put("object_storage", this.objectManager.save(new CompoundTag()));
        tag.put("script",this.scriptManager.save(new CompoundTag()));
        tag.put("user_target", this.targetUserManager.save(new CompoundTag()));
        tag.put("parameters", this.parameterManager.save(new CompoundTag()));
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        this.timer = tag.getInt("Timer");
        this.maxObjectAmount = tag.getInt("MaxObjectAmount");
        //this.bindingToUserPosition = compoundTag.getBoolean("PositionBinding");
        this.luaClassKey = tag.getString("LuaClassKey");
        this.objectManager.load(tag.getCompound("object_storage"));
        this.scriptManager.load(tag.getCompound("script"));
        this.targetUserManager.load(tag.getCompound("user_target"));
        this.parameterManager.load(tag.getCompound("parameters"));
    }

    public void injectScript(String script) {
        this.scriptManager.setScript(script);
    }

    @Override
    public ScriptManager getScriptManager() {
        return this.scriptManager;
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
        object.initLuaValue();
        object.setLuaClassKey(luaClassKey);
        //object.scriptEvent("onInit",object.getLuaValue());
        LuaValue luaObject = object.getLuaValue();
        if(args == LuaValue.NIL){
            object.scriptEvent("onInit",luaObject);
            return;
        }
        object.scriptEvent("onInit",LuaValue.varargsOf(luaObject,args));
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
            return object.getLuaValue();
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
            return bullet.getLuaValue();
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
            return laser.getLuaValue();
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
            return container.getParameterManager().getLuaValue();
        }
    };

    private static final LibFunction discard = new OneArgFunction(){
        @Override
        public LuaValue call(LuaValue luaValue) {
            checkTHObjectContainer(luaValue).discard();
            return LuaValue.NIL;
        }
    };

    public LuaValue ofLuaValue(){
        LuaValue library = LuaValue.tableOf();
        //fields
        library.set( "source", LuaValue.userdataOf(this));
        //functions
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
        return library;
    }

    @Override
    public LuaValue getLuaValue() {
        return this.luaValueForm == null ? LuaValue.NIL : this.luaValueForm;
    }
}
