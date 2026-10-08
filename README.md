# FlexHUD – Freie HUD-Widgets, Timer, StepCount-Bridge & Marketplace (Fabric, Client-Side)

![FlexHUD Logo](docs/assets/logo.png)

> **Diese README ist gleichzeitig die CurseForge-/Modrinth-Description.**
> 🌐 **Website + Entwickler-Docs (GitHub Pages):** nach Aktivierung unter
> `https://lokrogaming.github.io/flexhud-mod/` –
> aktivieren via Repo → Settings → Pages → Deploy from branch → `/docs`.
> Abschnitte sind bewusst so geschrieben, dass sie 1:1 als Projektbeschreibung übernommen werden können.

## Was ist FlexHUD?

**FlexHUD** macht aus festgenagelten Anzeigen **frei verschiebbare Widgets**:
Timer, StepCount, Uhrzeit, FPS und eigene Texte – jedes mit eigenem **Gradienten,
Animation, Position, Größe und Stil**. Dazu gibt es einen **Widget-Editor**
(Widgets per Drag & Drop anordnen) und einen **eingebauten Marketplace**,
über den du fertige Widget-Packs (z. B. das StepCount-Bridge-Pack) per Klick
installierst.

Die Mod läuft **komplett client-seitig** – auch auf Servern ohne Server-Mod.

## Features

- ⏱️ **Timer-Widget** über der Hotbar (wie StepCount) – Start/Stopp/Reset per
  Hotkey, Command oder Menü. Überlebt Neustarts (wird pausiert gespeichert).
  Millisekunden pro Button/Command (`/flexhud timer ms`) an-/abschaltbar,
  Format wächst automatisch (M:SS → H:MM:SS → dD H:MM:SS). Vorlagen mit
  `{value}` oder einzeln: `{d}` Tage, `{h}` Stunden, `{m}` Minuten,
  `{s}` Sekunden, `{ms}` Millisekunden (z. B. `{h}:{m}:{s}.{ms}`).
- 🧩 **Widget-System**: TIMER, STEPCOUNT-Bridge, UHR, TEXT, FPS – adden, löschen,
  an/ausschalten, **frei positionieren** (relativ, also auflösungsunabhängig).
- 🎨 **Style pro Widget**: Gradient-Presets (u. a. StepCount-Classic, Ocean, Sunset,
  Candy, Gold), Animationen (Scroll links/rechts, Puls, Regenbogen, statisch),
  Dauer, Fett/Kursiv, Schatten, Hintergrund, Skalierung, `{value}`-Vorlagen.
  Jedes Element einzeln schaltbar: **Text** (nur Wert anzeigen), Schatten,
  Hintergrund – z. B. Timer ohne „Timer:"-Label, nur die Zeit.
  Hintergrund-Deckkraft in 20er-Schritten einstellbar.
- 🌉 **StepCount-Bridge**: steuert die StepCount-Mod (`start/stop/reset/show`)
  und zeigt die Steps als **frei positionierbares** FlexHUD-Widget – während
  StepCounts eigenes HUD fix über der Hotbar bleibt. Inklusive
  **Konflikt-Warnung** bei doppelten Anzeigen.
- 🛒 **Marketplace (eingebaut)**: lädt eine `marketplace.json` per URL
  (Name, Beschreibung, Icon, **Download-Link**), lädt Zips (z. B. GitHub-Releases)
  nach `flexhud/marketplace/downloaded/` und installiert sie automatisch nach
  `flexhud/marketplace/packs/<id>/` (inkl. Widget-Import aus `pack.json`).
- 🔧 **Pack-Updates**: Beim Spielstart prüft FlexHUD alle Pack-Links
  (Version aus `.flexconfig`/`pack.json`, Temp-Dateien werden danach gelöscht).
  Beim Weltbeitritt meldet der Chat zählbar Updates vs. Probleme
  (nicht verfügbar/kaputt/gesperrt) – Klick öffnet die **Update-Übersicht**
  (`/flexhud updates`) für Einzel- oder Alle-Updates. Reinstalls übernehmen
  dein Layout automatisch (bei geänderter Pack-Struktur: Warnung).
