package com.lokro.flexhud.client.key;

import com.lokro.flexhud.client.gui.FlexhudScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Hotkey für das FlexHUD-Menü (Standard: H). Funktioniert auch ohne ModMenu –
 * das ist der garantierte Weg ins Menü, falls die ModMenu-Integration fehlt.
 *
 * <p>26.3-Stand: {@code KeyMappingHelper.registerKeyMapping},
 * {@code new KeyMapping(String, int, Category)}, Keycode via
 * {@code InputConstants.KEY_* } (kein LWJGL-Direktimport nötig).
 */
public final class FlexhudKeys {
	private FlexhudKeys() {}

	private static KeyMapping openMenu;

	public static void register() {
		openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.flexhud.open",
			InputConstants.KEY_H,
			KeyMapping.Category.MISC
		));
	}

	public static void tick(Minecraft client) {
		if (client == null || client.player == null) {
			return;
		}
		while (openMenu != null && openMenu.consumeClick()) {
			if (client.gui.screen() == null) {
				client.gui.setScreen(new FlexhudScreen(null));
			}
		}
	}
}
