package xyz.memothelemo.dystopia.deathchest.interfaces;

import net.minecraft.network.chat.Component;

/**
 * Interface injected into container block entities to allow setting custom display names for death chests.
 */
public interface DcBaseContainerBlockEntity {
    /**
     * Sets the custom display name component of this container block entity.
     *
     * @param component the text component representing the custom name
     */
    void dc$setCustomName(Component component);
}
