package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.market.ConflictChecker;
import com.lokro.flexhud.client.market.FlexConfig;
import com.lokro.flexhud.client.i18n.Lang;
import com.lokro.flexhud.client.market.MarketplaceCache;
import com.lokro.flexhud.client.market.MarketplaceEntry;
import com.lokro.flexhud.client.market.PackInstaller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** In-Game-Marketplace: Packliste, Konfliktcheck, Install/Deinstall. */
public class MarketplaceScreen extends Screen {
	private final Screen parent;
	private int page = 0;
	private String status = Lang.t("market.loading");
	private String pendingUninstall = null;
	private static final int PER_PAGE = 4;

	public MarketplaceScreen(Screen parent) {
		super(Component.literal(Lang.t("market.title")));
		this.parent = parent;
		MarketplaceCache.refreshAsyncIfStale();
	}

	/** Breite, die der Text pro Eintrag maximal nutzen darf (Buttons rechts abziehen). */
	private int textMaxChars(int textX) {
		int installX = installBtnX();
		return Math.max(24, (installX - textX - 6) / 6);
	}

	private int installBtnX() {
		return this.width - 10 - 105 - (hasUninstallOnPage() ? 5 + 130 : 0);
	}

	private boolean hasUninstallOnPage() {
		List<MarketplaceEntry> all = MarketplaceCache.entries();
		int start = page * PER_PAGE;
		for (int i = 0; i < PER_PAGE; i++) {
			int idx = start + i;
			if (idx >= all.size()) {
				break;
			}
			if (PackInstaller.isInstalled(all.get(idx))) {
				return true;
			}
		}
		return false;
	}

	private String cut(String s, int max) {
		if (s == null) {
			return "";
		}
		return s.length() > max ? s.substring(0, max) + "…" : s;
	}

