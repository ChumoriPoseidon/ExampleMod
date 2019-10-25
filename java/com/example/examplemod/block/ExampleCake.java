package com.example.examplemod.block;

import com.example.examplemod.ExampleMod.RegistryEvents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ExampleCake extends Block {

	public static final IntegerProperty BITES = BlockStateProperties.BITES_0_6;
//
	protected static final VoxelShape[] SHAPES = new VoxelShape[]{
			Block.makeCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D),
			Block.makeCuboidShape(3.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D),
			Block.makeCuboidShape(5.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D),
			Block.makeCuboidShape(7.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D),
			Block.makeCuboidShape(9.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D),
			Block.makeCuboidShape(11.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D),
			Block.makeCuboidShape(13.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D)
			};

	public ExampleCake() {
		super(Properties.create(Material.CAKE).sound(SoundType.CLOTH).hardnessAndResistance(0.5F, 0.5F));
		this.setRegistryName("block_example_cake");
		this.setDefaultState(this.stateContainer.getBaseState().with(BITES, Integer.valueOf(0)));

		RegistryEvents.BLOCKS.add(this);
		RegistryEvents.ITEMS.add(new BlockItem(this, new Item.Properties().group(ItemGroup.FOOD)).setRegistryName(this.getRegistryName()));
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if(!worldIn.isRemote) {
			return this.eatCake(worldIn, pos, state, player);
		}
		else {
			ItemStack stack = player.getHeldItem(handIn);
			return this.eatCake(worldIn, pos, state, player) || stack.isEmpty();
		}
	}

	private boolean eatCake(IWorld worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		if(!player.canEat(false)) {
			return false;
		}
		else {
			player.addStat(Stats.EAT_CAKE_SLICE);
			player.getFoodStats().addStats(2, 0.1F);
			int i = state.get(BITES);
			if(i < 6) {
				worldIn.setBlockState(pos, state.with(BITES, Integer.valueOf(i + 1)), 3);
			}
			else {
				worldIn.removeBlock(pos, false);
			}
			return true;
		}
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.down()).getMaterial().isSolid();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(BITES);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPES[state.get(BITES)];
	}
}
