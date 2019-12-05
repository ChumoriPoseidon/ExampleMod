package com.example.examplemod.container;

import com.example.examplemod.ExampleMod.RegistryEvents;
import com.example.examplemod.tileentity.ExampleDecomposerTileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ExampleDecomposerContainer extends Container {

	private final IInventory inventory;
	private final IIntArray data;
	protected final World world;

	public ExampleDecomposerContainer(int id, PlayerInventory playerInventory) {
		this(id, playerInventory, new Inventory(4), new IntArray(4));
	}

	public ExampleDecomposerContainer(int id, PlayerInventory playerInventory, IInventory inventoryIn, IIntArray dataIn) {
		super(RegistryEvents.EXAMPLE_DECOMPOSER_CONTAINER, id);
		assertInventorySize(inventoryIn, 4);
		assertIntArraySize(dataIn, 4);
		this.inventory = inventoryIn;
		this.data = dataIn;
		this.world = playerInventory.player.world;

		this.addSlot(new Slot(inventory, 0, 56, 17));
		this.addSlot(new ExampleDecomposerFuelSlot(this, inventory, 1, 56, 53));
		this.addSlot(new ExampleDecomposerResultSlot(playerInventory.player, inventory, 2, 116, 35));
		this.addSlot(new ExampleDecomposerResultSlot(playerInventory.player, inventory, 3, 142, 35));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		for(int k = 0; k < 9; k++) {
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
		}

		this.trackIntArray(data);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return this.inventory.isUsableByPlayer(playerIn);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if(slot != null && slot.getHasStack()) {
			ItemStack itemStack = slot.getStack();
			stack = itemStack.copy();
			if(index == 2 || index == 3) {
				if(!this.mergeItemStack(itemStack, 4, 40, true)) {
					return ItemStack.EMPTY;
				}
				slot.onSlotChange(itemStack, stack);
			}
			else if(index != 1 && index != 0){
				if(this.canSmelt(itemStack)) {
					if(!this.mergeItemStack(itemStack, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				}
				else if(this.isFuel(itemStack)) {
					if(!this.mergeItemStack(itemStack, 1, 2, false)) {
						return ItemStack.EMPTY;
					}
				}
				else if(index >= 4 && index < 31) {
					if(!this.mergeItemStack(itemStack, 31, 40, false)) {
						return ItemStack.EMPTY;
					}
				}
				else if(index >= 31 && index < 40) {
					if(!this.mergeItemStack(itemStack, 4, 31, false)) {
						return ItemStack.EMPTY;
					}
				}
			}
			else if(!this.mergeItemStack(itemStack, 4, 40, false)) {
				return ItemStack.EMPTY;
			}

			if(itemStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			}
			else {
				slot.onSlotChanged();
			}

			if(itemStack.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemStack);
		}
		return stack;
	}

	private boolean canSmelt(ItemStack stack) {
		return this.world.getRecipeManager().getRecipe(RegistryEvents.EXAMPLE_DECOMPOSER_RECIPE_TYPE, new Inventory(stack), this.world).isPresent();
	}

	public boolean isFuel(ItemStack stack) {
		return ExampleDecomposerTileEntity.isFuel(stack);
	}

	// for GUI
	@OnlyIn(Dist.CLIENT)
	public int getCookProgressionScaled() {
		int i = this.data.get(2);
		int j = this.data.get(3);
		return j != 0 && i != 0 ? i * 24 / j : 0;
	}

	@OnlyIn(Dist.CLIENT)
	public int getBurnLeftScaled() {
		int i = this.data.get(1);
		if(i == 0) {
			i = 200;
		}
		return this.data.get(0) * 13 / i;
	}

	@OnlyIn(Dist.CLIENT)
	public boolean isBurning() {
		return this.data.get(0) > 0;
	}
}
