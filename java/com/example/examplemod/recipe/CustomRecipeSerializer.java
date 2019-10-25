package com.example.examplemod.recipe;

import java.util.Arrays;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CustomRecipeSerializer<T extends ExampleProcessorRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

	private final CustomRecipeSerializer.IFactory<T> factory;
	private final int cookTime;

	public CustomRecipeSerializer(CustomRecipeSerializer.IFactory<T> factoryIn, int cookTimeIn) {
		this.factory = factoryIn;
		this.cookTime = cookTimeIn;
	}

	@Override
	public T read(ResourceLocation recipeId, JsonObject json) {
		String group = JSONUtils.getString(json, "group", "");
		JsonElement jsonIngredient = (JsonElement)(JSONUtils.isJsonArray(json, "ingredient") ? JSONUtils.getJsonArray(json, "ingredient") : JSONUtils.getJsonObject(json, "ingredient"));
		Ingredient ingredient = Ingredient.deserialize(jsonIngredient);
		if(!json.has("result")) {
			throw new JsonSyntaxException("Missing result, expected to find a string or object");
		}
		ItemStack result;
		if(json.get("result").isJsonObject()) {
			result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
		}
		else {
			String resultName = JSONUtils.getString(json, "result");
			ResourceLocation resultResource = new ResourceLocation(resultName);
			result = new ItemStack(Registry.ITEM.getValue(resultResource).orElseThrow(() -> {
				return new IllegalStateException("Item: " + resultName + " does not exist");
			}));
		}
//		float experience = JSONUtils.getFloat(json, "experience", 0.0F);
		int cookTime = JSONUtils.getInt(json, "cookingtime", 200);
		{
			System.out.println("Check Recipe");
			String info = "\n";
			info += "Group: " + group + "\n";
			info += "Ingredient: " + Arrays.toString(ingredient.getMatchingStacks()) + "\n";
			info += "Result: " + result.toString() + "\n";
			info += "Cook Time: " + cookTime;
			System.out.println(info);
		}
		return this.factory.create(recipeId, group, ingredient, result, cookTime);
	}

	@Override
	public T read(ResourceLocation recipeId, PacketBuffer buffer) {
		String group = buffer.readString(32767);
		Ingredient ingredient = Ingredient.read(buffer);
		ItemStack result = buffer.readItemStack();
//		float experience = buffer.readFloat();
		int cookTime = buffer.readVarInt();
		return this.factory.create(recipeId, group, ingredient, result, cookTime);
	}

	@Override
	public void write(PacketBuffer buffer, T recipe) {
		buffer.writeString(recipe.group);
		recipe.ingredient.write(buffer);
		buffer.writeItemStack(recipe.result);
//		buffer.writeFloat(recipe.experience);
		buffer.writeVarInt(recipe.cookTime);
	}

	public interface IFactory<T extends ExampleProcessorRecipe> {
		T create(ResourceLocation id, String group, Ingredient ingredient, ItemStack result, int cookTimeIn);
	}
}
