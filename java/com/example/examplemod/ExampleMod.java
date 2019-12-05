package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.examplemod.block.ExampleCake;
import com.example.examplemod.block.ExampleChest;
import com.example.examplemod.block.ExampleCraftingTable;
import com.example.examplemod.block.ExampleDecomposer;
import com.example.examplemod.block.ExampleProcessor;
import com.example.examplemod.block.ExampleThornsBlock;
import com.example.examplemod.container.ExampleChestContainer;
import com.example.examplemod.container.ExampleCraftingTableContainer;
import com.example.examplemod.container.ExampleCraftingTableContainerType;
import com.example.examplemod.container.ExampleDecomposerContainer;
import com.example.examplemod.container.ExampleProcessorContainer;
import com.example.examplemod.item.ExampleTool;
import com.example.examplemod.proxy.ClientProxy;
import com.example.examplemod.proxy.IProxy;
import com.example.examplemod.proxy.ServerProxy;
import com.example.examplemod.recipe.CustomRecipeSerializer;
import com.example.examplemod.recipe.ExampleDecomposerRecipe;
import com.example.examplemod.recipe.ExampleProcessorRecipe;
import com.example.examplemod.recipe.ExampleShapedRecipe;
import com.example.examplemod.tileentity.ExampleChestTileEntity;
import com.example.examplemod.tileentity.ExampleDecomposerTileEntity;
import com.example.examplemod.tileentity.ExampleProcessorTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("examplemod")
public class ExampleMod {

	public static final String MODID = "examplemod";

	public static IProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();

	public ExampleMod() {
    	// Register the setup method for modloading
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
//		↑
//		FMLJavaModLoadingContext.get().getModEventBus().addListener(event -> setup((FMLCommonSetupEvent)event));
//		↑
//		FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup(event));
//		↑
//		FMLJavaModLoadingContext.get().getModEventBus().addListener(new Consumer<FMLCommonSetupEvent>() {
//			@Override
//			public void accept(FMLCommonSetupEvent event) {
//				setup(event);
//			}
//		});

        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
    	// some preinit code
    	LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

        proxy.init();
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
    	// do something that can only be done on the client
    	LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
	}

	private void enqueueIMC(final InterModEnqueueEvent event) {
    	// some example code to dispatch IMC to another mod
    	InterModComms.sendTo("examplemod", "helloworld", () -> {
    		LOGGER.info("Hello world from the MDK");
    		return "Hello world";
    	});
	}

