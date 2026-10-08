package com.lokro.flexhud.client.config;

import java.util.ArrayList;
import java.util.List;

/** Ein einzelnes HUD-Widget (Position relativ 0..1, frei verschiebbar). */
public class WidgetConfig {
	public String id = "timer-1";
	public WidgetType type = WidgetType.TIMER;
	/** Anzeigename/Vorlage. Platzhalter {value} wird ersetzt. */
	public String template = "Timer: {value}";
	public float x = 0.5f;
	public float y = 0.72f;
	public boolean enabled = true;
	public WidgetStyle style = new WidgetStyle();

	public WidgetConfig() {}

	public WidgetConfig(String id, WidgetType type, String template, float x, float y) {
		this.id = id;
		this.type = type;
		this.template = template;
		this.x = x;
		this.y = y;
	}

	public WidgetConfig copy() {
		WidgetConfig c = new WidgetConfig();
		c.id = id;
		c.type = type;
		c.template = template;
		c.x = x;
		c.y = y;
		c.enabled = enabled;
		c.style = style.copy();
		return c;
	}
}
