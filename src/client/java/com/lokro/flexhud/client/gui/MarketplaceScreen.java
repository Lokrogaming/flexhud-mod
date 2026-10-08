package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.market.ConflictChecker;
import com.lokro.flexhud.client.market.MarketplaceCache;
import com.lokro.flexhud.client.market.MarketplaceEntry;
import com.lokro.flexhud.client.market.PackInstaller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Eingebauter Marketplace: lädt {@code marketplace.json} (URL aus Config),
 * zeigt Name/Beschreibung/Version, prüft .jar-Konflikte (modId + jarHints)
 * und installiert Zips nach {@code flexhud/marketplace/downloaded/}.
 */
public class MarketplaceScreen extends Screen {
	private final Screen parent;
	private int page = 0;
	private String status = "Lade… (Refresh-Button bei Offline-Fehlern)";
	private static final int PER_PAGE = 4;

	public MarketplaceScreen(Screen parent) {
		super(Component.literal("FlexHUD – Marketplace"));
		this.parent = parent;
		MarketplaceCache.refreshAsyncIfStale();
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
			ConflictChecker.Conflict c = ConflictChecker.check(e);
			String warn = ConflictChecker.warnText(e, c);
			boolean installed = PackInstaller.isInstalled(e);
			String btnLabel = installed ? "Installiert ✓ (erneut laden)" : "Installieren";
			MarketplaceEntry ref = e;
			addRenderableWidget(Button.builder(Component.literal(btnLabel),
				b -> {
					status = "Lade " + ref.name + " …";
					PackInstaller.installAsync(ref, msg -> {
						status = msg;
						Minecraft client = Minecraft.getInstance();
						if (client != null) {
							client.execute(this::rebuildWidgets);
						}
					});
				}).bounds(cx + 40, y, 170, 20).build());
			y += 44;
		}

		addRenderableWidget(Button.builder(Component.literal("◀ Zurück"),
			b -> {
				if (page > 0) {
					page--;
					rebuildWidgets();
				}
			}).bounds(cx - 220, this.height - 58, 100, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Weiter ▶"),
			b -> {
				if ((page + 1) * PER_PAGE < MarketplaceCache.entries().size()) {
					page++;
					rebuildWidgets();
				}
			}).bounds(cx + 120, this.height - 58, 100, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Aktualisieren"),
			b -> {
				MarketplaceCache.refreshAsync();
				status = "Aktualisiere …";
			}).bounds(cx - 110, this.height - 58, 110, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Fertig"),
			b -> onClose()).bounds(cx + 5, this.height - 58, 110, 20).build());

		if (MarketplaceCache.lastError() != null && all.isEmpty()) {
			status = "Offline/Fehler: " + MarketplaceCache.lastError();
		} else if (!all.isEmpty() && status.startsWith("Lade")) {
			status = all.size() + " Pack(s) verfügbar.";
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		List<MarketplaceEntry> all = MarketplaceCache.entries();
		graphics.text(this.font, "Marketplace (" + all.size() + " Packs, Seite " + (page + 1) + ")",
			this.width / 2 - 220, 20, 0xFFFFFF);
		graphics.text(this.font, "Quelle: " + FlexhudConfig.get().marketplaceUrl,
			this.width / 2 - 220, 32, 0x666666);

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
			graphics.text(this.font, e.name + " v" + e.version + " [" + e.packType + "]",
				this.width / 2 - 220, y, 0x55FFFF);
			String desc = e.description == null ? "" : e.description;
			if (desc.length() > 70) {
				desc = desc.substring(0, 70) + "…";
			}
			graphics.text(this.font, desc, this.width / 2 - 220, y + 11, 0xCCCCCC);
			String meta = "by " + e.author + " | modId: " + e.modId
				+ (PackInstaller.isInstalled(e) ? " | INSTALLIERT" : "");
			graphics.text(this.font, meta, this.width / 2 - 220, y + 22, 0x888888);
			if (warn != null) {
				String w = warn.length() > 80 ? warn.substring(0, 80) + "…" : warn;
				graphics.text(this.font, "⚠ " + w, this.width / 2 - 220, y + 33, 0xFFAA00);
			}
			y += 44;
		}

		// Statuszeile (mehrzeilig umbrechen, simpel)
		String s = status == null ? "" : status;
		int sy = this.height - 80;
		for (String line : s.split("\n")) {
			while (line.length() > 90) {
				graphics.text(this.font, line.substring(0, 90), this.width / 2 - 220, sy, 0xFFFFAA);
				line = line.substring(90);
				sy += 10;
			}
			graphics.text(this.font, line, this.width / 2 - 220, sy, 0xFFFFAA);
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
