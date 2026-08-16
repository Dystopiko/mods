package xyz.memothelemo.itemfilter.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import xyz.memothelemo.itemfilter.block.entity.ItemFilterBlockEntity;

import java.util.HashSet;
import java.util.Set;

// This class contains an inventory of items are accepted to pass through
public class ItemAllowlistInventory extends SimpleContainer {
    private static final int CONTAINER_SIZE = 27;

    private final @NonNull ItemFilterBlockEntity entity;
    private final @NonNull Set<Item> filterCache = new HashSet<>();

    public ItemAllowlistInventory(@NonNull ItemFilterBlockEntity entity) {
        super(CONTAINER_SIZE);
        this.entity = entity;
    }

    // This function has O(1) lookup instead of O(n) scan in naive approach.
    public boolean test(ItemStack itemStack) {
        return this.filterCache.contains(itemStack.getItem());
    }

    @Override
    public void setChanged() {
        this.entity.setChanged();
        this.rebuildFilterCache();
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack itemStack) {
        this.items.set(slot, itemStack);
        itemStack.limitSize(this.getMaxStackSize(itemStack));
        this.setChanged();
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return Container.stillValidBlockEntity(this.entity, player);
    }

    public void fromSlots(final ValueInput.TypedInputList<ItemStackWithSlot> list) {
        for (int i = 0; i < this.getContainerSize(); i++) {
            this.setItem(i, ItemStack.EMPTY);
        }

        for (ItemStackWithSlot item : list) {
            if (item.isValidInContainer(this.getContainerSize())) {
                this.setItem(item.slot(), item.stack());
            }
        }

        this.rebuildFilterCache();
    }

    public void storeAsSlots(final ValueOutput.TypedOutputList<ItemStackWithSlot> output) {
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack itemStack = this.getItem(i);
            if (!itemStack.isEmpty()) {
                output.add(new ItemStackWithSlot(i, itemStack));
            }
        }
    }

    private void rebuildFilterCache() {
        this.filterCache.clear();
        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty()) continue;
            this.filterCache.add(itemStack.getItem());
        }
    }
}
