# FlexHUD – Free HUD Widgets, Timer, StepCount Bridge & Marketplace (Fabric, Client-Side)

![FlexHUD Banner](https://raw.githubusercontent.com/Lokrogaming/flexhud-mod/master/docs/assets/banner.jpg)

> This description is used for GitHub, CurseForge and Modrinth.
> Website + developer docs: `https://lokrogaming.github.io/flexhud-mod/`

## What is FlexHUD?

**FlexHUD** turns fixed HUD displays into **freely moveable widgets**:
timer, StepCount, clock, FPS and custom text – each with its own **gradient,
animation, position, size and style**. It also includes a **widget editor**
(arrange widgets via drag & drop) and a **built-in marketplace** to install
ready-made widget packs (e.g. the StepCount bridge pack) with one click.

The mod runs **fully client-side** – including on servers without a server-side mod.

## Features

- **Timer widget** above the hotbar – start/stop/reset via hotkey, command or menu.
  Survives restarts (kept paused). Milliseconds toggleable via button/command
  (`/flexhud timer ms`), format auto-expands (M:SS → H:MM:SS → dD H:MM:SS).
  Templates with `{value}` or individual parts: `{d}` days, `{h}` hours,
  `{m}` minutes, `{s}` seconds, `{ms}` milliseconds.
- **Widget system**: TIMER, STEPCOUNT bridge, CLOCK, TEXT, FPS – add, delete,
  enable/disable, **position freely** (relative, resolution-independent).
- **Per-widget style**: gradient presets (StepCount Classic, Ocean, Sunset,
  Candy, Gold), animations (scroll left/right, pulse, rainbow, static),
  duration, bold/italic, shadow, background + opacity, scaling,
  text on/off, `{value}` templates – all with **live preview**.
- **StepCount bridge**: controls the StepCount mod (`start/stop/reset/show`)
  and shows steps as a **freely placeable** FlexHUD widget – including a
  **conflict warning** for duplicate displays.
- **Built-in marketplace**: install and uninstall packs with one click
  (including imported widgets), with overlap warnings.
- **Pack updates**: on game start FlexHUD checks all pack links. On world join,
  chat reports countable updates vs. problems (unavailable/broken/blocked) –
  click opens the **update overview** (`/flexhud updates`) for single or
  update-all. Reinstalls keep your layout automatically.
- **Pack overview** (`/flexhud packs`): installed packs with their
  `.flexconfig` menus (custom buttons affect all pack widgets).
- **Automatic language**: German for German games, English otherwise.
- **Controls**: hotkey `H`, `/flexhud`, `/timer` – or ModMenu tab (if ModMenu installed).

## Requirements

- Minecraft Java **26.3**
- Fabric Loader **0.19.5**
- Fabric API **0.162.0+26.3** (or newer for 26.3)
- Java **25**
- Optional: **ModMenu** (for the ModMenu tab), **StepCount mod** (for the steps widget)

## Installation

1. Put `flexhud-0.1.1-alpha.jar` from the releases into your `mods` folder.
2. Put Fabric API into `mods` as well.
3. Start the game → press hotkey **`H`** or type **`/flexhud`**.

## Quick start

1. `H` → **Arrange widgets** → `+ Timer` → **grab and drag** in the preview.
   Fine-tune: **arrow keys** (extra fine with Shift) or arrow buttons.
2. **Style** button → pick gradient preset + animation (e.g. Sunset + scroll left).
3. Timer: `/timer` (start/pause), `/timer reset`, or via menu.
4. StepCount (if installed): use **StepCount controls** in the FlexHUD menu,
   then **hide** StepCount's own HUD with `/stepcount show`,
   so only the FlexHUD widget stays visible.
5. Marketplace: `H` → **Marketplace** → **StepCount bridge pack** → Install.

## Commands (all client-side)

- `/flexhud` – open main menu
- `/flexhud editor` – open widget editor
- `/flexhud market` – open marketplace
- `/flexhud packs` – installed packs (overview)
- `/flexhud updates` – pack updates (overview)
- `/flexhud timer start|stop|reset|show|ms`
- `/flexhud stepcount start|stop|reset|show` – controls the StepCount mod
- `/timer` – toggle timer start/pause
- `/timer start|stop|reset`

## Marketplace (for players)

- **Installing** downloads the zip to `.minecraft/flexhud/marketplace/downloaded/`
  and extracts to `.minecraft/flexhud/marketplace/packs/<id>/`.
  Widgets from `pack.json` are adopted automatically (IDs are reassigned).
- On overlaps (mod already present as `.jar`) FlexHUD shows a **warning**.
- **Uninstalling**: button in the marketplace (click twice to confirm) –
  removes pack files, zip **and** imported widgets.

## Custom packs (for pack authors)

1. Create folder: `mein-pack/pack.json` (+ optional `mein-pack.flexconfig`, README, assets).
2. Define widgets (`type`, `template`, `x`, `y`, `enabled`, `style`).
3. Zip it (`pack.json` in zip root!) and upload as **GitHub release asset**.
4. Submit: **GitHub issue** in the FlexHUD repo ("submit marketplace pack") –
   the download link becomes an entry in `marketplace.json`.
   Required per entry: `id`, `name`, `downloadUrl`, **`modId`**, **`jarHints`**
   (Fabric ID + filename parts for `.jar` detection – without them no warning
   about duplicate modules!).
5. Optional `.flexconfig` for meta (author, website, update version) +
   custom buttons (`configMenu` for the overview menu, `widgetButtons` for the
   style menu, actions: `toggleStyle`, `setStyle`, `applyPreset`, `message`).
   Full reference: website → developer docs.

## Files & folders

- `config/flexhud.json` – widgets, styles, marketplace URL
- `config/flexhud-timer.json` – timer state
- `.minecraft/flexhud/marketplace/downloaded/` – downloaded pack zips
- `.minecraft/flexhud/marketplace/packs/<id>/` – extracted packs
- `.minecraft/flexhud/marketplace/marketplace-cache.json` – offline pack-list cache
- `.minecraft/flexhud/marketplace/installed.json` – origin of installed widgets

## FAQ

**Do I need StepCount for FlexHUD?**
No. Only the STEPCOUNT widget needs the StepCount `.jar` in `mods/`.

**Why do I see steps twice?**
StepCount's native HUD (`/stepcount show`) + FlexHUD widget run in parallel.
Hide one (recommended: native off, FlexHUD widget on).

**Does my layout survive updates?**
Yes. Positions are backed up before reinstalls and restored.
Only on changed pack structure you get a warning.

**ModMenu tab missing?**
ModMenu is optional. Menu always works via hotkey `H` or `/flexhud`.

**Marketplace won't load?**
Offline/wrong URL? Button **Reload** forces refresh (cache in `marketplace-cache.json`).

## Building (developers)

- JDK 25, then `gradlew.bat build` → `build/libs/flexhud-0.1.1-alpha.jar`
- Example pack: `powershell -File scripts/Build-ExamplePack.ps1` → `dist/*.zip`
- Full developer docs (pack schema, `.flexconfig`, bridge, i18n):
  website or `docs/developers.html` in the repo.

## Links

- Website + developer docs: `https://lokrogaming.github.io/flexhud-mod/`
- GitHub (issues, marketplace submissions): `https://github.com/Lokrogaming/flexhud-mod`
- Marketplace JSON: `https://raw.githubusercontent.com/Lokrogaming/flexhud-mod/master/marketplace/marketplace.json`

## License

MIT (see LICENSE). The StepCount bridge only uses the StepCount mod's
public client API.

---

## Deutsche Version

### Was ist FlexHUD?

**FlexHUD** macht aus festgenagelten Anzeigen **frei verschiebbare Widgets**:
Timer, StepCount, Uhrzeit, FPS und eigene Texte – jedes mit eigenem **Gradienten,
Animation, Position, Größe und Stil**. Dazu gibt es einen **Widget-Editor**
(Widgets per Drag & Drop anordnen) und einen **eingebauten Marketplace**,
über den du fertige Widget-Packs (z. B. das StepCount-Bridge-Pack) per Klick
installierst.

Die Mod läuft **komplett client-seitig** – auch auf Servern ohne Server-Mod.

### Features

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

### Voraussetzungen

- Minecraft Java **26.3**
- Fabric Loader **0.19.5**
- Fabric API **0.162.0+26.3** (oder neuer für 26.3)
- Java **25**
- Optional: **ModMenu** (für den ModMenü-Tab), **StepCount-Mod** (für das Steps-Widget)

### Installation

1. `flexhud-0.1.1-alpha.jar` aus den Releases in deinen `mods`-Ordner legen.
2. Fabric API ebenfalls in `mods` legen.
3. Spiel starten → Hotkey **`H`** drücken oder **`/flexhud`** eingeben.

### Schnellstart

1. `H` → **Widgets anordnen** → `+ Timer` → in der Vorschau **packen und ziehen**.
   Feinjustierung: **Pfeiltasten** (mit Shift extra-fein) oder Pfeil-Buttons.
2. **Stil**-Button → Gradient-Preset + Animation wählen (z. B. Sunset + Scroll links).
3. Timer: `/timer` (Start/Pause), `/timer reset`, oder im Menü.
4. StepCount (falls installiert): im FlexHUD-Menü **StepCount steuern**,
   dann StepCounts eigenes HUD mit `/stepcount show` **ausblenden**,
   damit nur das FlexHUD-Widget sichtbar ist.
5. Marketplace: `H` → **Marketplace** → **StepCount-Bridge-Pack** → Installieren.

### Commands (alle client-side)

- `/flexhud` – Hauptmenü öffnen
- `/flexhud editor` – Widget-Editor öffnen
- `/flexhud market` – Marketplace öffnen
- `/flexhud packs` – Installierte Packs (Übersicht)
- `/flexhud updates` – Pack-Updates (Übersicht)
- `/flexhud timer start|stop|reset|show|ms`
- `/flexhud stepcount start|stop|reset|show` – steuert die StepCount-Mod
- `/timer` – Timer Start/Pause umschalten
- `/timer start|stop|reset`

### Marketplace (für Spieler)

- **Installieren** lädt die Zip nach `.minecraft/flexhud/marketplace/downloaded/`
  und entpackt nach `.minecraft/flexhud/marketplace/packs/<id>/`.
  Widgets aus `pack.json` werden automatisch übernommen (IDs werden neu vergeben).
- Bei Überschneidungen (Mod schon als `.jar` da) zeigt FlexHUD eine **Warnung**.
- **Deinstallieren**: Button im Marketplace (2x klicken zur Bestätigung) –
  entfernt Pack-Dateien, Zip **und** importierte Widgets.

### Eigene Packs (für Pack-Autoren)

1. Ordner anlegen: `mein-pack/pack.json` (+ optionale `mein-pack.flexconfig`, README, Assets).
2. Widgets definieren (`type`, `template`, `x`, `y`, `enabled`, `style`).
3. Als `.zip` packen (`pack.json` im Zip-Root!) und als **GitHub-Release-Asset** hochladen.
4. Einreichung: **GitHub-Issue** im FlexHUD-Repo („Marketplace-Pack einreichen“) –
   der Download-Link wird als Eintrag in `marketplace.json` übernommen.
   Pflicht pro Eintrag: `id`, `name`, `downloadUrl`, **`modId`**, **`jarHints`**.
5. Optional `.flexconfig` für Meta + Custom-Buttons. Referenz: Website → Entwickler-Docs.

### FAQ

**Brauche ich StepCount für FlexHUD?**
Nein. Nur das STEPCOUNT-Widget braucht die StepCount-`.jar` in `mods/`.

**Warum sehe ich Steps doppelt?**
StepCounts natives HUD (`/stepcount show`) + FlexHUD-Widget laufen parallel.
Eines ausblenden (empfohlen: natives aus, FlexHUD-Widget an).

**Bleibt mein Layout bei Updates erhalten?**
Ja. Positionen werden vor Reinstalls gesichert und zurückgespielt.

**ModMenu-Tab fehlt?**
ModMenu ist optional. Menü geht immer via Hotkey `H` oder `/flexhud`.

### Links

- Website + Entwickler-Docs: `https://lokrogaming.github.io/flexhud-mod/`
- GitHub (Issues, Marketplace-Einreichungen): `https://github.com/Lokrogaming/flexhud-mod`

### Lizenz

MIT (siehe LICENSE).
