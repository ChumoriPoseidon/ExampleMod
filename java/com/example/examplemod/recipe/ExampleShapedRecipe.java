package com.example.examplemod.recipe;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.ExampleMod.RegistryEvents;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ExampleShapedRecipe implements IRecipe<CraftingInventory>, net.minecraftforge.common.crafting.IShapedRecipe<CraftingInventory> {

	static int MAX_WIDTH = 3;
	static int MAX_HEIGHT = 3;

	private final int recipeWidth;
	private final int recipeHeight;
	private final NonNullList<Ingredient> recipeItems;
	private final ItemStack recipeOutput;
	private final ResourceLocation id;
	private final String group;

	public ExampleShapedRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemIn, ItemStack recipeOutputIn) {
		this.id = idIn;
		this.group = groupIn;
		this.recipeWidth = recipeWidthIn;
		this.recipeHeight = recipeHeightIn;
		this.recipeItems = recipeItemIn;
		this.recipeOutput = recipeOutputIn;
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
//		System.out.print("matches(): ");
		for(int i = 0; i <= inv.getWidth() - this.recipeWidth; i++) {
			for(int j = 0; j <= inv.getHeight() - this.recipeHeight; j++) {
				if(this.checkMatch(inv, i, j, true)) {
//					System.out.println("True 1");
					return true;
				}
				if(this.checkMatch(inv, i, j, false)) {
//					System.out.println("True 2");
					return true;
				}
			}
		}
//		System.out.println("False");
		return false;
	}

	private boolean checkMatch(CraftingInventory inventory, int width, int height, boolean flag) {
		for(int i = 0; i < inventory.getWidth(); i++) {
			for(int j = 0; j < inventory.getHeight(); j++) {
				int k = i - width;
				int l = j - height;
				Ingredient ingredient = Ingredient.EMPTY;
				if(k >= 0 && l >= 0 && k < this.recipeWidth && l < this.recipeHeight) {
					if(flag) {
						ingredient = this.recipeItems.get(this.recipeWidth - k - 1 + l * this.recipeWidth);
					}
					else {
						ingredient = this.recipeItems.get(k + l * this.recipeWidth);
					}
				}
				if(!ingredient.test(inventory.getStackInSlot(i + j * inventory.getWidth()))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		return this.getRecipeOutput().copy();
	}

	@Override
	public boolean canFit(int width, int height) {
		return width >= this.recipeWidth && height >= this.recipeHeight;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return this.recipeOutput;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return RegistryEvents.EXAMPLE_CRAFTING_SHAPED_RECIPE;
	}

	@Override
	public IRecipeType<?> getType() {
		return RegistryEvents.EXAMPLE_CRAFTING_SHAPED_RECIPE_TYPE;
	}

	@Override
	public int getRecipeWidth() {
		return this.recipeWidth;
	}

	@Override
	public int getRecipeHeight() {
		return this.recipeHeight;
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return this.recipeItems;
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ExampleShapedRecipe> {

		private static final ResourceLocation NAME = new ResourceLocation(ExampleMod.MODID, "example_crafting_shaped");

		@Override
		public ExampleShapedRecipe read(ResourceLocation recipeId, JsonObject json) {
//			System.out.println("READ JSON");
			String group = JSONUtils.getString(json, "group", "");
			Map<String, Ingredient> map = ExampleShapedRecipe.deserializeKey(JSONUtils.getJsonObject(json, "key"));
			String[] pattern = ExampleShapedRecipe.shrink(ExampleShapedRecipe.patternFromJson(JSONUtils.getJsonArray(json, "pattern")));
			int i = pattern[0].length();
			int j = pattern.length;
//			System.out.println("Pattern Height: " + pattern.length);
//			for(int height = 0; height < pattern.length; height++) {
//				System.out.println("Pattern Width[" + height + "]: " + pattern[height].length());
//				System.out.println("\t" + pattern[height]);
//			}
			NonNullList<Ingredient> ingredients = ExampleShapedRecipe.deserializeIngredients(pattern, map, i, j);
			ItemStack result = ExampleShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
//			System.out.println("Complete construst recipe from json.");
			ExampleShapedRecipe recipe = new ExampleShapedRecipe(recipeId, group, i, j, ingredients, result);
//			{
//				System.out.println("Check Recipe");
//				String info = "\n";
//				info += "Group: " + recipe.getGroup() + "\n";
//				info += "Size: " + recipe.getRecipeWidth() + " x " + recipe.getRecipeHeight() + "\n";
//				info += "Ingredient(size: " + recipe.getIngredients().size() + "):\n";
//				for(Ingredient ingredient : recipe.getIngredients()) {
//					info += "\t-" + Arrays.toString(ingredient.getMatchingStacks()) + "\n";
//				}
//				info += "Result: " + recipe.getRecipeOutput().toString();
//				System.out.println(info);
//			}
			return new ExampleShapedRecipe(recipeId, group, i, j, ingredients, result);
		}

		@Override
		public ExampleShapedRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			int i = buffer.readVarInt();
			int j = buffer.readVarInt();
			String str = buffer.readString(32767);
			NonNullList<Ingredient> list = NonNullList.withSize(i * j, Ingredient.EMPTY);
			for(int k = 0; k < list.size(); k++) {
				list.set(k, Ingredient.read(buffer));
			}
			ItemStack stack = buffer.readItemStack();
			return new ExampleShapedRecipe(recipeId, str, i, j, list, stack);
		}

		@Override
		public void write(PacketBuffer buffer, ExampleShapedRecipe recipe) {
			buffer.writeVarInt(recipe.recipeWidth);
			buffer.writeVarInt(recipe.recipeHeight);
			buffer.writeString(recipe.group);
			for(Ingredient ingredient : recipe.recipeItems) {
				ingredient.write(buffer);
			}
			buffer.writeItemStack(recipe.recipeOutput);
		}
	}

	public static Map<String, Ingredient> deserializeKey(JsonObject jsonObject) {
		Map<String, Ingredient> map = Maps.newHashMap();
		for(Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			if(entry.getKey().length() != 1) {
				throw new JsonSyntaxException("Invalid key entry: '" + (String)entry.getKey() + "' is an invalid symbol (must be 1 character only).");
			}
			if(" ".equals(entry.getKey())) {
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
			}
//			System.out.println(jsonObject.toString());
			map.put(entry.getKey(), Ingredient.deserialize(entry.getValue()));
		}
		map.put(" ", Ingredient.EMPTY);
		return map;
	}

	public static String[] shrink(String... patternFromJson) {
		int max = Integer.MAX_VALUE;
		int min = 0;
		int height = 0;
		int width = 0;
		for(int i = 0; i < patternFromJson.length; i++) {
			String line = patternFromJson[i];
			max = Math.min(max, firstNonSpace(line));
			int last = lastNonSpace(line);
			min = Math.max(min, last);
			if(last < 0) {
				if(height == i) {
					height++;
				}
				width++;
			}
			else {
				width = 0;
			}
		}
		if(patternFromJson.length == width) {
			return new String[0];
		}
		else {
			String[] pattern = new String[patternFromJson.length - width - height];
			for(int j = 0; j < pattern.length; j++) {
				pattern[j] = patternFromJson[j + height].substring(max, min + 1);
			}
			return pattern;
		}
	}

	private static int firstNonSpace(String line) {
		int i;
		for(i = 0; i < line.length() && line.charAt(i) == ' '; i++) {
			;
		}
		return i;
	}

	private static int lastNonSpace(String line) {
		int i;
		for(i = line.length() - 1; i >= 0 && line.charAt(i) == ' '; i++) {
			;
		}
		return i;
	}

	public static String[] patternFromJson(JsonArray jsonArray) {
		String[] pattern = new String[jsonArray.size()];
		if(pattern.length > MAX_HEIGHT) {
			throw new JsonSyntaxException("Invalid pattern: too many rows, " + MAX_HEIGHT + " is maximum");
		}
		else if(pattern.length == 0) {
			throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
		}
		else {
			for(int i = 0; i < pattern.length; i++) {
				String line = JSONUtils.getString(jsonArray.get(i), "pattern[" + i + "]");
				if(line.length() > MAX_WIDTH) {
					throw new JsonSyntaxException("Invalid pattern: too many columns, " + MAX_WIDTH + " is maximum");
				}
				if(i > 0 && pattern[0].length() != line.length()) {
					throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
				}
				pattern[i] = line;
			}
			return pattern;
		}
	}

	public static NonNullList<Ingredient> deserializeIngredients(String[] pattern, Map<String, Ingredient> keys, int width, int height) {
		NonNullList<Ingredient> ingredientsList = NonNullList.withSize(width * height, Ingredient.EMPTY);
		Set<String> set = Sets.newHashSet(keys.keySet());
		set.remove(" ");
		for(int i = 0; i < pattern.length; i++) {
			for(int j = 0; j < pattern[i].length(); j++) {
				String str = pattern[i].substring(j, j + 1);
				Ingredient ingredient = keys.get(str);
				if(ingredient == null) {
					throw new JsonSyntaxException("Pattern references symbol '" + str + "' but it's not defined in the key");
				}
				set.remove(str);
//				System.out.println("i:" + i + ", j: " + j + ", ingre: " + Arrays.toString(ingredient.getMatchingStacks()));
				ingredientsList.set(j + width * i, ingredient);
			}
		}
		if(!set.isEmpty()) {
			throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
		}
		else {
			return ingredientsList;
		}
	}

	public static ItemStack deserializeItem(JsonObject jsonObject) {
		String str = JSONUtils.getString(jsonObject, "item");
		Item item = Registry.ITEM.getValue(new ResourceLocation(str)).orElseThrow(() -> {
			return new JsonSyntaxException("Unknown item '" + str + "'");
		});
		if(jsonObject.has("data")) {
			throw new JsonParseException("Disallowed data tag found");
		}
		else {
			int count = JSONUtils.getInt(jsonObject, "count", 1);
			return CraftingHelper.getItemStack(jsonObject, true);
		}
	}
}
