package de.dafuqs.spectrum.blocks.pedestal;

import de.dafuqs.spectrum.SpectrumCommon;
import de.dafuqs.spectrum.GlowInTheDarkRenderLayer;
import de.dafuqs.spectrum.blocks.ColoredSporeBlossomBlock;
import de.dafuqs.spectrum.enums.GemstoneColor;
import de.dafuqs.spectrum.particle.SpectrumParticleTypes;
import de.dafuqs.spectrum.recipe.pedestal.PedestalCraftingRecipe;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class PedestalBlockEntityRenderer<T extends PedestalBlockEntity> implements BlockEntityRenderer<T> {
	
	Random random = new Random();
	private final Identifier GROUND_MARK = new Identifier(SpectrumCommon.MOD_ID, "textures/misc/circle.png");
	
	private final ModelPart circle;
	
	public PedestalBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		super();
		this.circle = getTexturedModelData().createModel().getChild("circle");
	}
	
	@Override
	public void render(T entity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
		if(entity.getWorld() == null) {
			return;
		}
		
		// render floating item stacks
		Recipe currentRecipe = entity.getCurrentRecipe();
		if(currentRecipe instanceof PedestalCraftingRecipe) {
			circle.yaw = (entity.getWorld().getTime() + tickDelta) / 25.0F;
			circle.render(matrixStack, vertexConsumerProvider.getBuffer(GlowInTheDarkRenderLayer.get(GROUND_MARK)), light, overlay);
			
			ItemStack outputItemStack = entity.getCurrentRecipe().getOutput();
			float time = entity.getWorld().getTime() + tickDelta;

			matrixStack.push();
			double height = Math.sin((time) / 8.0) / 6.0; // item height
			matrixStack.translate(0.5F, 1.25 + height, 0.5F); // position offset
			matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((time) * 2)); // item stack rotation
			
			// fixed lighting because:
			// 1. light variable would always be 0 anyways (the pedestal is opaque, making the inside black)
			// 2. the floating item looks like a hologram
			MinecraftClient.getInstance().getItemRenderer().renderItem(outputItemStack, ModelTransformation.Mode.GROUND, 15728768, overlay, matrixStack, vertexConsumerProvider, 0);
			matrixStack.pop();
		}
	}
	
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		
		modelPartData.addChild("circle", ModelPartBuilder.create(), ModelTransform.pivot(8.0F, 0.1F, 8.0F));
		modelPartData.getChild("circle").addChild("circle2", ModelPartBuilder.create().uv(0, 0).cuboid(-32.0F, 0.0F, -29F, 64.0F, 0.0F, 64.0F), ModelTransform.rotation(0.0F, 0.0F, 0.0F));
		
		return TexturedModelData.of(modelData, 256, 256);
	}
	
}