package xyz.memothelemo.instantrestock.mixin;

import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.memothelemo.instantrestock.interfaces.IRMerchantOffer;

@Mixin(MerchantOffer.class)
public abstract class MerchantOfferMixin implements IRMerchantOffer {
    // Making this mod less overpowered by increasing the prices by a certain percentage
    @Unique private static final float ir$PRICE_INCREASE_MULTIPLER = 0.55F;
    @Unique private boolean ir$appliedIREffect = false;
    // This injected method is needed because `out of stock` byte
    // is included in the MerchantOffer codec.
    @Inject(method = "isOutOfStock", at = @At("HEAD"), cancellable = true)
    public void ir$overrideIsOutOfStock(CallbackInfoReturnable<Boolean> cir) {
        if (this.ir$appliedIREffect) cir.setReturnValue(false);
    }

    @Inject(method = "getMaxUses", at = @At("HEAD"), cancellable = true)
    public void ir$overrideMaxUses(CallbackInfoReturnable<Integer> cir) {
        if (this.ir$appliedIREffect) cir.setReturnValue(Integer.MAX_VALUE);
    }

    @Inject(method = "getUses", at = @At("HEAD"), cancellable = true)
    public void ir$overrideUses(CallbackInfoReturnable<Integer> cir) {
        if (this.ir$appliedIREffect) cir.setReturnValue(0);
    }

    @Inject(method = "needsRestock", at = @At("HEAD"), cancellable = true)
    public void ir$overrideNeedsRestock(CallbackInfoReturnable<Boolean> cir) {
        if (this.ir$appliedIREffect) cir.setReturnValue(false);
    }

    @Inject(method = "increaseUses", at = @At("HEAD"), cancellable = true)
    public void ir$cancelIncreaseUses(CallbackInfo ci) {
        if (this.ir$appliedIREffect) ci.cancel();
    }

    @Inject(method = "copy", at = @At("TAIL"))
    public void ir$copyAppliedIREffect(CallbackInfoReturnable<MerchantOffer> cir) {
        MerchantOfferMixin offer = (MerchantOfferMixin) (IRMerchantOffer) cir.getReturnValue();
        assert offer != null;
        offer.ir$appliedIREffect = this.ir$appliedIREffect;
    }

    @Override
    public void ir$applyEffect() {
        this.ir$appliedIREffect = true;
    }

    @Override
    public void ir$rollbackEffect() {
        this.ir$appliedIREffect = false;
    }
}
