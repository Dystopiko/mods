package xyz.memothelemo.itemfilter.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import xyz.memothelemo.itemfilter.inventory.ItemAllowlistInventory;

public class ItemFilterBlockEntity extends BlockEntity {
    // This is set to `@Nullable` to prevent from memory leaks occurred in this
    // field here if we no longer use it in the future.
    private @Nullable ItemAllowlistInventory allowlist;

    public ItemFilterBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.ITEM_FILTER, pos, state);
        this.allowlist = new ItemAllowlistInventory(this);
    }

    public @Nullable ItemAllowlistInventory getAllowlist() {
        return this.allowlist;
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);

        if (this.allowlist == null) return;
        this.allowlist.storeAsSlots(output.list("allowlist", ItemStackWithSlot.CODEC));
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);

        if (this.allowlist == null) return;
        this.allowlist.fromSlots(input.listOrEmpty("allowlist", ItemStackWithSlot.CODEC));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.allowlist = null;
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        this.level.blockEntityChanged(this.worldPosition);

        BlockState blockState = this.getBlockState();
        if (!blockState.isAir()) {
            this.level.updateNeighbourForOutputSignal(this.worldPosition, blockState.getBlock());
        }
    }

    public static void onGameTick(
        final Level level,
        final BlockPos pos,
        final BlockState state,
        final ItemFilterBlockEntity entity
    ) {
        tryMoveItems(level, pos, state, entity);
    }

    private static boolean trySuckItems(final Level level, final ItemFilterBlockEntity filterBlock) {
        ItemAllowlistInventory allowlist = filterBlock.allowlist;
        if (allowlist == null) return false;

        BlockPos filterBlockPos = filterBlock.getBlockPos();
        Container source = getSourceHopper(level, filterBlockPos);
        if (source == null || source.isEmpty()) return false;

        // We can suck out of the items from the source if the following items are met.
        Container into = getDestinationHopper(level, filterBlockPos);
        boolean changed = false;

        for (int slot = 0; slot < source.getContainerSize(); ++slot) {
            ItemStack itemStack = source.getItem(slot);
            if (itemStack.isEmpty()) continue;
            if (!allowlist.test(itemStack)) continue;

            if (into == null) {
                dropItemStack(level, filterBlockPos, itemStack.split(1));
                source.setItem(slot, itemStack.isEmpty() ? ItemStack.EMPTY : itemStack);
                changed = true;
                break;
            }

            ItemStack toTransfer = itemStack.copyWithCount(1);
            ItemStack remaining = HopperBlockEntity.addItem(source, into, toTransfer, Direction.UP);
            if (remaining.isEmpty()) {
                itemStack.shrink(1);
                source.setItem(slot, itemStack.isEmpty() ? ItemStack.EMPTY : itemStack);
                changed = true;
            }

            // Do it one item at a time per tick
            break;
        }

        return changed;
    }


    private static void dropItemStack(Level level, BlockPos blockPos, ItemStack item) {
        double spawnX = blockPos.getX();
        double spawnY = blockPos.getY() - (double) 0.125F;
        double spawnZ = blockPos.getZ();

        ItemEntity droppedItem = new ItemEntity(level, spawnX, spawnY, spawnZ, item);
        droppedItem.setDeltaMovement(0, -1, 0);
        level.addFreshEntity(droppedItem);
    }

    private static void tryMoveItems(
        final Level level,
        final BlockPos pos,
        final BlockState state,
        final ItemFilterBlockEntity filterBlock
    ) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = trySuckItems(level, filterBlock);
        if (changed) {
            setChanged(level, pos, state);
        }
    }

    private static @Nullable HopperBlockEntity getSourceHopper(
        final Level level,
        final BlockPos filterBlockPos
    ) {
        BlockPos blockPos = filterBlockPos.above();
        return getHopper(level, blockPos, level.getBlockState(blockPos));
    }

    private static @Nullable HopperBlockEntity getDestinationHopper(
        final Level level,
        final BlockPos filterBlockPos
    ) {
        BlockPos blockPos = filterBlockPos.below();
        return getHopper(level, blockPos, level.getBlockState(blockPos));
    }

    private static @Nullable HopperBlockEntity getHopper(
        final Level level,
        final BlockPos pos,
        final BlockState state
    ) {
        if (!state.hasBlockEntity()) {
            return null;
        }

        BlockEntity entity = level.getBlockEntity(pos);
        Block block = state.getBlock();
        if (!(entity instanceof HopperBlockEntity hopper && block instanceof HopperBlock)) {
            return null;
        }

        return hopper;
    }
}
