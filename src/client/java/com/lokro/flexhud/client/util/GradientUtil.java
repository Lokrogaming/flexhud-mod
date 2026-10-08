package com.lokro.flexhud.client.util;

import com.lokro.flexhud.client.config.AnimationMode;
import com.lokro.flexhud.client.config.WidgetStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;

/** Stilisierte Texte: statisch oder animierter Gradient (customizbar). */
public final class GradientUtil {
	private GradientUtil() {}

	private static final int[][] RAINBOW = {
		{0xFF, 0x00, 0x00}, {0xFF, 0x7F, 0x00}, {0xFF, 0xFF, 0x00},
		{0x00, 0xFF, 0x00}, {0x00, 0x00, 0xFF}, {0x8B, 0x00, 0xFF},
	};

	public static MutableComponent style(String plain, WidgetStyle style, long nowMillis) {
		if (plain == null) {
			plain = "";
		}
		if (plain.isEmpty()) {
			return Component.empty().copy();
		}
		if (style.animation == AnimationMode.OFF) {
			int rgb = parseHex(style.staticColor, 0xFFFFFF);
			Style s = Style.EMPTY
				.withBold(style.bold)
				.withItalic(style.italic)
				.withColor(TextColor.fromRgb(rgb));
			return Component.literal(plain).withStyle(s).copy();
		}
		int[][] stops = stopsOf(style);
		float t = cycleT(nowMillis, style.cycleMs);
		int len = plain.length();
		MutableComponent out = Component.empty().copy();
		for (int i = 0; i < len; i++) {
			float p = phase(i, len, t, style.animation);
			int rgb = sampleLoop(stops, p);
			Style s = Style.EMPTY
				.withBold(style.bold)
				.withItalic(style.italic)
				.withColor(TextColor.fromRgb(rgb));
			out.append(Component.literal(String.valueOf(plain.charAt(i))).withStyle(s));
		}
		return out;
	}

	private static float cycleT(long now, long cycleMs) {
		if (cycleMs <= 0) {
			cycleMs = 3500L;
		}
		return (now % cycleMs) / (float) cycleMs;
	}

	private static float phase(int i, int len, float t, AnimationMode mode) {
		float frac = len <= 1 ? 0f : (i / (float) (len - 1));
		return switch (mode) {
			case SCROLL_L -> (frac + t) % 1.0f;
			case SCROLL_R -> (frac - t + 1.0f) % 1.0f;
			case PULSE -> (float) ((Math.sin((frac * 2 - t * 2) * Math.PI * 2) + 1.0) / 2.0);
			case RAINBOW -> (frac + t) % 1.0f;
			default -> frac;
		};
	}

	private static int[][] stopsOf(WidgetStyle style) {
		if (style.animation == AnimationMode.RAINBOW) {
			return RAINBOW;
		}
		List<int[]> list = new ArrayList<>();
		for (String hex : style.gradient) {
			int rgb = parseHex(hex, 0xFFFFFF);
			list.add(new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF});
		}
		if (list.isEmpty()) {
			list.add(new int[]{0xFF, 0xFF, 0xFF});
		}
		if (list.size() == 1) {
			list.add(list.get(0));
		}
		return list.toArray(new int[0][]);
	}

	private static int sampleLoop(int[][] stops, float p) {
		int n = stops.length;
		float scaled = ((p % 1.0f) + 1.0f) % 1.0f * n;
		int idx = (int) Math.floor(scaled) % n;
		if (idx < 0) {
			idx += n;
		}
		float frac = scaled - (float) Math.floor(scaled);
		frac = frac * frac * (3.0f - 2.0f * frac); // smoothstep
		int[] a = stops[idx];
		int[] b = stops[(idx + 1) % n];
		int r = Math.round(a[0] + (b[0] - a[0]) * frac);
		int g = Math.round(a[1] + (b[1] - a[1]) * frac);
		int bl = Math.round(a[2] + (b[2] - a[2]) * frac);
		return (r << 16) | (g << 8) | bl;
	}

	public static int parseHex(String hex, int fallback) {
		if (hex == null) {
			return fallback;
		}
		String h = hex.trim().replace("#", "").replace("0x", "");
		if (h.length() == 3) {
			char[] c = h.toCharArray();
			h = "" + c[0] + c[0] + c[1] + c[1] + c[2] + c[2];
		}
		try {
			return Integer.parseInt(h, 16) & 0xFFFFFF;
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}
