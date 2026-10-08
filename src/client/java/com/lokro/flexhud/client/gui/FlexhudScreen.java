package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.bridge.StepcountBridge;
import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.state.TimerState;
import com.lokro.flexhud.client.i18n.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Hauptmenü (Hotkey H, /flexhud, ModMenu-Tab). */
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

		addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.editor")),
			b -> switchTo(new WidgetEditorScreen(this))).bounds(cx - 150, y, 300, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.market")),
			b -> switchTo(new MarketplaceScreen(this))).bounds(cx - 150, y, 300, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.packs")),
			b -> switchTo(new PackOverviewScreen(this))).bounds(cx - 150, y, 148, 20).build());
		addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.updates")),
			b -> switchTo(new UpdateScreen(this))).bounds(cx + 2, y, 148, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.style_first")),
			b -> {
				if (!cfg.widgets.isEmpty()) {
					switchTo(new StyleEditScreen(this, cfg.widgets.get(0).id));
				}
			}).bounds(cx - 150, y, 300, 20).build());
		y += 32;

		String timerLabel = TimerState.isRunning()
			? Lang.f("menu.timer_pause", TimerState.format(TimerState.getElapsedMs(), TimerState.shouldShowMillis()))
			: Lang.f("menu.timer_start", TimerState.format(TimerState.getElapsedMs(), TimerState.shouldShowMillis()));
		addRenderableWidget(Button.builder(Component.literal(timerLabel),
			b -> {
				TimerState.toggle();
				rebuildWidgets();
			}).bounds(cx - 150, y, 148, 20).build());
		addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.timer_reset")),
			b -> {
				TimerState.reset();
				rebuildWidgets();
			}).bounds(cx + 2, y, 148, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal(Lang.f("menu.timer_ms",
				Lang.t(TimerState.shouldShowMillis() ? "on" : "off"))),
			b -> {
				TimerState.toggleShowMillis();
				rebuildWidgets();
			}).bounds(cx - 150, y, 300, 20).build());
		y += 24;

		addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.step_toggle")),
			b -> {
				if (!StepcountBridge.installed()) {
					send(Lang.t("menu.step_nomod"));
					return;
				}
				StepcountBridge.command("start");
				String r = StepcountBridge.command("show");
				send(Lang.f("menu.step_hint", r));
			}).bounds(cx - 150, y, 300, 20).build());
		y += 24;
		addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.step_reset")),
			b -> send("[FlexHUD→StepCount] " + StepcountBridge.command("reset")))
			.bounds(cx - 150, y, 300, 20).build());
		y += 32;

		addRenderableWidget(Button.builder(Component.literal(Lang.t("done")),
			b -> onClose()).bounds(cx - 100, this.height - 30, 200, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.text(this.font, Lang.t("menu.head"),
			this.width / 2 - 170, 20, 0xFFFFFF);
		String step = StepcountBridge.installed()
			? Lang.f("menu.step_found", StepcountBridge.getSteps())
			: Lang.t("menu.step_missing");
		graphics.text(this.font, step, this.width / 2 - 170, 32, 0xAAAAAA);
		String mp = Lang.f("menu.source", FlexhudConfig.get().marketplaceUrl);
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
