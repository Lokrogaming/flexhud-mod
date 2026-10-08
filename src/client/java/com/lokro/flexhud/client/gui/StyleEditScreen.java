package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.config.AnimationMode;
import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.config.WidgetConfig;
import com.lokro.flexhud.client.config.WidgetStyle;
import com.lokro.flexhud.client.hud.WidgetHud;
import com.lokro.flexhud.client.util.GradientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;

/**
 * Stil-Editor für ein Widget: Live-Vorschau (echter Stil + echte Animation),
 * Vorlage ({value}-Platzhalter), Gradient-Presets, Animation, Dauer,
 * Fett/Kursiv, Schatten, Hintergrund + Deckkraft, Text an/aus, Skalierung.
 *
 * <p>Layout ist responsiv (Spaltenbreite aus der Fensterbreite) und die
 * Zeilen scrollen, falls der Bildschirm zu niedrig ist.
 */
public class StyleEditScreen extends Screen {
	private final Screen parent;
	private final String widgetId;
	private EditBox templateBox;
	private int presetIndex = 0;
	private int scrollOffset = 0;

	private static final int ROW_H = 22;
	private static final int ROWS = 9;

	public StyleEditScreen(Screen parent, String widgetId) {
		super(Component.literal("FlexHUD – Stil"));
		this.parent = parent;
		this.widgetId = widgetId;
	}

	private WidgetConfig widget() {
		return FlexhudConfig.get().byId(widgetId);
	}

	private int contentW() {
		return Math.min(320, this.width - 20);
	}

	private int colX() {
		return (this.width - contentW()) / 2;
	}

	private int contentTop() {
		return 98;
	}

	private int footerTop() {
		return this.height - 30;
	}

	private int maxScroll() {
		int avail = footerTop() - contentTop();
		return Math.max(0, ROWS * ROW_H - avail);
	}

	private int rowY(int row) {
		return contentTop() + row * ROW_H - scrollOffset;
	}

	private boolean visible(int y) {
		return y + 20 >= contentTop() && y <= footerTop() - 4;
	}

