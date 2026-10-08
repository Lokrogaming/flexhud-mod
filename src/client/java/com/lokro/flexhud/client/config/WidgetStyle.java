package com.lokro.flexhud.client.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Kosmetik pro Widget: Gradient, Animation, Fett/Kursiv, Schatten, Hintergrund, Skalierung. */
public class WidgetStyle {
	/** Hex-Farben ohne '#', z.B. ["00FFCC","33AAFF","AA55FF","FF55AA","FFAA33","FFFF66"]. */
	public List<String> gradient = new ArrayList<>(Arrays.asList(
		"00FFCC", "33AAFF", "AA55FF", "FF55AA", "FFAA33", "FFFF66"));
	public AnimationMode animation = AnimationMode.SCROLL_L;
	/** Dauer eines Durchlaufs in ms. */
	public long cycleMs = 3500L;
	public boolean bold = true;
	public boolean italic = false;
	public boolean shadow = true;
	public boolean background = false;
	public int backgroundOpacity = 120;
	public float scale = 1.0f;
	/** Statische Farbe (wenn animation == OFF), Hex ohne '#'. */
	public String staticColor = "FFFFFF";

	public WidgetStyle copy() {
		WidgetStyle s = new WidgetStyle();
		s.gradient = new ArrayList<>(gradient);
		s.animation = animation;
		s.cycleMs = cycleMs;
		s.bold = bold;
		s.italic = italic;
		s.shadow = shadow;
		s.background = background;
		s.backgroundOpacity = backgroundOpacity;
		s.scale = scale;
		s.staticColor = staticColor;
		return s;
	}

	/** Vordefinierte Gradient-Presets für den Style-Editor. */
	public static List<NamedPreset> presets() {
		return List.of(
			new NamedPreset("StepCount Classic", List.of("00FFCC", "33AAFF", "AA55FF", "FF55AA", "FFAA33", "FFFF66")),
			new NamedPreset("Ocean", List.of("00FFCC", "00CCFF", "3399FF", "3366FF")),
			new NamedPreset("Sunset", List.of("FF55AA", "FF7744", "FFAA33", "FFFF66")),
			new NamedPreset("Candy", List.of("FF55FF", "AA55FF", "55AAFF", "55FFAA")),
			new NamedPreset("Mono Weiss", List.of("FFFFFF")),
			new NamedPreset("Gold", List.of("FFAA00", "FFDD55", "FFFFFF", "FFDD55", "FFAA00"))
		);
	}

	public record NamedPreset(String name, List<String> colors) {}
}
