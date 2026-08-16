package xyz.memothelemo.dystopia.deathchest;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import xyz.memothelemo.dystopia.deathchest.model.DeathChestDataEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utility class for handling inventory item drops, equipment resolution, and conflict management during death chest operations.
 */
public class InventorySolver {
    /**
     * Drops all inventory and equipment items contained within a death chest data entry into the world.
     *
     * @param entry the death chest data entry containing stored items
     * @param level the server level where items will spawn
     * @param position the 3D position vector where items will drop
     */
    public static void dropAllItems(DeathChestDataEntry entry, ServerLevel level, Vec3 position) {
        for (ItemStackWithSlot item : entry.inventory()) {
            ItemEntity entity = new ItemEntity(
                level,
                position.x,
                position.y,
                position.z,
                item.stack()
            );
            level.addFreshEntity(entity);
        }

        List<EquipmentSlot> possibleSlots = new ArrayList<>(EquipmentSlot.VALUES);
        possibleSlots.remove(EquipmentSlot.MAINHAND);

        for (EquipmentSlot slot : possibleSlots) {
            ItemEntity entity = new ItemEntity(
                level,
                position.x,
                position.y,
                position.z,
                entry.equipment().get(slot)
            );
            level.addFreshEntity(entity);
        }
    }

    /**
     * Drops a single item stack into the world with a default pickup delay.
     *
     * @param level the server level
     * @param position the position vector
     * @param stack the item stack to drop
     */
    public static void dropItem(ServerLevel level, Vec3 position, ItemStack stack) {
        if (stack.isEmpty()) return;

        ItemEntity entity = new ItemEntity(level, position.x, position.y, position.z, stack);
        entity.setPickUpDelay(40);
        level.addFreshEntity(entity);
    }

    /**
     * Iterates over a list of items, resolving conflicts between existing items and new item stacks, dropping conflicting items into the world.
     *
     * @param <A> the type of item entry being processed
     * @param level the server level
     * @param position the spawn position vector
     * @param list the list of item entries to resolve
     * @param currentGetter function to extract the current item stack from an entry
     * @param onEmpty consumer called when an entry is empty
     * @param dropResolver function to determine which item stack should be dropped on conflict
     */
    public static <A> void resolveAndDropConflicts(
        ServerLevel level,
        Vec3 position,
        List<A> list,
        Function<A, @NonNull ItemStack> currentGetter,
        BiConsumer<A, @NonNull ItemStack> onEmpty,
        BiFunction<A, ItemStack, @Nullable ItemStack> dropResolver
    ) {
        for (A entry : list) {
            ItemStack output = currentGetter.apply(entry);
            if (output.isEmpty()) {
                onEmpty.accept(entry, output);
                continue;
            }

            ItemStack droppableItem = dropResolver.apply(entry, output);
            if (droppableItem == null) continue;
            dropItem(level, position, droppableItem);
        }
    }
}
