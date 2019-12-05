package com.example.examplemod.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ExampleDecomposerFuelSlot extends Slot {

	private final ExampleDecomposerContainer container;

	public ExampleDecomposerFuelSlot(ExampleDecomposerContainer containerIn, IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
		this.container = containerIn;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return this.container.isFuel(stack);
	}
}
