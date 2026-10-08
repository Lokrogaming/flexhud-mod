package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.bridge.StepcountBridge;
import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.state.TimerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Hauptmenü der Mod. Erreichbar über Hotkey (H), {@code /flexhud} oder den
 * ModMenu-Tab (falls ModMenu installiert ist).
 */
public class FlexhudScreen extends Screen {
	private final Screen parent;

	public FlexhudScreen(Screen parent) {
		super(Component.literal("FlexHUD"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		FlexhudConfig cfg = FlexhudConfig.get();
		int cx = this.width / 2;
		int y = 60;

		addRenderableWidget(Button.builder(Component.literal("Widgets anordnen (Editor)"),
			b -> switchTo(new WidgetEditorScreen(this))).bounds(cx - 150, y, 300, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("Marketplace (Packs laden)"),
			b -> switchTo(new MarketplaceScreen(this))).bounds(cx - 150, y, 300, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("Stil des ersten Widgets bearbeiten"),
			b -> {
				if (!cfg.widgets.isEmpty()) {
					switchTo(new StyleEditScreen(this, cfg.widgets.get(0).id));
				}
			}).bounds(cx - 150, y, 300, 20).build());
		y += 32;

		String timerLabel = TimerState.isRunning()
			? "Timer: PAUSE (" + TimerState.format(TimerState.getElapsedMs(), true) + ")"
			: "Timer: START (" + TimerState.format(TimerState.getElapsedMs(), true) + ")";
		addRenderableWidget(Button.builder(Component.literal(timerLabel),
			b -> {
				TimerState.toggle();
				rebuildWidgets();
			}).bounds(cx - 150, y, 148, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Timer: RESET"),
			b -> {
				TimerState.reset();
				rebuildWidgets();
			}).bounds(cx + 2, y, 148, 20).build());
		y += 24;

		addRenderableWidget(Button.builder(Component.literal("StepCount: Start/Show umschalten"),
			b -> {
				if (!StepcountBridge.installed()) {
					send("StepCount-Mod nicht gefunden (als .jar in mods/ installieren).");
					return;
				}
				StepcountBridge.command("start");
				String r = StepcountBridge.command("show");
				send("[FlexHUD→StepCount] " + r + " Nutze das FlexHUD-Widget für freie Position!");
			}).bounds(cx - 150, y, 300, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("StepCount: Reset"),
			b -> send("[FlexHUD→StepCount] " + StepcountBridge.command("reset")))
			.bounds(cx - 150, y, 300, 20).build());
		y += 32;

		addRenderableWidget(Button.builder(Component.literal("Fertig"),
			b -> onClose()).bounds(cx - 100, this.height - 30, 200, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.text(this.font, "FlexHUD – freie Widgets, Timer, StepCount-Bridge, Marketplace",
			this.width / 2 - 170, 20, 0xFFFFFF);
		String step = StepcountBridge.installed()
			? "StepCount: erkannt (" + StepcountBridge.getSteps() + " Steps)"
			: "StepCount: NICHT installiert (Bridge inaktiv)";
		graphics.text(this.font, step, this.width / 2 - 170, 32, 0xAAAAAA);
		String mp = "Marketplace: " + FlexhudConfig.get().marketplaceUrl;
		String shortMp = mp.length() > 64 ? mp.substring(0, 64) + "…" : mp;
		graphics.text(this.font, shortMp, this.width / 2 - 170, 44, 0x666666);
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.gui.setScreen(parent);
		}
	}

	private void switchTo(Screen s) {
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			client.gui.setScreen(s);
		}
	}

	private void send(String msg) {
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.player != null) {
			client.player.sendSystemMessage(Component.literal("§b[FlexHUD] §f" + msg));
		}
	}
}
