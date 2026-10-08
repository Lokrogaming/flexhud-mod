package com.lokro.flexhud.client.config;

import com.lokro.flexhud.client.i18n.Lang;

/** Animations-Modi für Gradienten. */
public enum AnimationMode {
	OFF,
	SCROLL_L,
	SCROLL_R,
	PULSE,
	RAINBOW;

	public String displayName() {
		return switch (this) {
			case OFF -> Lang.t("anim.off");
			case SCROLL_L -> Lang.t("anim.scroll_l");
			case SCROLL_R -> Lang.t("anim.scroll_r");
			case PULSE -> Lang.t("anim.pulse");
			case RAINBOW -> Lang.t("anim.rainbow");
		};
	}

	public AnimationMode next() {
		AnimationMode[] v = values();
		return v[(ordinal() + 1) % v.length];
	}
}
