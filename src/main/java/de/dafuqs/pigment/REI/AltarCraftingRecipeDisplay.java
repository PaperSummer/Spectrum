package de.dafuqs.pigment.REI;

import de.dafuqs.pigment.enums.PigmentColor;
import de.dafuqs.pigment.recipe.altar.AltarCraftingRecipe;
import de.dafuqs.pigment.registries.PigmentItems;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomDisplay;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AltarCraftingRecipeDisplay<R extends AltarCraftingRecipe> implements Display {

	protected List<EntryIngredient> craftingInputs;
	protected final EntryIngredient output;
	protected final float experience;
	protected final int craftingTime;

	protected final int height;
	protected final int width;

	public AltarCraftingRecipeDisplay(AltarCraftingRecipe recipe) {
		this.craftingInputs = recipe.getCraftingInputs().stream().map(EntryIngredients::ofIngredient).collect(Collectors.toCollection(ArrayList::new));

		// since some recipes use less than 9 ingredients it will be serialized with less than 9 length.
		// => fill up to 9 so everything is in order
		/*while(craftingInputs.size() < 9) {
			craftingInputs.add(EntryIngredient.empty());
		}*/

		HashMap<PigmentColor, Integer> pigmentInputs = recipe.getPigmentInputs();
		addPigmentCraftingInput(pigmentInputs, PigmentColor.MAGENTA, PigmentItems.MAGENTA_PIGMENT);
		addPigmentCraftingInput(pigmentInputs, PigmentColor.YELLOW, PigmentItems.YELLOW_PIGMENT);
		addPigmentCraftingInput(pigmentInputs, PigmentColor.CYAN, PigmentItems.CYAN_PIGMENT);
		addPigmentCraftingInput(pigmentInputs, PigmentColor.BLACK, PigmentItems.BLACK_PIGMENT);
		addPigmentCraftingInput(pigmentInputs, PigmentColor.WHITE, PigmentItems.WHITE_PIGMENT);

		this.output = EntryIngredients.of(recipe.getOutput());
		this.experience = recipe.getExperience();
		this.craftingTime = recipe.getCraftingTime();
		this.height = recipe.getHeight();
		this.width = recipe.getWidth();

	}

	private void addPigmentCraftingInput(HashMap<PigmentColor, Integer> pigmentInputs, PigmentColor pigmentColor, Item item) {
		if(pigmentInputs.containsKey(pigmentColor)) {
			int amount = pigmentInputs.get(pigmentColor);
			if(amount > 0) {
				this.craftingInputs.add(EntryIngredients.of(new ItemStack(item, amount)));
			} else {
				this.craftingInputs.add(EntryIngredient.empty());
			}
		} else {
			this.craftingInputs.add(EntryIngredient.empty());
		}
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return craftingInputs;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return Collections.singletonList(output);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return AltarCategory.ID;
	}

}