- 📦 **Pack-Übersicht** (`/flexhud packs`): installierte Packs mit ihren
  `.flexconfig`-Menüs (Custom-Buttons wirken auf alle Pack-Widgets).
- 🌍 **Sprache automatisch**: Deutsch bei deutschem Spiel, sonst Englisch.
- ⚠️ **Konflikterkennung**: Jedes Pack MUSS `modId` + `jarHints` angeben.
  FlexHUD prüft Fabric-Loader UND scannt `mods/*.jar` – und warnt, wenn sich
  Module überschneiden können.
- ⌨️ **Hotkey `H`** + **`/flexhud`** + **`/timer`**: eigenes Menü, unabhängig von ModMenu.
- 🧾 **ModMenu-Tab**: Wenn ModMenu installiert ist, öffnet der Config-Button
  direkt das FlexHUD-Menü (alle Einstellungen dort).

## Voraussetzungen (Spielen)

- Minecraft Java **26.3**
- Fabric Loader **0.19.5**
- Fabric API **0.162.0+26.3** (oder neuer für 26.3)
- Java **25**
- Optional: **ModMenu** (für den ModMenü-Tab), **StepCount-Mod** (für das Steps-Widget)

## Installation

1. `flexhud-0.1.0.jar` aus den Releases in deinen `mods`-Ordner legen.
2. Fabric API ebenfalls in `mods` legen.
3. Spiel starten → Hotkey **`H`** drücken oder **`/flexhud`** eingeben.

## Schnellstart

1. `H` → **Widgets anordnen** → `+ Timer` → in der Vorschau **packen und ziehen**
   (geht jetzt überall in der Vorschau, nicht nur direkt am Widget).
   Feinjustierung: **Pfeiltasten** (mit Shift extra-fein) oder ◀▶▲▼-Buttons.
2. `H` → **Stil** → Gradient-Preset + Animation wählen (z. B. Sunset + Scroll links).
3. Timer: `/timer` (Start/Pause), `/timer reset`, oder im Menü.
4. StepCount (falls installiert): im FlexHUD-Menü **StepCount steuern**,
   dann StepCounts eigenes HUD mit `/stepcount show` **ausblenden**,
   damit nur das FlexHUD-Widget sichtbar ist.
5. Marketplace: `H` → **Marketplace** → **StepCount-Bridge-Pack** → Installieren.

## Commands (alle client-side)

- `/flexhud` – Hauptmenü öffnen
- `/flexhud editor` – Widget-Editor öffnen
- `/flexhud market` – Marketplace öffnen
- `/flexhud timer start|stop|reset|show`
- `/flexhud stepcount start|stop|reset|show` – steuert die StepCount-Mod
- `/timer` – Timer Start/Pause umschalten
- `/timer start|stop|reset`

## Der Marketplace (für Spieler)

- Quelle: URL in den Einstellungen (Standard: `marketplace.json` im FlexHUD-Repo).
- **Installieren** lädt die Zip nach `.minecraft/flexhud/marketplace/downloaded/`
  und entpackt nach `.minecraft/flexhud/marketplace/packs/<id>/`.
- Enthält das Pack eine `pack.json` mit `widgets[]`, werden die Widgets
  automatisch in deine Config übernommen (IDs werden neu vergeben).
- Bei erkannten Überschneidungen (Mod schon als `.jar` da) zeigt FlexHUD eine
  **Warnung** – z. B. StepCount-HUD vs. FlexHUD-Widget.
- **Deinstallieren**: Installierte Packs haben im Marketplace einen
  **Deinstallieren**-Button (2x klicken zur Bestätigung). Entfernt Pack-Dateien,
  heruntergeladene Zip **und** die daraus importierten Widgets.
  Die Herkunft steht in `.minecraft/flexhud/marketplace/installed.json`.

