package xyz.memothelemo.deathchest.mixin;

import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Inventory.class)
public interface DcInventory {
    @Accessor("equipment")
    @NonNull EntityEquipment getEquipment();
}
