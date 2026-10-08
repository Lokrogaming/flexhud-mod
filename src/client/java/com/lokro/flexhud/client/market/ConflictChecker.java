package com.lokro.flexhud.client.market;

import com.lokro.flexhud.FlexhudMod;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Konflikt-Erkennung: Prüft anhand von {@code modId} (Fabric-Loader) UND
 * anhand von {@code jarHints} (Dateiscan in {@code mods/}), ob die zum
 * Marketplace-Pack gehörende Mod bereits als .jar installiert ist.
 *
 * <p>Warum beides? {@code isModLoaded} greift nur, wenn die Mod im aktuellen
 * Spiel geladen ist. Der Dateiscan findet auch deaktivierte/umbenannte Jars
 * (z.B. {@code stepcount-mod-1.0.0.jar.disabled}).
 */
public final class ConflictChecker {
	private ConflictChecker() {}

	public record Conflict(boolean jarInstalled, boolean loadedNow, String jarName) {
		public boolean any() {
			return jarInstalled || loadedNow;
		}
	}

	public static Conflict check(MarketplaceEntry entry) {
		boolean loaded = false;
		try {
			loaded = entry.modId != null && !entry.modId.isBlank()
				&& FabricLoader.getInstance().isModLoaded(entry.modId);
		} catch (Throwable t) {
			FlexhudMod.LOGGER.debug("[FlexHUD] isModLoaded fehlgeschlagen.", t);
		}
		String foundJar = findJar(entry);
		return new Conflict(foundJar != null, loaded, foundJar);
	}

	/** Sucht in mods/ nach Dateien, die einen der jarHints enthalten. */
	static String findJar(MarketplaceEntry entry) {
		try {
			Path mods = FabricLoader.getInstance().getGameDir().resolve("mods");
			if (!Files.isDirectory(mods)) {
				return null;
			}
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(mods, "*.jar*")) {
				for (Path p : ds) {
					String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
					if (entry.modId != null && !entry.modId.isBlank()
						&& name.contains(entry.modId.toLowerCase(Locale.ROOT))) {
						return p.getFileName().toString();
					}
					if (entry.jarHints != null) {
						for (String hint : entry.jarHints) {
							if (hint != null && !hint.isBlank()
								&& name.contains(hint.toLowerCase(Locale.ROOT))) {
								return p.getFileName().toString();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			FlexhudMod.LOGGER.debug("[FlexHUD] mods-Scan fehlgeschlagen.", e);
		}
		return null;
	}

	/** Warntext für UI/Chat, oder null wenn alles sauber. */
	public static String warnText(MarketplaceEntry entry, Conflict c) {
		if (!c.any()) {
			return null;
		}
		String modLabel = entry.modId == null || entry.modId.isBlank()
			? com.lokro.flexhud.client.i18n.Lang.t("conflict.modlabel")
			: "»" + entry.modId + "«";
		if (c.loadedNow() && c.jarInstalled()) {
			return com.lokro.flexhud.client.i18n.Lang.f("conflict.both", modLabel, c.jarName());
		}
		if (c.loadedNow()) {
			return com.lokro.flexhud.client.i18n.Lang.f("conflict.loaded", modLabel);
		}
		return com.lokro.flexhud.client.i18n.Lang.f("conflict.jar", modLabel, c.jarName());
	}

	/**
	 * Erkennt per Paket-/Mod-Namen, ob dieselbe Mod schon als ANDERES Pack
	 * installiert ist (installed.json). Gibt Warntext zurück oder null.
	 */
	public static String packConflictText(MarketplaceEntry entry) {
		if (entry.modId == null || entry.modId.isBlank()) {
			return null;
		}
		for (var en : InstalledRegistry.load().entrySet()) {
			if (en.getKey().equals(entry.id)) {
				continue;
			}
			InstalledRegistry.Entry rec = en.getValue();
			if (rec != null && entry.modId.equalsIgnoreCase(rec.modId)) {
				return com.lokro.flexhud.client.i18n.Lang.f("conflict.pack", entry.modId, en.getKey());
			}
		}
		return null;
	}
}
