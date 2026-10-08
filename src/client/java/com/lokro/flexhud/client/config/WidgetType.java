package com.lokro.flexhud.client.config;

/** Widget-Typen. STEPCOUNT ist eine Bridge zur StepCount-Mod (per Reflection, kein Hard-Dep). */
public enum WidgetType {
	TIMER,
	STEPCOUNT,
	CLOCK,
	TEXT,
	FPS;

	public String displayName() {
		return switch (this) {
			case TIMER -> "Timer";
			case STEPCOUNT -> "StepCount-Bridge";
			case CLOCK -> "Uhrzeit";
			case TEXT -> "Text";
			case FPS -> "FPS";
		};
	}
}
