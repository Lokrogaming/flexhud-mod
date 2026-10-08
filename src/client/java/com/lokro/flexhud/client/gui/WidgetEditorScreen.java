package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.config.WidgetConfig;
import com.lokro.flexhud.client.config.WidgetType;
import com.lokro.flexhud.client.hud.WidgetHud;
import com.lokro.flexhud.client.util.GradientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;

/**
 * Widget-Editor: Widgets adden/löschen/an-aus, per Drag in der Live-Vorschau
 * frei positionieren, Skalierung ändern, Stil-Editor öffnen.
 *
 * <p>Layout ist responsiv (keine fixen Pixel-Annahmen): links die Widget-Liste,
 * rechts die Aktionen, unten eine Live-Vorschau, die alle Widgets mit ihrem
 * ECHTEN Stil (Gradient + Animation) rendert. Klick in der Vorschau wählt das
 * nächste Widget, Ziehen verschiebt es.
 */
public class WidgetEditorScreen extends Screen {
	private final Screen parent;
	private String selectedId = null;
	private boolean dragging = false;
	private int listPage = 0;

	// Vorschau-Rechteck (wird in init()/extractRenderState identisch berechnet)
	private int prevX, prevY, prevW, prevH;

	public WidgetEditorScreen(Screen parent) {
		super(Component.literal("FlexHUD – Widget-Editor"));
		this.parent = parent;
		FlexhudConfig cfg = FlexhudConfig.get();
		if (!cfg.widgets.isEmpty()) {
			selectedId = cfg.widgets.get(0).id;
		}
	}

	// ---------- Layout-Helfer (eine Quelle für init + Rendering + Maus) ----------

	private int listW() {
		return Math.max(150, Math.min(240, this.width / 3));
	}

	private int panelW() {
		return Math.max(150, Math.min(200, this.width / 3 - 20));
	}

	private int panelX() {
		return this.width - panelW() - 10;
	}

	private int contentTop() {
		return 36;
	}

	private int footerH() {
		return 26;
	}

	private void computePreview() {
		prevH = Math.max(80, Math.min(130, this.height / 4));
		prevW = this.width - 20;
		prevX = 10;
		prevY = this.height - footerH() - prevH - 6;
	}

	private int pageSize() {
		int avail = Math.max(60, prevY - contentTop() - 26);
		return Math.max(1, avail / 22);
	}

	// ---------- Aufbau ----------

	@Override
	protected void init() {
		computePreview();
		FlexhudConfig cfg = FlexhudConfig.get();

		// Linke Liste (mit Paging bei Überlauf)
		int lw = listW();
		int ps = pageSize();
		int pages = Math.max(1, (cfg.widgets.size() + ps - 1) / ps);
		listPage = Math.max(0, Math.min(listPage, pages - 1));
		int y = contentTop();
		for (int i = listPage * ps; i < Math.min(cfg.widgets.size(), (listPage + 1) * ps); i++) {
			WidgetConfig w = cfg.widgets.get(i);
			String label = (w.id.equals(selectedId) ? "> " : "")
				+ w.id + " [" + w.type.displayName() + "] " + (w.enabled ? "AN" : "AUS");
			if (label.length() > 32) {
				label = label.substring(0, 32) + "…";
			}
			String wid = w.id;
			addRenderableWidget(Button.builder(Component.literal(label),
				b -> {
					selectedId = wid;
					rebuildWidgets();
				}).bounds(10, y, lw, 20).build());
			y += 22;
		}
		if (pages > 1) {
			String pg = "Seite " + (listPage + 1) + "/" + pages;
			addRenderableWidget(Button.builder(Component.literal("▲ " + pg),
				b -> {
					listPage = Math.max(0, listPage - 1);
					rebuildWidgets();
				}).bounds(10, y, lw / 2 - 1, 18).build());
			addRenderableWidget(Button.builder(Component.literal("▼ " + pg),
				b -> {
					listPage = Math.min(pages - 1, listPage + 1);
					rebuildWidgets();
				}).bounds(10 + lw / 2 + 1, y, lw - lw / 2 - 1, 18).build());
		}

		// Rechtes Aktions-Panel (volle Breite, kein Überlauf möglich)
		int px = panelX();
		int pw = panelW();
		int by = contentTop();
		by = addFull(px, by, pw, "+ Timer", () -> add(WidgetType.TIMER, "Timer: {value}"));
		by = addFull(px, by, pw, "+ StepCount", () -> add(WidgetType.STEPCOUNT, "Steps: {value}"));
		by = addFull(px, by, pw, "+ Uhr", () -> add(WidgetType.CLOCK, "{value}"));
		by = addFull(px, by, pw, "+ Text", () -> add(WidgetType.TEXT, "FlexHUD"));
		by = addFull(px, by, pw, "+ FPS", () -> add(WidgetType.FPS, "FPS: {value}"));
		by = addFull(px, by, pw, "An/Aus", this::toggleSelected);
		by = addFull(px, by, pw, "Stil…", () -> {
			if (selectedId != null) {
				switchTo(new StyleEditScreen(this, selectedId));
			}
		});
		by = addFull(px, by, pw, "Löschen", this::deleteSelected);
		// Scale nebeneinander
		if (by + 20 <= prevY - 4) {
			addRenderableWidget(Button.builder(Component.literal("Scale -"),
				b -> scaleSelected(0.9f)).bounds(px, by, pw / 2 - 1, 20).build());
			addRenderableWidget(Button.builder(Component.literal("Scale +"),
				b -> scaleSelected(1.1f)).bounds(px + pw / 2 + 1, by, pw - pw / 2 - 1, 20).build());
			by += 22;
		}
		// Pfeil-Tasten in einer Reihe
		if (by + 20 <= prevY - 4) {
			int aw = (pw - 3 * 2) / 4;
			String[] arrows = {"◀", "▶", "▲", "▼"};
			float[] dx = {-0.01f, 0.01f, 0f, 0f};
			float[] dy = {0f, 0f, -0.01f, 0.01f};
			for (int i = 0; i < 4; i++) {
				final float ddx = dx[i];
				final float ddy = dy[i];
				addRenderableWidget(Button.builder(Component.literal(arrows[i]),
					b -> nudge(ddx, ddy)).bounds(px + i * (aw + 2), by, aw, 20).build());
			}
		}

		addRenderableWidget(Button.builder(Component.literal("Zurück"),
			b -> onClose()).bounds(10, this.height - footerH() + 2, 100, 20).build());
	}

