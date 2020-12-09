package space.bbkr.druidsropes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import space.bbkr.druidsropes.block.CeramicLanternBlock;
import space.bbkr.druidsropes.block.RopeBlock;
import space.bbkr.druidsropes.block.RopeLanternBlock;
import space.bbkr.druidsropes.block.SmallBeamBlock;
import space.bbkr.druidsropes.item.KnifeItem;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class DruidsRopes implements ModInitializer {
	public static final String MODID = "druidsropes";

	public static final Logger logger = LogManager.getLogger();

	public static final Item KNIFE = register("knife", new KnifeItem(new Item.Settings().group(ItemGroup.TOOLS).maxCount(1)));

	public static final Block ROPE = register("rope", new RopeBlock(FabricBlockSettings.of(Material.WOOL).sounds(BlockSoundGroup.WOOL).strength(0)), ItemGroup.DECORATIONS);
	public static final Block ROPE_LANTERN = register("rope_lantern", new RopeLanternBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.LANTERN).strength(3.5f).breakByTool(FabricToolTags.PICKAXES).luminance(15).dropsLike(Blocks.LANTERN)));
	public static final Block CERAMIC_LANTERN = register("ceramic_lantern", new CeramicLanternBlock(FabricBlockSettings.of(Material.STONE).sounds(BlockSoundGroup.STONE).breakByTool(FabricToolTags.PICKAXES).strength(1.5F).luminance(13)), ItemGroup.DECORATIONS);
	public static final Block OAK_SMALL_BEAM = register("oak_small_beam", new SmallBeamBlock(FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES).sounds(BlockSoundGroup.WOOD).strength(2)), ItemGroup.DECORATIONS);
	public static final Block SPRUCE_SMALL_BEAM = register("spruce_small_beam", new SmallBeamBlock(FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES).sounds(BlockSoundGroup.WOOD).strength(2)), ItemGroup.DECORATIONS);
	public static final Block BIRCH_SMALL_BEAM = register("birch_small_beam", new SmallBeamBlock(FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES).sounds(BlockSoundGroup.WOOD).strength(2)), ItemGroup.DECORATIONS);
	public static final Block JUNGLE_SMALL_BEAM = register("jungle_small_beam", new SmallBeamBlock(FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES).sounds(BlockSoundGroup.WOOD).strength(2)), ItemGroup.DECORATIONS);
	public static final Block ACACIA_SMALL_BEAM = register("acacia_small_beam", new SmallBeamBlock(FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES).sounds(BlockSoundGroup.WOOD).strength(2)), ItemGroup.DECORATIONS);
	public static final Block DARK_OAK_SMALL_BEAM = register("dark_oak_small_beam", new SmallBeamBlock(FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES).sounds(BlockSoundGroup.WOOD).strength(2)), ItemGroup.DECORATIONS);

	@Override
	public void onInitialize() {

	}

	public static Block register(String name, Block block, ItemGroup group) {
		register(name, new BlockItem(block, new Item.Settings().group(group)));
		return Registry.register(Registry.BLOCK, new Identifier(MODID, name), block);
	}

	public static Block register(String name, Block block) {
		return Registry.register(Registry.BLOCK, new Identifier(MODID, name), block);
	}

	public static Item register(String name, Item item) {
		return Registry.register(Registry.ITEM, new Identifier(MODID, name), item);
	}
}
