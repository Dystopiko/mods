package xyz.memothelemo.dystopia.deathchest.interfaces;

import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import xyz.memothelemo.dystopia.deathchest.model.DeathChestDataEntry;
import xyz.memothelemo.edenmc.api.model.User;
import xyz.memothelemo.edenmc.api.model.organization.Member;

/**
 * Interface injected into {@link net.minecraft.server.level.ServerPlayer} to access Eden profile mappings and restore death chest inventories.
 */
public interface DcServerPlayer {
    /**
     * Resolves the Eden member representation associated with this server player.
     *
     * @return the {@link Member} instance if valid, or {@code null}
     */
    @Nullable Member dc$asEdenMember();

    /**
     * Resolves the Eden user representation associated with this server player.
     *
     * @return the non-null {@link User} instance
     */
    @NonNull User dc$asEdenUser();

    /**
     * Restores inventory contents and equipment from a death chest data entry into the player's inventory.
     *
     * @param data the death chest data entry containing saved items
     * @param position the world position vector where the chest was claimed
     */
    void dc$applyFromData(
        @NonNull DeathChestDataEntry data,
        @NonNull Vec3 position
    );
}
