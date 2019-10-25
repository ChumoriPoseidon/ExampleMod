package com.example.examplemod.tileentity;

import java.util.Map;

import javax.annotation.Nullable;

import com.example.examplemod.ExampleMod.RegistryEvents;
import com.example.examplemod.block.ExampleProcessor;
import com.example.examplemod.container.ExampleProcessorContainer;
import com.example.examplemod.recipe.ExampleProcessorRecipe;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ExampleProcessorTileEntity extends TileEntity implements ISidedInventory, ITickableTileEntity, INamedContainerProvider {

	private static final int[] SLOTS_UP = new int[]{0};
	private static final int[] SLOTS_DOWN = new int[]{2, 1};
	private static final int[] SLOTS_HORIZONTAL = new int[]{1};

	protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

	private int burnTime;
//	private int recipesUsed;
	private int cookTime;
	private int cookTimeTotal;

	protected final IIntArray furnaceData = new IIntArray() {

		@Override
		public int get(int index) {
			switch(index) {
			case 0:
				return ExampleProcessorTileEntity.this.burnTime;
			case 1:
				return ExampleProcessorTileEntity.this.cookTime;
			case 2:
				return ExampleProcessorTileEntity.this.cookTimeTotal;
			default:
				return 0;
			}
		}

		@Override
		public void set(int index, int value) {
			switch(index) {
			case 0:
				ExampleProcessorTileEntity.this.burnTime = value;
				break;
			case 1:
				ExampleProcessorTileEntity.this.cookTime = value;
				break;
			case 2:
				ExampleProcessorTileEntity.this.cookTimeTotal = value;
			}
		}

		@Override
		public int size() {
			return 3;
		}
	};

	public static Map<Item, Integer> getBurnTimes(){
		Map<Item, Integer> map = Maps.newLinkedHashMap();
		addItemTagBurnTime(map, ItemTags.SAPLINGS, 800);
		addItemTagBurnTime(map, ItemTags.LEAVES, 200);
		addItemBurnTime(map, RegistryEvents.EXAMPLE_ITEM, 400);
		addItemBurnTime(map, RegistryEvents.EXAMPLE_BLOCK, 4000);
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

	// Initialization
	public ExampleProcessorTileEntity() {
		super(RegistryEvents.EXAMPLE_PROCESSOR_TILEENTITY);
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

	// [NEED ADJUST] > adjusted
	private int initCookTimeTotal() {
		return this.world.getRecipeManager().getRecipe(RegistryEvents.EXAMPLE_PROCESSOR_RECIPE_TYPE, this, this.world).map(ExampleProcessorRecipe::getCookTime).orElse(200);
//		return this.world.getRecipeManager().getRecipe(IRecipeType.SMELTING, this, this.world).map(AbstractCookingRecipe::getCookTime).orElse(200);
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
//		return new ExampleProcessorContainer(type, this.world, this.pos, inventory, player);
		return new ExampleProcessorContainer(id, inventory, this, this.furnaceData);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("container.example_block_processor");
	}

	@Override
	public void tick() {
//		System.out.println("tick()");
		boolean flag = this.isBurning();
		boolean update = false;
		if(this.isBurning()) {
			this.burnTime--;
		}
		if(!this.world.isRemote) {
//			System.out.println("check [!world.isRemote()]");
			ItemStack stack = this.items.get(1);
			if(this.isBurning() || !stack.isEmpty() && !this.items.get(0).isEmpty()) {
//				System.out.println("chech [isBurning() || stack.isEmpty() && !items.get(0).isEmpty()]");
				IRecipe<?> recipe = this.world.getRecipeManager().getRecipe(RegistryEvents.EXAMPLE_PROCESSOR_RECIPE_TYPE, this, this.world).orElse(null);
				if(!this.isBurning() && this.canSmelt(recipe)) {
//					System.out.println("check [isBurning() && canSmelt()]");
					this.burnTime = this.initBurnTime(stack);
					if(this.isBurning()) {
//						System.out.println("chech [isBurning()]");
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
//					System.out.println("check [isBurning() && canSmelt()]");
					this.cookTime++;
					if(this.cookTime >= this.cookTimeTotal) {
						this.cookTime = 0;
						this.cookTimeTotal = this.initCookTimeTotal();
						this.smelt(recipe);
						update = true;
					}
				}
				else {
//					System.out.println("check [else]");
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
			ItemStack result = recipe.getRecipeOutput();
			if(result.isEmpty()) {
				return false;
			}
			else {
				ItemStack stack = this.items.get(2);
				if(stack.isEmpty()) {
					return true;
				}
				else if(!stack.isItemEqual(result)) {
					return false;
				}
				else if(stack.getCount() + result.getCount() <= this.getInventoryStackLimit() && stack.getCount() + result.getCount() <= stack.getMaxStackSize()) {
					return true;
				}
				else {
					return stack.getCount() + result.getCount() <= stack.getMaxStackSize();
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

	private void smelt(IRecipe<?> recipe) {
		if(recipe != null && this.canSmelt(recipe)) {
			ItemStack ingredient = this.items.get(0);
			ItemStack result = recipe.getRecipeOutput();
			ItemStack stack = this.items.get(2);
			if(stack.isEmpty()) {
				this.items.set(2, result.copy());
			}
			else {
				if(stack.isItemEqual(result)) {
					stack.grow(result.getCount());
				}
			}
			ingredient.shrink(1);
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
		if(index == 2) {
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
		this.cookTime = compound.getInt("CookTime");
		this.cookTimeTotal = compound.getInt("CookTimeTotal");
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("BurnTime", this.burnTime);
		compound.putInt("CookTime", this.cookTime);
		compound.putInt("CookTimeTotal", this.cookTimeTotal);
		ItemStackHelper.saveAllItems(compound, this.items);
		return compound;
	}
}
