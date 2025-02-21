package com.adrian.thDanmakuCraft.world.danmaku.thobject.laser;

import com.adrian.thDanmakuCraft.THDanmakuCraftMod;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.CompoundTagUtil;
import com.adrian.thDanmakuCraft.util.FriendlyByteBufUtil;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import com.adrian.thDanmakuCraft.world.LuaValueHelper;
import com.adrian.thDanmakuCraft.world.danmaku.ITHObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.THObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.TaskManager;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.CollisionType;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet.THBullet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.List;

public class THLaser extends THObject {

    protected float width = 0.0f;
    protected float length = 0.0f;
    protected float lastWidth = 0.0f;
    protected float lastLength = 0.0f;
    protected float targetWidth = 0.0f;
    protected float targetLength = 0.0f;
    protected Color laserColor = THBullet.BULLET_INDEX_COLOR.COLOR_RED.getColor();

    public THLaser(THObjectType<? extends THLaser> type, ITHObjectContainer container) {
        super(type, container);
    }

    public THLaser(THObjectContainer container){
        this(THObjectInit.TH_LASER.get(),container);
        this.initPosition(container.getPosition());
    }

    @Override
    public void registerTasks(){
        super.registerTasks();
        this.taskManager.registerTask("laser_width_set", new TaskManager.Task<>((target, timer,lifetime) ->{
            if(target instanceof THLaser self){
                float v = (float) timer /lifetime;
                self.width = Mth.lerp(v,self.width,self.targetWidth);
            }
        }));
        this.taskManager.registerTask("laser_length_set", new TaskManager.Task<>((target, timer,lifetime) ->{
            if(target instanceof THLaser self){
                float v = (float) timer /lifetime;
                self.length = Mth.lerp(v,self.length,self.targetLength);
            }
        }));
    }

    @Override
    public void onTick(){
        this.lastWidth = width;
        this.lastLength = length;
        super.onTick();
        //this.vectorTo(new Vec3(0.0f,0.0f,10.0f).yRot(this.timer/20.0f));
    }

    public void growWidth(float width, int duration){
        this.targetWidth = width;
        this.taskManager.startTask("laser_width_set",duration,0);
    }

    public void growLength(float length, int duration){
        this.targetLength = length;
        this.taskManager.startTask("laser_length_set",duration,0);
    }

    public void growVector(Vec3 vec3, int duration){
        this.targetLength = (float) vec3.length();
        this.setRotationByDirectionalVector(vec3.normalize());
        this.taskManager.startTask("laser_length_set",duration,0);
    }

    public void grow(float width, float length, int duration){
        this.growWidth(width,duration);
        this.growLength(length,duration);
    }

    public void vectorTo(Vec3 vec3){
        this.length = (float) vec3.length()/2;
        this.setRotationByDirectionalVector(vec3);
    }

    public Vec3 getLaserCenter(){
        Vector3f rotation = this.getRotation();
        return new Vec3(0.0f,0.0f,1.0f).xRot(-rotation.x).yRot(-rotation.y).scale(length);
    }

    public Vec3 getLaserCenterForRender(float partialTick){
        Vector3f rotation = this.getOffsetRotation(partialTick);
        float length = Mth.lerp(partialTick,this.lastLength,this.length);
        return new Vec3(0.0f,0.0f,1.0f).xRot(-rotation.x).yRot(-rotation.y).scale(length);
    }

    @Override
    public boolean shouldCollision(){
        return this.width > 0.1f && super.shouldCollision();
    }

    @Override
    public void collisionLogic(){
        List<Entity> entitiesInBound = this.container.getEntitiesInBound();
        if (entitiesInBound.isEmpty()) {
            return;
        }

        for (Entity entity : entitiesInBound) {
                if (!this.canHitUser && entity.equals(this.getContainer().getUser())) {
                    continue;
                }
                Vector3f r = this.getRotation();

                if (CollisionType.Ellipsoid(
                        this.getPosition().add(this.getLaserCenter()),
                        new Vec3(width*0.6f, width*0.6f, length*0.9f),
                        new Vector3f(-r.x, -r.y, -r.z),
                        entity.getBoundingBox())) {
                    this.shouldSetDeadWhenCollision = false;
                    this.onHit(new EntityHitResult(entity, entity.position()));
                    /*THBullet bullet = new THBullet((THObjectContainer) this.getContainer(), THBullet.DefaultBulletStyle.arrow_mid, THBullet.BULLET_INDEX_COLOR.COLOR_BLUE);
                    bullet.initPosition(entity.position().add(0.0f, 2.0f, 0.0f));
                    bullet.spawn();*/
                }
            }
    }

    public void setWidth(float width){
        this.width = width;
    }

    public void setLength(float length){
        this.length = length;
    }

    public float getWidth(){
        return this.width;
    }

    public float getLength(){
        return this.length;
    }

