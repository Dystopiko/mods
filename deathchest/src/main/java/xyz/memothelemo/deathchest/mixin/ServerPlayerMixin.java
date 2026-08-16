package xyz.memothelemo.deathchest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.memothelemo.deathchest.Components;
import xyz.memothelemo.deathchest.DeathChestMod;
import xyz.memothelemo.deathchest.InventorySolver;
import xyz.memothelemo.deathchest.PlacementSolver;
import xyz.memothelemo.deathchest.interfaces.DcChestBlockEntity;
import xyz.memothelemo.deathchest.interfaces.DcServerPlayer;
import xyz.memothelemo.deathchest.model.DeathChestDataEntry;
import xyz.memothelemo.edenmc.api.EdenProvider;
import xyz.memothelemo.edenmc.api.model.User;
import xyz.memothelemo.edenmc.api.model.organization.Member;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements DcServerPlayer {
    @Shadow
    public abstract ServerLevel level();

    @Shadow
    @Final
    private static Logger LOGGER;

    @Override @Unique
    public void dc$applyFromData(
        @NonNull DeathChestDataEntry data,
        @NonNull Vec3 position
    ) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel level = player.level();
        Member member = this.dc$asEdenMember();

        if (member == null) return;
        assert player != null && member.getDiscordId().equals(data.ownerId());

        Inventory inventory = player.getInventory();
        InventorySolver.resolveAndDropConflicts(
            level,
            position,
            data.inventory(),
            (saved) -> inventory.getItem(saved.slot()),

            // onEmpty
            (saved, $) -> inventory.setItem(saved.slot(), saved.stack()),

            // dropResolver
            (saved, current) -> {
                int freeSlot = inventory.getFreeSlot();
                inventory.setItem(saved.slot(), saved.stack());

                if (freeSlot == -1) return current;
                inventory.setItem(freeSlot, current);

                return null;
            }
        );

        // For backwards compatibility with entries that stored equipment separately:
        // Only set equipment if the slot is currently empty.
        EntityEquipment currentEquipment = ((DcInventory) (inventory)).getEquipment();
        EntityEquipment lastEquipment = data.equipment();
        if (lastEquipment != null) {
            List<EquipmentSlot> possibleSlots = List.of(
                EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.OFFHAND
            );
            for (EquipmentSlot slot : possibleSlots) {
                if (currentEquipment.get(slot).isEmpty() && !lastEquipment.get(slot).isEmpty()) {
                    currentEquipment.set(slot, lastEquipment.get(slot).copy());
                }
            }
        }
    }

    @Override @Unique
    public @Nullable Member dc$asEdenMember() {
        return this.dc$asEdenUser() instanceof Member member ? member : null;
    }

    @Override @Unique
    public @NonNull User dc$asEdenUser() {
        ServerPlayer player = (ServerPlayer) (Object) this;
        User user = EdenProvider.get().getUser(player.getUUID());
        assert user != null;
        return user;
    }

    @Unique
    private void dc$dropInventoryFromOldState(ServerPlayer oldState) {
        oldState.getInventory().dropAll();
    }

    @Inject(
        method = "restoreFrom",
        // The other condition is when the player was from the end dimension.
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setHealth(F)V"
        )
    )
    @SuppressWarnings("resource")
    private void dc$tryPlaceDeathChest(ServerPlayer oldState, boolean restoreAll, CallbackInfo ci) {
        // restoreFrom is also called when the player jumps into the End portal
        if (restoreAll) return;

        boolean keepInventory = this.level().getGameRules().get(GameRules.KEEP_INVENTORY);
        boolean hasPermissions = DeathChestMod.hasPermissions(oldState);

        Member member = this.dc$asEdenMember();
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (keepInventory || !hasPermissions || oldState.isSpectator() || member == null) {
            return;
        }

        boolean shouldPlaceDeathChest = !oldState.getInventory().isEmpty();
        if (!shouldPlaceDeathChest) {
            player.sendMessage(Components.IMPRACTICAL_TO_PLACE);
            return;
        }

        Level lastLevel = oldState.level();
        BlockPos chestPosition = PlacementSolver.findNearestForBlock(lastLevel, oldState.blockPosition());
        if (chestPosition == null) {
            player.sendMessage(Components.FAILED_TO_GET_POSITION);
            this.dc$dropInventoryFromOldState(oldState);
            return;
        }

        LOGGER.debug("Generating death chest at {} for {}", chestPosition, oldState.getUUID());
        lastLevel.setBlockAndUpdate(chestPosition, Blocks.CHEST.defaultBlockState());

        BlockEntity block = lastLevel.getBlockEntity(chestPosition);
        String dimension = lastLevel.dimension().identifier().getPath();
        if (block instanceof ChestBlockEntity chest) {
            ((DcChestBlockEntity) chest).dc$setAsDeathChest(player, oldState, member);
            player.sendMessage(Components.newDeathChest(chestPosition, dimension));
        } else {
            throw new IllegalStateException("Newly placed death chest should exists!");
        }
    }
}

