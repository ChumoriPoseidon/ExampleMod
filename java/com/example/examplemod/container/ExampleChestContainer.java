package com.example.examplemod.container;

import com.example.examplemod.ExampleMod.RegistryEvents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ExampleChestContainer extends Container {

	private final IInventory chest;

	public ExampleChestContainer(int id, PlayerInventory playerInventory) {
		this(id, playerInventory, new Inventory(27));
	}

	public ExampleChestContainer(int id, PlayerInventory playerInventory, IInventory inventory) {
		super(RegistryEvents.EXAMPLE_CHEST_CONTAINER, id);
		assertInventorySize(inventory, 27);
		this.chest = inventory;

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inventory, j + i * 9, 8 + j * 18, 18 + i * 18));
			}
		}
		for(int k = 0; k < 3; k++) {
			for(int l = 0; l < 9; l++) {
				this.addSlot(new Slot(playerInventory, l + k * 9 + 9, 8 + l * 18, 85 + k * 18));
			}
		}
		for(int m = 0; m < 9; m++) {
			this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 143));
		}
		inventory.openInventory(playerInventory.player);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return this.chest.isUsableByPlayer(playerIn);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if(slot != null && slot.getHasStack()) {
			ItemStack itemStack = slot.getStack();
			stack = itemStack.copy();
			if(index < 27) {
				if(!this.mergeItemStack(itemStack, 27, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			}
			else {
				if(!this.mergeItemStack(itemStack, 0, 27, false)) {
					return ItemStack.EMPTY;
				}
			}

			if(itemStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			}
			else {
				slot.onSlotChanged();
			}
		}
		return stack;
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		this.chest.closeInventory(playerIn);
	}
}
