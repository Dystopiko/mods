package xyz.memothelemo.deathchest.interfaces;

import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import xyz.memothelemo.deathchest.model.DeathChestDataEntry;
import xyz.memothelemo.edenmc.api.model.User;
import xyz.memothelemo.edenmc.api.model.organization.Member;

public interface DcServerPlayer {
    @Nullable Member dc$asEdenMember();
    @NonNull User dc$asEdenUser();

    void dc$applyFromData(
        @NonNull DeathChestDataEntry data,
        @NonNull Vec3 position
    );
}
