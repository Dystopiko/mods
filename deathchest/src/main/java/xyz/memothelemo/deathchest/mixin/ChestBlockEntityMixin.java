package xyz.memothelemo.deathchest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.memothelemo.deathchest.DeathChestMod;
import xyz.memothelemo.deathchest.interfaces.DcChestBlockEntity;
import xyz.memothelemo.deathchest.model.DeathChestDataEntry;
import xyz.memothelemo.edenmc.api.model.organization.Member;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin
    extends BaseContainerBlockEntity
    implements DcChestBlockEntity
{
    protected ChestBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override @Unique
    public void dc$setAsDeathChest(
        @NonNull ServerPlayer owner,
        @NonNull ServerPlayer oldState,
        @NonNull Member member
    ) {
        Inventory inventory = oldState.getInventory();

        List<ItemStackWithSlot> items = new ArrayList<>();
        ValueOutput.TypedOutputList<ItemStackWithSlot> pointer = new ValueOutput.TypedOutputList<>() {
            @Override
            public void add(ItemStackWithSlot object) {
                items.add(object);
            }

            @Override
            public boolean isEmpty() {
                return items.isEmpty();
            }
        };
        inventory.save(pointer);

        DeathChestDataEntry data = new DeathChestDataEntry(
            member.getDiscordId(),
            member.getDiscordName(),
            items,
            ((DcInventory) inventory).getEquipment()
        );
        DeathChestMod.data.onNewDeathChest(owner, member, this.getBlockPos(), data);
    }
}