    private void processIMC(final InterModProcessEvent event) {
    	// some example code to receive and process InterModComms from other mods
    	LOGGER.info("Got IMC {}", event.getIMCStream().map(m->m.getMessageSupplier().get()).collect(Collectors.toList()));
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {
    	// do something when the server starts
    	LOGGER.info("HELLO from server starting");
	}

	// You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@SuppressWarnings("unchecked")
	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {

    	public static List<Item> ITEMS = new ArrayList<Item>();
		public static List<Block> BLOCKS = new ArrayList<Block>();

		// Item
		public static Item EXAMPLE_ITEM;
		public static Item EXAMPLE_FOOD; // like "food"
		public static Item EXAMPLE_TOOL; // like "tool" [personal class]
		/*
		 * 課題
		 * ・アイテム側からブロック破壊時のドロップを設定できない？
		 */

		// Block
		public static Block EXAMPLE_BLOCK;
		public static Block EXAMPLE_THORNS; // [personal class]
		public static Block EXAMPLE_CAKE; // like "cake"[meta, using block]
		public static Block EXAMPLE_CRAFTING_TABLE; // like "crafting_table"[gui, container]
		public static Block EXAMPLE_PROCESSOR; // like "furnace"[tileentity]
		public static Block EXAMPLE_DECOMPOSER; // like "furnace"[tileentity] + by-product
		public static Block EXAMPLE_CHEST; // like "chest"[tileentity]

		// ContainerType
		public static ContainerType<ExampleCraftingTableContainer> EXAMPLE_CRAFTING_TABLE_CONTAINER;
		public static ContainerType<ExampleProcessorContainer> EXAMPLE_PROCESSOR_CONTAINER;
		public static ContainerType<ExampleDecomposerContainer> EXAMPLE_DECOMPOSER_CONTAINER;
		public static ContainerType<ExampleChestContainer> EXAMPLE_CHEST_CONTAINER;

		// TileEntityType
		public static TileEntityType<ExampleProcessorTileEntity> EXAMPLE_PROCESSOR_TILEENTITY;
		public static TileEntityType<ExampleDecomposerTileEntity> EXAMPLE_DECOMPOSER_TILEENTITY;
		public static TileEntityType<ExampleChestTileEntity> EXAMPLE_CHEST_TILEENTITY;

		// RecipeType & RecipeSerializer
		public static IRecipeType<ExampleShapedRecipe> EXAMPLE_CRAFTING_SHAPED_RECIPE_TYPE;
		public static IRecipeSerializer<ExampleShapedRecipe> EXAMPLE_CRAFTING_SHAPED_RECIPE;
		public static IRecipeType<ExampleProcessorRecipe> EXAMPLE_PROCESSOR_RECIPE_TYPE;
		public static IRecipeSerializer<ExampleProcessorRecipe> EXAMPLE_PROCESSOR_RECIPE;
		public static IRecipeType<ExampleDecomposerRecipe> EXAMPLE_DECOMPOSER_RECIPE_TYPE;
		public static IRecipeSerializer<ExampleDecomposerRecipe> EXAMPLE_DECOMPOSER_RECIPE;


		@SubscribeEvent
		public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
			// register a new item here
			LOGGER.info("HELLO from Register Item");

			registerItem();
			event.getRegistry().registerAll(ITEMS.toArray(new Item[0]));
		}

		public static void registerItem() {
			EXAMPLE_ITEM = new Item(new Item.Properties().group(ItemGroup.MATERIALS)).setRegistryName("item_example");
			ITEMS.add(EXAMPLE_ITEM);

			EXAMPLE_FOOD = new Item(new Item.Properties().group(ItemGroup.FOOD).food((new Food.Builder()).hunger(10).saturation(1.0F).build())).setRegistryName("item_example_food");
			ITEMS.add(EXAMPLE_FOOD);

			EXAMPLE_TOOL = new ExampleTool();
		}

		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
			// register a new block here
			LOGGER.info("HELLO from Register Block");

			registerBlock();
			event.getRegistry().registerAll(BLOCKS.toArray(new Block[0]));
		}

