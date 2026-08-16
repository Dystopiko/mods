package xyz.memothelemo.deathchest.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.memothelemo.deathchest.DeathChestMod;

import static xyz.memothelemo.deathchest.DeathChestMod.LOGGER;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow
    protected abstract void destroyVanishingCursedItems();

    @Inject(method = "dropEquipment", at = @At("HEAD"), order = 5000, cancellable = true)
    private void dc$keepInventory(ServerLevel level, CallbackInfo ci) {
        boolean keepInventory = level.getGameRules().get(GameRules.KEEP_INVENTORY);
        boolean hasPermissions = DeathChestMod.hasPermissions((ServerPlayer) (Object) this);
        if (keepInventory || !hasPermissions) return;

        LOGGER.debug("[drop_equipment] kept inventory");
        this.destroyVanishingCursedItems();
        ci.cancel();
    }
}
