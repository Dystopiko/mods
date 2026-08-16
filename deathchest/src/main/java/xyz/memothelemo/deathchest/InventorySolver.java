package xyz.memothelemo.deathchest;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import xyz.memothelemo.deathchest.model.DeathChestDataEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InventorySolver {
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

    public static void dropItem(ServerLevel level, Vec3 position, ItemStack stack) {
        if (stack.isEmpty()) return;

        ItemEntity entity = new ItemEntity(level, position.x, position.y, position.z, stack);
        entity.setPickUpDelay(40);
        level.addFreshEntity(entity);
    }

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
