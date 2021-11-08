package de.dafuqs.spectrum.blocks.fusion_shrine;

import de.dafuqs.spectrum.GlowInTheDarkRenderLayer;
import de.dafuqs.spectrum.SpectrumCommon;
import de.dafuqs.spectrum.recipe.fusion_shrine.FusionShrineRecipe;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FusionShrineBlockEntityRenderer<T extends FusionShrineBlockEntity> implements BlockEntityRenderer<T> {
	
	
	public FusionShrineBlockEntityRenderer(Context ctx) {
	
	}

	public void render(FusionShrineBlockEntity fusionShrineBlockEntity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
		// the fluid in the shrine
		Fluid fluid = fusionShrineBlockEntity.getFluid();
		if(fluid != Fluids.EMPTY) {
			matrixStack.push();
			FluidVariant fluidVariant = FluidVariant.of(fluid);
			Sprite sprite = FluidVariantRendering.getSprite(fluidVariant);
			int color = FluidVariantRendering.getColor(fluidVariant, fusionShrineBlockEntity.getWorld(), fusionShrineBlockEntity.getPos());
			int[] colors = unpackColor(color);

			renderFluid(vertexConsumerProvider.getBuffer(RenderLayer.getTranslucent()), matrixStack.peek().getModel(), sprite, light, overlay, 0.25F, 0.75F, 0.9F, 0.25F, 0.75F, colors);
			matrixStack.pop();
		}
		
		if(!fusionShrineBlockEntity.getInventory().isEmpty()) {
			// the floating item stacks
			List<ItemStack> inventoryStacks = new ArrayList<>();

			for(int i = 0; i < fusionShrineBlockEntity.getInventory().size(); i++) {
				ItemStack stack = fusionShrineBlockEntity.getInventory().getStack(i);
				if(!stack.isEmpty()) {
					inventoryStacks.add(stack);
				}
			}

			float time = fusionShrineBlockEntity.getWorld().getTime() + tickDelta;
			double radiant = Math.toRadians(360.0F / inventoryStacks.size());
			float distance = 1.2F;

			for(int i = 0; i < inventoryStacks.size(); i++) {
				matrixStack.push();
				double currentRadiant = radiant * i  + (radiant * (time / 16.0) / (8.0F / inventoryStacks.size()));
				double height = Math.sin((time + currentRadiant) / 8.0) / 3.0; // item height
				matrixStack.translate(distance * Math.sin(currentRadiant) + 0.5, 1.5 + height, distance * Math.cos(currentRadiant) + 0.5); // position offset
				matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((time) * 2)); // item stack rotation

				MinecraftClient.getInstance().getItemRenderer().renderItem(inventoryStacks.get(i), ModelTransformation.Mode.GROUND, light, overlay, matrixStack, vertexConsumerProvider, 0);
				matrixStack.pop();
			}
			
			// the ground overlay
			// definitely does not fit the fusion shrine
			// maybe use that for some other block
			/*FusionShrineRecipe currentRecipe = fusionShrineBlockEntity.getCurrentRecipe();
			if(currentRecipe != null && fusionShrineBlockEntity.getFluid().equals(currentRecipe.getFluidInput())) {
				//circle.yaw = Math. (;
				double overlay_mod = Math.sin((fusionShrineBlockEntity.getWorld().getTime() + tickDelta) / 25.0F);
				circle.render(matrixStack, vertexConsumerProvider.getBuffer(GlowInTheDarkRenderLayer.get(GROUND_MARK)), light, (int) (overlay * overlay_mod));
			}*/
		}
	}

	private static void renderFluid(VertexConsumer builder, Matrix4f pos, Sprite sprite, int light, int overlay, float x1, float x2, float y, float z1, float z2, int[] color) {
		// Convert block size to pixel size
		final double px1 = x1 * 16;
		final double px2 = x2 * 16;
		final double pz1 = z1 * 16;
		final double pz2 = z2 * 16;

		final float u1 = sprite.getFrameU(px1);
		final float u2 = sprite.getFrameU(px2);
		final float v1 = sprite.getFrameV(pz1);
		final float v2 = sprite.getFrameV(pz2);
		builder.vertex(pos, x1, y, z2).color(color[1], color[2], color[3], color[0]).texture(u1, v2).overlay(overlay).light(light).normal(0f, 1f, 0f).next();
		builder.vertex(pos, x2, y, z2).color(color[1], color[2], color[3], color[0]).texture(u2, v2).overlay(overlay).light(light).normal(0f, 1f, 0f).next();
		builder.vertex(pos, x2, y, z1).color(color[1], color[2], color[3], color[0]).texture(u2, v1).overlay(overlay).light(light).normal(0f, 1f, 0f).next();
		builder.vertex(pos, x1, y, z1).color(color[1], color[2], color[3], color[0]).texture(u1, v1).overlay(overlay).light(light).normal(0f, 1f, 0f).next();
	}

	public static int[] unpackColor(int color) {
		final int[] colors = new int[4];
		colors[0] = color >> 24 & 0xff; // alpha
		colors[1] = color >> 16 & 0xff; // red
		colors[2] = color >> 8 & 0xff; // green
		colors[3] = color & 0xff; // blue
		return colors;
	}
	
	/*public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		
		modelPartData.addChild("circle", ModelPartBuilder.create(), ModelTransform.pivot(8.0F, 0.01F, 8.0F));
		modelPartData.getChild("circle").addChild("circle2", ModelPartBuilder.create().uv(0, 0).cuboid(-56.0F, 0.0F, -56F, 112.0F, 0.0F, 112.0F), ModelTransform.rotation(0.0F, 0.0F, 0.0F));
		
		return TexturedModelData.of(modelData, 448, 448);
	}*/


}
