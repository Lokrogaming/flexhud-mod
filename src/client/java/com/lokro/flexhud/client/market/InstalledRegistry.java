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
 * Datei flexhud/marketplace/installed.json (für Deinstall + Layout-Save).
 */
public final class InstalledRegistry {
	private InstalledRegistry() {}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Entry>>() {}.getType();
	private static final Type BACKUP_TYPE = new TypeToken<List<com.lokro.flexhud.client.config.WidgetConfig>>() {}.getType();

	public static class Entry {
		public String version = "";
		public String modId = "";
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

	public static synchronized void recordInstall(String packId, String version, String modId, List<String> widgetIds) {
		Map<String, Entry> map = load();
		Entry e = new Entry();
		e.version = version == null ? "" : version;
		e.modId = modId == null ? "" : modId;
		e.widgetIds = new ArrayList<>(widgetIds);
		e.installedAt = System.currentTimeMillis();
		map.put(packId, e);
		save(map);
	}

	public static synchronized Entry get(String packId) {
		return load().get(packId);
	}

	/** Pack-Herkunft eines Widgets (null wenn handgemacht). */
	public static synchronized String findPackForWidget(String widgetId) {
		for (Map.Entry<String, Entry> en : load().entrySet()) {
			if (en.getValue().widgetIds != null && en.getValue().widgetIds.contains(widgetId)) {
				return en.getKey();
			}
		}
		return null;
	}

	private static Path backupFile(String packId) {
		String safe = packId.replaceAll("[^a-zA-Z0-9._-]", "_");
		return MarketplaceCache.dir().resolve("layout-backup").resolve(safe + ".json");
	}

	/** Layout-Backup vor Reinstall/Update. */
	public static synchronized void saveBackup(String packId, List<com.lokro.flexhud.client.config.WidgetConfig> widgets) {
		try {
			Path p = backupFile(packId);
			Files.createDirectories(p.getParent());
			try (Writer w = Files.newBufferedWriter(p)) {
				GSON.toJson(widgets, w);
			}
		} catch (Exception e) {
			FlexhudMod.LOGGER.warn("[FlexHUD] Layout-Backup fehlgeschlagen.", e);
		}
	}

	public static synchronized List<com.lokro.flexhud.client.config.WidgetConfig> loadBackup(String packId) {
		Path p = backupFile(packId);
		if (!Files.isRegularFile(p)) {
			return new ArrayList<>();
		}
		try (Reader r = Files.newBufferedReader(p)) {
			List<com.lokro.flexhud.client.config.WidgetConfig> list = GSON.fromJson(r, BACKUP_TYPE);
			return list == null ? new ArrayList<>() : list;
		} catch (Exception e) {
			FlexhudMod.LOGGER.warn("[FlexHUD] Layout-Backup unlesbar.", e);
			return new ArrayList<>();
		}
	}

	public static synchronized void clearBackup(String packId) {
		try {
			Files.deleteIfExists(backupFile(packId));
		} catch (Exception ignored) {
		}
	}

	public static synchronized void remove(String packId) {
		Map<String, Entry> map = load();
		if (map.remove(packId) != null) {
			save(map);
		}
	}
}
