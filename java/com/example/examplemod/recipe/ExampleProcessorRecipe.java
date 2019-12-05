package com.example.examplemod.recipe;

import com.example.examplemod.ExampleMod.RegistryEvents;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ExampleProcessorRecipe implements IRecipe<IInventory> {
//public class ExampleProcessorRecipe extends SingleItemRecipe {

	protected final ResourceLocation id;
	protected final String group;
	protected final Ingredient ingredient;
	protected final ItemStack result;
	protected final float experience;
	protected final int cookTime;

	public ExampleProcessorRecipe(ResourceLocation idIn, String groupIn, Ingredient ingredientIn, ItemStack resultIn, float experienceIn, int cookTimeIn) {
//		super(RegistryEvents.EXAMPLE_PROCESSOR_RECIPE_TYPE, RegistryEvents.EXAMPLE_PROCESSOR_RECIPE, idIn, groupIn, ingredientIn, resultIn);
		this.id = idIn;
		this.group = groupIn;
		this.ingredient = ingredientIn;
		this.result = resultIn;
		this.experience = experienceIn;
		this.cookTime = cookTimeIn;
	}

	@Override
	public boolean matches(IInventory inv, World worldIn) {
		return this.ingredient.test(inv.getStackInSlot(0));
	}

	@Override
	public ItemStack getCraftingResult(IInventory inv) {
		return this.result.copy();
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
	public String getGroup() {
		return this.group;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> list = NonNullList.create();
		list.add(this.ingredient);
		return list;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return this.result;
	}

	public float getExperience() {
	return this.experience;
}

	public int getCookTime() {
		return this.cookTime;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return RegistryEvents.EXAMPLE_PROCESSOR_RECIPE;
	}

	@Override
	public IRecipeType<?> getType() {
		return RegistryEvents.EXAMPLE_PROCESSOR_RECIPE_TYPE;
	}

//	public static class Serializer<T extends ExampleProcessorRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
//
//		final IFactory<T> factory;
//
//		protected Serializer(IFactory<T> factoryIn) {
//			this.factory = factoryIn;
//		}
//
//		@SuppressWarnings("deprecation")
//		@Override
//		public T read(ResourceLocation recipeId, JsonObject json) {
//			String group = JSONUtils.getString(json, "group");
//			JsonElement jsonIngredient = (JsonElement)(JSONUtils.isJsonArray(json, "ingredient") ? JSONUtils.getJsonArray(json, "ingredient") : JSONUtils.getJsonObject(json, "ingredient"));
//			Ingredient ingredient = Ingredient.deserialize(jsonIngredient);
//			if(!json.has("result")) {
//				throw new JsonSyntaxException("Missing result, expected to find a string or object");
//			}
//			ItemStack result;
//			if(json.get("result").isJsonObject()) {
//				result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
//			}
//			else {
//				String resultName = JSONUtils.getString(json, "result");
//				ResourceLocation resultResource = new ResourceLocation(resultName);
//				result = new ItemStack(Registry.ITEM.getValue(resultResource).orElseThrow(() -> {
//					return new IllegalStateException("Item: " + resultName + " does not exist");
//				}));
//			}
////			float experience = JSONUtils.getFloat(json, "experience", 0.0F);
////			int cookTime = JSONUtils.getInt(json, "cookingtime", 200);
//			return this.factory.create(recipeId, group, ingredient, result);
//		}
//
//		@Override
//		public T read(ResourceLocation recipeId, PacketBuffer buffer) {
//			String group = buffer.readString(32767);
//			Ingredient ingredient = Ingredient.read(buffer);
//			ItemStack result = buffer.readItemStack();
////			float experience = buffer.readFloat();
////			int cookTime = buffer.readVarInt();
//			return this.factory.create(recipeId, group, ingredient, result);
//		}
//
//		@Override
//		public void write(PacketBuffer buffer, T recipe) {
//			buffer.writeString(recipe.group);
//			recipe.ingredient.write(buffer);
//			buffer.writeItemStack(recipe.result);
////			buffer.writeFloat(recipe.experience);
////			buffer.writeVarInt(recipe.cookTime);
//		}
//
//		public interface IFactory<T extends ExampleProcessorRecipe> {
//			T create(ResourceLocation id, String group, Ingredient ingredient, ItemStack result);
//		}
//	}
}
