package xyz.memothelemo.dystopia.fasthoppers;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class IsHopperWaxed {
    private static final AttachmentType<Boolean> VALUE = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(FastHoppers.MOD_ID, "waxed"),
        builder -> builder.initializer(() -> false).persistent(Codec.BOOL)
    );

    public static void set(@NonNull AttachmentTarget target, boolean value) {
        target.setAttached(VALUE, value);
    }

    public static boolean get(AttachmentTarget target) {
        return target.getAttachedOrElse(VALUE, false);
    }
}