		public static void registerBlock() {
			EXAMPLE_BLOCK = new Block(Block.Properties.create(Material.LEAVES).sound(SoundType.PLANT).hardnessAndResistance(0.25F, 0.1F)).setRegistryName("block_example");
			BLOCKS.add(EXAMPLE_BLOCK);
			ITEMS.add(new BlockItem(EXAMPLE_BLOCK, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("block_example"));

			EXAMPLE_THORNS = new ExampleThornsBlock();

			EXAMPLE_CAKE = new ExampleCake();

			EXAMPLE_CRAFTING_TABLE = new ExampleCraftingTable();

			EXAMPLE_PROCESSOR = new ExampleProcessor();

			EXAMPLE_DECOMPOSER = new ExampleDecomposer();

			EXAMPLE_CHEST = new ExampleChest();
		}

		@SubscribeEvent
		public static void onContainersRegistry(final RegistryEvent.Register<ContainerType<?>> event) {
			// register a new container type here
			LOGGER.info("HELLO from Register ContainerType");

			EXAMPLE_CRAFTING_TABLE_CONTAINER = new ExampleCraftingTableContainerType();
//			EXAMPLE_CRAFTING_TABLE_CONTAINER = (ContainerType<ExampleCraftingTableContainer>) IForgeContainerType.create((windowId, inventory, extraData) -> {
//				return new ExampleCraftingTableContainer(windowId, inventory);
//			}).setRegistryName("containertype_example_crafting_table");

//			EXAMPLE_PROCESSOR_CONTAINER = new ExampleProcessorContainerType();
			EXAMPLE_PROCESSOR_CONTAINER = (ContainerType<ExampleProcessorContainer>) IForgeContainerType.create((windowId, inventory, extraData) -> {
				return new ExampleProcessorContainer(windowId, inventory);
			}).setRegistryName("containertype_example_processor");

//			EXAMPLE_DECOMPOSER_CONTAINER = new ExampleDecomposerContainerType();
			EXAMPLE_DECOMPOSER_CONTAINER = (ContainerType<ExampleDecomposerContainer>) IForgeContainerType.create((windowId, inventory, extraData) -> {
				return new ExampleDecomposerContainer(windowId, inventory);
			}).setRegistryName("containertype_example_decomposer");

//			EXAMPLE_CHEST_CONTAINER = new ExampleChestContainerType();
			EXAMPLE_CHEST_CONTAINER = (ContainerType<ExampleChestContainer>) IForgeContainerType.create((windowId, inventory, extraData) -> {
				return new ExampleChestContainer(windowId, inventory);
			}).setRegistryName("containertype_example_chest");

			event.getRegistry().registerAll(
					EXAMPLE_CRAFTING_TABLE_CONTAINER,
					EXAMPLE_PROCESSOR_CONTAINER,
					EXAMPLE_DECOMPOSER_CONTAINER,
					EXAMPLE_CHEST_CONTAINER
					);
		}

		@SubscribeEvent
		public static void onTileEntitysRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {
			// register a new tileentity type here
			LOGGER.info("HELLO from Register TileEntityType");

			EXAMPLE_PROCESSOR_TILEENTITY = (TileEntityType<ExampleProcessorTileEntity>) TileEntityType.Builder.create(ExampleProcessorTileEntity::new, EXAMPLE_PROCESSOR).build(null).setRegistryName("tileentitytype_example_processor");
			EXAMPLE_DECOMPOSER_TILEENTITY = (TileEntityType<ExampleDecomposerTileEntity>) TileEntityType.Builder.create(ExampleDecomposerTileEntity::new, EXAMPLE_DECOMPOSER).build(null).setRegistryName("tileentitytype_example_decomposer");
			EXAMPLE_CHEST_TILEENTITY = (TileEntityType<ExampleChestTileEntity>) TileEntityType.Builder.create(ExampleChestTileEntity::new, EXAMPLE_CHEST).build(null).setRegistryName("tileentitytype_example_chest");

			event.getRegistry().registerAll(
					EXAMPLE_PROCESSOR_TILEENTITY,
					EXAMPLE_DECOMPOSER_TILEENTITY,
					EXAMPLE_CHEST_TILEENTITY
					);
		}

		@SubscribeEvent
		public static void onRecipesRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
			// register a new recipe serializer here
			LOGGER.info("HELLO from Register RecipeSerializer");

			EXAMPLE_CRAFTING_SHAPED_RECIPE_TYPE = IRecipeType.register("example_crafting");
			EXAMPLE_PROCESSOR_RECIPE_TYPE = IRecipeType.register("example_processor");
			EXAMPLE_DECOMPOSER_RECIPE_TYPE = IRecipeType.register("example_decomposer");

			EXAMPLE_CRAFTING_SHAPED_RECIPE = (IRecipeSerializer<ExampleShapedRecipe>) IRecipeSerializer.register("example_crafting_shaped", new ExampleShapedRecipe.Serializer());
			EXAMPLE_PROCESSOR_RECIPE = (IRecipeSerializer<ExampleProcessorRecipe>) IRecipeSerializer.register("example_processing", new CustomRecipeSerializer<>(ExampleProcessorRecipe::new, 200));
			EXAMPLE_DECOMPOSER_RECIPE = (IRecipeSerializer<ExampleDecomposerRecipe>) IRecipeSerializer.register("example_decomposing", new ExampleDecomposerRecipe.Serializer<>(ExampleDecomposerRecipe::new, 200));

			event.getRegistry().registerAll(
					EXAMPLE_CRAFTING_SHAPED_RECIPE,
					EXAMPLE_PROCESSOR_RECIPE,
					EXAMPLE_DECOMPOSER_RECIPE
					);
		}
	}

//	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
//	public static class GeneratorData {
//
//		@SubscribeEvent
//		public static void onGenerateData(GatherDataEvent event) {
//			DataGenerator generator = event.getGenerator();
////			generator.addProvider(new RecipeProvider(generator));
////			generator.addProvider(new LootProvider(generator));
//		}
//	}
}
