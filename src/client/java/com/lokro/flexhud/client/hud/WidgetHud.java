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

/**
 * Rendert ALLE aktivierten FlexHUD-Widgets als ein HUD-Element.
 * Jedes Widget ist frei positionierbar (x/y relativ), skalierbar und
 * per Gradient/Animation customizbar – im Gegensatz zum fixen StepCount-HUD.
 */
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

	/** Vorschau-Text für den Editor (ohne Spielwelt nutzbar). */
	public static String previewText(WidgetConfig w) {
		return switch (w.type) {
			case TIMER -> applyTemplate(w.template, TimerState.format(754000L, true));
			case STEPCOUNT -> applyTemplate(w.template, "1234");
			case CLOCK -> applyTemplate(w.template, "21:37:00");
			case TEXT -> applyTemplate(w.template, "FlexHUD");
			case FPS -> applyTemplate(w.template, "120");
		};
	}

	static String textFor(WidgetConfig w, Minecraft client) {
		return switch (w.type) {
			case TIMER -> {
				if (!TimerState.shouldShowHud()) {
					yield null;
				}
				yield applyTemplate(w.template, TimerState.format(TimerState.getElapsedMs(), true));
			}
			case STEPCOUNT -> {
				if (!StepcountBridge.installed()) {
					yield applyTemplate(w.template, "n/a (StepCount fehlt)");
				}
				yield applyTemplate(w.template, String.valueOf(StepcountBridge.getSteps()));
			}
			case CLOCK -> applyTemplate(w.template, LocalTime.now().format(CLOCK));
			case TEXT -> applyTemplate(w.template, "FlexHUD");
			case FPS -> applyTemplate(w.template, String.valueOf(client.getFps()));
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
