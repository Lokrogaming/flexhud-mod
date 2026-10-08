package com.lokro.flexhud.client.config;

/** Animations-Modi für Gradienten. */
public enum AnimationMode {
	OFF,
	SCROLL_L,
	SCROLL_R,
	PULSE,
	RAINBOW;

	public String displayName() {
		return switch (this) {
			case OFF -> "Aus (statisch)";
			case SCROLL_L -> "Scroll links";
			case SCROLL_R -> "Scroll rechts";
			case PULSE -> "Pulsieren";
			case RAINBOW -> "Regenbogen";
		};
	}

	public AnimationMode next() {
		AnimationMode[] v = values();
		return v[(ordinal() + 1) % v.length];
	}
}
