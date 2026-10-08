package com.lokro.flexhud.client.market;

import com.lokro.flexhud.FlexhudMod;
import com.lokro.flexhud.client.config.FlexhudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Start-Check aller Pack-Links + Chat-Zusammenfassung beim Join. */
public final class UpdateChecker {
	private UpdateChecker() {}

	public enum Status {
		UP_TO_DATE,
		UPDATE_AVAILABLE,
		NOT_INSTALLED,
		UNAVAILABLE,
		BROKEN,
		BLOCKED;

		public boolean problem() {
			return this == UNAVAILABLE || this == BROKEN || this == BLOCKED;
		}
	}

	public static class Result {
		public String packId = "";
		public String name = "";
		public String installedVersion = "";
		public String remoteVersion = "";
		public String note = "";
		public Status status = Status.BROKEN;
	}

	private static volatile Map<String, Result> last = new LinkedHashMap<>();
	private static volatile boolean running = false;
	private static volatile long lastCheckMs = 0L;
	private static volatile long notifiedCheckMs = 0L;
	private static volatile boolean joinPending = false;

	public static Map<String, Result> last() {
		return last;
	}

	public static boolean isRunning() {
		return running;
	}

	public static void refreshAsync(Runnable onDone) {
		if (running) {
			return;
		}
		running = true;
		CompletableFuture.runAsync(() -> {
			try {
				List<MarketplaceEntry> entries;
				try {
					entries = MarketplaceCache.fetch(FlexhudConfig.get().marketplaceUrl);
				} catch (Exception e) {
					FlexhudMod.LOGGER.warn("[FlexHUD] Update-Check: Marketplace offline, nutze Cache.", e);
					entries = MarketplaceCache.entries();
				}
				Map<String, Result> out = new LinkedHashMap<>();
				for (MarketplaceEntry e : entries) {
					try {
						out.put(e.id, check(e));
					} catch (Exception ex) {
						Result r = new Result();
						r.packId = e.id;
						r.name = e.name;
						r.note = String.valueOf(ex.getMessage());
						out.put(e.id, r);
					}
				}
				last = out;
				lastCheckMs = System.currentTimeMillis();
				FlexhudMod.LOGGER.info("[FlexHUD] Update-Check fertig: {} Packs.", out.size());
			} finally {
				running = false;
				if (onDone != null) {
					onDone.run();
				}
			}
		});
	}

	static Result check(MarketplaceEntry entry) {
		Result r = new Result();
		r.packId = entry.id;
		r.name = entry.name == null || entry.name.isBlank() ? entry.id : entry.name;
		InstalledRegistry.Entry rec = InstalledRegistry.get(entry.id);
		r.installedVersion = rec == null ? "" : rec.version;
		Path tmp = null;
		try {
			HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
			tmp = Files.createTempFile("flexhud-update-", ".zip");
			HttpRequest req = HttpRequest.newBuilder(URI.create(entry.downloadUrl))
				.timeout(Duration.ofSeconds(60))
				.header("User-Agent", "FlexHUD/0.1.0 (Minecraft-Mod)")
				.GET()
				.build();
			HttpResponse<Path> res = client.send(req, HttpResponse.BodyHandlers.ofFile(tmp));
			int code = res.statusCode();
			if (code == 401 || code == 403) {
				r.status = Status.BLOCKED;
				r.note = "HTTP " + code;
				return r;
			}
			if (code == 404 || code == 410) {
				r.status = Status.UNAVAILABLE;
				r.note = "HTTP " + code;
				return r;
			}
			if (code < 200 || code >= 300) {
				r.status = Status.BROKEN;
				r.note = "HTTP " + code;
				return r;
			}
			r.remoteVersion = FlexConfig.readVersion(tmp, entry);
			if (rec == null) {
				r.status = Status.NOT_INSTALLED;
				r.note = "remote v" + r.remoteVersion;
				return r;
			}
			if (compareVersions(r.remoteVersion, rec.version) > 0) {
				r.status = Status.UPDATE_AVAILABLE;
			} else {
				r.status = Status.UP_TO_DATE;
			}
			return r;
		} catch (Exception e) {
			r.status = Status.BROKEN;
			r.note = e.getClass().getSimpleName();
			return r;
		} finally {
			if (tmp != null) {
				try {
					Files.deleteIfExists(tmp);
				} catch (Exception ignored) {
				}
			}
		}
	}

	/** Versionsvergleich (1.10 > 1.9). */
	public static int compareVersions(String a, String b) {
		String[] pa = (a == null ? "" : a).split("[.\\-+]");
		String[] pb = (b == null ? "" : b).split("[.\\-+]");
		int n = Math.max(pa.length, pb.length);
		for (int i = 0; i < n; i++) {
			String x = i < pa.length ? pa[i] : "0";
			String y = i < pb.length ? pb[i] : "0";
			int c = comparePart(x, y);
			if (c != 0) {
				return c;
			}
		}
		return 0;
	}

	private static int comparePart(String x, String y) {
		Integer nx = leadingInt(x);
		Integer ny = leadingInt(y);
		if (nx != null && ny != null) {
			int c = Integer.compare(nx, ny);
			if (c != 0) {
				return c;
			}
			return x.compareToIgnoreCase(y);
		}
		return x.compareToIgnoreCase(y);
	}

	private static Integer leadingInt(String s) {
		int i = 0;
		while (i < s.length() && Character.isDigit(s.charAt(i))) {
			i++;
		}
		if (i == 0) {
			return null;
		}
		try {
			return Integer.parseInt(s.substring(0, i));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** Chat-Zusammenfassung beim Weltbeitritt. */
	public static void notifyOnJoin() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null) {
			return;
		}
		if (!last.isEmpty() && notifiedCheckMs != lastCheckMs) {
			sendSummary();
			notifiedCheckMs = lastCheckMs;
			return;
		}
		if (last.isEmpty() && !running) {
			joinPending = true;
			refreshAsync(() -> {
				if (joinPending) {
					joinPending = false;
					Minecraft c2 = Minecraft.getInstance();
					if (c2 != null && c2.player != null && c2.level != null) {
						notifyOnJoin();
					}
				}
			});
		}
	}

	private static void sendSummary() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null) {
			return;
		}
		int updates = 0;
		int problems = 0;
		for (Result r : last.values()) {
			if (r.status == Status.UPDATE_AVAILABLE) {
				updates++;
			} else if (r.status.problem() && !r.installedVersion.isEmpty()) {
				problems++;
			}
		}
		if (updates == 0 && problems == 0) {
			return;
		}
		Component base = Component.literal(com.lokro.flexhud.client.i18n.Lang.f("update.join", updates, problems));
		Component link = Component.literal(com.lokro.flexhud.client.i18n.Lang.t("update.join_open"))
			.withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/flexhud updates")));
		client.player.sendSystemMessage(Component.literal("§b[FlexHUD] §f").copy().append(base).append(link));
	}
}
