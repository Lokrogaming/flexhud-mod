package com.lokro.flexhud.client.hud;

import com.lokro.flexhud.client.bridge.StepcountBridge;
import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.config.WidgetConfig;
import com.lokro.flexhud.client.config.WidgetType;
import com.lokro.flexhud.client.state.TimerState;
import com.lokro.flexhud.client.util.GradientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Rendert alle aktivierten Widgets als ein HUD-Element. */
public final class WidgetHud {
	private WidgetHud() {}

	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

	public static void render(GuiGraphicsExtractor graphics) {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null || client.level == null) {
			return;
		}
		if (client.gui.hud.isHidden()) {
			return;
		}
		if (client.gui.screen() != null) {
			return;
		}

		int screenW = client.getWindow().getGuiScaledWidth();
		int screenH = client.getWindow().getGuiScaledHeight();
		long now = Util.getMillis();

		for (WidgetConfig w : FlexhudConfig.get().widgets) {
			if (!w.enabled) {
				continue;
			}
			String plain = textFor(w, client);
			if (plain == null) {
				continue;
			}
			MutableComponent text = GradientUtil.style(plain, w.style, now);
			drawCentered(graphics, client, text, w, screenW, screenH);
		}
	}

	/** Vorschau-Text für den Editor. */
	public static String previewText(WidgetConfig w) {
		if (w.type == WidgetType.TEXT) {
			return w.style.hideText ? "" : w.template;
		}
		if (w.type == WidgetType.TIMER) {
			return TimerState.render(w.template, 754000L);
		}
		String tpl = w.style.hideText ? "{value}" : w.template;
		String sample = switch (w.type) {
			case TIMER -> tpl;
			case STEPCOUNT -> "1234";
			case CLOCK -> "21:37:00";
			case TEXT -> tpl;
			case FPS -> "120";
			default -> WidgetValues.previewFor(w.type);
		};
		return applyTemplate(tpl, sample);
	}

	static String textFor(WidgetConfig w, Minecraft client) {
		if (w.type == WidgetType.TEXT) {
			return w.style.hideText ? "" : w.template;
		}
		if (w.type == WidgetType.TIMER) {
			if (!TimerState.shouldShowHud()) {
				return null;
			}
			return TimerState.render(w.template, TimerState.getElapsedMs());
		}
		String tpl = w.style.hideText ? "{value}" : w.template;
		return switch (w.type) {
			case TIMER -> tpl;
			case STEPCOUNT -> {
				if (!StepcountBridge.installed()) {
					yield applyTemplate(tpl, "n/a (StepCount fehlt)");
				}
				yield applyTemplate(tpl, String.valueOf(StepcountBridge.getSteps()));
			}
			case CLOCK -> applyTemplate(tpl, LocalTime.now().format(CLOCK));
			case TEXT -> tpl;
			case FPS -> applyTemplate(tpl, String.valueOf(client.getFps()));
			default -> {
				String v = WidgetValues.valueFor(w.type, client);
				yield v == null ? null : applyTemplate(tpl, v);
			}
		};
	}

	static String applyTemplate(String template, String value) {
		if (template == null || template.isEmpty()) {
			return value;
		}
		if (template.contains("{value}")) {
			return template.replace("{value}", value);
		}
		return template + " " + value;
	}

	private static void drawCentered(GuiGraphicsExtractor graphics, Minecraft client,
			MutableComponent text, WidgetConfig w, int screenW, int screenH) {
		float scale = w.style.scale <= 0 ? 1.0f : w.style.scale;
		int textWidth = client.font.width(text.getVisualOrderText());
		int cx = Math.round(w.x * screenW);
		int cy = Math.round(w.y * screenH);

		if (w.style.background) {
			int pad = 3;
			int alpha = Math.max(0, Math.min(255, w.style.backgroundOpacity)) << 24;
			int wPx = Math.round(textWidth * scale) + pad * 2;
			int hPx = Math.round(9 * scale) + pad * 2;
			graphics.fill(cx - wPx / 2, cy - pad, cx + wPx / 2, cy + hPx - pad, alpha | 0x000000);
		}

		graphics.pose().pushMatrix();
		graphics.pose().translate(cx, cy);
		graphics.pose().scale(scale, scale);
		int x = -textWidth / 2;
		graphics.text(client.font, (Component) text, x, 0, 0xFFFFFFFF, w.style.shadow);
		graphics.pose().popMatrix();
	}
}
