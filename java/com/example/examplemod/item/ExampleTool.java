package com.example.examplemod.item;

import java.util.List;

import com.example.examplemod.ExampleMod.RegistryEvents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ExampleTool extends Item {

	public ExampleTool() {
		super(new Properties().group(ItemGroup.TOOLS).maxStackSize(1).maxDamage(128));
		this.setRegistryName("item_example_tool");

		RegistryEvents.ITEMS.add(this);
	}

	@Override
	public boolean canHarvestBlock(BlockState blockIn) {
		Block block = blockIn.getBlock();
		return block == Blocks.COBWEB || block == Blocks.REDSTONE_WIRE || block == Blocks.TRIPWIRE;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		Block block = state.getBlock();
		if(block == Blocks.COBWEB || block.isIn(BlockTags.LEAVES) || block == RegistryEvents.EXAMPLE_BLOCK || block == RegistryEvents.EXAMPLE_THORNS) {
			return 15.0F;
		}
		else if(block.isIn(BlockTags.WOOL)) {
			return 5.0F;
		}
		else {
			return super.getDestroySpeed(stack, state);
		}
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		Block block = state.getBlock();
		if(!worldIn.isRemote) {
			stack.attemptDamageItem(1, Item.random, (ServerPlayerEntity)entityLiving);
			if(Block.getDrops(state, (ServerWorld)worldIn, pos, worldIn.getTileEntity(pos)).isEmpty()) {
				if(block == Blocks.GRASS || block == Blocks.FERN || block == Blocks.DEAD_BUSH || block == Blocks.VINE || block.isIn(BlockTags.LEAVES)) {
					Block.spawnAsEntity(worldIn, pos, new ItemStack(block));
				}
			}
		}
		return block != Blocks.COBWEB && block != Blocks.GRASS && block != Blocks.FERN && block != Blocks.DEAD_BUSH && block != Blocks.VINE && block != Blocks.TRIPWIRE && !block.isIn(BlockTags.LEAVES) && !block.isIn(BlockTags.WOOL) && block != RegistryEvents.EXAMPLE_BLOCK && block != RegistryEvents.EXAMPLE_THORNS ? super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving) : true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
}
