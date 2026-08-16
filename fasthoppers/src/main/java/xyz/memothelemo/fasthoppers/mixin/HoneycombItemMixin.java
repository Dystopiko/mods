package xyz.memothelemo.fasthoppers.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.memothelemo.fasthoppers.FastHoppers;
import xyz.memothelemo.fasthoppers.IsHopperWaxed;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
    @Unique
    private static Component fasthoppers$makeAlertMessage(BlockPos blockPos) {
        Component position = Component.literal(blockPos.toShortString()).withStyle(ChatFormatting.GOLD);
        return Component.literal("Successfully waxed hopper at ")
                .withStyle(ChatFormatting.GREEN)
                .append(position);
    }

    @Inject(
        method = "useOn",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void fasthoppers$upgradeToFastHopper(
        UseOnContext context,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.HOPPER) && level.getBlockEntity(pos) instanceof HopperBlockEntity hopper) {
            if (IsHopperWaxed.get(hopper)) return;
            if (level.isClientSide()) {
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }

            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (!FastHoppers.canWaxHoppers(player)) return;

            ItemStack itemInHand = context.getItemInHand();
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, pos, itemInHand);

            itemInHand.shrink(1);
            IsHopperWaxed.set(hopper, true);

            level.playSound(
                null, pos,
                net.minecraft.sounds.SoundEvents.HONEYCOMB_WAX_ON,
                net.minecraft.sounds.SoundSource.BLOCKS,
                1.0F, 1.0F
            );

            player.sendOverlayMessage(fasthoppers$makeAlertMessage(pos));
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}

