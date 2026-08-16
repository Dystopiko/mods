package xyz.memothelemo.dystopia.deathchest.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable data record representing the contents and ownership information of a single death chest.
 *
 * @param ownerId the player's Discord ID (snowflake)
 * @param ownerName the player's cached display name
 * @param inventory the stored list of inventory items with slot indices
 * @param equipment the stored entity equipment stacks
 */
public record DeathChestDataEntry(
    @NonNull String ownerId,
    @NonNull String ownerName,
    @NonNull List<ItemStackWithSlot> inventory,
    @NotNull EntityEquipment equipment
) {
    /**
     * Codec for serializing and deserializing {@link DeathChestDataEntry} records.
     */
    public static final Codec<DeathChestDataEntry> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ExtraCodecs.NON_EMPTY_STRING
                .fieldOf("ownerId")
                .forGetter(DeathChestDataEntry::ownerId),
            ExtraCodecs.NON_EMPTY_STRING
                .fieldOf("ownerName")
                .forGetter(DeathChestDataEntry::ownerName),
            ItemStackWithSlot.CODEC
                .listOf()
                .optionalFieldOf("inventory", new ArrayList<>())
                .forGetter(DeathChestDataEntry::inventory),
            EntityEquipment.CODEC
                .optionalFieldOf("equipment", new EntityEquipment())
                .forGetter(DeathChestDataEntry::equipment)
        ).apply(instance, DeathChestDataEntry::new)
    );
}
