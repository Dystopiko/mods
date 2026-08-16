package xyz.memothelemo.deathchest.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.memothelemo.deathchest.Components;
import xyz.memothelemo.deathchest.DeathChestMod;
import xyz.memothelemo.deathchest.interfaces.DcServerPlayer;
import xyz.memothelemo.deathchest.model.DeathChestDataEntry;
import xyz.memothelemo.edenmc.api.model.organization.Member;

import static xyz.memothelemo.deathchest.DeathChestMod.LOGGER;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    @Inject(
        method = "useWithoutItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/ChestBlock;getMenuProvider(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/MenuProvider;"
        ),
        cancellable = true
    )
    public void dc$onChestClicked(
        CallbackInfoReturnable<InteractionResult> cir,
        @Local(argsOnly = true) Player $,
        @Local(argsOnly = true) Level $$,
        @Local(argsOnly = true) BlockPos chestPos
    ) {
        ServerPlayer player = (ServerPlayer) $;
        ServerLevel level = (ServerLevel) $$;

        // Get the associated data of the death chest
        DeathChestDataEntry data = DeathChestMod.data.getDataForChest(chestPos);
        if (data == null) return;

        Member member = ((DcServerPlayer) player).dc$asEdenMember();
        if (member == null || !member.getDiscordId().equals(data.ownerId())) {
            player.sendMessage(Components.ownedByError(data.ownerName()));
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        LOGGER.debug("[on_chest_clicked({})] setting to air", chestPos);

        // Remove the chest, but safely
        try {
            level.setBlockAndUpdate(chestPos, Blocks.AIR.defaultBlockState());
        } catch (Exception ex) {
            player.sendMessage(Components.CANNOT_OPEN_CHEST);
            LOGGER.warn("Failed to destroy death chest", ex);
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        LOGGER.debug(
            "[on_chest_clicked({})] applying death chest data to player (data={})",
            chestPos, data
        );

        Vec3 exactPlayerPosition = player.position();
        level.playSound(
            null,
            exactPlayerPosition.x,
            exactPlayerPosition.y,
            exactPlayerPosition.z,
            SoundEvents.PLAYER_LEVELUP,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        ((DcServerPlayer) player).dc$applyFromData(data, chestPos.getCenter());
        player.sendMessage(Components.CLAIMED);
        DeathChestMod.data.onDeathChestRemoved(member, chestPos);
    }
}
