package com.lokro.flexhud.client.config;

import com.lokro.flexhud.client.i18n.Lang;

/** Widget-Typen. STEPCOUNT ist eine Bridge zur StepCount-Mod (per Reflection, kein Hard-Dep). */
public enum WidgetType {
	TIMER,
	STEPCOUNT,
	CLOCK,
	TEXT,
	FPS;

	public String displayName() {
		return switch (this) {
			case TIMER -> Lang.t("type.timer");
			case STEPCOUNT -> Lang.t("type.stepcount");
			case CLOCK -> Lang.t("type.clock");
			case TEXT -> Lang.t("type.text");
			case FPS -> Lang.t("type.fps");
		};
	}
}
