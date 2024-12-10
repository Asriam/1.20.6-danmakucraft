package com.adrian.thDanmakuCraft.world.entity.danmaku.laser;

import com.adrian.thDanmakuCraft.client.renderer.THObjectRenderHelper;
import com.adrian.thDanmakuCraft.client.renderer.THRenderType;
import com.adrian.thDanmakuCraft.client.renderer.entity.EntityTHObjectContainerRenderer;
import com.adrian.thDanmakuCraft.init.THObjectInit;
import com.adrian.thDanmakuCraft.world.entity.EntityTHObjectContainer;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THBullet;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObject;
import com.adrian.thDanmakuCraft.world.entity.danmaku.THObjectType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.utils.Lists;
import org.joml.Vector2f;

import java.util.List;
import java.util.function.Predicate;

public class THCurvedLaser extends THObject {

    public THBullet.BULLET_COLOR laserColor;
    public final NodeManager nodeManager;
    public int nodeMount;
    public float width;
    public boolean shouldUpdateNodes = true;
    public boolean noNodeCulling;

    public THCurvedLaser(THObjectType<THCurvedLaser> type, EntityTHObjectContainer container) {
        super(type, container);
        this.nodeManager = new NodeManager(this);
    }

    public THCurvedLaser(EntityTHObjectContainer container,THBullet.BULLET_COLOR laserColor, int nodeMount, float width){
        this(THObjectInit.TH_CURVED_LASER.get(),container);
        this.laserColor = laserColor;
        this.nodeMount = nodeMount;
        this.nodeManager.initNodeList(nodeMount);
        this.width = width;
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeFloat(this.width);
        buffer.writeEnum(this.laserColor);
        this.nodeManager.writeData(buffer);
    }

    @Override
    public void readData(FriendlyByteBuf buffer){
        super.readData(buffer);
        this.width = buffer.readFloat();
        this.laserColor = buffer.readEnum(THBullet.BULLET_COLOR.class);
        this.nodeManager.readData(buffer);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag nbt = super.save(tag);
        tag.putFloat("Width",this.width);
        tag.putInt("LaserColor",this.laserColor.ordinal());
        this.nodeManager.save(tag);
        return nbt;
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        this.width = tag.getFloat("Width");
        this.laserColor = THBullet.BULLET_COLOR.class.getEnumConstants()[tag.getInt("LaserColor")];
        this.nodeManager.load(tag);
    }

    @Override
    public void onTick(){
        super.onTick();
        if(this.shouldUpdateNodes) {
            this.nodeManager.updateNode(this.getPosition());
        }
    }

