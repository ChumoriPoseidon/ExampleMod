package com.example.examplemod.container;

import java.util.Optional;

import com.example.examplemod.ExampleMod.RegistryEvents;
import com.example.examplemod.recipe.ExampleShapedRecipe;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;

public class ExampleCraftingTableContainer extends Container {

	private final CraftingInventory matrix = new CraftingInventory(this, 3, 3);
	private final CraftResultInventory result = new CraftResultInventory();
	private final IWorldPosCallable posCallable;
	private final PlayerEntity player;

	public ExampleCraftingTableContainer(int type, PlayerInventory inventory) {
		this(type, inventory, IWorldPosCallable.DUMMY);
	}

	public ExampleCraftingTableContainer(int type, PlayerInventory inventory, IWorldPosCallable callable) {
		super(RegistryEvents.EXAMPLE_CRAFTING_TABLE_CONTAINER, type);
		this.posCallable = callable;
		this.player = inventory.player;
//		this.addSlot(new CraftingResultSlot(inventory.player, this.matrix, this.result, 0, 124, 35));
		this.addSlot(new ExampleCraftingResultSlot(inventory.player, this.matrix, this.result, 0, 124, 35));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlot(new Slot(this.matrix, j + i * 3, 30 + j * 18, 17 + i * 18));
			}
		}

		for(int k = 0; k < 3; k++) {
			for(int l = 0; l < 9; l++) {
				this.addSlot(new Slot(inventory, l + k * 9 + 9, 8 + l * 18, 84 + k * 18));
			}
		}

		for(int m = 0; m < 9; m++) {
			this.addSlot(new Slot(inventory, m, 8 + m * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return isWithinUsableDistance(this.posCallable, playerIn, RegistryEvents.EXAMPLE_CRAFTING_TABLE);
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		this.posCallable.consume((world, pos) -> {
			checkRecipeFromMatrix(this.windowId, world, this.player, this.matrix, this.result);
		});
	}

	protected void checkRecipeFromMatrix(int windowId, World world, PlayerEntity player, CraftingInventory matrix, CraftResultInventory result) {
		if(!world.isRemote) {
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
			ItemStack stack = ItemStack.EMPTY;
			Optional<ExampleShapedRecipe> optional = world.getServer().getRecipeManager().getRecipe(RegistryEvents.EXAMPLE_CRAFTING_SHAPED_RECIPE_TYPE, matrix, world);
//			Optional<ICraftingRecipe> optional = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, matrix, world);
			System.out.println("Check: " + optional.toString());
			/**
			 * e.g. Optional[net.minecraft.item.crafting.ShapelessRecipe@65b97c]
			 */
			if(optional.isPresent()) {
				ExampleShapedRecipe recipe = optional.get();
//				ICraftingRecipe recipe = optional.get();
				if(result.canUseRecipe(world, serverPlayer, recipe)) {
//					ItemStack itemStack = recipe.getCraftingResult(matrix);
//					itemStack.setCount(itemStack.getCount() * 2);
//					stack = itemStack.copy();
					stack = recipe.getCraftingResult(matrix).copy();
				}
			}
			result.setInventorySlotContents(0, stack);
			serverPlayer.connection.sendPacket(new SSetSlotPacket(windowId, 0, stack));
		}
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		this.posCallable.consume((world, pos) -> {
			this.clearContainer(playerIn, world, this.matrix);
		});
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if(slot != null && slot.getHasStack()) {
			ItemStack stackSlot = slot.getStack();
			stack = stackSlot.copy();
			if(index == 0) {
				this.posCallable.consume((world, pos) -> {
					stackSlot.getItem().onCreated(stackSlot, world, playerIn);
				});
				if(!this.mergeItemStack(stackSlot, 10, 46, true)) {
					return ItemStack.EMPTY;
				}
				slot.onSlotChange(stackSlot, stack);
			}
			else if(index >= 10 && index < 37) {
				if(!this.mergeItemStack(stackSlot, 37, 46, false)) {
					return ItemStack.EMPTY;
				}
			}
			else if(index >= 37 && index < 46) {
				if(!this.mergeItemStack(stackSlot, 10, 37, false)) {
					return ItemStack.EMPTY;
				}
			}
			else {
				if(!this.mergeItemStack(stackSlot, 10, 46, false)) {
					return ItemStack.EMPTY;
				}
			}

			if(stackSlot.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			}
			else {
				slot.onSlotChanged();
			}

			if(stackSlot.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}

			ItemStack stackDrop = slot.onTake(playerIn, stackSlot);
			if(index == 0) {
				playerIn.dropItem(stackDrop, false);
			}
		}
		return stack;
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		return slotIn.inventory != this.result && super.canMergeSlot(stack, slotIn);
	}
}