## Eigene Marketplace-Packs (für Pack-Autoren)

1. Ordner anlegen: `mein-pack/pack.json` (+ README, Assets).
2. `pack.json`-Widgets definieren (Felder wie in der Config: `type`, `template`,
   `x`, `y`, `enabled`, `style`).
3. Ordner als `.zip` packen (pack.json im Zip-Root!).
4. Zip als **GitHub-Release-Asset** hochladen (stabiler Direkt-Link!).
5. Eintrag in `marketplace.json` – **Pflichtfelder**: `id`, `name`,
   `downloadUrl`, **`modId`**, **`jarHints`**:

```json
{
  "id": "mein-pack",
  "name": "Mein Pack",
  "description": "Was es tut",
  "author": "Du",
  "version": "1.0.0",
  "iconUrl": "https://...",
  "downloadUrl": "https://github.com/DU/REPO/releases/download/v1.0.0/mein-pack-1.0.0.zip",
  "modId": "meine-mod",
  "jarHints": ["meine-mod"],
  "packType": "widget-pack",
  "installNote": "Hinweis für Spieler"
}
```

> `modId` = Fabric-Mod-ID (aus `fabric.mod.json`), `jarHints` = Dateinamen-Teile
> zur `.jar`-Erkennung (z. B. `["stepcount-mod", "stepcount"]`). Ohne diese Felder
> kann FlexHUD **nicht** vor doppelten Modulen warnen!

## Eigene `.flexconfig` (für Pack-Autoren, optional aber empfohlen)

Eine `*.flexconfig`-Datei im Zip-Root (Name frei, z. B. `mein-pack.flexconfig`)
liefert Meta + Custom-Buttons. Vorlage: siehe StepCount-Bridge-Pack im Repo.

```json
{
  "packId": "mein-pack",
  "author": "Du",
  "website": "https://...",
  "created": "2026-10-09",
  "updated": "2026-10-09",
  "version": "1.0.0",
  "description": "...",
  "icons": {},
  "configMenu": {
    "enabled": true,
    "title": "Mein Pack",
    "buttons": [
      {"id": "sunset", "label": "Sunset für alle", "action": "applyPreset", "target": "style", "value": "Sunset"},
      {"id": "shadow", "label": "Schatten umschalten", "action": "toggleStyle", "target": "style", "field": "shadow"}
    ]
  },
  "widgetButtons": [
    {"id": "numonly", "label": "Nur Wert", "action": "setStyle", "target": "style", "field": "template", "value": "{value}"}
  ]
}
```

- **Meta** (`author`, `website`, `created`/`updated`, `version` …) wird im
  Marketplace bei installierten Packs angezeigt; `version` steuert die
  Update-Erkennung (fällt zurück auf `packVersion`, dann Eintrags-Version).
- **`configMenu`** (muss explizit deklariert sein!) erscheint im
  **Overview-Menü** (`/flexhud packs`) und wirkt auf **alle** Pack-Widgets.
- **`widgetButtons`** erscheinen im **Stil-Menü** (★-Buttons) und wirken aufs
  gewählte Widget. Ohne diese Blöcke gibt es schlicht keine Extra-Buttons.
- **Aktionen**: `toggleStyle` (Felder: bold, italic, shadow, background, hideText),
  `setStyle` (scale, cycleMs, backgroundOpacity, staticColor, animation,
  template, gradient als Kommaliste, Booleans), `applyPreset` (Preset-Name),
  `message` (Chat-Hinweis). `icons` ist reserviert/optional – Auswertung später.

## Marketplace-Einträge verwalten (für Maintainer)

Jeder Download-Link ist ein **einzelner Eintrag** in `marketplace/marketplace.json`
(`packs[]`). Wenn dir jemand ein Pack einreicht, trägst du es so ein:

