package xyz.memothelemo.dystopia.itemfilter.block;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import xyz.memothelemo.dystopia.itemfilter.ItemFilterMod;
import xyz.memothelemo.dystopia.itemfilter.block.entity.ModBlockEntities;

import java.util.function.BiFunction;

public class ModBlocks {
    public static Block ITEM_FILTER;

    public static void initialize() {
        ModBlockEntities.initialize();
        setupItemGroups();
    }

    public static void setupItemGroups() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS)
            .register((tab) -> tab.accept(ModBlocks.ITEM_FILTER.asItem()));
    }

    @SuppressWarnings("all")
    private static Block register(
        String name,
        BiFunction<BlockBehaviour.Properties, Identifier, Block> factory,
        Block abstractBlock,
        BlockBehaviour.Properties props
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(ItemFilterMod.MOD_ID, name);
        Identifier modelId = Identifier.fromNamespaceAndPath(ItemFilterMod.MOD_ID, "block/" + name);

        Block block = factory.apply(props.setId(ResourceKey.create(Registries.BLOCK, id)), modelId);
        Registry.register(BuiltInRegistries.BLOCK, id, block);

        Item.Properties itemProps = new Item.Properties()
            .useBlockDescriptionPrefix()
            .requiredFeatures(block.requiredFeatures())
            .setId(ResourceKey.create(Registries.ITEM, id));

        PolymerBlockItem item = new PolymerBlockItem(block, itemProps, abstractBlock.asItem(), true);
        Registry.register(BuiltInRegistries.ITEM, id, item);

        return block;
    }

    static {
        ITEM_FILTER = register(
            "item_filter",
            ItemFilterBlock::new,
            Blocks.NOTE_BLOCK,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .strength(3.0F)
                .requiresCorrectToolForDrops()
                .isRedstoneConductor(Blocks::never)
        );
    }
}
