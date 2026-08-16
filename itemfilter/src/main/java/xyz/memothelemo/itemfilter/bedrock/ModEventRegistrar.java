package xyz.memothelemo.itemfilter.bedrock;

import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.block.custom.CustomBlockData;
import org.geysermc.geyser.api.block.custom.component.BoxComponent;
import org.geysermc.geyser.api.block.custom.component.CustomBlockComponents;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;

@SuppressWarnings("unused")
public class ModEventRegistrar implements EventRegistrar {
    @Subscribe
    public void onDefineCustomBlocks(GeyserDefineCustomBlocksEvent event) {
//        CustomBlockComponents components = CustomBlockComponents.builder()
//            .collisionBoxes(BoxComponent.fullBox())
//            .selectionBox(BoxComponent.fullBox())
//            .build();
//
//        CustomBlockData itemFilterBlock = CustomBlockData.builder()
//            .name("item_filter")
//            .components(components)
//            .includedInCreativeInventory(true)
//            .build();
//
//        event.register(itemFilterBlock);
//        event.registerItemOverride("itemfilter:item_filter", itemFilterBlock);
    }
}
