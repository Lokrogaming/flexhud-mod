package com.lokro.flexhud.client.market;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lokro.flexhud.FlexhudMod;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Herkunfts-Tracking für installierte Packs: speichert pro Pack-ID, welche
 * Widget-IDs beim Installieren aus dessen {@code pack.json} übernommen wurden.
 * Datei: {@code flexhud/marketplace/installed.json}.
 *
 * <p>Wird fürs Deinstallieren gebraucht (Pack-Dateien + importierte Widgets
 * gemeinsam entfernen). Alte Installs ohne Eintrag lassen sich trotzdem
 * entfernen – dann nur ohne Widget-Zuordnung.
 */
public final class InstalledRegistry {
	private InstalledRegistry() {}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Entry>>() {}.getType();

	public static class Entry {
		public String version = "";
		public List<String> widgetIds = new ArrayList<>();
		public long installedAt = 0L;
	}

	private static Path file() {
		return MarketplaceCache.dir().resolve("installed.json");
	}

	public static synchronized Map<String, Entry> load() {
		Path p = file();
		if (Files.isRegularFile(p)) {
			try (Reader r = Files.newBufferedReader(p)) {
				Map<String, Entry> map = GSON.fromJson(r, MAP_TYPE);
				if (map != null) {
					return map;
				}
			} catch (Exception e) {
				FlexhudMod.LOGGER.warn("[FlexHUD] installed.json konnte nicht gelesen werden.", e);
			}
		}
		return new LinkedHashMap<>();
	}

	private static synchronized void save(Map<String, Entry> map) {
		try {
			Files.createDirectories(file().getParent());
			try (Writer w = Files.newBufferedWriter(file())) {
				GSON.toJson(map, w);
			}
		} catch (Exception e) {
			FlexhudMod.LOGGER.warn("[FlexHUD] installed.json konnte nicht gespeichert werden.", e);
		}
	}

	public static synchronized void recordInstall(String packId, String version, List<String> widgetIds) {
		Map<String, Entry> map = load();
		Entry e = new Entry();
		e.version = version == null ? "" : version;
		e.widgetIds = new ArrayList<>(widgetIds);
		e.installedAt = System.currentTimeMillis();
		map.put(packId, e);
		save(map);
	}

	public static synchronized Entry get(String packId) {
		return load().get(packId);
	}

	public static synchronized void remove(String packId) {
		Map<String, Entry> map = load();
		if (map.remove(packId) != null) {
			save(map);
		}
	}
}
