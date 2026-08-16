package xyz.memothelemo.dystopia.fasthoppers.mixin;

import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.memothelemo.dystopia.fasthoppers.IsHopperWaxed;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Shadow
    private int cooldownTime;

    @Inject(method = "setCooldown", at = @At("HEAD"), cancellable = true)
    private void fasthoppers$overrideCooldown(int time, CallbackInfo ci) {
        if (time > 1 && IsHopperWaxed.get((HopperBlockEntity) (Object) this)) {
            this.cooldownTime = 1;
            ci.cancel();
        }
    }
}