package com.lokro.flexhud.client.market;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lokro.flexhud.FlexhudMod;
import com.lokro.flexhud.client.config.FlexhudConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Lädt die Marketplace-JSON (URL aus der Config) und cached sie lokal unter
 * {@code flexhud/marketplace/marketplace-cache.json}.
 */
public final class MarketplaceCache {
	private MarketplaceCache() {}

	private static final Gson GSON = new Gson();
	private static volatile List<MarketplaceEntry> entries = new ArrayList<>();
	private static volatile long lastFetchMs = 0L;
	private static volatile String lastError = null;

	public static Path dir() {
		return FabricLoader.getInstance().getGameDir().resolve("flexhud").resolve("marketplace");
	}

	public static Path downloadedDir() {
		return dir().resolve("downloaded");
	}

	public static Path packsDir() {
		return dir().resolve("packs");
	}

	public static List<MarketplaceEntry> entries() {
		return entries;
	}

	public static String lastError() {
		return lastError;
	}

	public static void refreshAsyncIfStale() {
		long now = System.currentTimeMillis();
		if (now - lastFetchMs < 5 * 60 * 1000L && !entries.isEmpty()) {
			return;
		}
		refreshAsync();
	}

	public static void refreshAsync() {
		CompletableFuture.runAsync(() -> {
			try {
				List<MarketplaceEntry> fresh = fetch(FlexhudConfig.get().marketplaceUrl);
				entries = fresh;
				lastFetchMs = System.currentTimeMillis();
				lastError = null;
				// Cache für Offline-Nutzung speichern
				try {
					Files.createDirectories(dir());
					Files.writeString(dir().resolve("marketplace-cache.json"), GSON.toJson(fresh));
				} catch (Exception ignored) {
				}
				FlexhudMod.LOGGER.info("[FlexHUD] Marketplace: {} Einträge geladen.", fresh.size());
			} catch (Exception e) {
				lastError = e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage());
				FlexhudMod.LOGGER.warn("[FlexHUD] Marketplace konnte nicht geladen werden: {}", lastError);
				// Offline-Fallback: Cache laden
				try {
					Path cache = dir().resolve("marketplace-cache.json");
					if (Files.isRegularFile(cache)) {
						String json = Files.readString(cache);
						entries = parseEntries(json);
					}
				} catch (Exception ignored) {
				}
			}
		});
	}

	static List<MarketplaceEntry> fetch(String url) throws Exception {
		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
		HttpRequest req = HttpRequest.newBuilder(URI.create(url))
			.timeout(Duration.ofSeconds(15))
			.header("User-Agent", "FlexHUD/0.1.0 (Minecraft-Mod)")
			.GET()
			.build();
		HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
		if (res.statusCode() < 200 || res.statusCode() >= 300) {
			throw new IllegalStateException("HTTP " + res.statusCode() + " für " + url);
		}
		return parseEntries(res.body());
	}

	/** Akzeptiert Array-Form oder Objekt-Form {packs:[...]}. */
	static List<MarketplaceEntry> parseEntries(String json) {
		List<MarketplaceEntry> out = new ArrayList<>();
		String clean = json == null ? "" : json.strip();
		if (!clean.isEmpty() && clean.charAt(0) == '\uFEFF') {
			clean = clean.substring(1); // BOM entfernen (manche Editoren/Skripte schreiben eins)
		}
		JsonElement root = JsonParser.parseString(clean);
		JsonArray arr;
		if (root.isJsonArray()) {
			arr = root.getAsJsonArray();
		} else if (root.isJsonObject()) {
			JsonObject obj = root.getAsJsonObject();
			if (obj.has("packs") && obj.get("packs").isJsonArray()) {
				arr = obj.getAsJsonArray("packs");
			} else if (obj.has("entries") && obj.get("entries").isJsonArray()) {
				arr = obj.getAsJsonArray("entries");
			} else {
				throw new IllegalArgumentException("marketplace.json: Array oder {packs:[...]} erwartet.");
			}
		} else {
			throw new IllegalArgumentException("marketplace.json: ungültiges Format.");
		}
		for (JsonElement el : arr) {
			try {
				MarketplaceEntry e = GSON.fromJson(el, MarketplaceEntry.class);
				if (e != null && e.valid()) {
					out.add(e);
				}
			} catch (Exception ex) {
				FlexhudMod.LOGGER.warn("[FlexHUD] Marketplace-Eintrag übersprungen (ungültig).", ex);
			}
		}
		return out;
	}
}