	@Override
	protected void init() {
		WidgetConfig w = widget();
		if (w == null) {
			onClose();
			return;
		}
		scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll()));
		int x = colX();
		int cw = contentW();
		int half = (cw - 4) / 2;

		// Zeile 0: Vorlage
		if (visible(rowY(0))) {
			templateBox = new EditBox(this.font, x, rowY(0), cw, 20, Component.literal("Vorlage"));
			templateBox.setValue(w.template);
			templateBox.setResponder(v -> {
				WidgetConfig ww = widget();
				if (ww != null) {
					ww.template = v;
				}
			});
			addRenderableWidget(templateBox);
		}

		// Zeile 1: Animation (voll)
		if (visible(rowY(1))) {
			addRenderableWidget(Button.builder(Component.literal("Animation: " + w.style.animation.displayName()),
				b -> {
					w.style.animation = w.style.animation.next();
					b.setMessage(Component.literal("Animation: " + w.style.animation.displayName()));
					FlexhudConfig.save();
				}).bounds(x, rowY(1), cw, 20).build());
		}

		// Zeile 2: Dauer -/+
		if (visible(rowY(2))) {
			addRenderableWidget(Button.builder(Component.literal("Dauer - (" + w.style.cycleMs + "ms)"),
				b -> {
					w.style.cycleMs = Math.max(500, w.style.cycleMs - 500);
					b.setMessage(Component.literal("Dauer - (" + w.style.cycleMs + "ms)"));
					FlexhudConfig.save();
				}).bounds(x, rowY(2), half, 20).build());
			addRenderableWidget(Button.builder(Component.literal("Dauer + (" + w.style.cycleMs + "ms)"),
				b -> {
					w.style.cycleMs = Math.min(20000, w.style.cycleMs + 500);
					b.setMessage(Component.literal("Dauer + (" + w.style.cycleMs + "ms)"));
					FlexhudConfig.save();
				}).bounds(x + half + 4, rowY(2), cw - half - 4, 20).build());
		}

		// Zeile 3: Fett / Kursiv
		if (visible(rowY(3))) {
			addRenderableWidget(Button.builder(Component.literal("Fett: " + anAus(w.style.bold)),
				b -> toggle(b, "Fett")).bounds(x, rowY(3), half, 20).build());
			addRenderableWidget(Button.builder(Component.literal("Kursiv: " + anAus(w.style.italic)),
				b -> toggle(b, "Kursiv")).bounds(x + half + 4, rowY(3), cw - half - 4, 20).build());
		}

		// Zeile 4: Schatten / Hintergrund
		if (visible(rowY(4))) {
			addRenderableWidget(Button.builder(Component.literal("Schatten: " + anAus(w.style.shadow)),
				b -> toggle(b, "Schatten")).bounds(x, rowY(4), half, 20).build());
			addRenderableWidget(Button.builder(Component.literal("Hintergrund: " + anAus(w.style.background)),
				b -> toggle(b, "Hintergrund")).bounds(x + half + 4, rowY(4), cw - half - 4, 20).build());
		}

		// Zeile 5: Text (voll)
		if (visible(rowY(5))) {
			addRenderableWidget(Button.builder(Component.literal("Text: " + anAus(!w.style.hideText)),
				b -> toggle(b, "Text")).bounds(x, rowY(5), cw, 20).build());
		}

		// Zeile 6: Deckkraft -/+
		if (visible(rowY(6))) {
			addRenderableWidget(Button.builder(Component.literal("Deckkraft - (" + w.style.backgroundOpacity + ")"),
				b -> {
					w.style.backgroundOpacity = Math.max(0, w.style.backgroundOpacity - 20);
					FlexhudConfig.save();
					rebuildWidgets();
				}).bounds(x, rowY(6), half, 20).build());
			addRenderableWidget(Button.builder(Component.literal("Deckkraft + (" + w.style.backgroundOpacity + ")"),
				b -> {
					w.style.backgroundOpacity = Math.min(255, w.style.backgroundOpacity + 20);
					w.style.background = true; // Deckkraft erhöhen blendet den Hintergrund ein
					FlexhudConfig.save();
					rebuildWidgets();
				}).bounds(x + half + 4, rowY(6), cw - half - 4, 20).build());
		}

		// Zeile 7: Preset (voll)
		if (visible(rowY(7))) {
			addRenderableWidget(Button.builder(Component.literal("Preset: " + presetName()),
				b -> {
					applyNextPreset();
					b.setMessage(Component.literal("Preset: " + presetName()));
				}).bounds(x, rowY(7), cw, 20).build());
		}

		// Zeile 8: Scale -/+
		if (visible(rowY(8))) {
			addRenderableWidget(Button.builder(Component.literal("Scale - (" + fmt(w.style.scale) + ")"),
				b -> {
					w.style.scale = Math.max(0.5f, w.style.scale - 0.1f);
					FlexhudConfig.save();
					rebuildWidgets();
				}).bounds(x, rowY(8), half, 20).build());
			addRenderableWidget(Button.builder(Component.literal("Scale + (" + fmt(w.style.scale) + ")"),
				b -> {
					w.style.scale = Math.min(3.0f, w.style.scale + 0.1f);
					FlexhudConfig.save();
					rebuildWidgets();
				}).bounds(x + half + 4, rowY(8), cw - half - 4, 20).build());
		}

		// Zurück: immer sichtbar fixiert
		addRenderableWidget(Button.builder(Component.literal("Zurück (speichert)"),
			b -> onClose()).bounds(x + (cw - 200) / 2, this.height - 26, 200, 20).build());
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		int max = maxScroll();
		if (max > 0) {
			double d = vertical != 0 ? vertical : horizontal;
			int next = (int) Math.max(0, Math.min(max, scrollOffset - d * 14));
			if (next != scrollOffset) {
				scrollOffset = next;
				rebuildWidgets();
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}

	private void toggle(Button b, String what) {
		WidgetConfig w = widget();
		if (w == null) {
			return;
		}
		switch (what) {
			case "Fett" -> w.style.bold = !w.style.bold;
			case "Kursiv" -> w.style.italic = !w.style.italic;
			case "Schatten" -> w.style.shadow = !w.style.shadow;
			case "Hintergrund" -> w.style.background = !w.style.background;
			case "Text" -> w.style.hideText = !w.style.hideText;
		}
		b.setMessage(Component.literal(what + ": " + anAus(
			switch (what) {
				case "Fett" -> w.style.bold;
				case "Kursiv" -> w.style.italic;
				case "Schatten" -> w.style.shadow;
				case "Text" -> !w.style.hideText;
				default -> w.style.background;
			})));
		FlexhudConfig.save();
	}

	private String presetName() {
		return WidgetStyle.presets().get(presetIndex % WidgetStyle.presets().size()).name();
	}

	private void applyNextPreset() {
		WidgetConfig w = widget();
		if (w == null) {
			return;
		}
		presetIndex = (presetIndex + 1) % WidgetStyle.presets().size();
		WidgetStyle.NamedPreset p = WidgetStyle.presets().get(presetIndex);
		w.style.gradient = new java.util.ArrayList<>(p.colors());
		if (w.style.animation == AnimationMode.OFF) {
			w.style.animation = AnimationMode.SCROLL_L;
		}
		FlexhudConfig.save();
	}

	private static String anAus(boolean v) {
		return v ? "AN" : "AUS";
	}

	private static String fmt(float f) {
		return String.format(java.util.Locale.ROOT, "%.1f", f);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		// Abdunkeln für Lesbarkeit (hinter den Widgets)
		graphics.fill(0, 0, this.width, this.height, 0xA0000000);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		WidgetConfig w = widget();
		if (w == null) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		String title = "Stil: " + w.id + " [" + w.type.displayName() + "]";
		graphics.centeredText(this.font, title, this.width / 2, 6, 0xFFFFFF);

		// Live-Vorschau mit echtem Stil + echter Animation
		int x = colX();
		int cw = contentW();
		int pvY = 18;
		int pvH = 62;
		graphics.fill(x, pvY, x + cw, pvY + pvH, 0x60000000);
		graphics.outline(x, pvY, x + cw, pvY + pvH, 0xFF55FFFF);
		graphics.text(this.font, "Live-Vorschau", x + 6, pvY + 3, 0xAAAAAA);

		String plain = WidgetHud.previewText(w);
		MutableComponent text = GradientUtil.style(plain.isEmpty() ? "(leer)" : plain, w.style, Util.getMillis());
		int tw = this.font.width(text.getVisualOrderText());
		float scale = 1.3f;
		int cx = x + cw / 2;
		int cy = pvY + pvH / 2 - 4;
		if (w.style.background && !plain.isEmpty()) {
			int pad = 4;
			int alpha = Math.max(0, Math.min(255, w.style.backgroundOpacity)) << 24;
			int sw = Math.round(tw * scale) + pad * 2;
			int sh = Math.round(9 * scale) + pad * 2;
			graphics.fill(cx - sw / 2, cy - pad, cx + sw / 2, cy + sh - pad, alpha | 0x000000);
		}
		graphics.pose().pushMatrix();
		graphics.pose().translate(cx, cy);
		graphics.pose().scale(scale, scale);
		graphics.text(this.font, (Component) text, -tw / 2, 0, 0xFFFFFFFF, w.style.shadow);
		graphics.pose().popMatrix();

		if (maxScroll() > 0) {
			graphics.centeredText(this.font, "↕ Scrollen für mehr Optionen",
				this.width / 2, footerTop() - 12, 0x888888);
		}
	}

	@Override
	public void onClose() {
		FlexhudConfig.save();
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			client.gui.setScreen(parent);
		}
	}
}
