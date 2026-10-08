package com.lokro.flexhud.compat;

import com.lokro.flexhud.client.gui.FlexhudScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu-Tab: Verlinkt den ModMenü-Config-Button auf das FlexHUD-Hauptmenü.
 * Wird nur geladen, wenn ModMenu installiert ist (Entrypoint "modmenu").
 * Alle Einstellungen (Widgets, Stil, Marketplace) sind dort erreichbar.
 */
public class FlexhudModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return FlexhudScreen::new;
	}
}