	private int addFull(int x, int y, int w, String label, Runnable action) {
		if (y + 20 > prevY - 4) {
			return y; // kein Platz mehr -> weitere Buttons weglassen statt zu überlappen
		}
		addRenderableWidget(Button.builder(Component.literal(label),
			b -> action.run()).bounds(x, y, w, 20).build());
		return y + 22;
	}

	// ---------- Live-Vorschau ----------

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		computePreview();
		Minecraft client = Minecraft.getInstance();
		if (client == null) {
			return;
		}

		graphics.centeredText(this.font, "FlexHUD – Widget-Editor", this.width / 2, 8, 0xFFFFFF);
		graphics.centeredText(this.font, "Liste: wählen · Vorschau: anklicken + ziehen",
			this.width / 2, 20, 0xAAAAAA);

		// Detail-Infos zum gewählten Widget in der Mitte
		WidgetConfig sel = selected();
		int infoX = listW() + 20;
		int infoW = panelX() - infoX - 10;
		if (sel != null && infoW > 60) {
			int iy = contentTop() + 2;
			graphics.text(this.font, "Auswahl: " + sel.id, infoX, iy, 0x55FFFF);
			graphics.text(this.font, "Typ: " + sel.type.displayName()
				+ (sel.enabled ? " (AN)" : " (AUS)"), infoX, iy + 12, 0xCCCCCC);
			graphics.text(this.font, String.format("Pos: %d%% / %d%%   Scale: %.1f",
				Math.round(sel.x * 100), Math.round(sel.y * 100), sel.style.scale),
				infoX, iy + 24, 0xCCCCCC);
			graphics.text(this.font, "Stil: " + sel.style.animation.displayName()
				+ " · " + sel.style.cycleMs + "ms"
				+ (sel.style.bold ? " · fett" : "")
				+ (sel.style.hideText ? " · ohne Text" : ""), infoX, iy + 36, 0xCCCCCC);
			String tpl = sel.template.length() > 30 ? sel.template.substring(0, 30) + "…" : sel.template;
			graphics.text(this.font, "Vorlage: " + tpl, infoX, iy + 48, 0x888888);
		}

		// Vorschau-Rahmen
		graphics.fill(prevX, prevY, prevX + prevW, prevY + prevH, 0x60000000);
		graphics.outline(prevX, prevY, prevX + prevW, prevY + prevH, 0xFF55FFFF);
		graphics.text(this.font, "Live-Vorschau (echter Stil, echte Animation)",
			prevX + 6, prevY + 4, 0xAAAAAA);

