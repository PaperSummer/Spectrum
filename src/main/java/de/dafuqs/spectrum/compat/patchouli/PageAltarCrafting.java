package de.dafuqs.spectrum.compat.patchouli;

import com.mojang.blaze3d.systems.RenderSystem;
import de.dafuqs.spectrum.SpectrumCommon;
import de.dafuqs.spectrum.enums.GemstoneColor;
import de.dafuqs.spectrum.recipe.SpectrumRecipeTypes;
import de.dafuqs.spectrum.recipe.altar.AltarCraftingRecipe;
import de.dafuqs.spectrum.registries.SpectrumItems;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipeRegistry;

public class PageAltarCrafting extends PageDoubleRecipeRegistry<AltarCraftingRecipe> {

	private static final Identifier BACKGROUND_TEXTURE = new Identifier(SpectrumCommon.MOD_ID, "textures/gui/patchouli/altar_crafting.png");

	public PageAltarCrafting() {
		super(SpectrumRecipeTypes.ALTAR);
	}

	@Override
	protected ItemStack getRecipeOutput(AltarCraftingRecipe recipe) {
		if (recipe == null) {
			return ItemStack.EMPTY;
		} else {
			return recipe.getOutput();
		}
	}

	@Override
	protected void drawRecipe(MatrixStack ms, AltarCraftingRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second) {
		RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
		RenderSystem.enableBlend();
		DrawableHelper.drawTexture(ms, recipeX - 2, recipeY - 2, 0, 0, 104, 97, 128, 256);

		parent.drawCenteredStringNoShadow(ms, getTitle(second).asOrderedText(), GuiBook.PAGE_WIDTH / 2, recipeY - 10, book.headerColor);
		parent.renderItemStack(ms, recipeX + 78, recipeY + 22, mouseX, mouseY, recipe.getOutput());

		int h = 0;
		for(GemstoneColor color : GemstoneColor.values()) {
			int amount = recipe.getGemstoneDustInputs().get(color);
			if(amount > 0) {
				parent.renderItemStack(ms, recipeX + 3 + h * 19, recipeY + 72, mouseX, mouseY, new ItemStack(SpectrumItems.getGemstoneShard(color), amount));
			}
			h++;
		}

		DefaultedList<Ingredient> ingredients = recipe.getIngredients();
		int wrap = recipe.getWidth();

		for (int i = 0; i < ingredients.size(); i++) {
			parent.renderIngredient(ms, recipeX + (i % wrap) * 19 + 3, recipeY + (i / wrap) * 19 + 3, mouseX, mouseY, ingredients.get(i));
		}

		parent.renderItemStack(ms, recipeX + 78, recipeY + 41, mouseX, mouseY, recipe.createIcon());
	}

	@Override
	protected int getRecipeHeight() {
		return 108;
	}

}