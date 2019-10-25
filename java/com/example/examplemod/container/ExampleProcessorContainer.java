package com.example.examplemod.container;

import com.example.examplemod.ExampleMod.RegistryEvents;
import com.example.examplemod.tileentity.ExampleProcessorTileEntity;

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

public class ExampleProcessorContainer extends Container {

	private final IInventory furnaceIvnentory;
	private final IIntArray furnaceData;
	protected final World world;

	public ExampleProcessorContainer(int id, PlayerInventory playerInventory) {
		this(id, playerInventory, new Inventory(3), new IntArray(3));
	}

	public ExampleProcessorContainer(int id, PlayerInventory playerInventory, IInventory inventory, IIntArray data) {
		super(RegistryEvents.EXAMPLE_PROCESSOR_CONTAINER, id);
		assertInventorySize(inventory, 3);
		assertIntArraySize(data, 3);
		this.furnaceIvnentory = inventory;
		this.furnaceData = data;
		this.world = playerInventory.player.world;

		this.addSlot(new Slot(inventory, 0, 56, 17));
		this.addSlot(new ExampleProcessorFuelSlot(this, inventory, 1, 56, 53));
		this.addSlot(new ExampleProcessorResultSlot(playerInventory.player, inventory, 2, 116, 35));

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
		return this.furnaceIvnentory.isUsableByPlayer(playerIn);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if(slot != null && slot.getHasStack()) {
			ItemStack itemStack = slot.getStack();
			stack = itemStack.copy();
			if(index == 2) {
				if(!this.mergeItemStack(itemStack, 3, 39, true)) {
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
				else if(index >= 3 && index < 30) {
					if(!this.mergeItemStack(itemStack, 30, 39, false)) {
						return ItemStack.EMPTY;
					}
				}
				else if(index >= 30 && index < 39) {
					if(!this.mergeItemStack(itemStack, 3, 30, false)) {
						return ItemStack.EMPTY;
					}
				}
			}
			else if(!this.mergeItemStack(itemStack, 3, 39, false)) {
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
		return this.world.getRecipeManager().getRecipe(RegistryEvents.EXAMPLE_PROCESSOR_RECIPE_TYPE, new Inventory(stack), this.world).isPresent();
	}

	public boolean isFuel(ItemStack stack) {
		return ExampleProcessorTileEntity.isFuel(stack);
	}

	// for GUI
	@OnlyIn(Dist.CLIENT)
	public int getCookProgressionScaled() {
		int i = this.furnaceData.get(1);
		int j = this.furnaceData.get(2);
		return j != 0 && i != 0 ? i * 24 / j : 0;
	}

	// [NEED ADJUST]
	@OnlyIn(Dist.CLIENT)
	public int getBurnLeftScaled() {
		int i = 200;
		return this.furnaceData.get(0) * 13 / i;
	}

	@OnlyIn(Dist.CLIENT)
	public boolean isBurning() {
		return this.furnaceData.get(0) > 0;
	}
}
