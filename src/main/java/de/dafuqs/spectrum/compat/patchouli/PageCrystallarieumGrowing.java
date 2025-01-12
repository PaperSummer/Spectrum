package de.dafuqs.spectrum.compat.patchouli;

import com.mojang.blaze3d.systems.RenderSystem;
import de.dafuqs.spectrum.SpectrumCommon;
import de.dafuqs.spectrum.recipe.SpectrumRecipeTypes;
import de.dafuqs.spectrum.recipe.crystallarieum.CrystallarieumRecipe;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipeRegistry;

public class PageCrystallarieumGrowing extends PageDoubleRecipeRegistry<CrystallarieumRecipe> {
	
	private static final Identifier BACKGROUND_TEXTURE = new Identifier(SpectrumCommon.MOD_ID, "textures/gui/patchouli/crystallarieum.png");
	
	public PageCrystallarieumGrowing() {
		super(SpectrumRecipeTypes.CRYSTALLARIEUM);
	}
	
	@Override
	protected ItemStack getRecipeOutput(CrystallarieumRecipe recipe) {
		if (recipe == null) {
			return ItemStack.EMPTY;
		} else {
			return recipe.getOutput();
		}
	}
	
	@Override
	protected void drawRecipe(MatrixStack ms, @NotNull CrystallarieumRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second) {
		RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
		RenderSystem.enableBlend();
		DrawableHelper.drawTexture(ms, recipeX - 2, recipeY - 2, 0, 0, 104, 97, 128, 256);
		
		parent.drawCenteredStringNoShadow(ms, getTitle(second).asOrderedText(), GuiBook.PAGE_WIDTH / 2, recipeY - 10, book.headerColor);
		
		// the ingredient
		Ingredient ingredient = recipe.getIngredientStack();
		parent.renderIngredient(ms, recipeX + 3, recipeY + 8, mouseX, mouseY, ingredient); // left
		
		// the catalysts
		// TODO
		
		// crystallarieum
		parent.renderItemStack(ms, recipeX + 23, recipeY + 25, mouseX, mouseY, recipe.createIcon());
		
		// the output
		parent.renderItemStack(ms, recipeX + 79, recipeY + 8, mouseX, mouseY, recipe.getOutput());
	}
	
	@Override
	protected int getRecipeHeight() {
		return 58;
	}
	
}