package xyz.memothelemo.deathchest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.BlockPos;

public class Components {
    public static Component HEADER = Component.text("[DeathChest]")
        .color(NamedTextColor.LIGHT_PURPLE)
        .decorate(TextDecoration.BOLD)
        .appendSpace();

    public static Component IMPRACTICAL_TO_PLACE;
    public static Component FAILED_TO_GET_POSITION;
    public static Component CANNOT_OPEN_CHEST;
    public static Component CLAIMED;

    public static Component ownedByError(String owner) {
        return Component.text()
            .append(HEADER)
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
            .append(HEADER)
            .append(Component.text("You exploded near the chest at").color(NamedTextColor.YELLOW))
            .append(forPosition(position))
            .appendSpace()
            .append(Component.text("; items inside are dropped automatically.").color(NamedTextColor.YELLOW))
            .build();
    }

    public static Component newDeathChest(BlockPos position, String dimension) {
        return Component.text()
            .append(HEADER)
            .append(Component.text("Your new death chest is at: "))
            .append(forPosition(position))
            .append(Component.text(" in "))
            .append(Component.text(dimension).color(NamedTextColor.GOLD))
            .append(Component.text(" dimension."))
            .build();
    }

    static {
        IMPRACTICAL_TO_PLACE = Component.text()
            .append(HEADER)
            .append(Component.text("Your inventory is empty. No death chest is generated.")
                .color(NamedTextColor.RED))
            .build();

        FAILED_TO_GET_POSITION = Component.text()
            .append(HEADER)
            .append(Component.text("I cannot find a good place to put your death chest.")
                .color(NamedTextColor.RED))
            .appendNewline()
            .appendNewline()
            .append(Component.text("Your last items are dropped automatically.")
                .color(NamedTextColor.YELLOW))
            .build();

        CANNOT_OPEN_CHEST = Component.text()
            .append(HEADER)
            .append(Component.text("I can't open your death chest at this time. Please try again later.")
                .color(NamedTextColor.RED))
            .build();

        CLAIMED = Component.text()
            .append(HEADER)
            .append(Component.text("Successfully claimed death chest.").color(NamedTextColor.GREEN))
            .build();
    }
}
