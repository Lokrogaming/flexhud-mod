package com.lokro.flexhud.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lokro.flexhud.FlexhudMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lädt/speichert {@code config/flexhud.json}.
 * Enthält alle Widgets + Marketplace-URL. Wird vom Editor live gespeichert.
 */
public final class FlexhudConfig {
	private FlexhudConfig() {}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public String marketplaceUrl = defaultMarketplaceUrl();
	public List<WidgetConfig> widgets = defaultWidgets();

	private static FlexhudConfig INSTANCE = new FlexhudConfig();

	public static FlexhudConfig get() {
		return INSTANCE;
	}

	public static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("flexhud.json");
	}

	private static String defaultMarketplaceUrl() {
		// Aus gradle.properties übernommen; Fallback auf Repo-Pfad.
		return "https://raw.githubusercontent.com/Lokrogaming/flexhud-mod/main/marketplace/marketplace.json";
	}

	private static List<WidgetConfig> defaultWidgets() {
		List<WidgetConfig> list = new ArrayList<>();
		WidgetConfig timer = new WidgetConfig("timer-1", WidgetType.TIMER, "Timer: {value}", 0.5f, 0.72f);
		list.add(timer);
		WidgetConfig steps = new WidgetConfig("steps-1", WidgetType.STEPCOUNT, "Steps: {value}", 0.5f, 0.78f);
		steps.enabled = false; // standardmäßig aus, damit es nicht mit StepCounts eigenem HUD doppelt rendert
		list.add(steps);
		return list;
	}

	public static synchronized void load() {
		Path p = file();
		if (Files.isRegularFile(p)) {
			try (Reader r = Files.newBufferedReader(p)) {
				FlexhudConfig loaded = GSON.fromJson(r, FlexhudConfig.class);
				if (loaded != null) {
					if (loaded.widgets == null || loaded.widgets.isEmpty()) {
						loaded.widgets = defaultWidgets();
					}
					if (loaded.marketplaceUrl == null || loaded.marketplaceUrl.isBlank()) {
						loaded.marketplaceUrl = defaultMarketplaceUrl();
					}
					INSTANCE = loaded;
					FlexhudMod.LOGGER.info("[FlexHUD] Config geladen: {} Widgets.", INSTANCE.widgets.size());
					return;
				}
			} catch (Exception e) {
				FlexhudMod.LOGGER.warn("[FlexHUD] Config konnte nicht gelesen werden, nutze Defaults.", e);
			}
		}
		INSTANCE = new FlexhudConfig();
		save();
	}

	public static synchronized void save() {
		try {
			Files.createDirectories(file().getParent());
			try (Writer w = Files.newBufferedWriter(file())) {
				GSON.toJson(INSTANCE, w);
			}
		} catch (Exception e) {
			FlexhudMod.LOGGER.warn("[FlexHUD] Config konnte nicht gespeichert werden.", e);
		}
	}

	public synchronized WidgetConfig byId(String id) {
		for (WidgetConfig w : widgets) {
			if (w.id.equals(id)) {
				return w;
			}
		}
		return null;
	}

	/** Erzeugt eine freie ID wie "timer-2". */
	public synchronized String freeId(WidgetType type) {
		String base = type.name().toLowerCase();
		int n = 1;
		while (byId(base + "-" + n) != null) {
			n++;
		}
		return base + "-" + n;
	}
}
