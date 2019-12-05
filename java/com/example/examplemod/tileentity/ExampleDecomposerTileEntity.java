package com.example.examplemod.tileentity;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.example.examplemod.ExampleMod.RegistryEvents;
import com.example.examplemod.block.ExampleProcessor;
import com.example.examplemod.container.ExampleDecomposerContainer;
import com.example.examplemod.recipe.ExampleDecomposerRecipe;
import com.google.common.collect.Maps;

import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ExampleDecomposerTileEntity extends TileEntity implements ISidedInventory, ITickableTileEntity, INamedContainerProvider {

	// 0: input, 1: fuel, 2: result, 3:by-product
	private static final int[] SLOTS_UP = new int[]{0};
	private static final int[] SLOTS_DOWN = new int[]{3, 2, 1};
	private static final int[] SLOTS_HORIZONTAL = new int[]{1};

	protected NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);

	private int burnTime;
	private int burnTimeTotal;
	private int cookTime;
	private int cookTimeTotal;

	private final Map<ResourceLocation, Integer> recipeUsed = Maps.newHashMap();

	protected final IIntArray furnaceData = new IIntArray() {

		@Override
		public int get(int index) {
			switch(index) {
			case 0:
				return ExampleDecomposerTileEntity.this.burnTime;
			case 1:
				return ExampleDecomposerTileEntity.this.burnTimeTotal;
			case 2:
				return ExampleDecomposerTileEntity.this.cookTime;
			case 3:
				return ExampleDecomposerTileEntity.this.cookTimeTotal;
			default:
				return 0;
			}
		}

		@Override
		public void set(int index, int value) {
			switch(index) {
			case 0:
				ExampleDecomposerTileEntity.this.burnTime = value;
				break;
			case 1:
				ExampleDecomposerTileEntity.this.burnTimeTotal = value;
				break;
			case 2:
				ExampleDecomposerTileEntity.this.cookTime = value;
				break;
			case 3:
				ExampleDecomposerTileEntity.this.cookTimeTotal = value;
			}
		}

		@Override
		public int size() {
			return 4;
		}
	};

	public static Map<Item, Integer> getBurnTimes(){
		Map<Item, Integer> map = Maps.newLinkedHashMap();
//		Map<Item, Integer> map = AbstractFurnaceTileEntity.getBurnTimes();
		addItemBurnTime(map, Items.REDSTONE, 100);
		addItemBurnTime(map, Blocks.REDSTONE_BLOCK, 1000);
//		addItemTagBurnTime(map, ItemTags.SAPLINGS, 800);
//		addItemTagBurnTime(map, ItemTags.LEAVES, 200);
//		addItemBurnTime(map, RegistryEvents.EXAMPLE_ITEM, 400);
//		addItemBurnTime(map, RegistryEvents.EXAMPLE_BLOCK, 4000);
		return map;
	}

	private static void addItemTagBurnTime(Map<Item, Integer> map, Tag<Item> tag, int time) {
		for(Item item : tag.getAllElements()) {
			map.put(item, time);
		}
	}

	private static void addItemBurnTime(Map<Item, Integer> map, IItemProvider item, int time) {
		map.put(item.asItem(), time);
	}

	public ExampleDecomposerTileEntity() {
		super(RegistryEvents.EXAMPLE_DECOMPOSER_TILEENTITY);
	}

	@Override
	public int getSizeInventory() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		for(ItemStack stack : this.items) {
			if(!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return this.items.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.items, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(this.items, index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		ItemStack itemStack = this.items.get(index);
		boolean flag = !stack.isEmpty() && stack.isItemEqual(itemStack) && ItemStack.areItemStackTagsEqual(stack, itemStack);
		this.items.set(index, stack);
		if(stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
		if(index == 0 && !flag) {
			this.cookTimeTotal = initCookTimeTotal();
			this.cookTime = 0;
			this.markDirty();
		}
	}

	//[Need Adjusted]
	private int initCookTimeTotal() {
		return this.world.getRecipeManager().getRecipe(RegistryEvents.EXAMPLE_DECOMPOSER_RECIPE_TYPE, this, this.world).map(ExampleDecomposerRecipe::getCookTime).orElse(200);
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		if(this.world.getTileEntity(this.pos) != this) {
			return false;
		}
		else {
			return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}

	@Override
	public void clear() {
		this.items.clear();
	}

	@Override
	public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
		return new ExampleDecomposerContainer(id, inventory, this, this.furnaceData);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("container.example_block_decomposer");
	}

	//[Need Adjusted]
	@Override
	public void tick() {
		boolean flag = this.isBurning();
		boolean update = false;
		if(this.isBurning()) {
			this.burnTime--;
		}
		if(!this.world.isRemote) {
			ItemStack stack = this.items.get(1);
			if(this.isBurning() || !stack.isEmpty() && !this.items.get(0).isEmpty()) {
				IRecipe<?> recipe = this.world.getRecipeManager().getRecipe(RegistryEvents.EXAMPLE_DECOMPOSER_RECIPE_TYPE, this, this.world).orElse(null);
				if(!this.isBurning() && this.canSmelt(recipe)) {
					this.burnTime = this.initBurnTime(stack);
					this.burnTimeTotal = this.burnTime;
					if(this.isBurning()) {
						update = true;
						if(stack.hasContainerItem()) {
							this.items.set(1, stack.getContainerItem());
						}
						else {
							if(!stack.isEmpty()) {
								stack.shrink(1);
								if(stack.isEmpty()) {
									this.items.set(1, stack.getContainerItem());
								}
							}
						}
					}
				}
				if(this.isBurning() && this.canSmelt(recipe)) {
					this.cookTime++;
					if(this.cookTime >= this.cookTimeTotal) {
						this.cookTime = 0;
						this.cookTimeTotal = this.initCookTimeTotal();
						this.smelt(recipe);
						update = true;
					}
				}
				else {
					this.cookTime = 0;
				}
			}
			else {
				if(!this.isBurning() && this.cookTime > 0) {
					this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.cookTimeTotal);
				}
			}
			if(flag != this.isBurning()) {
				update = true;
				this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(ExampleProcessor.LIT, Boolean.valueOf(this.isBurning())), 3);
			}
		}
		if(update) {
			this.markDirty();
		}
	}

	private boolean isBurning() {
		return this.burnTime > 0;
	}

	private boolean canSmelt(@Nullable IRecipe<?> recipe) {
		if(!this.items.get(0).isEmpty() && recipe != null) {
			ItemStack resultMain = recipe.getRecipeOutput();
			ItemStack resultSub = ((ExampleDecomposerRecipe)recipe).getRecipeByproduct();
			if(resultMain.isEmpty()) {
				return false;
			}
			else {
				ItemStack stackMain = this.items.get(2);
				ItemStack stackSub = this.items.get(3);
				//主スロットが空or副産物があり、副スロットが空
				if(stackMain.isEmpty() || (!resultSub.isEmpty() && stackSub.isEmpty())) {
					return true;
				}
				//↓[主スロットにアイテムがあるor両スロットにアイテムがある]
				//どちらかのスロットアイテムと結果アイテムが違う
				else if(!stackMain.isItemEqual(resultMain) || (!resultSub.isEmpty() && !stackSub.isItemEqual(resultSub))) {
					return false;
				}
				//↓[両スロットアイテムが同種]
				//両スロットアイテムとも総数が上限以下
//				else if((stackMain.getCount() + resultMain.getCount() <= this.getInventoryStackLimit() && stackMain.getCount() + resultMain.getCount() <= stackMain.getMaxStackSize())
//						&& (stackSub.getCount() + resultSub.getCount() <= this.getInventoryStackLimit() && stackSub.getCount() + resultSub.getCount() <= stackSub.getMaxStackSize())) {
//					return true;
//				}
				else {
					return (stackMain.getCount() + resultMain.getCount() <= this.getInventoryStackLimit() && stackMain.getCount() + resultMain.getCount() <= stackMain.getMaxStackSize())
							&& (stackSub.getCount() + resultSub.getCount() <= this.getInventoryStackLimit() && stackSub.getCount() + resultSub.getCount() <= stackSub.getMaxStackSize());
//					return (stackMain.getCount() + resultMain.getCount() <= stackMain.getMaxStackSize()) && (stackSub.getCount() + resultSub.getCount() <= stackSub.getMaxStackSize());
				}
			}
		}
		else {
			return false;
		}
	}

	private int initBurnTime(ItemStack stack) {
		if(stack.isEmpty()) {
			return 0;
		}
		else {
			Item item = stack.getItem();
			return getBurnTimes().getOrDefault(item, 0);
		}
	}

	// ↓11/25
	private void smelt(IRecipe<?> recipe) {
		if(recipe != null && this.canSmelt(recipe)) {
			ItemStack ingredient = this.items.get(0);
			ItemStack resultMain = recipe.getRecipeOutput();
			ItemStack resultSub = ((ExampleDecomposerRecipe)recipe).getRecipeByproduct();
			ItemStack stackMain = this.items.get(2);
			ItemStack stackSub = this.items.get(3);
			if(stackMain.isEmpty()) {
				this.items.set(2, resultMain.copy());
			}
			else {
				if(stackMain.isItemEqual(resultMain)) {
					stackMain.grow(resultMain.getCount());
				}
			}
			if(!resultSub.isEmpty()) {
				if(stackSub.isEmpty()) {
					this.items.set(3, resultSub.copy());
				}
				else {
					if(stackSub.isItemEqual(resultSub)) {
						stackSub.grow(resultSub.getCount());
					}
				}
			}
			if(!this.world.isRemote) {
				this.setRecipeUsed(recipe);
			}
			ingredient.shrink(1);
		}
	}

	private void setRecipeUsed(IRecipe<?> recipe) {
		if(recipe != null) {
			this.recipeUsed.compute(recipe.getId(), (resource, amount) -> {
				return 1 + (amount == null ? 0 : amount);
			});
		}
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		if(side == Direction.DOWN) {
			return SLOTS_DOWN;
		}
		else {
			return side == Direction.UP ? SLOTS_UP : SLOTS_HORIZONTAL;
		}
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
		return this.isItemValidForSlot(index, itemStackIn);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
		if(direction == Direction.DOWN && index == 1) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if(index == 2 || index == 3) {
			return false;
		}
		else if(index != 1) {
			return true;
		}
		else {
			return isFuel(stack);
		}
	}

	public static boolean isFuel(ItemStack stack) {
		return getBurnTimes().getOrDefault(stack.getItem(), 0) > 0;
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		this.items = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, this.items);
		this.burnTime = compound.getInt("BurnTime");
		this.burnTimeTotal = this.initBurnTime(this.items.get(1));
		this.cookTime = compound.getInt("CookTime");
		this.cookTimeTotal = compound.getInt("CookTimeTotal");
		for(int i = 0; i < compound.getShort("RecipeSize"); i++) {
			ResourceLocation resource = new ResourceLocation(compound.getString("RecipeResource" + i));
			int amount = compound.getInt("RecipeAmount" + i);
			this.recipeUsed.put(resource, amount);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("BurnTime", this.burnTime);
		compound.putInt("CookTime", this.cookTime);
		compound.putInt("CookTimeTotal", this.cookTimeTotal);
		ItemStackHelper.saveAllItems(compound, this.items);
		int i = 0;
		for(Entry<ResourceLocation, Integer> entry : this.recipeUsed.entrySet()) {
			compound.putString("RecipeResource" + i, entry.getKey().toString());
			compound.putInt("RecipeAmount" + i, entry.getValue());
			i++;
		}
		compound.putShort("RecipeSize", (short)this.recipeUsed.size());
		return compound;
	}

	public void onCrafting(PlayerEntity player) {
		//unlock recipe
		//give experience
		for(Entry<ResourceLocation, Integer> entry : this.recipeUsed.entrySet()) {
			player.world.getRecipeManager().getRecipe(entry.getKey()).ifPresent((recipe) -> {
				this.giveExperience(player, ((ExampleDecomposerRecipe)recipe).getExperience(), entry.getValue());
			});
		}
		this.recipeUsed.clear();
	}

	//各レシピについてやるから、出現するオーブが増えそう
		private void giveExperience(PlayerEntity player, float base, int amount) {
			int total = amount;
			if(base == 0.0F) {
				total = 0;
			}
			else {
				if(base < 1.0F) {
					int i = MathHelper.floor((float)amount * base);
					if(i < MathHelper.ceil((float)amount + base) && Math.random() < (double)((float)amount * base - (float)i)) {
						i++;
					}
					total = i;
				}
			}
			while(total > 0) {
				int split = ExperienceOrbEntity.getXPSplit(total);
				total -= split;
				player.world.addEntity(new ExperienceOrbEntity(player.world, player.posX, player.posY + 0.5D, player.posZ + 0.5D, split));
			}
		}
}
