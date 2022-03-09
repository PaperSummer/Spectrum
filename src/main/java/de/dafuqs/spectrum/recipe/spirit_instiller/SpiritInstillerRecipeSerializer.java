package de.dafuqs.spectrum.recipe.spirit_instiller;

import com.google.gson.JsonObject;
import de.dafuqs.spectrum.blocks.spirit_instiller.SpiritInstillerBlock;
import de.dafuqs.spectrum.recipe.RecipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SpiritInstillerRecipeSerializer implements RecipeSerializer<SpiritInstillerRecipe> {

	public final SpiritInstillerRecipeSerializer.RecipeFactory<SpiritInstillerRecipe> recipeFactory;

	public SpiritInstillerRecipeSerializer(SpiritInstillerRecipeSerializer.RecipeFactory<SpiritInstillerRecipe> recipeFactory) {
		this.recipeFactory = recipeFactory;
	}

	@Override
	public SpiritInstillerRecipe read(Identifier identifier, JsonObject jsonObject) {
		Ingredient ingredient1 = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "ingredient1"));
		Ingredient ingredient2 = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "ingredient2"));
		Ingredient centerIngredient = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "center_ingredient"));
		ItemStack outputItemStack = RecipeUtils.outputWithNbtFromJson(JsonHelper.getObject(jsonObject, "result"));
		
		int craftingTime = JsonHelper.getInt(jsonObject, "time", 200);
		float experience = JsonHelper.getFloat(jsonObject, "experience");
		
		Identifier requiredAdvancementIdentifier;
		if(JsonHelper.hasString(jsonObject, "required_advancement")) {
			requiredAdvancementIdentifier = Identifier.tryParse(JsonHelper.getString(jsonObject, "required_advancement"));
		} else {
			// No unlock advancement set. Will be set to the unlock advancement of the block itself
			requiredAdvancementIdentifier = SpiritInstillerBlock.UNLOCK_IDENTIFIER;
		}
		
		return this.recipeFactory.create(identifier, ingredient1, ingredient2, centerIngredient, outputItemStack, craftingTime, experience, requiredAdvancementIdentifier);
	}
	
	@Override
	public void write(PacketByteBuf packetByteBuf, SpiritInstillerRecipe spiritInstillerRecipe) {
		spiritInstillerRecipe.inputIngredient1.write(packetByteBuf);
		spiritInstillerRecipe.inputIngredient2.write(packetByteBuf);
		spiritInstillerRecipe.centerIngredient.write(packetByteBuf);
		packetByteBuf.writeItemStack(spiritInstillerRecipe.outputItemStack);
		packetByteBuf.writeInt(spiritInstillerRecipe.craftingTime);
		packetByteBuf.writeFloat(spiritInstillerRecipe.experience);
		packetByteBuf.writeIdentifier(spiritInstillerRecipe.requiredAdvancementIdentifier);
	}
	
	@Override
	public SpiritInstillerRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
		Ingredient ingredient1 = Ingredient.fromPacket(packetByteBuf);
		Ingredient ingredient2 = Ingredient.fromPacket(packetByteBuf);
		Ingredient centerIngredient = Ingredient.fromPacket(packetByteBuf);
		ItemStack outputItemStack = packetByteBuf.readItemStack();
		int craftingTime = packetByteBuf.readInt();
		float experience = packetByteBuf.readFloat();
		Identifier requiredAdvancementIdentifier = packetByteBuf.readIdentifier();
		return this.recipeFactory.create(identifier, ingredient1, ingredient2, centerIngredient, outputItemStack, craftingTime, experience, requiredAdvancementIdentifier);
	}

	
	public interface RecipeFactory<SpiritInstillerRecipe> {
		SpiritInstillerRecipe create(Identifier id, Ingredient inputIngredient1, Ingredient inputIngredient2, Ingredient centerIngredient, ItemStack outputItemStack, int craftingTime, float experience, Identifier requiredAdvancementIdentifier);
	}

}