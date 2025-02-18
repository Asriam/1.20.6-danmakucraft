package com.adrian.thDanmakuCraft.client.model;
// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
import com.adrian.thDanmakuCraft.util.ResourceLocationUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ModelBroom<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocationUtil.mod("modelbroom"), "main");
	private final ModelPart main;
	private final ModelPart bb_main;
	private final ModelPart bone3;
	private final ModelPart bone2;
	private final ModelPart bone8;
	private final ModelPart bone9;
	private final ModelPart bone7;
	private final ModelPart bone4;
	private final ModelPart bone6;
	private final ModelPart bone5;
	private final ModelPart bone;
	private final ModelPart bone10;
	private final ModelPart bone12;
	private final ModelPart bone14;
	private final ModelPart bone20;
	private final ModelPart bone19;
	private final ModelPart bone18;
	private final ModelPart bone17;
	private final ModelPart bone16;
	private final ModelPart bone15;
	private final ModelPart bone13;
	private final ModelPart bone21;
	private final ModelPart bone11;
	private final ModelPart bone22;
	private final ModelPart bone23;
	private final ModelPart bone24;
	private final ModelPart bone25;
	private final ModelPart bone26;
	private final ModelPart bone27;
	private final ModelPart bone28;
	private final ModelPart bone29;
	private final ModelPart bone30;

	public ModelBroom(ModelPart root) {
		this.main = root.getChild("main");
		this.bb_main = this.main.getChild("bb_main");
		this.bone3 = this.main.getChild("bone3");
		this.bone2 = this.bone3.getChild("bone2");
		this.bone8 = this.bone3.getChild("bone8");
		this.bone9 = this.bone3.getChild("bone9");
		this.bone7 = this.bone3.getChild("bone7");
		this.bone4 = this.bone3.getChild("bone4");
		this.bone6 = this.bone3.getChild("bone6");
		this.bone5 = this.bone3.getChild("bone5");
		this.bone = this.bone3.getChild("bone");
		this.bone10 = this.main.getChild("bone10");
		this.bone12 = this.bone10.getChild("bone12");
		this.bone14 = this.bone10.getChild("bone14");
		this.bone20 = this.bone10.getChild("bone20");
		this.bone19 = this.bone10.getChild("bone19");
		this.bone18 = this.bone10.getChild("bone18");
		this.bone17 = this.bone10.getChild("bone17");
		this.bone16 = this.bone10.getChild("bone16");
		this.bone15 = this.bone10.getChild("bone15");
		this.bone13 = this.bone10.getChild("bone13");
		this.bone21 = this.bone10.getChild("bone21");
		this.bone11 = this.bone10.getChild("bone11");
		this.bone22 = this.main.getChild("bone22");
		this.bone23 = this.bone22.getChild("bone23");
		this.bone24 = this.bone22.getChild("bone24");
		this.bone25 = this.bone22.getChild("bone25");
		this.bone26 = this.bone22.getChild("bone26");
		this.bone27 = this.bone22.getChild("bone27");
		this.bone28 = this.bone22.getChild("bone28");
		this.bone29 = this.bone22.getChild("bone29");
		this.bone30 = this.bone22.getChild("bone30");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 13.0F));

		PartDefinition bb_main = main.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(52, 0).addBox(-0.5F, -12.5F, -42.0F, 1.0F, 1.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone3 = main.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, -11.0F));

		PartDefinition bone2 = bone3.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(3, 3).addBox(0.0F, 1.0F, -2.0F, 6.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.2618F, 0.0F, 0.7854F));

		PartDefinition bone8 = bone3.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(3, 3).addBox(-13.0F, -1.0F, -2.0F, 6.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, -3.0F, 0.0F, 0.2618F, 0.0F, -0.7854F));

		PartDefinition bone9 = bone3.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(3, 3).addBox(-14.0F, 2.0F, -3.0F, 6.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 10.0F, 0.0F, 0.2618F, 0.0F, 0.7854F));

		PartDefinition bone7 = bone3.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(3, 3).addBox(-2.0F, 0.0F, -3.0F, 6.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 1.0F, 0.0F, -0.2618F, 0.0F, -0.7854F));

		PartDefinition bone4 = bone3.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(3, 3).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, -0.2618F, 0.0F, -1.5708F));

		PartDefinition bone6 = bone3.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(3, 3).addBox(-3.0F, 2.0F, -3.0F, 6.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.2618F, 0.0F, -1.5708F));

		PartDefinition bone5 = bone3.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(3, 3).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition bone = bone3.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(3, 3).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition bone10 = main.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone12 = bone10.addOrReplaceChild("bone12", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, -1.309F));

		PartDefinition bone14 = bone10.addOrReplaceChild("bone14", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, 0.6981F));

		PartDefinition bone20 = bone10.addOrReplaceChild("bone20", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, 1.309F));

		PartDefinition bone19 = bone10.addOrReplaceChild("bone19", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, 1.9199F));

		PartDefinition bone18 = bone10.addOrReplaceChild("bone18", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, 2.5307F));

		PartDefinition bone17 = bone10.addOrReplaceChild("bone17", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, -3.1416F));

		PartDefinition bone16 = bone10.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, -2.5307F));

		PartDefinition bone15 = bone10.addOrReplaceChild("bone15", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, -1.9199F));

		PartDefinition bone13 = bone10.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, -0.6981F));

		PartDefinition bone21 = bone10.addOrReplaceChild("bone21", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, 0.3491F));

		PartDefinition bone11 = bone10.addOrReplaceChild("bone11", CubeListBuilder.create().texOffs(0, 126).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.5F, 0.0F, 0.0F, -0.2618F));

		PartDefinition bone22 = main.addOrReplaceChild("bone22", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, -10.25F, 0.0F, 3.1416F, 0.0F, 0.0F));

		PartDefinition bone23 = bone22.addOrReplaceChild("bone23", CubeListBuilder.create().texOffs(2, 2).addBox(0.0F, 0.5F, -15.0F, 6.0F, 1.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -2.75F, 0.0F, -0.2618F, 0.0F, 0.7854F));

		PartDefinition bone24 = bone22.addOrReplaceChild("bone24", CubeListBuilder.create().texOffs(2, 2).addBox(-13.0F, -2.5F, -15.0F, 6.0F, 1.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -2.75F, 0.0F, 0.2618F, 0.0F, -0.7854F));

		PartDefinition bone25 = bone22.addOrReplaceChild("bone25", CubeListBuilder.create().texOffs(2, 2).addBox(-14.5F, 0.5F, -16.0F, 6.0F, 1.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 10.25F, 0.0F, 0.2618F, 0.0F, 0.7854F));

		PartDefinition bone26 = bone22.addOrReplaceChild("bone26", CubeListBuilder.create().texOffs(2, 2).addBox(-2.0F, -1.0F, -16.0F, 6.0F, 1.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 1.25F, 0.0F, -0.2618F, 0.0F, -0.7854F));

		PartDefinition bone27 = bone22.addOrReplaceChild("bone27", CubeListBuilder.create().texOffs(2, 2).addBox(-3.0F, -3.0F, -16.0F, 6.0F, 1.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 2.25F, 0.0F, -0.2618F, 0.0F, -1.5708F));

		PartDefinition bone28 = bone22.addOrReplaceChild("bone28", CubeListBuilder.create().texOffs(2, 2).addBox(-3.0F, 1.0F, -16.0F, 6.0F, 1.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 2.25F, 0.0F, 0.2618F, 0.0F, -1.5708F));

		PartDefinition bone29 = bone22.addOrReplaceChild("bone29", CubeListBuilder.create().texOffs(2, 2).addBox(-3.0F, -1.5F, -16.0F, 6.0F, 1.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 4.25F, 0.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition bone30 = bone22.addOrReplaceChild("bone30", CubeListBuilder.create().texOffs(2, 2).addBox(-3.0F, -1.0F, -16.0F, 6.0F, 1.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.25F, 0.0F, -0.2618F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}