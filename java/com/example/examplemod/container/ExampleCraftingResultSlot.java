package com.example.examplemod.container;

import com.example.examplemod.ExampleMod.RegistryEvents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IRecipeHolder;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.hooks.BasicEventHooks;

public class ExampleCraftingResultSlot extends Slot {

	private final PlayerEntity player;
	private final CraftingInventory inventoryCrafting;
	private int amountCrafted;

	public ExampleCraftingResultSlot(PlayerEntity playerIn, CraftingInventory inventoryCraftingIn, IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
		this.player = playerIn;
		this.inventoryCrafting = inventoryCraftingIn;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		if(this.getHasStack()) {
			this.amountCrafted += Math.min(amount, this.getStack().getCount());
		}
		return super.decrStackSize(amount);
	}

	@Override
	protected void onSwapCraft(int amount) {
		this.amountCrafted += amount;
	}

	@Override
	protected void onCrafting(ItemStack stack, int amount) {
		this.amountCrafted += amount;
		this.onCrafting(stack);
	}

	@Override
	protected void onCrafting(ItemStack stack) {
		if(this.amountCrafted > 0) {
			stack.onCrafting(this.player.world, this.player, this.amountCrafted);
			BasicEventHooks.firePlayerCraftingEvent(this.player, stack, this.inventoryCrafting);
		}
		if(this.inventory instanceof IRecipeHolder) {
			((IRecipeHolder)this.inventory).onCrafting(this.player);
		}
		this.amountCrafted = 0;
	}

	@Override
	public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
		this.onCrafting(stack);
		ForgeHooks.setCraftingPlayer(thePlayer);
		NonNullList<ItemStack> recipes = thePlayer.world.getRecipeManager().getRecipeNonNull(RegistryEvents.EXAMPLE_CRAFTING_SHAPED_RECIPE_TYPE, this.inventoryCrafting, thePlayer.world);
//		NonNullList<ItemStack> recipes = thePlayer.world.getRecipeManager().getRecipeNonNull(IRecipeType.CRAFTING, this.inventoryCrafting, thePlayer.world);
		System.out.println("Check: " + recipes.toString());
		ForgeHooks.setCraftingPlayer(null);
		for(int i = 0; i < recipes.size(); i++) {
			ItemStack slotItem = this.inventoryCrafting.getStackInSlot(i);
			ItemStack result = recipes.get(i);
			if(!slotItem.isEmpty()) {
				this.inventoryCrafting.decrStackSize(i, 1);
				slotItem = this.inventoryCrafting.getStackInSlot(i);
			}
			if(!result.isEmpty()) {
				if(slotItem.isEmpty()) {
					this.inventoryCrafting.setInventorySlotContents(i, result);
				}
				else if(ItemStack.areItemsEqual(slotItem, result) && ItemStack.areItemStackTagsEqual(slotItem, result)) {
					result.grow(slotItem.getCount());
					this.inventoryCrafting.setInventorySlotContents(i, result);
				}
				else if(!this.player.inventory.addItemStackToInventory(result)) {
					this.player.dropItem(result, false);
				}
			}
		}
		return stack;
	}
}
