package xyz.memothelemo.dystopia.itemfilter.block;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import xyz.memothelemo.dystopia.itemfilter.ItemFilterMod;
import xyz.memothelemo.dystopia.itemfilter.block.entity.ItemFilterBlockEntity;
import xyz.memothelemo.dystopia.itemfilter.block.entity.ModBlockEntities;
import xyz.memothelemo.dystopia.itemfilter.inventory.ItemAllowlistInventory;

public class ItemFilterBlock extends BaseEntityBlock implements PolymerTexturedBlock {
    private final BlockState polymerBlockState;
    private final Identifier modelId;

    public ItemFilterBlock(Properties settings, Identifier modelId) {
        super(settings);
        this.modelId = modelId;
        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
            BlockModelType.FULL_BLOCK,
            PolymerBlockModel.of(modelId)
        );
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, @Nullable PacketContext packetContext) {
        return this.polymerBlockState;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(
            type,
            ModBlockEntities.ITEM_FILTER,
            ItemFilterBlockEntity::onGameTick
        );
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
        @NonNull BlockState state,
        Level level,
        @NonNull BlockPos pos,
        @NonNull Player player,
        @NonNull BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof ItemFilterBlockEntity entity)) {
            return InteractionResult.SUCCESS;
        }

        ItemAllowlistInventory allowlistInventory = entity.getAllowlist();
        if (allowlistInventory == null) {
            ItemFilterMod.LOGGER.error("Could not open item allow list inventory from Item Filter block!");
            return InteractionResult.SUCCESS;
        }

        player.openMenu(new SimpleMenuProvider(
            (id, inventory, _) -> ChestMenu.threeRows(id, inventory, allowlistInventory),
            xyz.memothelemo.dystopia.common.constant.MessageConstants.ITEM_FILTER_COMPONENT
        ));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean isPathfindable(@NonNull BlockState state, @NonNull PathComputationType type) {
        return false;
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec((properties) -> new ItemFilterBlock(properties, this.modelId));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ItemFilterBlockEntity(pos, state);
    }
}
