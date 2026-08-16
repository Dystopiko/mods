package xyz.memothelemo.deathchest.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.memothelemo.deathchest.interfaces.DcBaseContainerBlockEntity;

@Mixin(BaseContainerBlockEntity.class)
public class BaseContainerBlockEntityMixin implements DcBaseContainerBlockEntity {
    @Shadow
    private @Nullable Component name;

    @Override
    public void dc$setCustomName(Component component) {
        this.name = component;
    }
}
