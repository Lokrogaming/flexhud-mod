package com.lokro.flexhud.client.market;

import java.util.ArrayList;
import java.util.List;

/**
 * EIN Marketplace-Eintrag – exakt so steht er in {@code marketplace.json}.
 *
 * <p><b>WICHTIG für Pack-Autoren:</b> {@code modId} + {@code jarHints} sind PFLICHT.
 * FlexHUD erkennt daran, ob die Mod schon als .jar in {@code mods/} liegt, und warnt
 * vor doppelten/überlappenden Modulen (z.B. StepCount-HUD vs. FlexHUD-Widget).
 */
public class MarketplaceEntry {
	public String id = "";
	/** Anzeigename, z.B. "StepCount-Bridge-Pack". */
	public String name = "";
	public String description = "";
	public String author = "";
	public String version = "1.0.0";
	/** Optionales Icon (http(s)-URL oder Data-URL). Wird im Menü angezeigt/ge-cached. */
	public String iconUrl = "";
	/** Direkter Download-Link zur .zip (z.B. GitHub-Release-Asset). */
	public String downloadUrl = "";
	/**
	 * Mod-ID (Fabric, aus fabric.mod.json) bzw. Paket-/Mod-Name zur Konflikterkennung.
	 * Beispiel StepCount: {@code "stepcount"}.
	 */
	public String modId = "";
	/** Dateinamen-Hinweise für die .jar-Erkennung, z.B. ["stepcount-mod","stepcount"]. */
	public List<String> jarHints = new ArrayList<>();
	/** Art des Packs: widget-pack | bridge | theme. */
	public String packType = "widget-pack";
	public String installNote = "";

	public boolean valid() {
		return id != null && !id.isBlank() && downloadUrl != null && !downloadUrl.isBlank();
	}
}
