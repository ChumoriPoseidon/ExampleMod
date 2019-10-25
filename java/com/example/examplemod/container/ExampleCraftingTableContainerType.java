package com.example.examplemod.container;

import net.minecraft.inventory.container.ContainerType;

public class ExampleCraftingTableContainerType extends ContainerType<ExampleCraftingTableContainer> {

	public ExampleCraftingTableContainerType() {
		super(ExampleCraftingTableContainer::new);
		this.setRegistryName("containertype_example_crafting_table");
	}

//	@Override
//	public ExampleCraftingTableContainer create(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
//		return super.create(windowId, playerInv, extraData);
//	}

}
