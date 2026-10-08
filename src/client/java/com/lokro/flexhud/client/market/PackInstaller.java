package com.lokro.flexhud.client.market;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lokro.flexhud.FlexhudMod;
import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.config.WidgetConfig;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Lädt Marketplace-Zips (z.B. GitHub-Release-Assets) herunter nach
 * {@code flexhud/marketplace/downloaded/} und entpackt sie nach
 * {@code flexhud/marketplace/packs/<id>/}.
 *
 * <p>Enthält die Zip eine {@code pack.json} mit {@code widgets[]}, werden diese
 * als neue Widgets in die Config übernommen (mit frischen IDs).
 */
public final class PackInstaller {
	private PackInstaller() {}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static boolean isInstalled(MarketplaceEntry entry) {
		try {
			Path marker = MarketplaceCache.packsDir().resolve(entry.id).resolve("pack.json");
			if (Files.isRegularFile(marker)) {
				return true;
			}
			return Files.isRegularFile(MarketplaceCache.downloadedDir()
				.resolve(safeName(entry) + ".zip"));
		} catch (Exception e) {
			return false;
		}
	}

	public static void installAsync(MarketplaceEntry entry, Consumer<String> done) {
		CompletableFuture.runAsync(() -> {
			try {
				String msg = install(entry);
				done.accept(msg);
			} catch (Exception e) {
				FlexhudMod.LOGGER.warn("[FlexHUD] Installation von '{}' fehlgeschlagen.", entry.id, e);
				done.accept("FEHLER: " + e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage()));
			}
		});
	}

	static String install(MarketplaceEntry entry) throws Exception {
		Files.createDirectories(MarketplaceCache.downloadedDir());
		Files.createDirectories(MarketplaceCache.packsDir());

		Path zip = MarketplaceCache.downloadedDir().resolve(safeName(entry) + ".zip");

		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
		HttpRequest req = HttpRequest.newBuilder(URI.create(entry.downloadUrl))
			.timeout(Duration.ofMinutes(2))
			.header("User-Agent", "FlexHUD/0.1.0 (Minecraft-Mod)")
			.GET()
			.build();
		HttpResponse<Path> res = client.send(req, HttpResponse.BodyHandlers.ofFile(
			Files.createTempFile("flexhud-pack-", ".zip")));
		if (res.statusCode() < 200 || res.statusCode() >= 300) {
			throw new IllegalStateException("Download HTTP " + res.statusCode());
		}
		Files.move(res.body(), zip, StandardCopyOption.REPLACE_EXISTING);

		Path target = MarketplaceCache.packsDir().resolve(safeId(entry.id));
		unzip(zip, target);

		int imported = importWidgets(target);

		ConflictChecker.Conflict c = ConflictChecker.check(entry);
		String warn = ConflictChecker.warnText(entry, c);

		String ok = "Installiert: " + entry.name + " v" + entry.version
			+ " (" + zip.getFileName() + (imported > 0 ? ", " + imported + " Widget(s) übernommen" : "") + ").";
		if (warn != null) {
			ok += "\n" + warn;
		}
		FlexhudMod.LOGGER.info("[FlexHUD] {}", ok.replace("\n", " "));
		return ok;
	}

	private static void unzip(Path zip, Path target) throws Exception {
		Files.createDirectories(target);
		try (ZipFile zf = new ZipFile(zip.toFile())) {
			Enumeration<? extends ZipEntry> en = zf.entries();
			while (en.hasMoreElements()) {
				ZipEntry e = en.nextElement();
				Path out = target.resolve(e.getName()).normalize();
				if (!out.startsWith(target)) {
					throw new IllegalStateException("Zip enthält Pfad außerhalb des Ziels: " + e.getName());
				}
				if (e.isDirectory()) {
					Files.createDirectories(out);
				} else {
					Files.createDirectories(out.getParent());
					try (InputStream in = zf.getInputStream(e);
							OutputStream os = Files.newOutputStream(out)) {
						in.transferTo(os);
					}
				}
			}
		}
	}

	/** Liest packs/<id>/pack.json und übernimmt enthaltene Widgets. */
	static int importWidgets(Path packDir) {
		Path packJson = packDir.resolve("pack.json");
		if (!Files.isRegularFile(packJson)) {
			return 0;
		}
		try {
			String json = Files.readString(packJson);
			JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
			if (!obj.has("widgets") || !obj.get("widgets").isJsonArray()) {
				return 0;
			}
			JsonArray arr = obj.getAsJsonArray("widgets");
			List<WidgetConfig> fresh = new ArrayList<>();
			for (var el : arr) {
				try {
					WidgetConfig w = GSON.fromJson(el, WidgetConfig.class);
					if (w != null) {
						synchronized (FlexhudConfig.class) {
							w.id = FlexhudConfig.get().freeId(w.type);
							FlexhudConfig.get().widgets.add(w);
						}
						fresh.add(w);
					}
				} catch (Exception ex) {
					FlexhudMod.LOGGER.warn("[FlexHUD] Widget aus pack.json übersprungen.", ex);
				}
			}
			if (!fresh.isEmpty()) {
				FlexhudConfig.save();
			}
			return fresh.size();
		} catch (Exception e) {
			FlexhudMod.LOGGER.warn("[FlexHUD] pack.json konnte nicht gelesen werden.", e);
			return 0;
		}
	}

	private static String safeId(String id) {
		return id.replaceAll("[^a-zA-Z0-9._-]", "_");
	}

	private static String safeName(MarketplaceEntry e) {
		return safeId(e.id) + "-" + safeId(e.version);
	}
}
