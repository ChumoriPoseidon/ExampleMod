package com.example.examplemod.container;

import com.example.examplemod.tileentity.ExampleDecomposerTileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.hooks.BasicEventHooks;

public class ExampleDecomposerResultSlot extends Slot {

	private final PlayerEntity player;
	private int removeCount;

	public ExampleDecomposerResultSlot(PlayerEntity playerIn, IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
		this.player = playerIn;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		if(this.getHasStack()) {
			this.removeCount += Math.min(amount, this.getStack().getCount());
		}
		return super.decrStackSize(amount);
	}

	@Override
	public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
		this.onCrafting(stack);
		super.onTake(thePlayer, stack);
		return stack;
	}

	@Override
	protected void onCrafting(ItemStack stack, int amount) {
		this.removeCount += amount;
		this.onCrafting(stack);
	}

	@Override
	protected void onCrafting(ItemStack stack) {
		stack.onCrafting(this.player.world, this.player, this.removeCount);
		if(!this.player.world.isRemote && this.inventory instanceof ExampleDecomposerTileEntity) {
			((ExampleDecomposerTileEntity)this.inventory).onCrafting(this.player);
		}
		this.removeCount = 0;
		BasicEventHooks.firePlayerSmeltedEvent(this.player, stack); // ?
	}
}
