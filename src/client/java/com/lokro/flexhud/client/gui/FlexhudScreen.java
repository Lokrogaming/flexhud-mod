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

/** Hauptmenü (Hotkey H, /flexhud, ModMenu-Tab). Responsiv, Zeilen scrollen bei wenig Platz. */
public class FlexhudScreen extends Screen {
	private final Screen parent;
	private int scrollOffset = 0;

	private static final int ROW_H = 24;
	private static final int ROWS = 8;

	public FlexhudScreen(Screen parent) {
		super(Component.literal("FlexHUD"));
		this.parent = parent;
	}

	private int contentW() {
		return Math.min(320, this.width - 20);
	}

	private int colX() {
		return (this.width - contentW()) / 2;
	}

	private int contentTop() {
		return 62;
	}

	private int footerTop() {
		return this.height - 30;
	}

	private int maxScroll() {
		return Math.max(0, ROWS * ROW_H - (footerTop() - contentTop()));
	}

	private int rowY(int row) {
		return contentTop() + row * ROW_H - scrollOffset;
	}

	private boolean visible(int y) {
		return y + 20 >= contentTop() && y <= footerTop() - 4;
	}

	@Override
	protected void init() {
		FlexhudConfig cfg = FlexhudConfig.get();
		scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll()));
		int x = colX();
		int cw = contentW();
		int half = (cw - 4) / 2;

		if (visible(rowY(0))) {
			addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.editor")),
				b -> switchTo(new WidgetEditorScreen(this))).bounds(x, rowY(0), cw, 20).build());
		}
		if (visible(rowY(1))) {
			addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.market")),
				b -> switchTo(new MarketplaceScreen(this))).bounds(x, rowY(1), cw, 20).build());
		}
		if (visible(rowY(2))) {
			addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.packs")),
				b -> switchTo(new PackOverviewScreen(this))).bounds(x, rowY(2), half, 20).build());
			addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.updates")),
				b -> switchTo(new UpdateScreen(this))).bounds(x + half + 4, rowY(2), cw - half - 4, 20).build());
		}
		if (visible(rowY(3))) {
			addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.style_first")),
				b -> {
					if (!cfg.widgets.isEmpty()) {
						switchTo(new StyleEditScreen(this, cfg.widgets.get(0).id));
					}
				}).bounds(x, rowY(3), cw, 20).build());
		}
		if (visible(rowY(4))) {
			String timerLabel = TimerState.isRunning()
				? Lang.f("menu.timer_pause", TimerState.format(TimerState.getElapsedMs(), TimerState.shouldShowMillis()))
				: Lang.f("menu.timer_start", TimerState.format(TimerState.getElapsedMs(), TimerState.shouldShowMillis()));
			addRenderableWidget(Button.builder(Component.literal(timerLabel),
				b -> {
					TimerState.toggle();
					rebuildWidgets();
				}).bounds(x, rowY(4), half, 20).build());
			addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.timer_reset")),
				b -> {
					TimerState.reset();
					rebuildWidgets();
				}).bounds(x + half + 4, rowY(4), cw - half - 4, 20).build());
		}
		if (visible(rowY(5))) {
			addRenderableWidget(Button.builder(Component.literal(Lang.f("menu.timer_ms",
					Lang.t(TimerState.shouldShowMillis() ? "on" : "off"))),
				b -> {
					TimerState.toggleShowMillis();
					rebuildWidgets();
				}).bounds(x, rowY(5), cw, 20).build());
		}
		if (visible(rowY(6))) {
			addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.step_toggle")),
				b -> {
					if (!StepcountBridge.installed()) {
						send(Lang.t("menu.step_nomod"));
						return;
					}
					StepcountBridge.command("start");
					String r = StepcountBridge.command("show");
					send(Lang.f("menu.step_hint", r));
				}).bounds(x, rowY(6), cw, 20).build());
		}
		if (visible(rowY(7))) {
			addRenderableWidget(Button.builder(Component.literal(Lang.t("menu.step_reset")),
				b -> send("[FlexHUD→StepCount] " + StepcountBridge.command("reset")))
				.bounds(x, rowY(7), cw, 20).build());
		}

		addRenderableWidget(Button.builder(Component.literal(Lang.t("done")),
			b -> onClose()).bounds(x + (cw - 200) / 2, this.height - 26, 200, 20).build());
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		int max = maxScroll();
		if (max > 0) {
			double d = vertical != 0 ? vertical : horizontal;
			int next = (int) Math.max(0, Math.min(max, scrollOffset - d * 14));
			if (next != scrollOffset) {
				scrollOffset = next;
				rebuildWidgets();
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, 0xA0000000);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, Lang.t("menu.head") + "  v" + modVersion(), this.width / 2, 12, 0xFFFFFF);
		String step = StepcountBridge.installed()
			? Lang.f("menu.step_found", StepcountBridge.getSteps())
			: Lang.t("menu.step_missing");
		graphics.centeredText(this.font, step, this.width / 2, 24, 0xAAAAAA);
		String mp = Lang.f("menu.source", FlexhudConfig.get().marketplaceUrl);
		int max = Math.max(30, (this.width - 40) / 6);
		if (mp.length() > max) {
			mp = mp.substring(0, max) + "…";
		}
		graphics.centeredText(this.font, mp, this.width / 2, 36, 0x666666);
		if (maxScroll() > 0) {
			graphics.centeredText(this.font, Lang.t("style.scroll_hint"), this.width / 2, footerTop() - 12, 0x888888);
		}
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

	private static String modVersion() {
		try {
			return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("flexhud")
				.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?");
		} catch (Exception e) {
			return "?";
		}
	}
}
