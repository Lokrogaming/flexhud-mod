# FlexHUD – Freie HUD-Widgets, Timer, StepCount-Bridge & Marketplace (Fabric, Client-Side)

![FlexHUD Banner](https://raw.githubusercontent.com/Lokrogaming/flexhud-mod/master/docs/assets/banner.jpg)

> **Diese Beschreibung gilt für GitHub, CurseForge und Modrinth.**
> Website + Entwickler-Docs: `https://lokrogaming.github.io/flexhud-mod/`

## Was ist FlexHUD?

**FlexHUD** macht aus festgenagelten Anzeigen **frei verschiebbare Widgets**:
Timer, StepCount, Uhrzeit, FPS und eigene Texte – jedes mit eigenem **Gradienten,
Animation, Position, Größe und Stil**. Dazu gibt es einen **Widget-Editor**
(Widgets per Drag & Drop anordnen) und einen **eingebauten Marketplace**,
über den du fertige Widget-Packs (z. B. das StepCount-Bridge-Pack) per Klick
installierst.

Die Mod läuft **komplett client-seitig** – auch auf Servern ohne Server-Mod.

## Features

- **Timer-Widget** über der Hotbar – Start/Stopp/Reset per Hotkey, Command oder Menü.
  Überlebt Neustarts (wird pausiert gespeichert). Millisekunden per Button/Command
  (`/flexhud timer ms`) an-/abschaltbar, Format wächst automatisch
  (M:SS → H:MM:SS → dD H:MM:SS). Vorlagen mit `{value}` oder einzeln:
  `{d}` Tage, `{h}` Stunden, `{m}` Minuten, `{s}` Sekunden, `{ms}` Millisekunden.
- **Widget-System**: TIMER, STEPCOUNT-Bridge, UHR, TEXT, FPS – hinzufügen, löschen,
  an/ausschalten, **frei positionieren** (relativ, also auflösungsunabhängig).
- **Style pro Widget**: Gradient-Presets (u. a. StepCount-Classic, Ocean, Sunset,
  Candy, Gold), Animationen (Scroll links/rechts, Puls, Regenbogen, statisch),
  Dauer, Fett/Kursiv, Schatten, Hintergrund + Deckkraft, Skalierung,
  Text an/aus, `{value}`-Vorlagen – alles mit **Live-Vorschau**.
- **StepCount-Bridge**: steuert die StepCount-Mod (`start/stop/reset/show`)
  und zeigt die Steps als **frei positionierbares** FlexHUD-Widget – inklusive
  **Konflikt-Warnung** bei doppelten Anzeigen.
- **Marketplace (eingebaut)**: Packs per Klick installieren und deinstallieren
  (inkl. importierter Widgets), mit Konflikt-Warnung bei Überschneidungen.
- **Pack-Updates**: Beim Spielstart prüft FlexHUD alle Pack-Links. Beim Weltbeitritt
  meldet der Chat zählbar Updates vs. Probleme (nicht verfügbar/kaputt/gesperrt) –
  Klick öffnet die **Update-Übersicht** (`/flexhud updates`) für Einzel- oder
  Alle-Updates. Reinstalls übernehmen dein Layout automatisch.
- **Pack-Übersicht** (`/flexhud packs`): installierte Packs mit ihren
  `.flexconfig`-Menüs (Custom-Buttons wirken auf alle Pack-Widgets).
- **Sprache automatisch**: Deutsch bei deutschem Spiel, sonst Englisch.
- **Bedienung**: Hotkey `H`, `/flexhud`, `/timer` – oder ModMenu-Tab (falls ModMenu installiert).

## Voraussetzungen

- Minecraft Java **26.3**
- Fabric Loader **0.19.5**
- Fabric API **0.162.0+26.3** (oder neuer für 26.3)
- Java **25**
- Optional: **ModMenu** (für den ModMenü-Tab), **StepCount-Mod** (für das Steps-Widget)

## Installation

1. `flexhud-0.1.1-alpha.jar` aus den Releases in deinen `mods`-Ordner legen.
2. Fabric API ebenfalls in `mods` legen.
3. Spiel starten → Hotkey **`H`** drücken oder **`/flexhud`** eingeben.

## Schnellstart

1. `H` → **Widgets anordnen** → `+ Timer` → in der Vorschau **packen und ziehen**.
   Feinjustierung: **Pfeiltasten** (mit Shift extra-fein) oder Pfeil-Buttons.
2. **Stil**-Button → Gradient-Preset + Animation wählen (z. B. Sunset + Scroll links).
3. Timer: `/timer` (Start/Pause), `/timer reset`, oder im Menü.
4. StepCount (falls installiert): im FlexHUD-Menü **StepCount steuern**,
   dann StepCounts eigenes HUD mit `/stepcount show` **ausblenden**,
   damit nur das FlexHUD-Widget sichtbar ist.
5. Marketplace: `H` → **Marketplace** → **StepCount-Bridge-Pack** → Installieren.

## Commands (alle client-side)

- `/flexhud` – Hauptmenü öffnen
- `/flexhud editor` – Widget-Editor öffnen
- `/flexhud market` – Marketplace öffnen
- `/flexhud packs` – Installierte Packs (Übersicht)
- `/flexhud updates` – Pack-Updates (Übersicht)
- `/flexhud timer start|stop|reset|show|ms`
- `/flexhud stepcount start|stop|reset|show` – steuert die StepCount-Mod
- `/timer` – Timer Start/Pause umschalten
- `/timer start|stop|reset`

## Der Marketplace (für Spieler)

- **Installieren** lädt die Zip nach `.minecraft/flexhud/marketplace/downloaded/`
  und entpackt nach `.minecraft/flexhud/marketplace/packs/<id>/`.
  Widgets aus `pack.json` werden automatisch übernommen (IDs werden neu vergeben).
- Bei Überschneidungen (Mod schon als `.jar` da) zeigt FlexHUD eine **Warnung**.
- **Deinstallieren**: Button im Marketplace (2x klicken zur Bestätigung) –
  entfernt Pack-Dateien, Zip **und** importierte Widgets.

## Eigene Packs (für Pack-Autoren)

1. Ordner anlegen: `mein-pack/pack.json` (+ optionale `mein-pack.flexconfig`, README, Assets).
2. Widgets definieren (`type`, `template`, `x`, `y`, `enabled`, `style`).
3. Als `.zip` packen (`pack.json` im Zip-Root!) und als **GitHub-Release-Asset** hochladen.
4. Einreichung: **GitHub-Issue** im FlexHUD-Repo („Marketplace-Pack einreichen“) –
   der Download-Link wird als Eintrag in `marketplace.json` übernommen.
   Pflicht pro Eintrag: `id`, `name`, `downloadUrl`, **`modId`**, **`jarHints`**
   (Fabric-ID + Dateinamen-Teile für die `.jar`-Erkennung – ohne sie keine Warnung
   vor doppelten Modulen!).
5. Optional `.flexconfig` für Meta (Autor, Website, Version für Updates) +
   Custom-Buttons (`configMenu` fürs Overview-Menü, `widgetButtons` fürs Stil-Menü,
   Aktionen: `toggleStyle`, `setStyle`, `applyPreset`, `message`).
   Vollständige Referenz: Website → Entwickler-Docs.

## Dateien & Ordner

- `config/flexhud.json` – Widgets, Styles, Marketplace-URL
- `config/flexhud-timer.json` – Timer-Status
- `.minecraft/flexhud/marketplace/downloaded/` – heruntergeladene Pack-Zips
- `.minecraft/flexhud/marketplace/packs/<id>/` – entpackte Packs
- `.minecraft/flexhud/marketplace/marketplace-cache.json` – Offline-Cache der Packliste
- `.minecraft/flexhud/marketplace/installed.json` – Herkunft installierter Widgets

## FAQ

**Brauche ich StepCount für FlexHUD?**
Nein. Nur das STEPCOUNT-Widget braucht die StepCount-`.jar` in `mods/`.

**Warum sehe ich Steps doppelt?**
StepCounts natives HUD (`/stepcount show`) + FlexHUD-Widget laufen parallel.
Eines ausblenden (empfohlen: natives aus, FlexHUD-Widget an).

**Bleibt mein Layout bei Updates erhalten?**
Ja. Positionen werden vor Reinstalls gesichert und zurückgespielt.
Nur bei geänderter Pack-Struktur gibt es eine Warnung.

**ModMenu-Tab fehlt?**
ModMenu ist optional. Menü geht immer via Hotkey `H` oder `/flexhud`.

**Marketplace lädt nicht?**
Offline/URL falsch? Button **Neu laden** erzwingt Neuladen (Cache in `marketplace-cache.json`).

## Bauen (Entwickler)

- JDK 25, dann `gradlew.bat build` → `build/libs/flexhud-0.1.1-alpha.jar`
- Beispiel-Pack: `powershell -File scripts/Build-ExamplePack.ps1` → `dist/*.zip`
- Vollständige Entwickler-Docs (Pack-Schema, `.flexconfig`, Bridge, i18n):
  Website oder `docs/developers.html` im Repo.

## Links

- Website + Entwickler-Docs: `https://lokrogaming.github.io/flexhud-mod/`
- GitHub (Issues, Marketplace-Einreichungen): `https://github.com/Lokrogaming/flexhud-mod`
- Marketplace-JSON: `https://raw.githubusercontent.com/Lokrogaming/flexhud-mod/master/marketplace/marketplace.json`

## Lizenz

MIT (siehe LICENSE). Die StepCount-Bridge spricht die StepCount-Mod nur über
deren öffentliche Client-API an.

---

## English (short)

**FlexHUD** is a client-side Fabric mod with **free-placeable HUD widgets**
(timer, StepCount bridge, clock, text, FPS), per-widget **gradient/animation
styles** with live preview, a drag-and-drop **widget editor**, and a built-in
**marketplace** with install/uninstall, conflict warnings and updates that keep
your layout. Each marketplace entry declares `modId` + `jarHints` so FlexHUD can
**warn about overlapping modules**. Menu via hotkey `H`, `/flexhud`, or the ModMenu
tab. Language follows the game (German/English).
