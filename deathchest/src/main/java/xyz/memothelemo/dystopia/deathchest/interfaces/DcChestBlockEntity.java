package xyz.memothelemo.dystopia.deathchest.interfaces;

import net.minecraft.server.level.ServerPlayer;
import xyz.memothelemo.edenmc.api.model.organization.Member;

/**
 * Interface injected into chest block entities for initializing death chest metadata and contents.
 */
public interface DcChestBlockEntity {
    /**
     * Configures this chest block entity as a death chest for a player.
     *
     * @param owner the active server player
     * @param oldState the server player state prior to respawning
     * @param member the Eden member profile associated with the player
     */
    void dc$setAsDeathChest(
        ServerPlayer owner,
        ServerPlayer oldState,
        Member member
    );
}
