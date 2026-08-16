package xyz.memothelemo.deathchest.interfaces;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import xyz.memothelemo.edenmc.api.model.organization.Member;

public interface DcChestBlockEntity {
    void dc$setAsDeathChest(
        ServerPlayer owner,
        ServerPlayer oldState,
        Member member
    );
}
