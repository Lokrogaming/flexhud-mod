package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.config.WidgetConfig;
import com.lokro.flexhud.client.config.WidgetType;
import com.lokro.flexhud.client.hud.WidgetHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Widget-Editor: Widgets adden/löschen/an-aus, per Drag in der Vorschau
 * frei positionieren, Skalierung ändern, Stil-Editor öffnen.
 *
 * <p>Bedienung: Widget in der Liste anklicken (auswählen), dann in der oberen
 * Vorschau-Fläche ziehen. Alternativ: Pfeil-Buttons für feine Schritte.
 */
public class WidgetEditorScreen extends Screen {
	private final Screen parent;
	private String selectedId = null;
	private boolean dragging = false;

	public WidgetEditorScreen(Screen parent) {
		super(Component.literal("FlexHUD – Widget-Editor"));
		this.parent = parent;
		FlexhudConfig cfg = FlexhudConfig.get();
		if (!cfg.widgets.isEmpty()) {
			selectedId = cfg.widgets.get(0).id;
		}
	}

	@Override
	protected void init() {
		FlexhudConfig cfg = FlexhudConfig.get();
		int listX = 10;
		int y = 60;
		for (WidgetConfig w : cfg.widgets) {
			String label = (w.id.equals(selectedId) ? "> " : "")
				+ w.id + " [" + w.type.displayName() + "] " + (w.enabled ? "AN" : "AUS");
			String wid = w.id;
			addRenderableWidget(Button.builder(Component.literal(label),
				b -> {
					selectedId = wid;
					rebuildWidgets();
				}).bounds(listX, y, 220, 20).build());
			y += 22;
			if (y > this.height - 160) {
				break;
			}
		}

		int cx = this.width / 2 + 60;
		int by = 60;
		addRenderableWidget(Button.builder(Component.literal("+ Timer"),
			b -> add(WidgetType.TIMER, "Timer: {value}")).bounds(cx, by, 110, 20).build());
		addRenderableWidget(Button.builder(Component.literal("+ StepCount"),
			b -> add(WidgetType.STEPCOUNT, "Steps: {value}")).bounds(cx + 115, by, 110, 20).build());
		by += 22;
		addRenderableWidget(Button.builder(Component.literal("+ Uhr"),
			b -> add(WidgetType.CLOCK, "{value}")).bounds(cx, by, 110, 20).build());
		addRenderableWidget(Button.builder(Component.literal("+ Text"),
			b -> add(WidgetType.TEXT, "FlexHUD")).bounds(cx + 115, by, 110, 20).build());
		by += 22;
		addRenderableWidget(Button.builder(Component.literal("+ FPS"),
			b -> add(WidgetType.FPS, "FPS: {value}")).bounds(cx, by, 110, 20).build());
		addRenderableWidget(Button.builder(Component.literal("An/Aus"),
			b -> toggleSelected()).bounds(cx + 115, by, 110, 20).build());
		by += 22;
		addRenderableWidget(Button.builder(Component.literal("Stil…"),
			b -> {
				if (selectedId != null) {
					switchTo(new StyleEditScreen(this, selectedId));
				}
			}).bounds(cx, by, 110, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Löschen"),
			b -> deleteSelected()).bounds(cx + 115, by, 110, 20).build());
		by += 22;
		addRenderableWidget(Button.builder(Component.literal("Scale -"),
			b -> scaleSelected(0.9f)).bounds(cx, by, 110, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Scale +"),
			b -> scaleSelected(1.1f)).bounds(cx + 115, by, 110, 20).build());
		by += 22;
		addRenderableWidget(Button.builder(Component.literal("◀"), b -> nudge(-0.01f, 0)).bounds(cx, by, 52, 20).build());
		addRenderableWidget(Button.builder(Component.literal("▶"), b -> nudge(0.01f, 0)).bounds(cx + 57, by, 52, 20).build());
		addRenderableWidget(Button.builder(Component.literal("▲"), b -> nudge(0, -0.01f)).bounds(cx + 114, by, 52, 20).build());
		addRenderableWidget(Button.builder(Component.literal("▼"), b -> nudge(0, 0.01f)).bounds(cx + 171, by, 52, 20).build());

		addRenderableWidget(Button.builder(Component.literal("Zurück"),
			b -> onClose()).bounds(10, this.height - 30, 100, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.text(this.font, "Widget-Editor: wählen, dann oben in der Vorschau ZIEHEN",
			10, 18, 0xFFFFFF);
		// Vorschau-Fläche oben rechts: simuliert Widget-Positionen im Kleinformat
		int px = this.width / 2 + 40;
		int py = 150;
		int pw = this.width - px - 10;
		int ph = 110;
		graphics.fill(px, py, px + pw, py + ph, 0x60000000);
		graphics.text(this.font, "Vorschau (ziehen!)", px + 4, py + 4, 0xAAAAAA);
		Minecraft client = Minecraft.getInstance();
		for (WidgetConfig w : FlexhudConfig.get().widgets) {
			int wx = px + Math.round(w.x * (pw - 8));
			int wy = py + 16 + Math.round(w.y * (ph - 30));
			boolean sel = w.id.equals(selectedId);
			String label = (w.enabled ? "" : "[aus] ") + w.id + ": " + WidgetHud.previewText(w);
			if (label.length() > 40) {
				label = label.substring(0, 40) + "…";
			}
			graphics.text(this.font, label, Math.min(wx, px + pw - 150), wy, sel ? 0x55FFFF : 0xFFFFFF);
		}
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
		int px = this.width / 2 + 40;
		int py = 150;
		int pw = this.width - px - 10;
		int ph = 110;
		if (event.button() == 0 && event.x() >= px && event.x() <= px + pw && event.y() >= py && event.y() <= py + ph) {
			WidgetConfig w = selected();
			if (w != null) {
				dragging = true;
				moveTo(w, event.x(), event.y(), px, py, pw, ph);
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {
		if (dragging) {
			int px = this.width / 2 + 40;
			int py = 150;
			int pw = this.width - px - 10;
			int ph = 110;
			WidgetConfig w = selected();
			if (w != null) {
				moveTo(w, event.x(), event.y(), px, py, pw, ph);
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

	private void moveTo(WidgetConfig w, double mx, double my, int px, int py, int pw, int ph) {
		float nx = (float) ((mx - px) / (pw - 8));
		float ny = (float) ((my - py - 16) / (ph - 30));
		w.x = Math.max(0f, Math.min(1f, nx));
		w.y = Math.max(0f, Math.min(1f, ny));
	}

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
