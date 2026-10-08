package com.lokro.flexhud.client;

import com.lokro.flexhud.FlexhudMod;
import com.lokro.flexhud.client.cmd.FlexhudCommands;
import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.hud.WidgetHud;
import com.lokro.flexhud.client.hud.WidgetValues;
import com.lokro.flexhud.client.key.FlexhudKeys;
import com.lokro.flexhud.client.market.MarketplaceCache;
import com.lokro.flexhud.client.market.UpdateChecker;
import com.lokro.flexhud.client.state.TimerState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

/** Client-Entrypoint (alles client-side). */
public class FlexhudClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FlexhudConfig.load();
		TimerState.load();
		FlexhudKeys.register();
		FlexhudCommands.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			TimerState.tick();
			WidgetValues.tick(client);
			FlexhudKeys.tick(client);
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			MarketplaceCache.refreshAsyncIfStale();
			WidgetValues.onWorldChange();
			UpdateChecker.notifyOnJoin();
		});

		HudElementRegistry.attachElementBefore(
			VanillaHudElements.CHAT,
			FlexhudMod.id("widgets"),
			(graphics, deltaTracker) -> WidgetHud.render(graphics)
		);

		MarketplaceCache.refreshAsyncIfStale();

		FlexhudMod.LOGGER.info("[FlexHUD] Client initialisiert: Hotkey H, /flexhud, /timer. Widgets: {}",
			FlexhudConfig.get().widgets.size());
		FlexhudMod.LOGGER.info("[FlexHUD] Version: {}, Marketplace-URL: {}",
			modVersion(), FlexhudConfig.get().marketplaceUrl);
	}

	private static String modVersion() {
		try {
			return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("flexhud")
				.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?");
		} catch (Exception e) {
			return "?";
		}
	}
}
