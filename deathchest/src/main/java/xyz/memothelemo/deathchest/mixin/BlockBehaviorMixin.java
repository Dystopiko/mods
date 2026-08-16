package xyz.memothelemo.deathchest.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.memothelemo.deathchest.Components;
import xyz.memothelemo.deathchest.DeathChestMod;
import xyz.memothelemo.deathchest.InventorySolver;
import xyz.memothelemo.deathchest.interfaces.DcServerPlayer;
import xyz.memothelemo.deathchest.model.DeathChestDataEntry;
import xyz.memothelemo.edenmc.api.model.organization.Member;

import static xyz.memothelemo.deathchest.DeathChestMod.LOGGER;

@Mixin(BlockBehaviour.class)
public class BlockBehaviorMixin {
    @Inject(
        method = "onExplosionHit",
        at = @At("HEAD"),
        cancellable = true
    )
    public void dc$cancelExplosionHitForDeathChest(
        CallbackInfo ci,
        @Local(argsOnly = true) Explosion explosion,
        @Local(argsOnly = true) ServerLevel level,
        @Local(argsOnly = true) BlockPos position
    ) {
        BlockEntity entity = level.getBlockEntity(position);
        if (!(entity instanceof ChestBlockEntity)) return;

        // Get the associated data of the death chest
        DeathChestDataEntry data = DeathChestMod.data.getDataForChest(position);
        if (data == null) return;

        LivingEntity entityAtFault = explosion.getIndirectSourceEntity();
        Vec3 directlyToChestPos = new Vec3(position.getX(), position.getY(), position.getZ());
        LOGGER.debug("[on_explosion_hit({})]: i got hit", position);

        if (entityAtFault instanceof ServerPlayer player) {
            Member member = ((DcServerPlayer) player).dc$asEdenMember();
            if (member != null && member.getDiscordId().equals(data.ownerId())) {
                LOGGER.debug("[on_explosion_hit({})]: owner is at fault for exploding; destroying block", position);
                InventorySolver.dropAllItems(data, level, directlyToChestPos);
                DeathChestMod.data.onDeathChestRemoved(member, position);
                player.sendMessage(Components.destroyedByExplosion(position));
                return;
            }
        }

        LOGGER.debug("[on_explosion_hit({})]: owner is not at fault for exploding", position);
        ci.cancel();
    }
}
