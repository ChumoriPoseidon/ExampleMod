package com.example.examplemod.block;

import com.example.examplemod.ExampleMod.RegistryEvents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ExampleThornsBlock extends Block {

	public ExampleThornsBlock() {
		super(Block.Properties.create(Material.CACTUS).sound(SoundType.PLANT).hardnessAndResistance(0.5F, 0.25F));
		this.setRegistryName("block_example_thorns");

		RegistryEvents.BLOCKS.add(this);
		RegistryEvents.ITEMS.add(new BlockItem(this, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName(this.getRegistryName()));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return Block.makeCuboidShape(0.5D, 0.0D, 0.5D, 15.5D, 15.5D, 15.5D);
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if(entityIn.isLiving() && !(entityIn instanceof PlayerEntity)) {
			entityIn.attackEntityFrom(DamageSource.CACTUS, 2.0F);
		}
	}

	@Override
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
		entityIn.setMotion(entityIn.getMotion().mul(0.4D, 1.0D, 0.4D));
	}

	@Override
	public boolean isSolid(BlockState state) {
		return true;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
}