1. **Einreichung prüfen**: Issue-Vorlage „Marketplace-Pack einreichen“
   (`.github/ISSUE_TEMPLATE/marketplace-submission.yml`) – enthält alle Felder
   + Checkliste (pack.json im Root, modId/jarHints, öffentlicher Link).
2. **Eintragen** (validiert Pflichtfelder, IDs, URL und Zip automatisch):
   `powershell -File scripts/Add-MarketplaceEntry.ps1 -Id "mein-pack" -Name "Mein Pack" -Description "..." -Author "Du" -Version "1.0.0" -DownloadUrl "https://..." -ModId "meine-mod" -JarHints "meine-mod,meine_mod" -CheckUrl -VerifyZip`
   Vorlage für die Felder: `marketplace/template-pack-entry.json`.
3. **Gesamt-Check**: `powershell -File scripts/Test-MarketplaceJson.ps1`
   (optional `-CheckUrls -VerifyZips`), dann committen + pushen – ab dann
   sehen alle Spieler den Eintrag im In-Game-Marketplace.

## Dateien & Ordner

- `config/flexhud.json` – Widgets, Styles, Marketplace-URL
- `config/flexhud-timer.json` – Timer-Status
- `.minecraft/flexhud/marketplace/downloaded/` – heruntergeladene Pack-Zips
- `.minecraft/flexhud/marketplace/packs/<id>/` – entpackte Packs
- `.minecraft/flexhud/marketplace/marketplace-cache.json` – Offline-Cache der Packliste

## FAQ

**Brauche ich StepCount für FlexHUD?**
Nein. Nur das STEPCOUNT-Widget braucht die StepCount-`.jar` in `mods/`.

**Warum sehe ich Steps doppelt?**
StepCounts natives HUD (`/stepcount show`) + FlexHUD-Widget laufen parallel.
Eines ausblenden (empfohlen: natives aus, FlexHUD-Widget an).

**ModMenu-Tab fehlt?**
ModMenu ist optional. Menü geht immer via Hotkey `H` oder `/flexHUD`.
Falls der Tab trotz installiertem ModMenu fehlt, bitte Issue mit
ModMenu-Version + Log öffnen.

**Marketplace lädt nicht?**
Offline/URL falsch? Die Mod nutzt einen lokalen Cache; Button
**Aktualisieren** erzwingt Neuladen. URL in `config/flexhud.json`
(`marketplaceUrl`) prüfen.

## Bauen (Entwickler)

- JDK 25, dann `gradlew.bat build` → `build/libs/flexhud-0.1.0.jar`
- Beispiel-Pack: `powershell -File scripts/Build-ExamplePack.ps1` → `dist/*.zip`
- Details für Agenten: siehe `AGENTS.md`, Entscheidungen/Ideen: `THOUGHTS.md`

## Links

- FlexHUD-Repo: `https://github.com/Lokrogaming/flexhud-mod`
- Marketplace-JSON: `https://raw.githubusercontent.com/Lokrogaming/flexhud-mod/main/marketplace/marketplace.json`
- StepCount-Mod: siehe StepCount-Repo (Bridge-Ziel)

## Lizenz

MIT (siehe LICENSE). StepCount-Bridge spricht die StepCount-Mod nur über
deren öffentliche Client-API an.

---

## English (short)

**FlexHUD** is a client-side Fabric mod with **free-placeable HUD widgets**
(timer, StepCount bridge, clock, text, FPS), per-widget **gradient/animation
styles**, a drag-and-drop **widget editor**, and a built-in **marketplace**
that installs widget packs (zips, e.g. from GitHub releases) into
`flexhud/marketplace/`. Each marketplace entry declares `modId` + `jarHints`
so FlexHUD can **warn about overlapping modules** (e.g. StepCount's native HUD
vs. the FlexHUD widget). Menu via hotkey `H`, `/flexhud`, or the ModMenu tab.