    public float getWidthForRender(float partialTick){
        return Mth.lerp(partialTick,lastWidth,width);
    }

    public float getLengthForRender(float partialTick){
        return Mth.lerp(partialTick,lastLength,length);
    }

    public Color getLaserColor(){
        return this.laserColor;
    }

    public void setLaserColor(Color color){
        this.laserColor = color;
    }

    public void setLaserColor(int r, int g, int b, int a){
        this.setLaserColor(new Color(r,g,b,a));
    }

    public void setLaserColorByIndex(int index){
        this.laserColor = THBullet.BULLET_INDEX_COLOR.getColorByIndex(index).getColor();
    }

    @Override
    public float getDamage(){
        return super.getDamage();
    }
    @Override
    public void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
        buffer.writeFloat(this.width);
        buffer.writeFloat(this.length);
        buffer.writeFloat(this.targetWidth);
        buffer.writeFloat(this.targetLength);
        FriendlyByteBufUtil.writeColor(buffer,this.laserColor);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        super.decode(buffer);
        this.width = buffer.readFloat();
        this.length = buffer.readFloat();
        this.lastWidth = width;
        this.lastLength = length;
        this.targetWidth = buffer.readFloat();
        this.targetLength = buffer.readFloat();
        this.laserColor = FriendlyByteBufUtil.readColor(buffer);
    }
    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putFloat("Width",this.width);
        tag.putFloat("Length",this.length);
        tag.putFloat("TargetWidth",this.targetWidth);
        tag.putFloat("TargetLength",this.targetLength);
        CompoundTagUtil.putColor(tag, "LaserColor", this.laserColor);
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        this.width = tag.getFloat("Width");
        this.length = tag.getFloat("Length");
        this.lastWidth = width;
        this.lastLength = length;
        this.targetWidth = tag.getFloat("TargetWidth");
        this.targetLength = tag.getFloat("TargetLength");
        this.laserColor = CompoundTagUtil.getColor(tag, "LaserColor");
    }

    protected static class LuaAPI{
        private static THLaser checkTHLaser(LuaValue luaValue) {
            if (luaValue.get("source").checkuserdata() instanceof THLaser laser) {
                return laser;
            }
            throw new NullPointerException();
        }
        private static final LibFunction setLaserColorByIndex = new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
                checkTHLaser(luaValue0).setLaserColorByIndex(luaValue.checkint());
                return LuaValue.NIL;
            }
        };
        private static final LibFunction setLaserColor = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHLaser(varargs.arg1()).setLaserColor(
                        varargs.arg(1).checkint(),
                        varargs.arg(3).checkint(),
                        varargs.arg(4).checkint(),
                        varargs.arg(5).checkint()
                );
                return LuaValue.NIL;
            }
        };
        private static final LibFunction growWidth = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHLaser(varargs.arg1()).growWidth(varargs.tofloat(2),varargs.checkint(3));
                return LuaValue.NIL;
            }
        };
        private static final LibFunction growLength = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHLaser(varargs.arg1()).growLength(varargs.tofloat(2),varargs.checkint(3));
                return LuaValue.NIL;
            }
        };
        private static final LibFunction setWidth = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHLaser(varargs.arg1()).setWidth(varargs.tofloat(2));
                return LuaValue.NIL;
            }
        };
        private static final LibFunction setLength = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHLaser(varargs.arg1()).setLength(varargs.tofloat(2));
                return LuaValue.NIL;
            }
        };
        private static final LibFunction grow = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHLaser(varargs.arg1()).grow(varargs.tofloat(2),varargs.tofloat(3),varargs.checkint(4));
                return LuaValue.NIL;
            }
        };
        private static final LibFunction growVector = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHLaser(varargs.arg1()).growVector(LuaValueHelper.LuaValueToVec3(varargs.arg(2)),varargs.checkint(3));
                return LuaValue.NIL;
            }
        };
        private static final LibFunction vectorTo = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHLaser(varargs.arg1()).vectorTo(LuaValueHelper.LuaValueToVec3(varargs.arg(2)));
                return LuaValue.NIL;
            }
        };
        protected static LuaValue functions(){
            LuaValue library = THObject.LuaAPI.functions();
            library.set("setLaserColorByIndex", setLaserColorByIndex);
            library.set("setLaserColor", setLaserColor);
            library.set("growWidth", growWidth);
            library.set("growLength", growLength);
            library.set("setWidth", setWidth);
            library.set("setLength", setLength);
            library.set("grow", grow);
            library.set("growVector", growVector);
            library.set("vectorTo", vectorTo);
            return library;
        }
        public static final LuaValue meta = ILuaValue.setMeta(functions());
    }

    /*public LuaValue ofLuaClass(){
        LuaValue library = super.ofLuaClass();
        return library;
    }*/

    @Override
    public LuaValue getMeta(){
        return LuaAPI.meta;
    }
}
