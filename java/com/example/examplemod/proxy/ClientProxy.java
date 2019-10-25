package com.example.examplemod.proxy;

import com.example.examplemod.ExampleMod.RegistryEvents;
import com.example.examplemod.container.ExampleChestScreen;
import com.example.examplemod.container.ExampleCraftingTableScreen;
import com.example.examplemod.container.ExampleProcessorScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class ClientProxy implements IProxy {

	@Override
	public void init() {
		ScreenManager.registerFactory(RegistryEvents.EXAMPLE_CRAFTING_TABLE_CONTAINER, ExampleCraftingTableScreen::new);

		ScreenManager.registerFactory(RegistryEvents.EXAMPLE_PROCESSOR_CONTAINER, ExampleProcessorScreen::new);
		ScreenManager.registerFactory(RegistryEvents.EXAMPLE_CHEST_CONTAINER, ExampleChestScreen::new);
	}

	@Override
	public World getWorld() {
		return Minecraft.getInstance().world;
	}

	@Override
	public PlayerEntity getPlayer() {
		return Minecraft.getInstance().player;
	}
}
