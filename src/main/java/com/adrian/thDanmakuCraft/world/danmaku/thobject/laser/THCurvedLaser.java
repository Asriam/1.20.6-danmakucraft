package com.adrian.thDanmakuCraft.world.danmaku.thobject.laser;

import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.util.Color;
import com.adrian.thDanmakuCraft.util.CompoundTagUtil;
import com.adrian.thDanmakuCraft.util.FriendlyByteBufUtil;
import com.adrian.thDanmakuCraft.world.ILuaValue;
import com.adrian.thDanmakuCraft.world.danmaku.ITHObjectContainer;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.bullet.THBullet;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObject;
import com.adrian.thDanmakuCraft.world.danmaku.thobject.THObjectType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.List;
import java.util.function.Predicate;

public class THCurvedLaser extends AbstractLaser {

    public Color laserColor;
    public final NodeManager nodeManager;
    public int nodeMount;
    public float width;
    public boolean shouldUpdateNodes = true;
    public boolean breakable = true;
    public int renderCull = 2;

    public THCurvedLaser(THObjectType<THCurvedLaser> type, ITHObjectContainer container) {
        super(type, container);
        this.nodeManager = new NodeManager(this);
        this.shouldSetDeadWhenCollision = false;
    }

    public THCurvedLaser(ITHObjectContainer container, THBullet.BULLET_INDEX_COLOR laserColor, int nodeMount, float width){
        this(THObjectInit.TH_CURVED_LASER.get(),container);
        this.laserColor = laserColor.getColor();
        this.nodeMount = nodeMount;
        this.width = width;
        float width2 = width * 0.5f;
        this.setSize(new Vec3(width2,width2,width2));
        this.nodeManager.initNodeList(nodeMount);
    }

    public void setLaserColor(Color color){
        this.laserColor = color;
    }

    public void setLaserColor(int r, int g, int b, int a){
        this.setLaserColor(new Color(r,g,b,a));
    }

