package xyz.memothelemo.dystopia.instantrestock.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.memothelemo.dystopia.instantrestock.InstantRestock;
import xyz.memothelemo.dystopia.instantrestock.interfaces.IRMerchantOffer;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {
    @Unique
    private boolean ir$hasAppliedEffect = false;

    public VillagerMixin(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
    }

    @Unique
    private void ir$applyEffect() {
        this.ir$hasAppliedEffect = true;
        for (MerchantOffer offer : this.getOffers()) {
            ((IRMerchantOffer) offer).ir$applyEffect();
        }
    }

    @Unique
    private void ir$rollbackEffect() {
        this.ir$hasAppliedEffect = false;
        for (MerchantOffer offer : this.getOffers()) {
            ((IRMerchantOffer) offer).ir$rollbackEffect();
        }
    }

    @Inject(
        method = "rewardTradeXp",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
        ),
        cancellable = true
    )
    @SuppressWarnings("resource")
    private void ir$reduceRewardXp(MerchantOffer merchantOffer, CallbackInfo ci, @Local int i) {
        if (!this.ir$hasAppliedEffect) return;

        ExperienceOrb orb = new ExperienceOrb(
            this.level(),
            this.getX(),
            this.getY() + (double) 0.5F,
            this.getZ(),
            (int)((float) i * 0.5F)
        );

        this.level().addFreshEntity(orb);
        ci.cancel();
    }

    @Inject(method = "onReputationEventFrom", at = @At("HEAD"), cancellable = true)
    private void ir$stopGossipingToTradingPlayer(ReputationEventType type, Entity source, CallbackInfo ci) {
        if (this.ir$hasAppliedEffect && type == ReputationEventType.TRADE && this.getTarget() == this.getTradingPlayer()) {
            ci.cancel();
        }
    }

    @Inject(method = "startTrading", at = @At("HEAD"))
    public void ir$tryApplyIREffect(Player maybeServerPlayer, CallbackInfo ci) {
        if (!(maybeServerPlayer instanceof ServerPlayer player)) return;
        if (!InstantRestock.canApplyIRForPlayer(player) || this.ir$hasAppliedEffect) return;
        this.ir$applyEffect();
    }

    @Inject(method = "stopTrading", at = @At("HEAD"))
    public void ir$disableIREffect(CallbackInfo ci) {
        if (this.ir$hasAppliedEffect) this.ir$rollbackEffect();
    }
}
