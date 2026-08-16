package xyz.memothelemo.dystopia.itemfilter.block.entity;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import xyz.memothelemo.dystopia.itemfilter.ItemFilterMod;
import xyz.memothelemo.dystopia.itemfilter.block.ModBlocks;

public class ModBlockEntities {
    public static final BlockEntityType<ItemFilterBlockEntity> ITEM_FILTER;

    public static void initialize() {
        PolymerBlockUtils.registerBlockEntity(ITEM_FILTER);
    }

    static {
        ITEM_FILTER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(ItemFilterMod.MOD_ID, "item_filter"),
            FabricBlockEntityTypeBuilder.create(ItemFilterBlockEntity::new, ModBlocks.ITEM_FILTER).build()
        );
    }
}
