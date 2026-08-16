package xyz.memothelemo.deathchest.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record DeathChestDataEntry(
    // This refers to player's Discord ID
    @NonNull String ownerId,
    // Player's cached Discord ownerName
    @NonNull String ownerName,
    // Player's last inventory contents before they died
    @NonNull List<ItemStackWithSlot> inventory,
    // Player's last entity equipment contents before they died
    @NotNull EntityEquipment equipment
) {
    public static Codec<DeathChestDataEntry> CODEC = RecordCodecBuilder.create(instance ->
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
