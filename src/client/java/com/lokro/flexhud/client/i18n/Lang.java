package com.lokro.flexhud.client.i18n;

import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

/**
 * DE/EN-Sprachsystem. Nutzt die Spiel-Sprache (options.languageCode):
 * Deutsch bei "de*", sonst Englisch. Fallback ist immer Englisch,
 * unbekannte Keys geben den Key selbst zurück (nie null/crash).
 */
public final class Lang {
	private Lang() {}

	private static final Map<String, String> EN = new HashMap<>();
	private static final Map<String, String> DE = new HashMap<>();

	public static boolean german() {
		try {
			Minecraft client = Minecraft.getInstance();
			if (client != null && client.options != null && client.options.languageCode != null) {
				return client.options.languageCode.toLowerCase(java.util.Locale.ROOT).startsWith("de");
			}
		} catch (Exception ignored) {
		}
		return false;
	}

	public static String t(String key) {
		Map<String, String> map = german() ? DE : EN;
		String s = map.get(key);
		if (s == null) {
			s = EN.get(key);
		}
		return s == null ? key : s;
	}

	public static String f(String key, Object... args) {
		try {
			return String.format(t(key), args);
		} catch (Exception e) {
			return t(key);
		}
	}

	private static void en(String k, String v) {
		EN.put(k, v);
	}

	private static void de(String k, String v) {
		DE.put(k, v);
	}

