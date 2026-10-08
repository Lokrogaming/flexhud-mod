package com.lokro.flexhud.client.state;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lokro.flexhud.FlexhudMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stoppuhr-Status (Timer-Widget). Wird in {@code config/flexhud-timer.json}
 * gespeichert, damit ein laufender Timer einen Neustart überlebt.
 */
public final class TimerState {
	private TimerState() {}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static boolean running = false;
	private static long accumulatedMs = 0L;
	private static long lastTickMs = 0L;
	private static boolean showHud = true;

	private static class Data {
		boolean running;
		long accumulatedMs;
		boolean showHud = true;
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("flexhud-timer.json");
	}

	public static synchronized void load() {
		Path p = file();
		if (Files.isRegularFile(p)) {
			try (Reader r = Files.newBufferedReader(p)) {
				Data d = GSON.fromJson(r, Data.class);
				if (d != null) {
					accumulatedMs = Math.max(0, d.accumulatedMs);
					showHud = d.showHud;
					// Timer läuft nach Reload pausiert weiter (kein Zeit-Cheat durch Abwesenheit).
					running = false;
				}
			} catch (Exception e) {
				FlexhudMod.LOGGER.warn("[FlexHUD] Timer-Status konnte nicht gelesen werden.", e);
			}
		}
		lastTickMs = System.currentTimeMillis();
	}

	public static synchronized void save() {
		try {
			Files.createDirectories(file().getParent());
			Data d = new Data();
			d.running = running;
			d.accumulatedMs = accumulatedMs;
			d.showHud = showHud;
			try (Writer w = Files.newBufferedWriter(file())) {
				GSON.toJson(d, w);
			}
		} catch (Exception e) {
			FlexhudMod.LOGGER.warn("[FlexHUD] Timer-Status konnte nicht gespeichert werden.", e);
		}
	}

	/** Wird jeden Client-Tick aufgerufen. */
	public static synchronized void tick() {
		long now = System.currentTimeMillis();
		if (running) {
			if (lastTickMs > 0) {
				accumulatedMs += Math.max(0, now - lastTickMs);
			}
		}
		lastTickMs = now;
	}

	public static synchronized void start() {
		running = true;
		lastTickMs = System.currentTimeMillis();
		save();
	}

	public static synchronized void pause() {
		tick();
		running = false;
		save();
	}

	public static synchronized void toggle() {
		if (running) {
			pause();
		} else {
			start();
		}
	}

	public static synchronized void reset() {
		accumulatedMs = 0L;
		lastTickMs = System.currentTimeMillis();
		save();
	}

	public static synchronized boolean isRunning() {
		return running;
	}

	public static synchronized long getElapsedMs() {
		return accumulatedMs;
	}

	public static synchronized boolean toggleShow() {
		showHud = !showHud;
		save();
		return showHud;
	}

	public static synchronized boolean shouldShowHud() {
		return showHud;
	}

	/** Format M:SS oder H:MM:SS, optional mit Zehntel. */
	public static String format(long ms, boolean withTenths) {
		long totalSec = ms / 1000;
		long h = totalSec / 3600;
		long m = (totalSec % 3600) / 60;
		long s = totalSec % 60;
		String base = h > 0
			? String.format("%d:%02d:%02d", h, m, s)
			: String.format("%02d:%02d", m, s);
		if (withTenths) {
			base += "." + ((ms % 1000) / 100);
		}
		return base;
	}
}
