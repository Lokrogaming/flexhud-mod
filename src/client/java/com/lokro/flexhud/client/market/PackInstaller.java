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

/** Marketplace-Downloads: Zip laden, entpacken, Widgets importieren (Herkunft -> InstalledRegistry). */
public final class PackInstaller {
	private PackInstaller() {}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static boolean isInstalled(MarketplaceEntry entry) {
		try {
			Path marker = MarketplaceCache.packsDir().resolve(safeId(entry.id)).resolve("pack.json");
			if (Files.isRegularFile(marker)) {
				return true;
			}
			Path dl = MarketplaceCache.downloadedDir();
			if (Files.isDirectory(dl)) {
				try (var ds = Files.newDirectoryStream(dl, safeId(entry.id) + "-*.zip")) {
					for (Path ignored : ds) {
						return true;
					}
				}
			}
			return InstalledRegistry.get(entry.id) != null;
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
				done.accept(com.lokro.flexhud.client.i18n.Lang.f("install.fail",
					e.getClass().getSimpleName(), String.valueOf(e.getMessage())));
			}
		});
	}

	public static void uninstallAsync(MarketplaceEntry entry, Consumer<String> done) {
		CompletableFuture.runAsync(() -> {
			try {
				done.accept(uninstall(entry));
			} catch (Exception e) {
				FlexhudMod.LOGGER.warn("[FlexHUD] Deinstallation von '{}' fehlgeschlagen.", entry.id, e);
				done.accept(com.lokro.flexhud.client.i18n.Lang.f("install.fail",
					e.getClass().getSimpleName(), String.valueOf(e.getMessage())));
			}
		});
	}

	/** Noch vorhandene Pack-Widgets zählen. */
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

	/** Pack deinstallieren (Dateien + importierte Widgets). */
	static String uninstall(MarketplaceEntry entry) throws Exception {
		List<String> removedFiles = removePackFiles(entry);

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
		String msg = com.lokro.flexhud.client.i18n.Lang.f("uninstall.ok", entry.name,
			removedFiles.isEmpty()
				? com.lokro.flexhud.client.i18n.Lang.t("uninstall.nofiles")
				: String.join(", ", removedFiles),
			removedWidgets > 0
				? com.lokro.flexhud.client.i18n.Lang.f("uninstall.widgets", removedWidgets)
				: "",
			hadRecord ? "" : com.lokro.flexhud.client.i18n.Lang.t("uninstall.norecord"));
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

		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
		HttpRequest req = HttpRequest.newBuilder(URI.create(entry.downloadUrl))
			.timeout(Duration.ofMinutes(2))
			.header("User-Agent", "FlexHUD/0.1.0 (Minecraft-Mod)")
			.GET()
			.build();
		Path tmp = Files.createTempFile("flexhud-pack-", ".zip");
		HttpResponse<Path> res = client.send(req, HttpResponse.BodyHandlers.ofFile(tmp));
		if (res.statusCode() < 200 || res.statusCode() >= 300) {
			Files.deleteIfExists(tmp);
			throw new IllegalStateException("Download HTTP " + res.statusCode());
		}

		String remoteVersion = FlexConfig.readVersion(tmp, entry);

		// Update/Reinstall: altes Layout sichern, dann alte Widgets + Dateien entfernen.
		// Erst HIER (Download steht) – bei Netzwerkfehler bleibt der alte Stand heil.
		List<WidgetConfig> backup = new ArrayList<>();
		boolean hadOld = InstalledRegistry.get(entry.id) != null;
		if (hadOld) {
			InstalledRegistry.Entry old = InstalledRegistry.get(entry.id);
			if (old.widgetIds != null) {
				for (String wid : old.widgetIds) {
					WidgetConfig w = FlexhudConfig.get().byId(wid);
					if (w != null) {
						backup.add(w.copy());
						FlexhudConfig.get().widgets.remove(w);
					}
				}
			}
			if (!backup.isEmpty()) {
				InstalledRegistry.saveBackup(entry.id, backup);
				FlexhudConfig.save();
			}
			removePackFiles(entry);
		}

		Path zip = MarketplaceCache.downloadedDir().resolve(safeName(entry, remoteVersion) + ".zip");
		Files.move(tmp, zip, StandardCopyOption.REPLACE_EXISTING);

		Path target = MarketplaceCache.packsDir().resolve(safeId(entry.id));
		unzip(zip, target);

		List<String> importedIds = importWidgets(target);
		InstalledRegistry.recordInstall(entry.id, remoteVersion, entry.modId, importedIds);

		// Layout zurückspielen (positionsweise); bei Strukturänderung warnen + Backup weg.
		String layoutNote = "";
		if (hadOld && !backup.isEmpty()) {
			if (backup.size() == importedIds.size()) {
				for (int i = 0; i < importedIds.size(); i++) {
					WidgetConfig fresh = FlexhudConfig.get().byId(importedIds.get(i));
					WidgetConfig oldW = backup.get(i);
					if (fresh != null) {
						fresh.x = oldW.x;
						fresh.y = oldW.y;
						fresh.style.scale = oldW.style.scale;
					}
				}
				FlexhudConfig.save();
				layoutNote = com.lokro.flexhud.client.i18n.Lang.t("install.layout_kept");
			} else {
				layoutNote = com.lokro.flexhud.client.i18n.Lang.t("install.layout_lost");
			}
			InstalledRegistry.clearBackup(entry.id);
		}

		String packWarn = ConflictChecker.packConflictText(entry);

		ConflictChecker.Conflict c = ConflictChecker.check(entry);
		String warn = ConflictChecker.warnText(entry, c);
		if (packWarn != null) {
			warn = (warn == null ? "" : warn + "\n") + packWarn;
		}

		String ok = com.lokro.flexhud.client.i18n.Lang.f("install.ok", entry.name, remoteVersion,
			zip.getFileName().toString(),
			importedIds.isEmpty() ? ""
				: com.lokro.flexhud.client.i18n.Lang.f("install.widgets", importedIds.size()),
			layoutNote);
		if (warn != null && !warn.isEmpty()) {
			ok += "\n" + warn;
		}
		FlexhudMod.LOGGER.info("[FlexHUD] {}", ok.replace("\n", " "));
		return ok;
	}

	/** Pack-Ordner + Zips löschen. */
	static List<String> removePackFiles(MarketplaceEntry entry) throws Exception {
		List<String> removed = new ArrayList<>();
		Path packDir = MarketplaceCache.packsDir().resolve(safeId(entry.id));
		if (Files.isDirectory(packDir)) {
			deleteRecursive(packDir);
			removed.add("packs/" + safeId(entry.id) + "/");
		}
		Path dl = MarketplaceCache.downloadedDir();
		if (Files.isDirectory(dl)) {
			String prefix = safeId(entry.id) + "-";
			try (var ds = Files.newDirectoryStream(dl, prefix + "*.zip")) {
				for (Path zip : ds) {
					Files.deleteIfExists(zip);
					removed.add("downloaded/" + zip.getFileName());
				}
			}
		}
		return removed;
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

	/** Widgets aus pack.json übernehmen (gibt neue IDs zurück). */
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

	private static String safeName(MarketplaceEntry e, String version) {
		String v = (version == null || version.isBlank()) ? e.version : version;
		return safeId(e.id) + "-" + safeId(v == null ? "?" : v);
	}
}
