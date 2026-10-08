package com.lokro.flexhud.client.bridge;

import com.lokro.flexhud.FlexhudMod;

import java.lang.reflect.Method;

/**
 * Bridge zur StepCount-Mod ({@code com.lokro.stepcount.StepCounter}).
 * Nutzt bewusst Reflection statt Hard-Dependency: FlexHUD läuft auch ohne
 * StepCount, das STEPCOUNT-Widget zeigt dann einen Hinweis an.
 *
 * Erwartete StepCounter-API (statisch, synchronized):
 * start(), stop(), reset(), toggleShow(), shouldShowHud(), isCounting(), getSteps()
 */
public final class StepcountBridge {
	private StepcountBridge() {}

	public static boolean installed() {
		return stepCounterClass() != null;
	}

	public static boolean loaderPresent() {
		try {
			Class.forName("net.fabricmc.loader.api.FabricLoader");
			return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("stepcount");
		} catch (Throwable t) {
			return false;
		}
	}

	public static int getSteps() {
		try {
			Object v = call("getSteps");
			if (v instanceof Number n) {
				return n.intValue();
			}
		} catch (Throwable t) {
			FlexhudMod.LOGGER.debug("[FlexHUD] StepCount.getSteps() fehlgeschlagen.", t);
		}
		return 0;
	}

	public static boolean isCounting() {
		try {
			Object v = call("isCounting");
			if (v instanceof Boolean b) {
				return b;
			}
		} catch (Throwable ignored) {
		}
		return false;
	}

	public static boolean shouldShowHud() {
		try {
			Object v = call("shouldShowHud");
			if (v instanceof Boolean b) {
				return b;
			}
		} catch (Throwable ignored) {
		}
		return false;
	}

	public static String command(String action) {
		try {
			Class<?> cls = stepCounterClass();
			if (cls == null) {
				return "StepCount-Mod ist nicht installiert (Mod-Datei fehlt).";
			}
			switch (action.toLowerCase()) {
				case "start" -> {
					cls.getMethod("start").invoke(null);
					return "StepCount gestartet.";
				}
				case "stop" -> {
					cls.getMethod("stop").invoke(null);
					return "StepCount gestoppt.";
				}
				case "reset" -> {
					cls.getMethod("reset").invoke(null);
					return "StepCount zurückgesetzt.";
				}
				case "show" -> {
					Object v = cls.getMethod("toggleShow").invoke(null);
					return Boolean.TRUE.equals(v) ? "StepCount-Anzeige EIN." : "StepCount-Anzeige AUS.";
				}
				default -> {
					return "Unbekannt. Nutze start/stop/reset/show.";
				}
			}
		} catch (Throwable t) {
			FlexhudMod.LOGGER.warn("[FlexHUD] StepCount-Befehl '{}' fehlgeschlagen.", action, t);
			return "Fehler beim Steuern von StepCount (API geändert?).";
		}
	}

	private static Class<?> stepCounterClass() {
		try {
			return Class.forName("com.lokro.stepcount.StepCounter");
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private static Object call(String method) throws Exception {
		Class<?> cls = stepCounterClass();
		if (cls == null) {
			throw new IllegalStateException("StepCount nicht installiert");
		}
		Method m = cls.getMethod(method);
		return m.invoke(null);
	}
}