	@Override
	protected void init() {
		List<MarketplaceEntry> all = MarketplaceCache.entries();
		int cx = this.width / 2;
		int y = 56;

		int start = page * PER_PAGE;
		for (int i = 0; i < PER_PAGE; i++) {
			int idx = start + i;
			if (idx >= all.size()) {
				break;
			}
			MarketplaceEntry e = all.get(idx);
			boolean installed = PackInstaller.isInstalled(e);
			String btnLabel = Lang.t(installed ? "market.reinstall" : "market.install");
			MarketplaceEntry ref = e;
			int bx = installBtnX();
			addRenderableWidget(Button.builder(Component.literal(btnLabel),
				b -> {
					pendingUninstall = null;
					status = Lang.f("market.loading_name", ref.name);
					PackInstaller.installAsync(ref, msg -> {
						status = msg;
						Minecraft client = Minecraft.getInstance();
						if (client != null) {
							client.execute(this::rebuildWidgets);
						}
					});
				}).bounds(bx, y, 105, 20).build());
			if (installed) {
				int widgets = PackInstaller.importedWidgetCount(e);
				boolean confirm = ref.id.equals(pendingUninstall);
				String unLabel = Lang.t(confirm ? "market.confirm_uninstall" : "market.uninstall");
				addRenderableWidget(Button.builder(Component.literal(unLabel),
					b -> {
						if (ref.id.equals(pendingUninstall)) {
							pendingUninstall = null;
							status = Lang.f("market.removing_name", ref.name);
							PackInstaller.uninstallAsync(ref, msg -> {
								status = msg;
								Minecraft client = Minecraft.getInstance();
								if (client != null) {
									client.execute(this::rebuildWidgets);
								}
							});
						} else {
							pendingUninstall = ref.id;
							status = Lang.f("market.confirm_msg", widgets > 0
								? Lang.f("market.confirm_widgets", widgets) : ".");
							rebuildWidgets();
						}
					}).bounds(bx + 110, y, 130, 20).build());
			}
			y += 44;
		}

		// Footer: 4 Buttons, Breite anteilig (kein Überlappen).
		int gap = 5;
		int bw = (this.width - 20 - gap * 3) / 4;
		if (bw < 40) {
			bw = 40;
		}
		int fy = this.height - 26;
		addRenderableWidget(Button.builder(Component.literal(Lang.t("market.prev")),
			b -> {
				if (page > 0) {
					page--;
					pendingUninstall = null;
					rebuildWidgets();
				}
			}).bounds(10, fy, bw, 20).build());
		addRenderableWidget(Button.builder(Component.literal(Lang.t("market.refresh")),
			b -> {
				pendingUninstall = null;
				MarketplaceCache.refreshAsync();
				status = Lang.t("market.refreshing");
			}).bounds(10 + (bw + gap), fy, bw, 20).build());
		addRenderableWidget(Button.builder(Component.literal(Lang.t("market.next")),
			b -> {
				if ((page + 1) * PER_PAGE < MarketplaceCache.entries().size()) {
					page++;
					pendingUninstall = null;
					rebuildWidgets();
				}
			}).bounds(10 + (bw + gap) * 2, fy, bw, 20).build());
		addRenderableWidget(Button.builder(Component.literal(Lang.t("done")),
			b -> onClose()).bounds(10 + (bw + gap) * 3, fy, bw, 20).build());

		if (MarketplaceCache.lastError() != null && all.isEmpty()) {
			status = Lang.f("market.offline", MarketplaceCache.lastError());
		} else if (!all.isEmpty() && status.startsWith("Lade")) {
			status = Lang.f("market.available", all.size());
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		List<MarketplaceEntry> all = MarketplaceCache.entries();
		int textX = Math.max(10, Math.min(this.width / 2 - 220, installBtnX() - 200));
		int max = textMaxChars(textX);
		graphics.text(this.font, Lang.f("market.head", all.size(), (page + 1)),
			textX, 20, 0xFFFFFF);
		graphics.text(this.font, cut(Lang.f("market.source", FlexhudConfig.get().marketplaceUrl), max),
			textX, 32, 0x666666);

		int y = 58;
		int start = page * PER_PAGE;
		for (int i = 0; i < PER_PAGE; i++) {
			int idx = start + i;
			if (idx >= all.size()) {
				break;
			}
			MarketplaceEntry e = all.get(idx);
			ConflictChecker.Conflict c = ConflictChecker.check(e);
			String warn = ConflictChecker.warnText(e, c);
			String packWarn = ConflictChecker.packConflictText(e);
			if (packWarn != null) {
				warn = (warn == null ? "" : warn + " ") + packWarn;
			}
			graphics.text(this.font, cut(e.name + " v" + e.version + " [" + e.packType + "]", max),
				textX, y, 0x55FFFF);
			graphics.text(this.font, cut(e.description, max), textX, y + 11, 0xCCCCCC);
			String meta = Lang.f("market.by", e.author, e.modId)
				+ (PackInstaller.isInstalled(e) ? Lang.t("market.installed") : "");
			graphics.text(this.font, cut(meta, max), textX, y + 22, 0x888888);
			int step = 44;
			if (PackInstaller.isInstalled(e)) {
				FlexConfig cfg = FlexConfig.load(e.id);
				if (cfg != null && !cfg.author.isBlank()) {
					String f = Lang.f("market.flexline", cfg.version, cfg.author);
					if (!cfg.website.isBlank()) {
						f += Lang.f("market.meta_site", cfg.website);
					}
					if (!cfg.updated.isBlank()) {
						f += Lang.f("market.meta_updated", cfg.updated);
					}
					graphics.text(this.font, cut(f, max), textX, y + 33, 0x99CCFF);
					step = 56;
				}
			}
			if (warn != null) {
				graphics.text(this.font, cut("⚠ " + warn, max), textX, y + (step == 56 ? 44 : 33), 0xFFAA00);
			}
			y += step;
		}

		String s = status == null ? "" : status;
		int sy = this.height - 48;
		for (String line : s.split("\n")) {
			while (line.length() > max + 20) {
				graphics.text(this.font, line.substring(0, max + 20), textX, sy, 0xFFFFAA);
				line = line.substring(max + 20);
				sy += 10;
			}
			graphics.text(this.font, line, textX, sy, 0xFFFFAA);
			sy += 10;
		}
	}

	@Override
	public void onClose() {
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			client.gui.setScreen(parent);
		}
	}
}
