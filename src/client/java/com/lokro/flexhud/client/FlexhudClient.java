package com.lokro.flexhud.client;

import com.lokro.flexhud.FlexhudMod;
import com.lokro.flexhud.client.cmd.FlexhudCommands;
import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.hud.WidgetHud;
import com.lokro.flexhud.client.key.FlexhudKeys;
import com.lokro.flexhud.client.market.MarketplaceCache;
import com.lokro.flexhud.client.state.TimerState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

/**
 * Client-Entrypoint: Config laden, HUD-Element, Ticks, Keybind, Commands.
 * Alles client-side – läuft auch auf Servern ohne Server-Mod.
 */
public class FlexhudClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FlexhudConfig.load();
		TimerState.load();
		FlexhudKeys.register();
		FlexhudCommands.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			TimerState.tick();
			FlexhudKeys.tick(client);
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
			MarketplaceCache.refreshAsyncIfStale());

		HudElementRegistry.attachElementBefore(
			VanillaHudElements.CHAT,
			FlexhudMod.id("widgets"),
			(graphics, deltaTracker) -> WidgetHud.render(graphics)
		);

		// Marketplace-JSON beim Start im Hintergrund laden (nicht blockieren).
		MarketplaceCache.refreshAsyncIfStale();

		FlexhudMod.LOGGER.info("[FlexHUD] Client initialisiert: Hotkey H, /flexhud, /timer. Widgets: {}",
			FlexhudConfig.get().widgets.size());
	}
}
