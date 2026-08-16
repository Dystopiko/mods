package xyz.memothelemo.dystopia.common.constant;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.BlockPos;

public final class MessageConstants {
    // General Headers
    public static final Component DEATHCHEST_HEADER = Component.text("[DeathChest]")
        .color(NamedTextColor.LIGHT_PURPLE)
        .decorate(TextDecoration.BOLD)
        .appendSpace();

    // DeathChest Message Components
    public static final Component IMPRACTICAL_TO_PLACE = Component.text()
        .append(DEATHCHEST_HEADER)
        .append(Component.text("Your inventory is empty. No death chest is generated.")
            .color(NamedTextColor.RED))
        .build();

    public static final Component FAILED_TO_GET_POSITION = Component.text()
        .append(DEATHCHEST_HEADER)
        .append(Component.text("I cannot find a good place to put your death chest.")
            .color(NamedTextColor.RED))
        .appendNewline()
        .appendNewline()
        .append(Component.text("Your last items are dropped automatically.")
            .color(NamedTextColor.YELLOW))
        .build();

    public static final Component CANNOT_OPEN_CHEST = Component.text()
        .append(DEATHCHEST_HEADER)
        .append(Component.text("I can't open your death chest at this time. Please try again later.")
            .color(NamedTextColor.RED))
        .build();

    public static final Component CLAIMED = Component.text()
        .append(DEATHCHEST_HEADER)
        .append(Component.text("Successfully claimed death chest.").color(NamedTextColor.GREEN))
        .build();

    // ItemFilter Titles & Components
    public static final String ITEM_FILTER_TITLE = "Item Filter";
    public static final net.minecraft.network.chat.Component ITEM_FILTER_COMPONENT =
        net.minecraft.network.chat.Component.literal(ITEM_FILTER_TITLE);

    private MessageConstants() {}

    public static Component ownedByError(String owner) {
        return Component.text()
            .append(DEATHCHEST_HEADER)
            .append(Component.text("This chest is owned by ").color(NamedTextColor.RED))
            .append(Component.text(owner).color(NamedTextColor.GOLD))
            .build();
    }

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

    public static Component destroyedByExplosion(BlockPos position) {
        return Component.text()
            .append(DEATHCHEST_HEADER)
            .append(Component.text("You exploded near the chest at").color(NamedTextColor.YELLOW))
            .append(forPosition(position))
            .appendSpace()
            .append(Component.text("; items inside are dropped automatically.").color(NamedTextColor.YELLOW))
            .build();
    }

    public static Component newDeathChest(BlockPos position, String dimension) {
        return Component.text()
            .append(DEATHCHEST_HEADER)
            .append(Component.text("Your new death chest is at: "))
            .append(forPosition(position))
            .append(Component.text(" in "))
            .append(Component.text(dimension).color(NamedTextColor.GOLD))
            .append(Component.text(" dimension."))
            .build();
    }
}
