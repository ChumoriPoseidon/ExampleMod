package com.example.examplemod.tileentity;

import com.example.examplemod.ExampleMod.RegistryEvents;
import com.example.examplemod.container.ExampleChestContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ExampleChestTileEntity extends TileEntity implements IInventory, INamedContainerProvider, ITickableTileEntity {

	private NonNullList<ItemStack> chestContents = NonNullList.withSize(27, ItemStack.EMPTY);
//	private LazyOptional<IItemHandlerModifiable> chestHandler;

	public ExampleChestTileEntity() {
		super(RegistryEvents.EXAMPLE_CHEST_TILEENTITY);
	}

	@Override
	public void clear() {
		this.chestContents.clear();
	}

	@Override
	public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
		return new ExampleChestContainer(id, inventory, this);
	}

	@Override
	public void tick() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("container.example_block_chest");
	}

	@Override
	public int getSizeInventory() {
		return this.chestContents.size();
	}

	@Override
	public boolean isEmpty() {
		for(ItemStack stack : this.chestContents) {
			if(!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return this.chestContents.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.chestContents, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(this.chestContents, index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		this.chestContents.set(index, stack);
		if(stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
		this.markDirty();
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		if(this.world.getTileEntity(this.pos) != this) {
			return false;
		}
		return !(player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) > 64.0D);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		this.chestContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, this.chestContents);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		ItemStackHelper.saveAllItems(compound, this.chestContents);
		return compound;
	}

//	@Override
//	public void updateContainingBlockInfo() {
//		super.updateContainingBlockInfo();
//		if(this.chestHandler != null) {
//			this.chestHandler.invalidate();
//			this.chestHandler = null;
//		}
//	}
//
//	@Override
//	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
//		if(!this.removed && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
//			if(this.chestHandler == null) {
//				this.chestHandler = LazyOptional.of(this::createHandler);
//			}
//		}
//		return super.getCapability(cap, side);
//	}
}
