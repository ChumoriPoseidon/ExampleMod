package com.example.examplemod.block;

import com.example.examplemod.ExampleMod.RegistryEvents;
import com.example.examplemod.container.ExampleCraftingTableContainer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ExampleCraftingTable extends Block {

	private static final ITextComponent container_name = new TranslationTextComponent("container.example_block_crafting_table");

	public ExampleCraftingTable() {
		super(Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2.5F, 2.5F));
		this.setRegistryName("block_example_crafting_table");

		RegistryEvents.BLOCKS.add(this);
		RegistryEvents.ITEMS.add(new BlockItem(this, new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(this.getRegistryName()));
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		player.openContainer(state.getContainer(worldIn, pos));
//		NetworkHooks.openGui((ServerPlayerEntity)player, state.getContainer(worldIn, pos));
		return true;
	}

	@Override
	public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
		return new SimpleNamedContainerProvider((type, playerInventory, posCallable) -> {
			return new ExampleCraftingTableContainer(type, playerInventory, IWorldPosCallable.of(worldIn, pos));
		}, container_name);
	}
}