    @Override
    public void collision(){

    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void onRender(EntityTHObjectContainerRenderer renderer, Vec3 laserPos, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay) {
        if (this.color.a <= 0) {
            return;
        }

        poseStack.pushPose();
        int edge = 6;
        Color indexColor = this.laserColor.getColor();
        Color color = Color(
                this.color.r * indexColor.r/255,
                this.color.g * indexColor.g/255,
                this.color.b * indexColor.b/255,
                (int) (this.color.a * 0.6)
        );

        Color coreColor = this.color;
        this.renderCurvedLaser(renderer,laserPos,bufferSource.getBuffer(THRenderType.TEST_RENDER_TYPE),poseStack,this.nodeManager.getNodes(),this.width,this.width*0.5f,edge, 1, color, coreColor,partialTicks,combinedOverlay,1.0f,0.95f);
        poseStack.popPose();
    }

    //曲線聚光渲染的一坨屎山
    public void renderCurvedLaser(EntityTHObjectContainerRenderer renderer, Vec3 laserPos, VertexConsumer vertexConsumer, PoseStack poseStack, List<LaserNode> nodeList, float width, float coreWidth, int edge, int cull,Color laserColor, Color coreColor, float partialTicks, int combinedOverlay, float laserLength, float coreLength) {
        if(nodeList.isEmpty() || nodeList.size() < 3){
            return;
        }
        PoseStack.Pose pose = poseStack.last();

        //Ball
        float width2 = width * 3.0f * (nodeList.size()-this.timer)/nodeList.size();
        if(width2 > 0) {
            poseStack.pushPose();
            Vec3 asd = laserPos.vectorTo(nodeList.get(nodeList.size() - 1).getOffsetPosition(partialTicks));
            poseStack.translate(asd.x, asd.y, asd.z);
            THObjectRenderHelper.renderSphere(vertexConsumer, poseStack.last(), combinedOverlay, 1,
                    Vec3.ZERO,
                    new Vec3(width2, width2, width2),
                    12, 12, false,
                    Vec2.ZERO,
                    Vec2.ONE,
                    laserColor, laserColor, coreColor);
            poseStack.popPose();
        }

        final float perAngle = Mth.DEG_TO_RAD * 360.0f/edge;

        //external
        Vec3[] lastNodePos_1 = new Vec3[edge];
        Vec3[] lastNodePos_2 = new Vec3[edge];
        //core
        Vec3[] lastNodePos_3 = new Vec3[edge];
        Vec3[] lastNodePos_4 = new Vec3[edge];

        /*
        Vec3 node0Pos   = nodeList.get(0).getOffsetPosition(partialTicks);
        Vec3 nodeEndPos = nodeList.get(nodeList.size()-1).getOffsetPosition(partialTicks);
        Vec2 angle00    = THObject.VectorAngleToRadAngle(node0Pos.vectorTo(nodeEndPos));
        Vec2 angle0     = new Vec2(angle00.x - Mth.DEG_TO_RAD * 90.0f, angle00.y);
         */
        THObject.Color coreColor2 = coreColor.multiply(0.5f);

        Vec3 _pos1   = null;
        Vec3 _pos2   = null;

        int index = 0;
        Color GRAY = THObject.Color.GRAY();
        for(LaserNode node:nodeList){
            if(index+1 >= nodeList.size()){
                break;
            }

            if(cull == 1 || (index+cull)%cull == 0) {
                LaserNode node2 = nodeList.get(index + 1);
                Vec3 pos1 = node.getOffsetPosition(partialTicks);
                Vec3 pos2 = node2.getOffsetPosition(partialTicks);
                LaserNode node3 = !(index >= nodeList.size() - 2) ? nodeList.get(index + 2) : null;
                Vec2 node1Angle = THObject.VectorAngleToRadAngle(pos1.vectorTo(pos2));
                Vec2 node2Angle = node3 != null ? THObject.VectorAngleToRadAngle(pos2.vectorTo(node3.getOffsetPosition(partialTicks))) : node1Angle;

                float right_angle = Mth.DEG_TO_RAD * 90.0f;
                Vec2 angle1 = new Vec2(node1Angle.x, node1Angle.y);
                Vec2 angle2 = new Vec2(node2Angle.x , node2Angle.y);

                _pos1 = laserPos.vectorTo(pos1);
                _pos2 = laserPos.vectorTo(pos2);

                if (shouldRenderNode(node, node2, renderer.frustum)) {
                    float nodeWidth1 = circle((float) (index) / (nodeList.size() - 1), laserLength );
                    float nodeWidth2 = circle((float) (index + 1) / (nodeList.size() - 1), laserLength);

                    float nodeWidth3 = circle((float) (index) / (nodeList.size() - 1), coreLength);
                    float nodeWidth4 = circle((float) (index + 1) / (nodeList.size() - 1), coreLength);

                    for (int i = 0; i < edge; i++) {
                        //external render
                        Vec3 posA = new Vec3(nodeWidth1 * width, 0.0d, 0.0d);
                        Vec3 posB = new Vec3(!(index >= nodeList.size() - 2) ? nodeWidth2 * width : 0.0f, 0.0d, 0.0d);
                        if(lastNodePos_1[i] == null){
                            lastNodePos_1[i] = posA.yRot(i * perAngle).       xRot(right_angle+angle1.x).yRot(angle1.y).add(_pos1);
                        }

                        if(lastNodePos_2[i] == null){
                            lastNodePos_2[i] = posA.yRot((i + 1) * perAngle). xRot(right_angle+angle1.x).yRot(angle1.y).add(_pos1);
                        }
                        Vec3 calculatedPos1_1 = lastNodePos_1[i];
                        Vec3 calculatedPos1_2 = lastNodePos_2[i];

                        Vec3 calculatedPos2_1 = posB.yRot(i * perAngle).      xRot(right_angle+angle2.x).yRot(angle2.y).add(_pos2);
                        Vec3 calculatedPos2_2 = posB.yRot((i + 1) * perAngle).xRot(right_angle+angle2.x).yRot(angle2.y).add(_pos2);

                        lastNodePos_1[i] = calculatedPos2_1;
                        lastNodePos_2[i] = calculatedPos2_2;

                        Vec3 posM = _pos1.lerp(_pos2,0.5D);


                        //TODO there is a bug in curved laser rendering

                        /*
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos1_1.toVector3f(), calculatedPos2_1.subtract(posM).toVector3f(), new Vector2f(0.0f, 0.0f), laserColor, coreColor2);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos2_1.toVector3f(), calculatedPos2_1.subtract(posM).toVector3f(), new Vector2f(0.0f, 0.0f), laserColor, coreColor2);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos2_2.toVector3f(), calculatedPos2_2.subtract(posM).toVector3f(), new Vector2f(0.0f, 0.0f), laserColor, coreColor2);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos1_2.toVector3f(), calculatedPos2_2.subtract(posM).toVector3f(), new Vector2f(0.0f, 0.0f), laserColor, coreColor2);
                        */


                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos1_1.toVector3f(), calculatedPos1_1.subtract(_pos1).toVector3f(), new Vector2f(0.0f, 0.0f), laserColor, coreColor2);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos2_1.toVector3f(), calculatedPos2_1.subtract(_pos2).toVector3f(), new Vector2f(0.0f, 0.0f), laserColor, coreColor2);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos2_2.toVector3f(), calculatedPos2_2.subtract(_pos2).toVector3f(), new Vector2f(0.0f, 0.0f), laserColor, coreColor2);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos1_2.toVector3f(), calculatedPos1_2.subtract(_pos1).toVector3f(), new Vector2f(0.0f, 0.0f), laserColor, coreColor2);


                        /*
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos1_1.toVector3f(), new Vector2f(0.0f, 0.0f), laserColor);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos2_1.toVector3f(), new Vector2f(0.0f, 0.0f), laserColor);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos2_2.toVector3f(), new Vector2f(0.0f, 0.0f), laserColor);
                        THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos1_2.toVector3f(), new Vector2f(0.0f, 0.0f), laserColor);
                        */

                        //core render

                        if(true) {
                            Vec3 posA1 = new Vec3(nodeWidth3 * coreWidth, 0.0d, 0.0d);
                            Vec3 posB2 = new Vec3(!(index >= nodeList.size() - 2) ? nodeWidth4 * coreWidth : 0.0f, 0.0d, 0.0d);
                            if (lastNodePos_3[i] == null) {
                                lastNodePos_3[i] = posA1.yRot(i * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y).add(_pos1);
                            }

                            if (lastNodePos_4[i] == null) {
                                lastNodePos_4[i] = posA1.yRot((i + 1) * perAngle).xRot(right_angle + angle1.x).yRot(angle1.y).add(_pos1);
                            }
                            Vec3 calculatedPos3_1 = lastNodePos_3[i];
                            Vec3 calculatedPos3_2 = lastNodePos_4[i];

                            Vec3 calculatedPos4_1 = posB2.yRot(i * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y).add(_pos2);
                            Vec3 calculatedPos4_2 = posB2.yRot((i + 1) * perAngle).xRot(right_angle + angle2.x).yRot(angle2.y).add(_pos2);

                            lastNodePos_3[i] = calculatedPos4_1;
                            lastNodePos_4[i] = calculatedPos4_2;

                            THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos3_1.toVector3f(), calculatedPos3_1.subtract(_pos1).toVector3f(), new Vector2f(0.0f, 0.0f), coreColor, GRAY);
                            THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos4_1.toVector3f(), calculatedPos4_1.subtract(_pos2).toVector3f(), new Vector2f(0.0f, 0.0f), coreColor, GRAY);
                            THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos4_2.toVector3f(), calculatedPos4_2.subtract(_pos2).toVector3f(), new Vector2f(0.0f, 0.0f), coreColor, GRAY);
                            THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos3_2.toVector3f(), calculatedPos3_2.subtract(_pos1).toVector3f(), new Vector2f(0.0f, 0.0f), coreColor, GRAY);

                            /*
                            THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos3_1.toVector3f(), new Vector2f(0.0f, 0.0f), coreColor);
                            THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos4_1.toVector3f(), new Vector2f(0.0f, 0.0f), coreColor);
                            THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos4_2.toVector3f(), new Vector2f(0.0f, 0.0f), coreColor);
                            THObjectRenderHelper.vertex(vertexConsumer, pose, combinedOverlay, calculatedPos3_2.toVector3f(), new Vector2f(0.0f, 0.0f), coreColor);
                            */
                        }
                    }
                }
            }
            index += 1;
        }
    }

    public static boolean shouldRenderNode(LaserNode node1, LaserNode node2, Frustum frustum) {
        AABB aabb1 = node1.getBoundingBoxForCulling().inflate(0.5D);
        AABB aabb2 = node2.getBoundingBoxForCulling().inflate(0.5D);
        if (aabb1.hasNaN() || aabb1.getSize() == 0.0D) {
            aabb1 = new AABB(node1.position.x - 2.0D, node1.position.y - 2.0D, node1.position.z - 2.0D, node1.position.x + 2.0D, node1.position.y + 2.0D, node1.position.z + 2.0D);
        }
        if (aabb2.hasNaN() || aabb2.getSize() == 0.0D) {
            aabb2 = new AABB(node2.position.x - 2.0D, node2.position.y - 2.0D, node2.position.z - 2.0D, node2.position.x + 2.0D, node2.position.y + 2.0D, node2.position.z + 2.0D);
        }

        return frustum.isVisible(aabb1) && frustum.isVisible(aabb2);
    }

    static float circle(float x, float width){
        float num = (x/width*2-1/width);
        return Mth.sqrt(1-num*num);
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

        public void writeData(FriendlyByteBuf buffer){
            buffer.writeInt(this.nodeList.size());
            for(LaserNode node:this.nodeList){
                node.writeData(buffer);
            }
        }

        public void readData(FriendlyByteBuf buffer){
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

        public void load(CompoundTag tag){
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

        public List<LaserNode> getNodes(){
            return this.nodeList;
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

        public LaserNode(Vec3 pos, Vec3 size){
            this.lastPosition =  pos;
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
            tag.put("Pos",newDoubleList(this.position.x,this.position.y,this.position.z));
            tag.put("LastPos",newDoubleList(this.lastPosition.x,this.lastPosition.y,this.lastPosition.z));
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
}