	static {
		// Typen + Animationen
		en("type.timer", "Timer"); de("type.timer", "Timer");
		en("type.stepcount", "StepCount bridge"); de("type.stepcount", "StepCount-Bridge");
		en("type.clock", "Clock"); de("type.clock", "Uhr");
		en("type.text", "Text"); de("type.text", "Text");
		en("type.fps", "FPS"); de("type.fps", "FPS");
		en("anim.off", "Off (static)"); de("anim.off", "Aus (statisch)");
		en("anim.scroll_l", "Scroll left"); de("anim.scroll_l", "Scroll links");
		en("anim.scroll_r", "Scroll right"); de("anim.scroll_r", "Scroll rechts");
		en("anim.pulse", "Pulse"); de("anim.pulse", "Pulsieren");
		en("anim.rainbow", "Rainbow"); de("anim.rainbow", "Regenbogen");
		en("on", "ON"); de("on", "AN");
		en("off", "OFF"); de("off", "AUS");
		en("done", "Done"); de("done", "Fertig");
		en("back", "Back"); de("back", "Zurück");

		// Hauptmenü
		en("menu.editor", "Arrange widgets (editor)"); de("menu.editor", "Widgets anordnen (Editor)");
		en("menu.market", "Marketplace (load packs)"); de("menu.market", "Marketplace (Packs laden)");
		en("menu.packs", "Installed packs (overview)"); de("menu.packs", "Installierte Packs (Übersicht)");
		en("menu.updates", "Pack updates"); de("menu.updates", "Pack-Updates");
		en("menu.style_first", "Edit first widget style"); de("menu.style_first", "Stil des ersten Widgets bearbeiten");
		en("menu.timer_pause", "Timer: PAUSE (%s)"); de("menu.timer_pause", "Timer: PAUSE (%s)");
		en("menu.timer_start", "Timer: START (%s)"); de("menu.timer_start", "Timer: START (%s)");
		en("menu.timer_reset", "Timer: RESET"); de("menu.timer_reset", "Timer: RESET");
		en("menu.timer_ms", "Timer milliseconds: %s"); de("menu.timer_ms", "Timer-Millisekunden: %s");
		en("menu.step_toggle", "StepCount: toggle start/show"); de("menu.step_toggle", "StepCount: Start/Show umschalten");
		en("menu.step_reset", "StepCount: reset"); de("menu.step_reset", "StepCount: Reset");
		en("menu.head", "FlexHUD - free widgets, timer, StepCount bridge, marketplace");
		de("menu.head", "FlexHUD – freie Widgets, Timer, StepCount-Bridge, Marketplace");
		en("menu.step_found", "StepCount: found (%s steps)"); de("menu.step_found", "StepCount: erkannt (%s Steps)");
		en("menu.step_missing", "StepCount: NOT installed (bridge inactive)");
		de("menu.step_missing", "StepCount: NICHT installiert (Bridge inaktiv)");
		en("menu.source", "Marketplace: %s"); de("menu.source", "Marketplace: %s");
		en("menu.step_nomod", "StepCount mod not found (put its .jar into mods/).");
		de("menu.step_nomod", "StepCount-Mod nicht gefunden (als .jar in mods/ installieren).");
		en("menu.step_hint", "[FlexHUD>StepCount] %s Use the FlexHUD widget for free placement!");
		de("menu.step_hint", "[FlexHUD→StepCount] %s Nutze das FlexHUD-Widget für freie Position!");

		// Editor
		en("editor.title", "FlexHUD - widget editor"); de("editor.title", "FlexHUD – Widget-Editor");
		en("editor.hint", "List: select - preview: grab + drag - arrow keys: move selection");
		de("editor.hint", "Liste: wählen · Vorschau: packen + ziehen · Pfeiltasten: Auswahl bewegen");
		en("editor.add_timer", "+ Timer"); de("editor.add_timer", "+ Timer");
		en("editor.add_steps", "+ StepCount"); de("editor.add_steps", "+ StepCount");
		en("editor.add_clock", "+ Clock"); de("editor.add_clock", "+ Uhr");
		en("editor.add_text", "+ Text"); de("editor.add_text", "+ Text");
		en("editor.add_fps", "+ FPS"); de("editor.add_fps", "+ FPS");
		en("editor.toggle", "On/Off"); de("editor.toggle", "An/Aus");
		en("editor.style", "Style..."); de("editor.style", "Stil…");
		en("editor.delete", "Delete"); de("editor.delete", "Löschen");
		en("editor.confirm_delete", "Really delete?"); de("editor.confirm_delete", "Sicher löschen?");
		en("editor.enable", "Enable"); de("editor.enable", "Einschalten");
		en("editor.disable", "Disable"); de("editor.disable", "Ausschalten");
		en("editor.preview", "Live preview (real style, real animation)");
		de("editor.preview", "Live-Vorschau (echter Stil, echte Animation)");
		en("editor.sel", "Selection: %s"); de("editor.sel", "Auswahl: %s");
		en("editor.type", "Type: %s (%s)"); de("editor.type", "Typ: %s (%s)");
		en("editor.pos", "Pos: %s%% / %s%%   Scale: %s"); de("editor.pos", "Pos: %s%% / %s%%   Scale: %s");
		en("editor.styleline", "Style: %s - %sms%s%s"); de("editor.styleline", "Stil: %s · %sms%s%s");
		en("editor.boldflag", " - bold"); de("editor.boldflag", " · fett");
		en("editor.notextflag", " - no text"); de("editor.notextflag", " · ohne Text");
		en("editor.template", "Template: %s"); de("editor.template", "Vorlage: %s");
		en("editor.page", "Page %s/%s"); de("editor.page", "Seite %s/%s");
		en("editor.scale_down", "Scale -"); de("editor.scale_down", "Scale -");
		en("editor.scale_up", "Scale +"); de("editor.scale_up", "Scale +");
		en("editor.disabled_prefix", "[off] "); de("editor.disabled_prefix", "[aus] ");

		// Stil-Editor
		en("style.title", "Style: %s [%s]"); de("style.title", "Stil: %s [%s]");
		en("style.template_tip", "{value} or single: {d}d {h}:{m}:{s}.{ms}");
		de("style.template_tip", "{value} oder einzeln: {d}T {h}:{m}:{s}.{ms}");
		en("style.template_tip2", "{value} = live value - template freely editable");
		de("style.template_tip2", "{value} = Live-Wert · Vorlage frei editierbar");
		en("style.animation", "Animation: %s"); de("style.animation", "Animation: %s");
		en("style.dur_down", "Duration - (%sms)"); de("style.dur_down", "Dauer - (%sms)");
		en("style.dur_up", "Duration + (%sms)"); de("style.dur_up", "Dauer + (%sms)");
		en("style.bold", "Bold: %s"); de("style.bold", "Fett: %s");
		en("style.italic", "Italic: %s"); de("style.italic", "Kursiv: %s");
		en("style.shadow", "Shadow: %s"); de("style.shadow", "Schatten: %s");
		en("style.background", "Background: %s"); de("style.background", "Hintergrund: %s");
		en("style.text", "Text: %s"); de("style.text", "Text: %s");
		en("style.opacity_down", "Opacity - (%s)"); de("style.opacity_down", "Deckkraft - (%s)");
		en("style.opacity_up", "Opacity + (%s)"); de("style.opacity_up", "Deckkraft + (%s)");
		en("style.preset", "Preset: %s"); de("style.preset", "Preset: %s");
		en("style.scale_down", "Scale - (%s)"); de("style.scale_down", "Scale - (%s)");
		en("style.scale_up", "Scale + (%s)"); de("style.scale_up", "Scale + (%s)");
		en("style.back", "Back (saves)"); de("style.back", "Zurück (speichert)");
		en("style.preview", "Live preview"); de("style.preview", "Live-Vorschau");
		en("style.empty", "(empty)"); de("style.empty", "(leer)");
		en("style.scroll_hint", "Scroll for more options"); de("style.scroll_hint", "↕ Scrollen für mehr Optionen");
		en("style.template_box", "Template"); de("style.template_box", "Vorlage");

		// Marketplace
		en("market.title", "FlexHUD - marketplace"); de("market.title", "FlexHUD – Marketplace");
		en("market.head", "Marketplace (%s packs, page %s)"); de("market.head", "Marketplace (%s Packs, Seite %s)");
		en("market.source", "Source: %s"); de("market.source", "Quelle: %s");
		en("market.install", "Install"); de("market.install", "Installieren");
		en("market.reinstall", "Reload"); de("market.reinstall", "Erneut laden");
		en("market.uninstall", "Uninstall"); de("market.uninstall", "Deinstallieren");
		en("market.confirm_uninstall", "Sure? Delete!"); de("market.confirm_uninstall", "Sicher? Löschen!");
		en("market.prev", "<"); de("market.prev", "◀");
		en("market.next", ">"); de("market.next", "▶");
		en("market.refresh", "Reload"); de("market.refresh", "Neu laden");
		en("market.loading", "Loading... (use reload when offline)");
		de("market.loading", "Lade… (Refresh-Button bei Offline-Fehlern)");
		en("market.refreshing", "Refreshing..."); de("market.refreshing", "Aktualisiere …");
		en("market.loading_name", "Loading %s ..."); de("market.loading_name", "Lade %s …");
		en("market.removing_name", "Removing %s ..."); de("market.removing_name", "Entferne %s …");
		en("market.available", "%s pack(s) available."); de("market.available", "%s Pack(s) verfügbar.");
		en("market.offline", "Offline/error: %s"); de("market.offline", "Offline/Fehler: %s");
		en("market.confirm_msg", "Click again to confirm: deletes pack files%s.");
		de("market.confirm_msg", "Nochmal klicken zum Bestätigen: löscht Pack-Dateien%s.");
		en("market.confirm_widgets", " + %s imported widget(s)"); de("market.confirm_widgets", " + %s importierte Widget(s)");
		en("market.installed", " | INSTALLED"); de("market.installed", " | INSTALLIERT");
		en("market.by", "by %s | modId: %s"); de("market.by", "von %s | modId: %s");
		en("market.meta_site", " - %s"); de("market.meta_site", " · %s");
		en("market.meta_updated", " - updated %s"); de("market.meta_updated", " · Stand %s");
		en("market.flexline", "cfg v%s by %s"); de("market.flexline", "cfg v%s von %s");

		// Konflikte
		en("conflict.both", "WARNING: %s is installed + loaded (%s). Modules may overlap (double HUD). Tip: hide the StepCount HUD with /stepcount show and use the FlexHUD widget.");
		de("conflict.both", "ACHTUNG: %s ist installiert + geladen (%s). Module können sich überschneiden (doppeltes HUD). Tipp: StepCount-HUD mit /stepcount show ausblenden und FlexHUD-Widget nutzen.");
		en("conflict.loaded", "Note: %s is currently loaded. Double display possible - hide one HUD.");
		de("conflict.loaded", "Hinweis: %s ist gerade geladen. Doppelte Anzeigen möglich – ein HUD davon ausblenden.");
		en("conflict.jar", "Note: %s found as file (%s). If both HUDs are active, hide one.");
		de("conflict.jar", "Hinweis: %s als Datei gefunden (%s). Falls beide HUDs aktiv sind, eines ausblenden.");
		en("conflict.modlabel", "the linked mod"); de("conflict.modlabel", "die zugehörige Mod");
		en("conflict.pack", "WARNING: '%s' is already installed as pack '%s'. Duplicate modules possible - uninstall one.");
		de("conflict.pack", "ACHTUNG: »%s« ist schon als Pack '%s' installiert. Doppelte Module möglich – eines deinstallieren.");

		// Install/Deinstall
		en("install.ok", "Installed: %s v%s (%s%s).%s");
		de("install.ok", "Installiert: %s v%s (%s%s).%s");
		en("install.widgets", ", %s widget(s) imported"); de("install.widgets", ", %s Widget(s) übernommen");
		en("install.layout_kept", " Layout kept."); de("install.layout_kept", " Layout übernommen.");
		en("install.layout_lost", " WARNING: pack structure changed - old fields removed, layout could NOT be kept.");
		de("install.layout_lost", " WARNUNG: Pack-Struktur geändert – alte Felder entfernt, Layout konnte NICHT übernommen werden.");
		en("install.fail", "ERROR: %s: %s"); de("install.fail", "FEHLER: %s: %s");
		en("uninstall.ok", "Uninstalled: %s (%s%s%s).");
		de("uninstall.ok", "Deinstalliert: %s (%s%s%s).");
		en("uninstall.nofiles", "no files left"); de("uninstall.nofiles", "keine Dateien mehr da");
		en("uninstall.widgets", ", %s widget(s) removed"); de("uninstall.widgets", ", %s Widget(s) entfernt");
		en("uninstall.norecord", ", note: no widget mapping stored (old install)");
		de("uninstall.norecord", ", Hinweis: keine Widget-Zuordnung gespeichert (alter Install)");

		// StepCount-Bridge
		en("bridge.missing", "StepCount mod is not installed (jar file missing).");
		de("bridge.missing", "StepCount-Mod ist nicht installiert (Mod-Datei fehlt).");
		en("bridge.started", "StepCount started."); de("bridge.started", "StepCount gestartet.");
		en("bridge.stopped", "StepCount stopped."); de("bridge.stopped", "StepCount gestoppt.");
		en("bridge.reset", "StepCount reset."); de("bridge.reset", "StepCount zurückgesetzt.");
		en("bridge.shown", "StepCount display ON."); de("bridge.shown", "StepCount-Anzeige EIN.");
		en("bridge.hidden", "StepCount display OFF."); de("bridge.hidden", "StepCount-Anzeige AUS.");
		en("bridge.unknown", "Unknown. Use start/stop/reset/show.");
		de("bridge.unknown", "Unbekannt. Nutze start/stop/reset/show.");
		en("bridge.error", "Error controlling StepCount (API changed?).");
		de("bridge.error", "Fehler beim Steuern von StepCount (API geändert?).");

		// Commands
		en("cmd.timer_state", "Timer: %s (%s)"); de("cmd.timer_state", "Timer: %s (%s)");
		en("cmd.running", "running"); de("cmd.running", "läuft");
		en("cmd.paused", "paused"); de("cmd.paused", "pausiert");
		en("cmd.timer_started", "Timer started."); de("cmd.timer_started", "Timer gestartet.");
		en("cmd.timer_paused", "Timer paused at %s."); de("cmd.timer_paused", "Timer pausiert bei %s.");
		en("cmd.timer_reset", "Timer reset."); de("cmd.timer_reset", "Timer zurückgesetzt.");
		en("cmd.timer_shown", "Timer display ON."); de("cmd.timer_shown", "Timer-Anzeige EIN.");
		en("cmd.timer_hidden", "Timer display OFF."); de("cmd.timer_hidden", "Timer-Anzeige AUS.");
		en("cmd.timer_mson", "Timer milliseconds ON."); de("cmd.timer_mson", "Timer-Millisekunden EIN.");
		en("cmd.timer_msoff", "Timer milliseconds OFF."); de("cmd.timer_msoff", "Timer-Millisekunden AUS.");
		en("cmd.toggled", "Timer toggled."); de("cmd.toggled", "Timer umgeschaltet.");
		en("cmd.bridge_tip", "Tip: use the FlexHUD STEPCOUNT widget for free placement.");
		de("cmd.bridge_tip", "Tipp: Nutze das FlexHUD-STEPCOUNT-Widget für frei positionierbare Anzeige.");

		// Updates
		en("update.title", "FlexHUD - pack updates"); de("update.title", "FlexHUD – Pack-Updates");
		en("update.head", "Pack updates (%s, page %s/%s)"); de("update.head", "Pack-Updates (%s, Seite %s/%s)");
		en("update.checking", "Checking marketplace ..."); de("update.checking", "Prüfe Marketplace …");
		en("update.nothing", "Nothing checked yet - press check again below.");
		de("update.nothing", "Noch nichts geprüft - unten Erneut prüfen drücken.");
		en("update.check", "Checking..."); de("update.check", "Prüfe …");
		en("update.update_btn", "Update"); de("update.update_btn", "Aktualisieren");
		en("update.all", "Update all"); de("update.all", "Alle aktualisieren");
		en("update.recheck", "Check again"); de("update.recheck", "Erneut prüfen");
		en("update.updating", "Updating %s ..."); de("update.updating", "Aktualisiere %s …");
		en("update.all_done", "All done - checking again ..."); de("update.all_done", "Alle fertig – prüfe erneut …");
		en("update.available", "v%s > v%s available"); de("update.available", "v%s → v%s verfügbar");
		en("update.uptodate", "up to date (v%s)"); de("update.uptodate", "aktuell (v%s)");
		en("update.notinstalled", "not installed (remote v%s)"); de("update.notinstalled", "nicht installiert (remote v%s)");
		en("update.unavailable", "no longer available%s"); de("update.unavailable", "nicht mehr verfügbar%s");
		en("update.blocked", "blocked%s"); de("update.blocked", "gesperrt%s");
		en("update.broken", "broken/error%s"); de("update.broken", "kaputt/Fehler%s");
		en("update.page_hint", "Page %s/%s (flip: < > arrow keys)"); de("update.page_hint", "Seite %s/%s (blättern: ◀ ▶ Pfeiltasten)");
		en("update.join", "Packs: §a%s updatable§f, §c%s with problems §7(missing/broken/blocked) ");
		de("update.join", "Packs: §a%s aktualisierbar§f, §c%s problematisch §7(nicht verfügbar/kaputt/gesperrt) ");
		en("update.join_open", "§e§n[Show all updates]"); de("update.join_open", "§e§n[Alle Updates anzeigen]");

		// Pack-Overview
		en("packs.title", "FlexHUD - installed packs"); de("packs.title", "FlexHUD – Installierte Packs");
		en("packs.head", "Installed packs (%s, page %s/%s)"); de("packs.head", "Installierte Packs (%s, Seite %s/%s)");
		en("packs.line", "%s v%s (%s widgets)"); de("packs.line", "%s v%s (%s Widgets)");
		en("packs.noconfig", "No config menu provided by pack."); de("packs.noconfig", "Kein Konfigurationsmenü vom Pack.");
		en("packs.noflex", "No .flexconfig (widgets only)."); de("packs.noflex", "Keine .flexconfig (nur Widgets).");
		en("packs.page", "Page %s/%s"); de("packs.page", "Seite %s/%s");
		en("packs.applied", "%s: applied to %s widget(s)."); de("packs.applied", "%s: auf %s Widget(s) angewendet.");
		en("packs.nowidgets", "No (more) widgets of this pack present."); de("packs.nowidgets", "Keine Widgets dieses Packs (mehr) vorhanden.");
		en("flex.unknown_action", "Unknown action: %s"); de("flex.unknown_action", "Unbekannte Aktion: %s");
		en("flex.unknown_field", "Unknown field/value: %s = %s"); de("flex.unknown_field", "Unbekanntes Feld/Wert: %s = %s");
		en("flex.toggled", "%s: %s toggled."); de("flex.toggled", "%s: %s umgeschaltet.");
		en("flex.set", "%s: %s = %s"); de("flex.set", "%s: %s = %s");
		en("flex.preset", "%s: preset %s."); de("flex.preset", "%s: Preset %s.");
		en("flex.preset_missing", "Preset not found: %s"); de("flex.preset_missing", "Preset nicht gefunden: %s");
		en("flex.empty", "Empty button."); de("flex.empty", "Leerer Button.");
	}
}
