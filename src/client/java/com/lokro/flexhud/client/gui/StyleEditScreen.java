package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.config.AnimationMode;
import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.config.WidgetConfig;
import com.lokro.flexhud.client.config.WidgetStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Stil-Editor für ein Widget: Gradient-Presets, Animation, Dauer, Fett/Kursiv,
 * Schatten, Hintergrund, Skalierung, Vorlage ({value}-Platzhalter).
 */
public class StyleEditScreen extends Screen {
	private final Screen parent;
	private final String widgetId;
	private EditBox templateBox;
	private int presetIndex = 0;

	public StyleEditScreen(Screen parent, String widgetId) {
		super(Component.literal("FlexHUD – Stil"));
		this.parent = parent;
		this.widgetId = widgetId;
	}

	private WidgetConfig widget() {
		return FlexhudConfig.get().byId(widgetId);
	}

	@Override
	protected void init() {
		WidgetConfig w = widget();
		if (w == null) {
			onClose();
			return;
		}
		int cx = this.width / 2;
		int y = 60;

		templateBox = new EditBox(this.font, cx - 150, y, 300, 20, Component.literal("Vorlage"));
		templateBox.setValue(w.template);
		templateBox.setResponder(v -> {
			WidgetConfig ww = widget();
			if (ww != null) {
				ww.template = v;
			}
		});
		addRenderableWidget(templateBox);
		y += 26;

		addRenderableWidget(Button.builder(Component.literal("Animation: " + w.style.animation.displayName()),
			b -> {
				w.style.animation = w.style.animation.next();
				b.setMessage(Component.literal("Animation: " + w.style.animation.displayName()));
				FlexhudConfig.save();
			}).bounds(cx - 150, y, 300, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("Dauer - (" + w.style.cycleMs + "ms)"),
			b -> {
				w.style.cycleMs = Math.max(500, w.style.cycleMs - 500);
				b.setMessage(Component.literal("Dauer - (" + w.style.cycleMs + "ms)"));
				FlexhudConfig.save();
			}).bounds(cx - 150, y, 148, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Dauer + (" + w.style.cycleMs + "ms)"),
			b -> {
				w.style.cycleMs = Math.min(20000, w.style.cycleMs + 500);
				b.setMessage(Component.literal("Dauer - (" + w.style.cycleMs + "ms)"));
				FlexhudConfig.save();
			}).bounds(cx + 2, y, 148, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("Fett: " + anAus(w.style.bold)),
			b -> toggle(b, "Fett")).bounds(cx - 150, y, 148, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Kursiv: " + anAus(w.style.italic)),
			b -> toggle(b, "Kursiv")).bounds(cx + 2, y, 148, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("Schatten: " + anAus(w.style.shadow)),
			b -> toggle(b, "Schatten")).bounds(cx - 150, y, 148, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Hintergrund: " + anAus(w.style.background)),
			b -> toggle(b, "Hintergrund")).bounds(cx + 2, y, 148, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("Text: " + anAus(!w.style.hideText)),
			b -> toggle(b, "Text")).bounds(cx - 150, y, 300, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("Deckkraft - (" + w.style.backgroundOpacity + ")"),
			b -> {
				w.style.backgroundOpacity = Math.max(0, w.style.backgroundOpacity - 20);
				FlexhudConfig.save();
				rebuildWidgets();
			}).bounds(cx - 150, y, 148, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Deckkraft + (" + w.style.backgroundOpacity + ")"),
			b -> {
				w.style.backgroundOpacity = Math.min(255, w.style.backgroundOpacity + 20);
				w.style.background = true; // Deckkraft erhöhen blendet den Hintergrund ein
				FlexhudConfig.save();
				rebuildWidgets();
			}).bounds(cx + 2, y, 148, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("Preset: " + presetName()),
			b -> {
				applyNextPreset();
				b.setMessage(Component.literal("Preset: " + presetName()));
			}).bounds(cx - 150, y, 300, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("Scale - (" + fmt(w.style.scale) + ")"),
			b -> {
				w.style.scale = Math.max(0.5f, w.style.scale - 0.1f);
				FlexhudConfig.save();
				rebuildWidgets();
			}).bounds(cx - 150, y, 148, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Scale + (" + fmt(w.style.scale) + ")"),
			b -> {
				w.style.scale = Math.min(3.0f, w.style.scale + 0.1f);
				FlexhudConfig.save();
				rebuildWidgets();
			}).bounds(cx + 2, y, 148, 20).build());
		y += 32;

		addRenderableWidget(Button.builder(Component.literal("Zurück (speichert)"),
			b -> onClose()).bounds(cx - 100, this.height - 30, 200, 20).build());
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
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		WidgetConfig w = widget();
		String title = w == null ? "Stil (?)" : "Stil: " + w.id + " [" + w.type.displayName() + "]";
		graphics.text(this.font, title, this.width / 2 - 150, 22, 0xFFFFFF);
		graphics.text(this.font, "Tipp: {value} = Live-Wert (Zeit/Steps/FPS).", this.width / 2 - 150, 34, 0xAAAAAA);
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