    public void setLaserColorByIndex(int i){
        this.laserColor = THBullet.BULLET_INDEX_COLOR.getColorByIndex(i).getColor();
    }
    @Override
    public void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
        buffer.writeFloat(this.width);
        buffer.writeInt(this.renderCull);
        FriendlyByteBufUtil.writeColor(buffer,this.laserColor);
        this.nodeManager.writeData(buffer);
    }

    @Override
    public void decode(FriendlyByteBuf buffer){
        super.decode(buffer);
        this.width = buffer.readFloat();
        this.renderCull = buffer.readInt();
        this.laserColor = FriendlyByteBufUtil.readColor(buffer);
        this.nodeManager.readData(buffer);
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putFloat("Width",this.width);
        tag.putInt("RenderCull",this.renderCull);
        CompoundTagUtil.putColor(tag, "LaserColor", this.laserColor);
        this.nodeManager.save(tag);
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        this.width = tag.getFloat("Width");
        this.renderCull = tag.getInt("RenderCull");
        this.laserColor = CompoundTagUtil.getColor(tag, "LaserColor");
        this.nodeManager.load(tag);
    }

    @Override
    public void onTick(){
        if(this.nodeManager.isEmpty()){
            this.remove();
        }
        super.onTick();
        if(this.shouldUpdateNodes) {
            this.nodeManager.updateNode(this.getPosition());
        }
    }

    public void setRenderCull(int renderCull){
        this.renderCull = renderCull;
    }

    public int getRenderCull(){
        return 2;
    }

    public int getNodeMount(){
        return this.nodeManager.nodeList.size();
    }

    public LaserNode getNode(int index){
        return this.nodeManager.getNode(index);
    }

    public List<LaserNode> getAllNodes(){
        return this.nodeManager.getAllNodes();
    }

    public void setNode(int index,Vec3 pos){
        LaserNode node = this.nodeManager.getNode(index);
        node.setPosition(pos);
    }

    public void setAllNodes(List<Vec3> posList){
        int index = 0;
        for(Vec3 pos : posList){
            this.setNode(index,pos);
            index++;
        }
    }

    @Override
    public void collisionLogic(){
        this.nodeManager.collision();
    }

    public static class NodeManager{
        private final List<LaserNode> nodeList;
        private final THCurvedLaser laser;

        public NodeManager(THCurvedLaser laser) {
            this.nodeList = Lists.newArrayList();
            this.laser = laser;
        }

        public void initNodeList(int nodeMount){
            for(int i=0;i<nodeMount;i++){
                this.nodeList.add(new LaserNode(laser.getPosition(),laser.size));
            }
        }

        public void updateNode(Vec3 pos){
            if(this.nodeList.isEmpty()){
                return;
            }
            for (int index = 0; index < this.nodeList.size(); index++){
                LaserNode node = this.nodeList.get(index);
                LaserNode lastNode = index > 0 ? this.nodeList.get(index-1) : null;
                if(index == 0){
                    node.updateNode(pos);
                }else {
                    node.updateNode(lastNode.lastPosition);
                }
            }
        }

        public void updateNodePos(int index, Vec3 pos){
            this.getNode(index).updateNode(pos);
        }

        public void updateAllNodePos(List<Vec3> posList){
            if(this.nodeList.isEmpty()){
                return;
            }

            int index = 0;
            for(Vec3 pos:posList){
                LaserNode node = this.getNode(index);
                if (node != null){
                    node.updateNode(pos);
                }
                index++;
            }
        }

        public void updateAllNode(List<LaserNode> nodeList){
            this.nodeList.clear();
            this.nodeList.addAll(nodeList);
        }

        public void collision(){
            List<Entity> entitiesInBound = laser.container.getEntitiesInBound();
            if(entitiesInBound.isEmpty()){
                return;
            }/*
            entitiesInBound.forEach((entity -> {
                laser.onHit(new EntityHitResult(entity, laser.getPosition()));
            }));*/

            //int index = 0;
            for(var node:nodeList){
                //index++;
                if(node.isValid()) {
                    entitiesInBound.forEach((entity -> {
                        if (!entity.equals(laser.getContainer().getUser()) && entity.getBoundingBox().intersects(node.getBoundingBox())) {
                            laser.onHit(new EntityHitResult(entity, node.getPosition()));
                            if (laser.breakable)
                                node.isValid = false;
                        }
                    }));
                }
            };
        }

        public void writeData(@NotNull FriendlyByteBuf buffer){
            buffer.writeInt(this.nodeList.size());
            for(LaserNode node:this.nodeList){
                node.writeData(buffer);
            }
        }

        public void readData(@NotNull FriendlyByteBuf buffer){
            int size = buffer.readInt();
            List<LaserNode> nodes = Lists.newArrayList();
            for(short i=0;i<size;i++){
                LaserNode node = new LaserNode(laser.getPosition(),laser.size);
                node.readData(buffer);
                nodes.add(node);
            }
            //this.updateAllNode(nodes);
            this.nodeList.addAll(nodes);
        }

        public void save(CompoundTag tag){
            int index = 0;
            CompoundTag list = new CompoundTag();
            for(LaserNode node:this.nodeList) {
                list.put("node_"+index,node.save(new CompoundTag()));
                index++;
            }
            tag.put("nodes",list);
        }

        public void load(@NotNull CompoundTag tag){
            CompoundTag listTag = tag.getCompound("nodes");
            int list_size = listTag.getAllKeys().size();
            List<LaserNode> nodes = Lists.newArrayList();
            for(int i=0;i<list_size;i++){
                LaserNode node = new LaserNode(laser.getPosition(),laser.size);
                node.load(listTag.getCompound("node_"+i));
                nodes.add(node);
            }
            this.updateAllNode(nodes);
        }

        public void clear(){
            this.nodeList.clear();
        }

        public void addNode(LaserNode node){
            this.nodeList.add(node);
        }

        public void removeNode(LaserNode node){
            this.nodeList.remove(node);
        }

        public void removeNode(int index){
            this.nodeList.remove(index);
        }

        public void removeIf(Predicate<LaserNode> filter){
            this.nodeList.removeIf(filter);
        }

        public List<LaserNode> getAllNodes(){
            return this.nodeList;
        }

        public boolean isEmpty(){
            return this.nodeList.isEmpty();
        }

        public LaserNode getNode(int index){
            if(index > this.nodeList.size()-1 || index < 0){
                return null;
            }

            return this.nodeList.get(index);
        }
    }

    public static class LaserNode {
        private Vec3 position;
        private Vec3 lastPosition;
        private AABB bb = INITIAL_AABB;
        private Vec3 size;/* = new Vec3(0.5f,0.5f,0.5f);*/
        private boolean isValid = true;

        public LaserNode(Vec3 pos, Vec3 size){
            this.lastPosition = pos;
            this.position = pos;
            this.size = size;
        }

        public void updateNode(Vec3 position){
            this.lastPosition = this.position;
            this.position = position;
            this.setBoundingBox(this.position,this.size);
        }

        public void setPosition(Vec3 position){
            this.lastPosition = position;
            this.position = position;
            this.setBoundingBox(this.position,this.size);
        }

        public Vec3 getPosition(){
            return this.position;
        }

        public void setBoundingBox(AABB boundingBox) {
            this.bb = boundingBox;
        }

        public final AABB getBoundingBox() {
            return this.bb;
        }

        public AABB getBoundingBoxForCulling() {
            return this.getBoundingBox();
        }

        public void setBoundingBox(Vec3 pos, Vec3 size) {
            this.setBoundingBox(new AABB(
                    pos.x - size.x / 2, pos.y - size.y / 2, pos.z - size.z / 2,
                    pos.x + size.x / 2, pos.y + size.y / 2, pos.z + size.z / 2
            ));
        }

        public void setSize(Vec3 size) {
            this.size = size;
        }

        public Vec3 getSize() {
            return size;
        }

        public boolean isValid(){
            return this.isValid;
        }

        public Vec3 getOffsetPosition(float partialTicks){
            double x = Mth.lerp(partialTicks, this.lastPosition.x, this.position.x);
            double y = Mth.lerp(partialTicks, this.lastPosition.y, this.position.y);
            double z = Mth.lerp(partialTicks, this.lastPosition.z, this.position.z);
            return new Vec3(x, y, z);
        }

        public void writeData(FriendlyByteBuf byteBuf){
            byteBuf.writeVec3(this.position);
            byteBuf.writeVec3(this.lastPosition);
        }

        public void readData(FriendlyByteBuf byteBuf){
            this.position = byteBuf.readVec3();
            this.lastPosition = byteBuf.readVec3();
            this.setBoundingBox(this.position,this.size);
        }

        public CompoundTag save(CompoundTag tag){
            tag.put("Pos", CompoundTagUtil.newDoubleList(this.position.x,this.position.y,this.position.z));
            tag.put("LastPos", CompoundTagUtil.newDoubleList(this.lastPosition.x,this.lastPosition.y,this.lastPosition.z));
            return tag;
        }

        public void load(CompoundTag tag){
            ListTag posTag = tag.getList("Pos", Tag.TAG_DOUBLE);
            ListTag LastPosTag = tag.getList("LastPos", Tag.TAG_DOUBLE);
            this.position = new Vec3(posTag.getDouble(0),posTag.getDouble(1),posTag.getDouble(2));
            this.lastPosition = new Vec3(LastPosTag.getDouble(0),LastPosTag.getDouble(1),LastPosTag.getDouble(2));
            this.setBoundingBox(this.position,this.size);
        }
    }

    private static class LuaAPI{
        private static THCurvedLaser checkTHCurvedLaser(LuaValue luaValue) {
            if (luaValue.get("source").checkuserdata() instanceof THCurvedLaser laser) {
                return laser;
            }
            throw new NullPointerException();
        }
        private static final LibFunction setLaserColorByIndex = new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue0, LuaValue luaValue) {
                checkTHCurvedLaser(luaValue0).setLaserColorByIndex(luaValue.checkint());
                return LuaValue.NIL;
            }
        };
        private static final LibFunction setLaserColor = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                checkTHCurvedLaser(varargs.arg1()).setLaserColor(
                        varargs.arg(1).checkint(),
                        varargs.arg(3).checkint(),
                        varargs.arg(4).checkint(),
                        varargs.arg(5).checkint()
                );
                return LuaValue.NIL;
            }
        };
        private static LuaValue functions(){
            LuaValue library = THObject.LuaAPI.functions();
            library.set("setLaserColorByIndex", setLaserColorByIndex);
            library.set("setLaserColor", setLaserColor);
            return library;
        }

        public static final LuaValue meta = ILuaValue.setMeta(functions());
    }

    /*@Override
    public LuaValue ofLuaClass(){
        LuaValue library = super.ofLuaClass();
        return library;
    }

    @Override
    public LuaValue ofLuaValue(){
        LuaValue library = super.ofLuaValue();
        return library;
    }*/

    @Override
    public LuaValue getMeta(){
        return LuaAPI.meta;
    }
}
