package com.lokro.flexhud.client.market;

import com.lokro.flexhud.FlexhudMod;
import com.lokro.flexhud.client.config.AnimationMode;
import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.config.WidgetConfig;
import com.lokro.flexhud.client.config.WidgetStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Führt .flexconfig-Buttons auf Widgets aus. */
public final class FlexAction {
	private FlexAction() {}

	/** Button auf einem Widget ausführen. */
	public static String runOnWidget(FlexConfig.FlexButton btn, WidgetConfig w) {
		if (btn == null || w == null) {
			return com.lokro.flexhud.client.i18n.Lang.t("flex.empty");
		}
		try {
			switch (btn.action == null ? "" : btn.action) {
				case "toggleStyle" -> {
					if (toggleFlag(w, btn.field)) {
						FlexhudConfig.save();
						return com.lokro.flexhud.client.i18n.Lang.f("flex.toggled", label(btn), btn.field);
					}
					return com.lokro.flexhud.client.i18n.Lang.f("flex.unknown_field", btn.field, "");
				}
				case "setStyle" -> {
					if (setField(w, btn.field, btn.value)) {
						FlexhudConfig.save();
						return com.lokro.flexhud.client.i18n.Lang.f("flex.set", label(btn), btn.field, btn.value);
					}
					return com.lokro.flexhud.client.i18n.Lang.f("flex.unknown_field", btn.field, btn.value);
				}
				case "applyPreset" -> {
					if (applyPreset(w, btn.value)) {
						FlexhudConfig.save();
						return com.lokro.flexhud.client.i18n.Lang.f("flex.preset", label(btn), btn.value);
					}
					return com.lokro.flexhud.client.i18n.Lang.f("flex.preset_missing", btn.value);
				}
				case "message" -> {
					send(btn.message == null || btn.message.isEmpty() ? label(btn) : btn.message);
					return label(btn);
				}
				default -> {
					return com.lokro.flexhud.client.i18n.Lang.f("flex.unknown_action", btn.action);
				}
			}
		} catch (Exception e) {
			FlexhudMod.LOGGER.warn("[FlexHUD] FlexButton '{}' fehlgeschlagen.", btn.id, e);
			return "Fehler: " + String.valueOf(e.getMessage());
		}
	}

	/** Button auf allen Pack-Widgets ausführen (Overview). */
	public static String runOnPack(FlexConfig.FlexButton btn, String packId) {
		InstalledRegistry.Entry rec = InstalledRegistry.get(packId);
		if (rec == null || rec.widgetIds == null || rec.widgetIds.isEmpty()) {
			return com.lokro.flexhud.client.i18n.Lang.t("packs.nowidgets");
		}
		int n = 0;
		for (String wid : rec.widgetIds) {
			WidgetConfig w = FlexhudConfig.get().byId(wid);
			if (w != null) {
				runOnWidget(btn, w);
				n++;
			}
		}
		FlexhudConfig.save();
		return com.lokro.flexhud.client.i18n.Lang.f("packs.applied", label(btn), n);
	}

	private static String label(FlexConfig.FlexButton btn) {
		return btn.label == null || btn.label.isEmpty() ? btn.id : btn.label;
	}

	private static boolean toggleFlag(WidgetConfig w, String field) {
		if (field == null) {
			return false;
		}
		switch (field) {
			case "bold" -> w.style.bold = !w.style.bold;
			case "italic" -> w.style.italic = !w.style.italic;
			case "shadow" -> w.style.shadow = !w.style.shadow;
			case "background" -> w.style.background = !w.style.background;
			case "hideText" -> w.style.hideText = !w.style.hideText;
			default -> {
				return false;
			}
		}
		return true;
	}

	private static boolean setField(WidgetConfig w, String field, String value) {
		if (field == null || value == null) {
			return false;
		}
		try {
			switch (field) {
				case "bold" -> w.style.bold = parseBool(value);
				case "italic" -> w.style.italic = parseBool(value);
				case "shadow" -> w.style.shadow = parseBool(value);
				case "background" -> w.style.background = parseBool(value);
				case "hideText" -> w.style.hideText = parseBool(value);
				case "scale" -> w.style.scale = clamp(Float.parseFloat(value), 0.5f, 3.0f);
				case "cycleMs" -> w.style.cycleMs = Math.max(200L, Long.parseLong(value));
				case "backgroundOpacity" ->
					w.style.backgroundOpacity = Math.max(0, Math.min(255, Integer.parseInt(value)));
				case "staticColor" -> w.style.staticColor = value.replace("#", "");
				case "animation" -> w.style.animation = AnimationMode.valueOf(value.toUpperCase(Locale.ROOT));
				case "template" -> w.template = value;
				case "gradient" -> {
					List<String> cols = Arrays.stream(value.split(","))
						.map(s -> s.trim().replace("#", ""))
						.filter(s -> !s.isEmpty())
						.collect(Collectors.toList());
					if (cols.isEmpty()) {
						return false;
					}
					w.style.gradient = cols;
				}
				default -> {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static boolean applyPreset(WidgetConfig w, String name) {
		if (name == null) {
			return false;
		}
		for (WidgetStyle.NamedPreset p : WidgetStyle.presets()) {
			if (p.name().equalsIgnoreCase(name.trim())) {
				w.style.gradient = new java.util.ArrayList<>(p.colors());
				return true;
			}
		}
		return false;
	}

	private static boolean parseBool(String v) {
		String s = v.trim().toLowerCase(Locale.ROOT);
		return s.equals("true") || s.equals("1") || s.equals("an") || s.equals("on") || s.equals("yes");
	}

	private static float clamp(float v, float min, float max) {
		return Math.max(min, Math.min(max, v));
	}

	private static void send(String msg) {
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.player != null) {
			client.player.sendSystemMessage(Component.literal("§b[FlexHUD] §f" + msg));
		}
	}
}
