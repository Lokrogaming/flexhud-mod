package com.lokro.flexhud;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server-seitiger (logischer) Entrypoint. Die Mod ist rein client-seitig,
 * hier passiert bewusst fast nichts – alles läuft in {@code FlexhudClient}.
 */
public class FlexhudMod implements ModInitializer {
	public static final String MOD_ID = "flexhud";
	public static final String MOD_NAME = "FlexHUD";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[FlexHUD] Geladen (client-side Mod, Logik läuft im Client-Entrypoint).");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
