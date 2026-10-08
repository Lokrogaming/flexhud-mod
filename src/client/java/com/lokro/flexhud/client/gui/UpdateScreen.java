package com.lokro.flexhud.client.gui;

import com.lokro.flexhud.client.config.FlexhudConfig;
import com.lokro.flexhud.client.market.MarketplaceCache;
import com.lokro.flexhud.client.market.MarketplaceEntry;
import com.lokro.flexhud.client.market.PackInstaller;
import com.lokro.flexhud.client.market.UpdateChecker;
import com.lokro.flexhud.client.i18n.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Update-Übersicht: Status pro Pack, einzeln/alle aktualisieren. */
public class UpdateScreen extends Screen {
	private final Screen parent;
	private int page = 0;
	private String status = "";
	private boolean updatingAll = false;
	private static final int PER_PAGE = 4;

	public UpdateScreen(Screen parent) {
		super(Component.literal(Lang.t("update.title")));
		this.parent = parent;
		if (UpdateChecker.last().isEmpty() && !UpdateChecker.isRunning()) {
			status = Lang.t("update.check");
			UpdateChecker.refreshAsync(() -> {
				Minecraft c = Minecraft.getInstance();
				if (c != null) {
					c.execute(this::rebuildWidgets);
				}
			});
		}
	}

	private List<UpdateChecker.Result> rows() {
		return new ArrayList<>(UpdateChecker.last().values());
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
		List<UpdateChecker.Result> rows = rows();
		int y = 56;
		int start = page * PER_PAGE;
		for (int i = 0; i < PER_PAGE; i++) {
			int idx = start + i;
			if (idx >= rows.size()) {
				break;
			}
			UpdateChecker.Result r = rows.get(idx);
			if (r.status == UpdateChecker.Status.UPDATE_AVAILABLE && !updatingAll) {
				MarketplaceEntry ref = entryOf(r.packId);
				if (ref != null) {
					addRenderableWidget(Button.builder(Component.literal(Lang.t("update.update_btn")),
						b -> {
							status = Lang.f("update.updating", r.name);
							PackInstaller.installAsync(ref, msg -> {
								status = msg;
								UpdateChecker.refreshAsync(() -> {
									Minecraft c = Minecraft.getInstance();
									if (c != null) {
										c.execute(this::rebuildWidgets);
									}
								});
							});
						}).bounds(this.width - 130, y, 120, 20).build());
				}
			}
			y += 34;
		}

		boolean anyUpdate = rows.stream().anyMatch(r -> r.status == UpdateChecker.Status.UPDATE_AVAILABLE);
		if (anyUpdate && !updatingAll) {
			addRenderableWidget(Button.builder(Component.literal(Lang.t("update.all")),
				b -> updateAll(new ArrayList<>(rows), 0)).bounds(10, this.height - 26, 150, 20).build());
		}
		addRenderableWidget(Button.builder(Component.literal(Lang.t("update.recheck")),
			b -> {
				status = Lang.t("update.check");
				UpdateChecker.refreshAsync(() -> {
					Minecraft c = Minecraft.getInstance();
					if (c != null) {
						c.execute(this::rebuildWidgets);
					}
				});
			}).bounds(this.width - 260, this.height - 26, 120, 20).build());
		addRenderableWidget(Button.builder(Component.literal(Lang.t("done")),
			b -> onClose()).bounds(this.width - 130, this.height - 26, 120, 20).build());

		if (UpdateChecker.isRunning() && status.isEmpty()) {
			status = Lang.t("update.check");
		}
	}

	private void updateAll(List<UpdateChecker.Result> rows, int idx) {
		if (idx >= rows.size()) {
			updatingAll = false;
			status = Lang.t("update.all_done");
			UpdateChecker.refreshAsync(() -> {
				Minecraft c = Minecraft.getInstance();
				if (c != null) {
					c.execute(this::rebuildWidgets);
				}
			});
			return;
		}
		UpdateChecker.Result r = rows.get(idx);
		if (r.status != UpdateChecker.Status.UPDATE_AVAILABLE) {
			updateAll(rows, idx + 1);
			return;
		}
		MarketplaceEntry ref = entryOf(r.packId);
		if (ref == null) {
			updateAll(rows, idx + 1);
			return;
		}
		updatingAll = true;
		status = Lang.f("update.updating", r.name);
		rebuildWidgets();
		PackInstaller.installAsync(ref, msg -> updateAll(rows, idx + 1));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		List<UpdateChecker.Result> rows = rows();
		int pages = Math.max(1, (rows.size() + PER_PAGE - 1) / PER_PAGE);
		graphics.centeredText(this.font, Lang.f("update.head", rows.size(), (page + 1), pages),
			this.width / 2, 20, 0xFFFFFF);
		if (rows.isEmpty()) {
			graphics.centeredText(this.font, UpdateChecker.isRunning() ? Lang.t("update.checking")
				: Lang.t("update.nothing"), this.width / 2, 60, 0xAAAAAA);
		}

		int y = 58;
		int start = page * PER_PAGE;
		for (int i = 0; i < PER_PAGE; i++) {
			int idx = start + i;
			if (idx >= rows.size()) {
				break;
			}
			UpdateChecker.Result r = rows.get(idx);
			int color = switch (r.status) {
				case UPDATE_AVAILABLE -> 0x55FF55;
				case UP_TO_DATE -> 0xAAAAAA;
				case NOT_INSTALLED -> 0x888888;
				default -> 0xFF5555;
			};
			graphics.text(this.font, cut(r.name, 50), 10, y, 0x55FFFF);
			graphics.text(this.font, cut(statusLine(r), 70), 10, y + 11, color);
			y += 34;
		}

		if (pages > 1) {
			String pg = Lang.f("update.page_hint", (page + 1), pages);
			graphics.centeredText(this.font, pg, this.width / 2, this.height - 40, 0x888888);
		}
		if (!status.isEmpty()) {
			String s = status.length() > 100 ? status.substring(0, 100) + "…" : status.split("\n")[0];
			graphics.text(this.font, s, 170, this.height - 20, 0xFFFFAA);
		}
	}

	private String statusLine(UpdateChecker.Result r) {
		return switch (r.status) {
			case UPDATE_AVAILABLE -> Lang.f("update.available", r.installedVersion, r.remoteVersion);
			case UP_TO_DATE -> Lang.f("update.uptodate", r.installedVersion);
			case NOT_INSTALLED -> Lang.f("update.notinstalled", r.remoteVersion);
			case UNAVAILABLE -> Lang.f("update.unavailable", note(r));
			case BLOCKED -> Lang.f("update.blocked", note(r));
			case BROKEN -> Lang.f("update.broken", note(r));
		};
	}

	private String note(UpdateChecker.Result r) {
		return r.note == null || r.note.isEmpty() ? "" : " (" + r.note + ")";
	}

	private String cut(String s, int max) {
		if (s == null) {
			return "";
		}
		return s.length() > max ? s.substring(0, max) + "…" : s;
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		List<UpdateChecker.Result> rows = rows();
		int pages = Math.max(1, (rows.size() + PER_PAGE - 1) / PER_PAGE);
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
		FlexhudConfig.save();
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			client.gui.setScreen(parent);
		}
	}
}
