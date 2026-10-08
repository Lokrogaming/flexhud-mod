package com.lokro.flexhud.client.market;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lokro.flexhud.FlexhudMod;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** *.flexconfig aus Pack-Ordnern: Meta + configMenu + widgetButtons (Icons: später). */
public final class FlexConfig {
	private FlexConfig() {}

	private static final Gson GSON = new GsonBuilder().create();

	public String packId = "";
	public String packName = "";
	public String author = "";
	public String website = "";
	public String created = "";
	public String updated = "";
	public String version = "";
	public String description = "";
	public JsonObject icons = null;
	public ConfigMenu configMenu = null;
	public List<FlexButton> widgetButtons = new ArrayList<>();

	public static class ConfigMenu {
		public boolean enabled = true;
		public String title = "";
		public List<FlexButton> buttons = new ArrayList<>();
	}

	/** Custom-Button: action toggleStyle/setStyle/applyPreset/message auf Widget-Stil. */
	public static class FlexButton {
		public String id = "";
		public String label = "";
		public String action = "message";
		public String target = "style";
		public String field = "";
		public String value = "";
		public String message = "";
	}

	public boolean hasOverview() {
		return configMenu != null && configMenu.enabled
			&& configMenu.buttons != null && !configMenu.buttons.isEmpty();
	}

	public boolean hasWidgetButtons() {
		return widgetButtons != null && !widgetButtons.isEmpty();
	}

	/** Erste *.flexconfig im Pack-Ordner parsen (null wenn keine). */
	public static FlexConfig load(String packId) {
		try {
			Path dir = MarketplaceCache.packsDir().resolve(packId);
			if (!Files.isDirectory(dir)) {
				return null;
			}
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.flexconfig")) {
				for (Path p : ds) {
					try {
						String json = Files.readString(p);
						FlexConfig cfg = GSON.fromJson(json, FlexConfig.class);
						if (cfg != null) {
							if (cfg.widgetButtons == null) {
								cfg.widgetButtons = new ArrayList<>();
							}
							return cfg;
						}
					} catch (Exception e) {
						FlexhudMod.LOGGER.warn("[FlexHUD] {}.flexconfig ungültig: {}", p.getFileName(), String.valueOf(e.getMessage()));
					}
				}
			}
		} catch (Exception e) {
			FlexhudMod.LOGGER.debug("[FlexHUD] FlexConfig-Suche fehlgeschlagen.", e);
		}
		return null;
	}

	/** Version aus Zip lesen (flexconfig > pack.json > Eintrag). */
	public static String readVersion(Path zip, MarketplaceEntry entry) {
		try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(zip.toFile())) {
			var en = zf.entries();
			String flexVersion = null;
			String packVersion = null;
			while (en.hasMoreElements()) {
				var e = en.nextElement();
				String name = e.getName();
				boolean root = !name.contains("/");
				if (!root) {
					continue;
				}
				if (name.endsWith(".flexconfig") && flexVersion == null) {
					try {
						JsonObject obj = JsonParser.parseString(
							new String(zf.getInputStream(e).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8))
							.getAsJsonObject();
						if (obj.has("version")) {
							flexVersion = obj.get("version").getAsString();
						}
					} catch (Exception ignored) {
					}
				} else if (name.equals("pack.json") && packVersion == null) {
					try {
						JsonObject obj = JsonParser.parseString(
							new String(zf.getInputStream(e).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8))
							.getAsJsonObject();
						if (obj.has("packVersion")) {
							packVersion = obj.get("packVersion").getAsString();
						} else if (obj.has("version")) {
							packVersion = obj.get("version").getAsString();
						}
					} catch (Exception ignored) {
					}
				}
			}
			if (flexVersion != null && !flexVersion.isBlank()) {
				return flexVersion.trim();
			}
			if (packVersion != null && !packVersion.isBlank()) {
				return packVersion.trim();
			}
		} catch (Exception e) {
			FlexhudMod.LOGGER.debug("[FlexHUD] Versions-Check fehlgeschlagen.", e);
		}
		return entry.version == null ? "" : entry.version;
	}
}
