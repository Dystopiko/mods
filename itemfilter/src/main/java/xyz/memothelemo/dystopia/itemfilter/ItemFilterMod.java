package xyz.memothelemo.dystopia.itemfilter;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.geysermc.geyser.api.GeyserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.memothelemo.dystopia.itemfilter.bedrock.ModEventRegistrar;
import xyz.memothelemo.dystopia.itemfilter.block.ModBlocks;

public class ItemFilterMod implements ModInitializer {
	public static final String MOD_ID = "itemfilter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PolymerResourcePackUtils.addModAssets(ItemFilterMod.MOD_ID);
		PolymerResourcePackUtils.markAsRequired();
		ModBlocks.initialize();

		ServerLifecycleEvents.SERVER_STARTING.register((_) -> {
			tryInitBedrockSupport();
		});
	}

	private void tryInitBedrockSupport() {
		try {
			GeyserApi api = GeyserApi.api();
			ModEventRegistrar registrar = new ModEventRegistrar();
			api.eventBus().register(registrar, registrar);
			
			LOGGER.info("Successfully added support for Bedrock");
		} catch (RuntimeException _) {}
	}
}
