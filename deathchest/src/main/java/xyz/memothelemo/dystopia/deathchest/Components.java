package xyz.memothelemo.dystopia.deathchest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.BlockPos;

/**
 * Utility class containing formatted Adventure and Minecraft components for DeathChest user messages.
 */
public final class Components {
    /**
     * Standard prefix header component appended to all DeathChest messages.
     */
    public static final Component DEATH_CHEST_HEADER = Component.text("[DeathChest]")
        .color(NamedTextColor.LIGHT_PURPLE)
        .decorate(TextDecoration.BOLD)
        .appendSpace();

    /**
     * Message displayed when a player dies with an empty inventory.
     */
    public static final Component IMPRACTICAL_TO_PLACE = Component.text()
        .append(DEATH_CHEST_HEADER)
        .append(Component.text("Your inventory is empty. No death chest is generated.")
            .color(NamedTextColor.RED))
        .build();

    /**
     * Message displayed when no suitable location could be found to place a death chest.
     */
    public static final Component FAILED_TO_GET_POSITION = Component.text()
        .append(DEATH_CHEST_HEADER)
        .append(Component.text("I cannot find a good place to put your death chest.")
            .color(NamedTextColor.RED))
        .appendNewline()
        .appendNewline()
        .append(Component.text("Your last items are dropped automatically.")
            .color(NamedTextColor.YELLOW))
        .build();

    /**
     * Message displayed when a death chest cannot be opened.
     */
    public static final Component CANNOT_OPEN_CHEST = Component.text()
        .append(DEATH_CHEST_HEADER)
        .append(Component.text("I can't open your death chest at this time. Please try again later.")
            .color(NamedTextColor.RED))
        .build();

    /**
     * Message displayed when a death chest is successfully claimed.
     */
    public static final Component CLAIMED = Component.text()
        .append(DEATH_CHEST_HEADER)
        .append(Component.text("Successfully claimed death chest.").color(NamedTextColor.GREEN))
        .build();

    /**
     * Default title string for the Item Filter inventory GUI.
     */
    public static final String ITEM_FILTER_TITLE = "Item Filter";

    /**
     * Title component for the Item Filter inventory GUI.
     */
    public static final net.minecraft.network.chat.Component ITEM_FILTER_COMPONENT =
        net.minecraft.network.chat.Component.literal(ITEM_FILTER_TITLE);

    private Components() {}

    /**
     * Constructs an error message indicating that a death chest is owned by another player.
     *
     * @param owner the name of the owner
     * @return the formatted error message component
     */
    public static Component ownedByError(String owner) {
        return Component.text()
            .append(DEATH_CHEST_HEADER)
            .append(Component.text("This chest is owned by ").color(NamedTextColor.RED))
            .append(Component.text(owner).color(NamedTextColor.GOLD))
            .build();
    }

    /**
     * Formats a block position as a gold text component.
     *
     * @param position the block position to format
     * @return the formatted position component
     */
    public static Component forPosition(BlockPos position) {
        return Component.text()
            .color(NamedTextColor.GOLD)
            .append(Component.text(position.getX()))
            .append(Component.text(", "))
            .append(Component.text(position.getY()))
            .append(Component.text(", "))
            .append(Component.text(position.getZ()))
            .build();
    }

    /**
     * Constructs a notification component when a death chest is destroyed by an explosion.
     *
     * @param position the position of the chest
     * @return the formatted explosion message component
     */
    public static Component destroyedByExplosion(BlockPos position) {
        return Component.text()
            .append(DEATH_CHEST_HEADER)
            .append(Component.text("You exploded near the chest at").color(NamedTextColor.YELLOW))
            .append(forPosition(position))
            .appendSpace()
            .append(Component.text("; items inside are dropped automatically.").color(NamedTextColor.YELLOW))
            .build();
    }

    /**
     * Constructs a notification component informing a player of their newly generated death chest location.
     *
     * @param position the block position of the chest
     * @param dimension the dimension name where the chest was created
     * @return the formatted new death chest message component
     */
    public static Component newDeathChest(BlockPos position, String dimension) {
        return Component.text()
            .append(DEATH_CHEST_HEADER)
            .append(Component.text("Your new death chest is at: "))
            .append(forPosition(position))
            .append(Component.text(" in "))
            .append(Component.text(dimension).color(NamedTextColor.GOLD))
            .append(Component.text(" dimension."))
            .build();
    }
}
