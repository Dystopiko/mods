package xyz.memothelemo.deathchest.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.memothelemo.deathchest.Components;
import xyz.memothelemo.deathchest.DeathChestMod;
import xyz.memothelemo.deathchest.interfaces.DcServerPlayer;
import xyz.memothelemo.deathchest.model.DeathChestDataEntry;
import xyz.memothelemo.edenmc.api.model.organization.Member;

import static xyz.memothelemo.deathchest.DeathChestMod.LOGGER;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(
        method = "handleBlockBreakAction",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;blockActionRestricted(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/GameType;)Z"
        ),
        cancellable = true)
    public void prohibitDestroyForDeathChests(
        CallbackInfo ci,
        @Local(argsOnly = true) BlockPos targetPosition
    ) {
        // Verify if the player is actually destroying a chest block
        if (!(this.level.getBlockEntity(targetPosition) instanceof ChestBlockEntity)) return;

        DeathChestDataEntry data = DeathChestMod.data.getDataForChest(targetPosition);
        if (data == null) return;

        Member member = ((DcServerPlayer) this.player).dc$asEdenMember();
        if (member == null || !member.getDiscordId().equals(data.ownerId())) {
            var packet = new ClientboundBlockUpdatePacket(
                targetPosition,
                this.level.getBlockState(targetPosition)
            );

            LOGGER.debug("[block_break_action] {} -> rejected", targetPosition);
            this.player.sendMessage(Components.ownedByError(data.ownerName()));
            this.player.connection.send(packet);
            ci.cancel();
        }
    }
}
