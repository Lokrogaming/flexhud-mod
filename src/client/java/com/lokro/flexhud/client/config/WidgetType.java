package com.lokro.flexhud.client.config;

import com.lokro.flexhud.client.i18n.Lang;

/** Widget-Typen. STEPCOUNT ist eine Bridge zur StepCount-Mod (per Reflection, kein Hard-Dep). */
public enum WidgetType {
	TIMER,
	STEPCOUNT,
	CLOCK,
	TEXT,
	FPS,
	ARMOR,
	CPS,
	COORDS,
	BIOME,
	NETHER,
	COMPASS,
	PITCH,
	DAY,
	PING,
	SERVER,
	MEMORY,
	SPEED,
	PLAYTIME,
	WORLD_TIME,
	HELD,
	LIGHT,
	DISTANCE,
	POTIONS,
	WEATHER,
	FULL_INV,
	KEYS,
	SPRINT,
	ENTITY_COUNT,
	REACH,
	TPS,
	TNT;

	public String displayName() {
		return switch (this) {
			case TIMER -> Lang.t("type.timer");
			case STEPCOUNT -> Lang.t("type.stepcount");
			case CLOCK -> Lang.t("type.clock");
			case TEXT -> Lang.t("type.text");
			case FPS -> Lang.t("type.fps");
			case ARMOR -> Lang.t("type.armor");
			case CPS -> Lang.t("type.cps");
			case COORDS -> Lang.t("type.coords");
			case BIOME -> Lang.t("type.biome");
			case NETHER -> Lang.t("type.nether");
			case COMPASS -> Lang.t("type.compass");
			case PITCH -> Lang.t("type.pitch");
			case DAY -> Lang.t("type.day");
			case PING -> Lang.t("type.ping");
			case SERVER -> Lang.t("type.server");
			case MEMORY -> Lang.t("type.memory");
			case SPEED -> Lang.t("type.speed");
			case PLAYTIME -> Lang.t("type.playtime");
			case WORLD_TIME -> Lang.t("type.world_time");
			case HELD -> Lang.t("type.held");
			case LIGHT -> Lang.t("type.light");
			case DISTANCE -> Lang.t("type.distance");
			case POTIONS -> Lang.t("type.potions");
			case WEATHER -> Lang.t("type.weather");
			case FULL_INV -> Lang.t("type.full_inv");
			case KEYS -> Lang.t("type.keys");
			case SPRINT -> Lang.t("type.sprint");
			case ENTITY_COUNT -> Lang.t("type.entity_count");
			case REACH -> Lang.t("type.reach");
			case TPS -> Lang.t("type.tps");
			case TNT -> Lang.t("type.tnt");
		};
	}

	/** Standard-Vorlage für neu erstellte Widgets. */
	public String defaultTemplate() {
		if (this == TEXT) {
			return "FlexHUD";
		}
		return displayName() + ": {value}";
	}
}
