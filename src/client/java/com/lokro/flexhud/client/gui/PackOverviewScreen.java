package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.market.FlexAction;
import com.lokro.flexhud.client.market.FlexConfig;
import com.lokro.flexhud.client.market.InstalledRegistry;
import com.lokro.flexhud.client.market.MarketplaceCache;
import com.lokro.flexhud.client.market.MarketplaceEntry;
import com.lokro.flexhud.client.i18n.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Overview installierter Packs mit ihren .flexconfig-Menüs. */
public class PackOverviewScreen extends Screen {
	private final Screen parent;
	private int page = 0;
	private String status = "";
	private static final int PER_PAGE = 2;

	public PackOverviewScreen(Screen parent) {
		super(Component.literal(Lang.t("packs.title")));
		this.parent = parent;
	}

	private List<String> installedPacks() {
		Set<String> ids = new LinkedHashSet<>(InstalledRegistry.load().keySet());
		try {
			Path packs = MarketplaceCache.packsDir();
			if (Files.isDirectory(packs)) {
				try (DirectoryStream<Path> ds = Files.newDirectoryStream(packs)) {
					for (Path p : ds) {
						if (Files.isDirectory(p)) {
							ids.add(p.getFileName().toString());
						}
					}
				}
			}
		} catch (Exception ignored) {
		}
		return new ArrayList<>(ids);
	}

	private MarketplaceEntry entryOf(String packId) {
		for (MarketplaceEntry e : MarketplaceCache.entries()) {
			if (e.id.equals(packId)) {
				return e;
			}
		}
		return null;
	}

	@Override
	protected void init() {
		List<String> packs = installedPacks();
		int y = 56;
		int start = page * PER_PAGE;
		for (int i = 0; i < PER_PAGE; i++) {
			int idx = start + i;
			if (idx >= packs.size()) {
				break;
			}
			String packId = packs.get(idx);
			FlexConfig cfg = FlexConfig.load(packId);
			if (cfg != null && cfg.hasOverview()) {
				String title = cfg.configMenu.title == null || cfg.configMenu.title.isBlank()
					? packId : cfg.configMenu.title;
				for (FlexConfig.FlexButton btn : cfg.configMenu.buttons) {
					String label = (btn.label == null || btn.label.isBlank() ? btn.id : btn.label);
					if (label.length() > 28) {
						label = label.substring(0, 28) + "…";
					}
					addRenderableWidget(Button.builder(Component.literal(title + ": " + label),
						b -> {
							status = FlexAction.runOnPack(btn, packId);
							rebuildWidgets();
						}).bounds(this.width - 250, y, 240, 20).build());
					y += 22;
					if (y > this.height - 60) {
						break;
					}
				}
			}
			y += 34;
		}

		addRenderableWidget(Button.builder(Component.literal(Lang.t("done")),
			b -> onClose()).bounds(this.width / 2 - 100, this.height - 26, 200, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		List<String> packs = installedPacks();
		int pages = Math.max(1, (packs.size() + PER_PAGE - 1) / PER_PAGE);
		graphics.centeredText(this.font, Lang.f("packs.head", packs.size(), (page + 1), pages),
			this.width / 2, 20, 0xFFFFFF);

		int y = 58;
		int start = page * PER_PAGE;
		for (int i = 0; i < PER_PAGE; i++) {
			int idx = start + i;
			if (idx >= packs.size()) {
				break;
			}
			String packId = packs.get(idx);
			InstalledRegistry.Entry rec = InstalledRegistry.get(packId);
			MarketplaceEntry e = entryOf(packId);
			String name = e != null && e.name != null && !e.name.isBlank() ? e.name : packId;
			String ver = rec != null ? rec.version : "?";
			int widgets = rec != null && rec.widgetIds != null ? rec.widgetIds.size() : 0;
			graphics.text(this.font, cut(Lang.f("packs.line", name, ver, widgets), 44), 10, y, 0x55FFFF);

			FlexConfig cfg = FlexConfig.load(packId);
			if (cfg != null) {
				String meta = cfg.author;
				if (!cfg.website.isBlank()) {
					meta += " · " + cfg.website;
				}
				if (!cfg.updated.isBlank()) {
					meta += " · Stand " + cfg.updated;
				}
				graphics.text(this.font, cut(meta, 44), 10, y + 11, 0x888888);
				if (!cfg.hasOverview()) {
					graphics.text(this.font, Lang.t("packs.noconfig"),
						10, y + 22, 0x666666);
				}
			} else {
				graphics.text(this.font, Lang.t("packs.noflex"), 10, y + 11, 0x666666);
			}
			y += 34 + (cfg != null && cfg.hasOverview() ? cfg.configMenu.buttons.size() * 22 : 0);
		}

		if (!status.isEmpty()) {
			graphics.text(this.font, cut(status, 80), 10, this.height - 40, 0xFFFFAA);
		}
		if (pages > 1) {
			graphics.centeredText(this.font, Lang.f("packs.page", (page + 1), pages),
				this.width / 2, this.height - 40, 0x888888);
		}
	}

	private String cut(String s, int max) {
		if (s == null) {
			return "";
		}
		return s.length() > max ? s.substring(0, max) + "…" : s;
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		List<String> packs = installedPacks();
		int pages = Math.max(1, (packs.size() + PER_PAGE - 1) / PER_PAGE);
		int k = event.key();
		if (k == com.mojang.blaze3d.platform.InputConstants.KEY_LEFT && page > 0) {
			page--;
			rebuildWidgets();
			return true;
		}
		if (k == com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT && page < pages - 1) {
			page++;
			rebuildWidgets();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			client.gui.setScreen(parent);
		}
	}
}
