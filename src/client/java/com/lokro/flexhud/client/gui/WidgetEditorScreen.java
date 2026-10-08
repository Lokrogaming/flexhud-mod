package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.config.WidgetConfig;
import com.lokro.flexhud.client.config.WidgetType;
import com.lokro.flexhud.client.hud.WidgetHud;
import com.lokro.flexhud.client.i18n.Lang;
import com.lokro.flexhud.client.util.GradientUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;

/** Widget-Editor: Liste + Aktionen + Live-Vorschau (Klick wählt, Ziehen bewegt). */
public class WidgetEditorScreen extends Screen {
	private final Screen parent;
	private String selectedId = null;
	private boolean dragging = false;
	private int listPage = 0;
	private String pendingDelete = null;
	private boolean fullscreen = false;
	private int addScroll = 0;

	// Vorschau-Rechteck (wird in init()/extractRenderState identisch berechnet)
	private int prevX, prevY, prevW, prevH;

	public WidgetEditorScreen(Screen parent) {
		super(Component.literal(Lang.t("editor.title")));
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
		if (fullscreen) {
			prevX = 0;
			prevY = 0;
			prevW = this.width;
			prevH = this.height - footerH() - 6;
			return;
		}
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

		if (!fullscreen) {
		// Linke Liste (mit Paging bei Überlauf)
		int lw = listW();
		int ps = pageSize();
		int pages = Math.max(1, (cfg.widgets.size() + ps - 1) / ps);
		listPage = Math.max(0, Math.min(listPage, pages - 1));
		int y = contentTop();
		for (int i = listPage * ps; i < Math.min(cfg.widgets.size(), (listPage + 1) * ps); i++) {
			WidgetConfig w = cfg.widgets.get(i);
			String label = (w.id.equals(selectedId) ? "> " : "")
				+ w.id + " [" + w.type.displayName() + "] " + Lang.t(w.enabled ? "on" : "off");
			if (label.length() > 32) {
				label = label.substring(0, 32) + "…";
			}
			String wid = w.id;
			addRenderableWidget(Button.builder(Component.literal(label),
				b -> {
					selectedId = wid;
					pendingDelete = null;
					rebuildWidgets();
				}).bounds(10, y, lw, 20).build());
			y += 22;
		}
		if (pages > 1) {
			String pg = Lang.f("editor.page", (listPage + 1), pages);
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

		// Typ-Grid oben: alle Widget-Typen einzeln (scrollbar). Darunter Fix-Buttons.
		int px = panelX();
		int pw = panelW();
		int by = contentTop();
		WidgetType[] allTypes = WidgetType.values();
		int cols = 2;
		int gw = (pw - 4) / cols;
		int gridTop = by + 12;
		int mgmtH = 6 * 22;
		int gridBottom = prevY - 4 - mgmtH - 4;
		int typeRows = (allTypes.length + cols - 1) / cols;
		int maxAdd = Math.max(0, typeRows * 20 - (gridBottom - gridTop));
		addScroll = Math.max(0, Math.min(addScroll, maxAdd));
		for (int i = 0; i < allTypes.length; i++) {
			int gy = gridTop + (i / cols) * 20 - addScroll;
			if (gy + 20 < gridTop || gy > gridBottom - 20) {
				continue;
			}
			WidgetType ref = allTypes[i];
			addRenderableWidget(Button.builder(Component.literal("+ " + ref.displayName()),
				b -> createAndSelect(ref)).bounds(px + (i % cols) * (gw + 4), gy, gw, 20).build());
		}
		by = gridBottom + 4;
		by = addFull(px, by, pw, Lang.f("editor.fullscreen", Lang.t(fullscreen ? "off" : "on")), () -> {
			fullscreen = !fullscreen;
			rebuildWidgets();
		});
		by = addFull(px, by, pw, Lang.t("editor.toggle"), this::toggleSelected);
		by = addFull(px, by, pw, Lang.t("editor.style"), () -> {
			if (selectedId != null) {
				switchTo(new StyleEditScreen(this, selectedId));
			}
		});
		by = addFull(px, by, pw, Lang.t("editor.delete"), this::deleteSelected);
		// Scale nebeneinander
		if (by + 20 <= prevY - 4) {
			addRenderableWidget(Button.builder(Component.literal(Lang.t("editor.scale_down")),
				b -> scaleSelected(0.9f)).bounds(px, by, pw / 2 - 1, 20).build());
			addRenderableWidget(Button.builder(Component.literal(Lang.t("editor.scale_up")),
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
		}

		if (fullscreen) {
			addRenderableWidget(Button.builder(Component.literal(Lang.f("editor.fullscreen", Lang.t("off"))),
				b -> {
					fullscreen = false;
					rebuildWidgets();
				}).bounds(this.width - 140, 10, 130, 20).build());
		}

		addRenderableWidget(Button.builder(Component.literal(Lang.t("back")),
			b -> onClose()).bounds(10, this.height - footerH() + 2, 100, 20).build());

		// Aktions-Leiste: erscheint bei Auswahl, fix in der Footer-Zeile.
		WidgetConfig sel = selected();
		if (sel != null) {
			int ax = 120;
			int abw = Math.max(50, (this.width - ax - 10 - 2 * 4) / 3);
			int ay = this.height - footerH() + 2;
			String toggleLabel = Lang.t(sel.enabled ? "editor.disable" : "editor.enable");
			addRenderableWidget(Button.builder(Component.literal(toggleLabel),
				b -> toggleSelected()).bounds(ax, ay, abw, 20).build());
			addRenderableWidget(Button.builder(Component.literal(Lang.t("editor.style")),
				b -> {
					if (selectedId != null) {
						switchTo(new StyleEditScreen(this, selectedId));
					}
				}).bounds(ax + abw + 4, ay, abw, 20).build());
			boolean confirm = sel.id.equals(pendingDelete);
			addRenderableWidget(Button.builder(Component.literal(confirm ? Lang.t("editor.confirm_delete") : Lang.t("editor.delete")),
				b -> {
					WidgetConfig s = selected();
					if (s == null) {
						return;
					}
					if (s.id.equals(pendingDelete)) {
						pendingDelete = null;
						deleteSelected();
					} else {
						pendingDelete = s.id;
						rebuildWidgets();
					}
				}).bounds(ax + (abw + 4) * 2, ay, abw, 20).build());
		}
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

		if (fullscreen) {
			graphics.centeredText(this.font, Lang.t("editor.full_hint"), this.width / 2, 10, 0xAAAAAA);
		} else {
			graphics.centeredText(this.font, Lang.t("editor.title"), this.width / 2, 8, 0xFFFFFF);
			graphics.centeredText(this.font, Lang.t("editor.hint"),
				this.width / 2, 20, 0xAAAAAA);
		}

			WidgetConfig sel = selected();
		int infoX = listW() + 20;
		int infoW = panelX() - infoX - 10;
		if (sel != null && infoW > 60 && !fullscreen) {
			int iy = contentTop() + 2;
			graphics.text(this.font, Lang.f("editor.sel", sel.id), infoX, iy, 0x55FFFF);
			graphics.text(this.font, Lang.f("editor.type", sel.type.displayName(), Lang.t(sel.enabled ? "on" : "off")), infoX, iy + 12, 0xCCCCCC);
			graphics.text(this.font, Lang.f("editor.pos",
				Math.round(sel.x * 100), Math.round(sel.y * 100), String.format(java.util.Locale.ROOT, "%.1f", sel.style.scale)),
				infoX, iy + 24, 0xCCCCCC);
			graphics.text(this.font, Lang.f("editor.styleline", sel.style.animation.displayName(),
				sel.style.cycleMs, sel.style.bold ? Lang.t("editor.boldflag") : "",
				sel.style.hideText ? Lang.t("editor.notextflag") : ""), infoX, iy + 36, 0xCCCCCC);
			String tpl = sel.template.length() > 30 ? sel.template.substring(0, 30) + "…" : sel.template;
			graphics.text(this.font, Lang.f("editor.template", tpl), infoX, iy + 48, 0x888888);
		}

		if (!fullscreen) {
			graphics.fill(prevX, prevY, prevX + prevW, prevY + prevH, 0x60000000);
			graphics.outline(prevX, prevY, prevX + prevW, prevY + prevH, 0xFF55FFFF);
			graphics.text(this.font, Lang.t("editor.preview"),
				prevX + 6, prevY + 4, 0xAAAAAA);
			graphics.text(this.font, Lang.t("editor.add_title"), panelX(), contentTop(), 0xAAAAAA);
		}

		long now = Util.getMillis();
		for (WidgetConfig w : FlexhudConfig.get().widgets) {
			int cx = fullscreen ? Math.round(w.x * this.width) : prevX + Math.round(w.x * prevW);
			int cy = fullscreen ? Math.round(w.y * this.height)
				: prevY + 16 + Math.round(w.y * (prevH - 30));
			boolean isSel = w.id.equals(selectedId);
			String plain = WidgetHud.previewText(w);
			if (!w.enabled) {
				plain = Lang.t("editor.disabled_prefix") + plain;
			}
			MutableComponent text = GradientUtil.style(plain, w.style, now);
			int textWidth = client.font.width(text.getVisualOrderText());
			float scale = fullscreen ? w.style.scale
				: Math.max(0.5f, Math.min(1.0f, prevW / 500f));

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
			if (nearest != null && (!fullscreen
				|| Math.hypot(event.x() - widgetPx(nearest), event.y() - widgetPy(nearest)) < 24)) {
				if (!nearest.id.equals(selectedId)) {
					pendingDelete = null;
				}
				selectedId = nearest.id;
				dragging = true;
				setDragging(true);
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
			setDragging(false);
			FlexhudConfig.save();
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (!fullscreen) {
			computePreview();
			int mgmtH = 6 * 22;
			int gb = prevY - 4 - mgmtH - 4;
			int gt = contentTop() + 12;
			if (mouseX >= panelX() && mouseX <= panelX() + panelW() && mouseY >= gt && mouseY <= gb) {
				WidgetType[] all = WidgetType.values();
				int rows = (all.length + 1) / 2;
				int max = Math.max(0, rows * 20 - (gb - gt));
				if (max > 0) {
					double d = vertical != 0 ? vertical : horizontal;
					int next = (int) Math.max(0, Math.min(max, addScroll - d * 14));
					if (next != addScroll) {
						addScroll = next;
						rebuildWidgets();
						return true;
					}
				}
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}

	private boolean inside(double mx, double my) {
		return mx >= prevX && mx <= prevX + prevW && my >= prevY && my <= prevY + prevH;
	}

	private int widgetPx(WidgetConfig w) {
		return fullscreen ? Math.round(w.x * this.width) : prevX + Math.round(w.x * prevW);
	}

	private int widgetPy(WidgetConfig w) {
		return fullscreen ? Math.round(w.y * this.height) : prevY + 16 + Math.round(w.y * (prevH - 30));
	}

	private WidgetConfig nearestWidget(double mx, double my) {
		WidgetConfig best = null;
		double bestDist = Double.MAX_VALUE;
		for (WidgetConfig w : FlexhudConfig.get().widgets) {
			double d = Math.hypot(mx - widgetPx(w), my - widgetPy(w));
			if (d < bestDist) {
				bestDist = d;
				best = w;
			}
		}
		return best;
	}

	/** Pfeiltasten bewegen das gewählte Widget ( Shift = fein ). */
	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		WidgetConfig w = selected();
		if (w != null && !FlexhudConfig.get().widgets.isEmpty()) {
			float step = (event.modifiers() & 0x1) != 0 ? 0.002f : 0.01f;
			int k = event.key();
			int kc = event.keycode();
			if (k == InputConstants.KEY_LEFT || kc == InputConstants.KEY_LEFT) {
				nudge(-step, 0);
				return true;
			}
			if (k == InputConstants.KEY_RIGHT || kc == InputConstants.KEY_RIGHT) {
				nudge(step, 0);
				return true;
			}
			if (k == InputConstants.KEY_UP || kc == InputConstants.KEY_UP) {
				nudge(0, -step);
				return true;
			}
			if (k == InputConstants.KEY_DOWN || kc == InputConstants.KEY_DOWN) {
				nudge(0, step);
				return true;
			}
		}
		return super.keyPressed(event);
	}

	private void moveTo(WidgetConfig w, double mx, double my) {
		float nx;
		float ny;
		if (fullscreen) {
			nx = (float) (mx / (double) this.width);
			ny = (float) (my / (double) this.height);
		} else {
			nx = (float) ((mx - prevX) / (double) prevW);
			ny = (float) ((my - (prevY + 16)) / (double) (prevH - 30));
		}
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

	void createAndSelect(WidgetType type) {
		FlexhudConfig cfg = FlexhudConfig.get();
		String id = cfg.freeId(type);
		WidgetConfig w = new WidgetConfig(id, type, type.defaultTemplate(), 0.5f, 0.7f);
		cfg.widgets.add(w);
		selectedId = id;
		pendingDelete = null;
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
		pendingDelete = null;
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
