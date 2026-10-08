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
 * als neue Widgets in die Config übernommen (mit frischen IDs). Die Herkunft
 * wird in {@link InstalledRegistry} festgehalten, damit Deinstallieren die
 * Pack-Dateien UND die importierten Widgets wieder entfernen kann.
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

	public static void uninstallAsync(MarketplaceEntry entry, Consumer<String> done) {
		CompletableFuture.runAsync(() -> {
			try {
				done.accept(uninstall(entry));
			} catch (Exception e) {
				FlexhudMod.LOGGER.warn("[FlexHUD] Deinstallation von '{}' fehlgeschlagen.", entry.id, e);
				done.accept("FEHLER: " + e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage()));
			}
		});
	}

	/** Zählt noch vorhandene Widgets, die aus diesem Pack importiert wurden. */
	public static int importedWidgetCount(MarketplaceEntry entry) {
		InstalledRegistry.Entry rec = InstalledRegistry.get(entry.id);
		if (rec == null || rec.widgetIds == null) {
			return 0;
		}
		int n = 0;
		for (String wid : rec.widgetIds) {
			if (FlexhudConfig.get().byId(wid) != null) {
				n++;
			}
		}
		return n;
	}

	/**
	 * Deinstalliert ein Pack: löscht entpackte Dateien + heruntergeladene Zip(s)
	 * sowie alle noch vorhandenen Widgets, die aus dem Pack importiert wurden.
	 */
	static String uninstall(MarketplaceEntry entry) throws Exception {
		List<String> removedFiles = new ArrayList<>();

		Path packDir = MarketplaceCache.packsDir().resolve(safeId(entry.id));
		if (Files.isDirectory(packDir)) {
			deleteRecursive(packDir);
			removedFiles.add("packs/" + safeId(entry.id) + "/");
		}
		Path dl = MarketplaceCache.downloadedDir();
		if (Files.isDirectory(dl)) {
			String prefix = safeId(entry.id) + "-";
			try (var ds = Files.newDirectoryStream(dl, prefix + "*.zip")) {
				for (Path zip : ds) {
					Files.deleteIfExists(zip);
					removedFiles.add("downloaded/" + zip.getFileName());
				}
			}
		}

		InstalledRegistry.Entry rec = InstalledRegistry.get(entry.id);
		int removedWidgets = 0;
		if (rec != null && rec.widgetIds != null) {
			for (String wid : rec.widgetIds) {
				WidgetConfig w = FlexhudConfig.get().byId(wid);
				if (w != null) {
					FlexhudConfig.get().widgets.remove(w);
					removedWidgets++;
				}
			}
			if (removedWidgets > 0) {
				FlexhudConfig.save();
			}
		}
		InstalledRegistry.remove(entry.id);

		boolean hadRecord = rec != null;
		String msg = "Deinstalliert: " + entry.name
			+ " (" + (removedFiles.isEmpty() ? "keine Dateien mehr da" : String.join(", ", removedFiles))
			+ (removedWidgets > 0 ? ", " + removedWidgets + " Widget(s) entfernt" : "")
			+ (hadRecord ? "" : ", Hinweis: keine Widget-Zuordnung gespeichert (alter Install)")
			+ ").";
		FlexhudMod.LOGGER.info("[FlexHUD] {}", msg);
		return msg;
	}

	private static void deleteRecursive(Path dir) throws Exception {
		try (var walk = Files.walk(dir)) {
			for (Path p : walk.sorted(java.util.Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(p);
			}
		}
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

		List<String> importedIds = importWidgets(target);
		InstalledRegistry.recordInstall(entry.id, entry.version, importedIds);

		ConflictChecker.Conflict c = ConflictChecker.check(entry);
		String warn = ConflictChecker.warnText(entry, c);

		String ok = "Installiert: " + entry.name + " v" + entry.version
			+ " (" + zip.getFileName() + (!importedIds.isEmpty() ? ", " + importedIds.size() + " Widget(s) übernommen" : "") + ").";
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

	/** Liest packs/<id>/pack.json, übernimmt Widgets und gibt deren neue IDs zurück. */
	static List<String> importWidgets(Path packDir) {
		List<String> ids = new ArrayList<>();
		Path packJson = packDir.resolve("pack.json");
		if (!Files.isRegularFile(packJson)) {
			return ids;
		}
		try {
			String json = Files.readString(packJson);
			JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
			if (!obj.has("widgets") || !obj.get("widgets").isJsonArray()) {
				return ids;
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
						ids.add(w.id);
					}
				} catch (Exception ex) {
					FlexhudMod.LOGGER.warn("[FlexHUD] Widget aus pack.json übersprungen.", ex);
				}
			}
			if (!fresh.isEmpty()) {
				FlexhudConfig.save();
			}
			return ids;
		} catch (Exception e) {
			FlexhudMod.LOGGER.warn("[FlexHUD] pack.json konnte nicht gelesen werden.", e);
			return ids;
		}
	}

	private static String safeId(String id) {
		return id.replaceAll("[^a-zA-Z0-9._-]", "_");
	}

	private static String safeName(MarketplaceEntry e) {
		return safeId(e.id) + "-" + safeId(e.version);
	}
}
