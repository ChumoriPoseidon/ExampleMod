package com.example.examplemod.recipe;

import java.util.Arrays;

import com.example.examplemod.ExampleMod.RegistryEvents;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ExampleDecomposerRecipe implements IRecipe<IInventory> {

	protected final ResourceLocation id;
//	protected final String group;
	protected final Ingredient ingredient;
	protected final ItemStack resultMain;
	protected final ItemStack resultSub;
	protected final float experience;
	protected final int cookTime;

	public ExampleDecomposerRecipe(ResourceLocation idIn, Ingredient ingredientIn, ItemStack resultMainIn, ItemStack resultSubIn, float experienceIn, int cookTimeIn) {
		this.id = idIn;
		this.ingredient = ingredientIn;
		this.resultMain = resultMainIn;
		this.resultSub = resultSubIn;
		this.experience = experienceIn;
		this.cookTime = cookTimeIn;
	}

	@Override
	public boolean matches(IInventory inv, World worldIn) {
		return this.ingredient.test(inv.getStackInSlot(0));
	}

	@Override
	public ItemStack getCraftingResult(IInventory inv) {
		return this.resultMain.copy();
	}

	public ItemStack getCraftingResultSub(IInventory inv) {
		return this.resultSub.copy();
	}

	@Override
	public boolean canFit(int width, int height) {
		return true;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> list = NonNullList.create();
		list.add(this.ingredient);
		return list;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return this.resultMain;
	}

	public ItemStack getRecipeByproduct() {
		return this.resultSub;
	}

	public int getCookTime() {
		return this.cookTime;
	}

	public float getExperience() {
		return this.experience;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return RegistryEvents.EXAMPLE_DECOMPOSER_RECIPE;
	}

	@Override
	public IRecipeType<?> getType() {
		return RegistryEvents.EXAMPLE_DECOMPOSER_RECIPE_TYPE;
	}

	public static class Serializer<T extends ExampleDecomposerRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

		private final Serializer.IFactory<T> factory;
		private final int cookTimeMin;

		public Serializer(Serializer.IFactory<T> factoryIn, int cookTimeMinIn) {
			this.factory = factoryIn;
			this.cookTimeMin = cookTimeMinIn;
		}

		@SuppressWarnings("deprecation")
		@Override
		public T read(ResourceLocation recipeId, JsonObject json) {
			JsonElement jsonIngredient = (JsonElement)(JSONUtils.isJsonArray(json, "ingredient") ? JSONUtils.getJsonArray(json, "ingredient") : JSONUtils.getJsonObject(json, "ingredient"));
			Ingredient ingredient = Ingredient.deserialize(jsonIngredient);
			if(!json.has("result")) {
				throw new JsonSyntaxException("Missing result, expected to find a string or object");
			}
			ItemStack resultMain;
			if(json.get("result").isJsonObject()) {
				resultMain = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
			}
			else {
				String resultName = JSONUtils.getString(json, "result");
				ResourceLocation resultResource = new ResourceLocation(resultName);
				resultMain = new ItemStack(Registry.ITEM.getValue(resultResource).orElseThrow(() -> {
					return new IllegalStateException("Item: " + resultName + " does not exist");
				}));
			}
			ItemStack resultSub = ItemStack.EMPTY;
			if(json.has("byproduct")) {
				if(json.get("byproduct").isJsonObject()) {
					resultSub = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "byproduct"));
				}
				else {
					String resultName = JSONUtils.getString(json, "byproduct");
					ResourceLocation resultResource = new ResourceLocation(resultName);
					resultSub = new ItemStack(Registry.ITEM.getValue(resultResource).orElseThrow(() -> {
						return new IllegalStateException("Item: " + resultName + " does not exist");
					}));
				}
			}
			float experience = JSONUtils.getFloat(json, "experience", 0.0F);
			int cookTime = JSONUtils.getInt(json, "cookingtime", this.cookTimeMin);
			{
				System.out.println("Check Recipe");
				String info = "\n";
				info += "Ingredient: " + Arrays.toString(ingredient.getMatchingStacks()) + "\n";
				info += "Result(Main): " + resultMain.toString() + "\n";
				info += "Result(Sub): " + resultSub.toString() + "\n";
				info += "Cook Time: " + cookTime;
				System.out.println(info);
			}
			return this.factory.create(recipeId, ingredient, resultMain, resultSub, experience, cookTime);
		}

		@Override
		public T read(ResourceLocation recipeId, PacketBuffer buffer) {
			Ingredient ingredient = Ingredient.read(buffer);
			ItemStack resultMain = buffer.readItemStack();
			ItemStack resultSub = buffer.readItemStack();
			float experience = buffer.readFloat();
			int cookTime = buffer.readVarInt();
			return this.factory.create(recipeId, ingredient, resultMain, resultSub, experience, cookTime);
		}

		@Override
		public void write(PacketBuffer buffer, T recipe) {
			recipe.ingredient.write(buffer);
			buffer.writeItemStack(recipe.resultMain);
			buffer.writeItemStack(recipe.resultSub);
			buffer.writeFloat(recipe.experience);
			buffer.writeVarInt(recipe.cookTime);
		}

		public interface IFactory<T extends ExampleDecomposerRecipe> {

			T create(ResourceLocation id, Ingredient ingredient, ItemStack resultMain, ItemStack resultSub, float experience, int cookTime);
		}
	}
}