		long now = Util.getMillis();
		for (WidgetConfig w : FlexhudConfig.get().widgets) {
			int cx = prevX + Math.round(w.x * prevW);
			int cy = prevY + 16 + Math.round(w.y * (prevH - 30));
			boolean isSel = w.id.equals(selectedId);
			String plain = WidgetHud.previewText(w);
			if (!w.enabled) {
				plain = "[aus] " + plain;
			}
			MutableComponent text = GradientUtil.style(plain, w.style, now);
			int textWidth = client.font.width(text.getVisualOrderText());
			float scale = Math.max(0.5f, Math.min(1.0f, prevW / 500f));

			if (isSel) {
				int pad = 3;
				int sw = Math.round(textWidth * scale) + pad * 2;
				graphics.fill(cx - sw / 2, cy - pad, cx + sw / 2, cy + Math.round(9 * scale) + pad, 0x4055FFFF);
			}
			graphics.pose().pushMatrix();
			graphics.pose().translate(cx, cy);
			graphics.pose().scale(scale, scale);
			graphics.text(client.font, (Component) text, -textWidth / 2, 0, 0xFFFFFFFF, w.style.shadow);
			graphics.pose().popMatrix();
		}
	}

	// ---------- Maus (Vorschau) ----------

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
		computePreview();
		if (event.button() == 0 && inside(event.x(), event.y())) {
			WidgetConfig nearest = nearestWidget(event.x(), event.y());
			if (nearest != null) {
				selectedId = nearest.id;
				dragging = true;
				moveTo(nearest, event.x(), event.y());
				rebuildWidgets();
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {
		if (dragging) {
			computePreview();
			WidgetConfig w = selected();
			if (w != null) {
				moveTo(w, event.x(), event.y());
				return true;
			}
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
		if (dragging && event.button() == 0) {
			dragging = false;
			FlexhudConfig.save();
			return true;
		}
		return super.mouseReleased(event);
	}

	private boolean inside(double mx, double my) {
		return mx >= prevX && mx <= prevX + prevW && my >= prevY && my <= prevY + prevH;
	}

	private int widgetPx(WidgetConfig w) {
		return prevX + Math.round(w.x * prevW);
	}

	private int widgetPy(WidgetConfig w) {
		return prevY + 16 + Math.round(w.y * (prevH - 30));
	}

	private WidgetConfig nearestWidget(double mx, double my) {
		WidgetConfig best = null;
		double bestDist = 16.0;
		for (WidgetConfig w : FlexhudConfig.get().widgets) {
			double d = Math.hypot(mx - widgetPx(w), my - widgetPy(w));
			if (d < bestDist) {
				bestDist = d;
				best = w;
			}
		}
		return best;
	}

	private void moveTo(WidgetConfig w, double mx, double my) {
		float nx = (float) ((mx - prevX) / (double) prevW);
		float ny = (float) ((my - (prevY + 16)) / (double) (prevH - 30));
		w.x = Math.max(0f, Math.min(1f, nx));
		w.y = Math.max(0f, Math.min(1f, ny));
	}

	// ---------- Aktionen ----------

	private WidgetConfig selected() {
		if (selectedId == null) {
			return null;
		}
		return FlexhudConfig.get().byId(selectedId);
	}

	private void add(WidgetType type, String template) {
		FlexhudConfig cfg = FlexhudConfig.get();
		String id = cfg.freeId(type);
		WidgetConfig w = new WidgetConfig(id, type, template, 0.5f, 0.7f);
		cfg.widgets.add(w);
		selectedId = id;
		FlexhudConfig.save();
		rebuildWidgets();
	}

	private void deleteSelected() {
		WidgetConfig w = selected();
		if (w == null) {
			return;
		}
		FlexhudConfig.get().widgets.remove(w);
		selectedId = FlexhudConfig.get().widgets.isEmpty() ? null : FlexhudConfig.get().widgets.get(0).id;
		FlexhudConfig.save();
		rebuildWidgets();
	}

	private void toggleSelected() {
		WidgetConfig w = selected();
		if (w == null) {
			return;
		}
		w.enabled = !w.enabled;
		FlexhudConfig.save();
		rebuildWidgets();
	}

	private void scaleSelected(float factor) {
		WidgetConfig w = selected();
		if (w == null) {
			return;
		}
		w.style.scale = Math.max(0.5f, Math.min(3.0f, w.style.scale * factor));
		FlexhudConfig.save();
	}

	private void nudge(float dx, float dy) {
		WidgetConfig w = selected();
		if (w == null) {
			return;
		}
		w.x = Math.max(0f, Math.min(1f, w.x + dx));
		w.y = Math.max(0f, Math.min(1f, w.y + dy));
		FlexhudConfig.save();
	}

	@Override
	public void onClose() {
		FlexhudConfig.save();
		switchTo(parent);
	}

	private void switchTo(Screen s) {
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			client.gui.setScreen(s);
		}
	}
}
