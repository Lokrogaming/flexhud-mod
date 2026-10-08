# FlexHUD – description.md (TEMPORÄR: Überblick für Agenten & AIs)

> Stand: 09.10.2026. Temporär, bis README/AGENTS/THOUGHTS alles abdecken.
> Nach dem Lesen dieser Datei: Details in README.md, AGENTS.md, THOUGHTS.md.

## Was ist FlexHUD?

Client-side Fabric-Mod für Minecraft Java 26.3 (Loader 0.19.5, Java 25, nur Fabric API
als Pflicht-Dep). Mod-ID `flexhud`, Paket `com.lokro.flexhud`, Repo
`Lokrogaming/flexhud-mod`. Sie macht aus festgenagelten HUD-Anzeigen frei
verschiebbare, gestylte **Widgets** und bringt einen eingebauten **Marketplace**
für Widget-Packs mit.

## Was kann sie? (Module)

- **Widget-System** (`client/config`, `client/hud/WidgetHud`): Typen TIMER,
  STEPCOUNT (Bridge), CLOCK, TEXT, FPS. Position relativ 0..1, pro Widget ein
  WidgetStyle (Gradient, Animation OFF/SCROLL_L/SCROLL_R/PULSE/RAINBOW, Dauer,
  fett/kursiv, Schatten, Hintergrund + Deckkraft, Scale, Text an/aus, Vorlage
  mit `{value}`). Ein HUD-Element rendert alle Widgets.
- **Timer** (`client/state/TimerState`): Stoppuhr mit ms-Toggle (persistiert),
  Format wächst automatisch (M:SS → H:MM:SS → dD H:MM:SS). Vorlagen mit
  `{value}` plus Einzel-Platzhaltern `{d} {h} {m} {s} {ms}`.
- **StepCount-Bridge** (`client/bridge/StepcountBridge`): steuert die
  StepCount-Mod per Reflection (KEIN Hard-Dep!) – `com.lokro.stepcount.StepCounter`.
- **Editor** (`client/gui/WidgetEditorScreen`): responsiv, Live-Vorschau mit
  echtem Stil, Drag (Klick packt nächstes Widget), Pfeiltasten (+Shift fein),
  Aktions-Leiste (Ein/Aus, Stil, Löschen mit Bestätigung).
- **Stil-Editor** (`StyleEditScreen`): Live-Vorschau, scrollbar, plus
  **Custom-Buttons aus .flexconfig** des Herkunfts-Packs (★-Buttons).
- **Marketplace** (`client/market`, `client/gui/MarketplaceScreen`): lädt
  `marketplace.json` per URL (Pflichtfelder pro Eintrag: id, name, downloadUrl,
  **modId, jarHints**). Download nach `flexhud/marketplace/downloaded/`,
  entpackt nach `flexhud/marketplace/packs/<id>/`, importiert `widgets[]` aus
  `pack.json` (frische IDs, Herkunft → `installed.json`). Deinstallieren mit
  Zwei-Klick-Bestätigung (Dateien + importierte Widgets).
- **.flexconfig** (`market/FlexConfig`, `FlexAction`): optionale Datei
  `*.flexconfig` im Pack-Root. Meta (author, website, created, updated, version,
  description; icons reserviert/später) → Anzeige im Marketplace. `configMenu`
  (muss deklariert sein!) → Buttons im **Overview-Menü** (wirken auf alle
  Pack-Widgets). `widgetButtons` → Buttons im Stil-Menü (wirken aufs Widget).
  Aktionen: toggleStyle, setStyle, applyPreset, message.
- **Overview** (`PackOverviewScreen`, `/flexhud packs`): installierte Packs +
  ihre configMenu-Buttons.
- **Update-Checker** (`UpdateChecker`, `UpdateScreen`, `/flexhud updates`):
  Beim Start alle Links fetchen, Zips temporär laden, Version aus Config lesen
  (flexconfig > pack.json > Eintrag), Temp löschen. Beim Join Chat-Zusammenfassung
  (zählbar: aktualisierbar vs. nicht-verfügbar/kaputt/gesperrt, nur installierte
  betreffende Probleme), Klick → Update-Screen (einzeln/alle). Reinstall sichert
  das Layout (positionsweise, bei Strukturänderung Warnung + alte Felder weg).
- **Konflikterkennung** (`ConflictChecker`): modId via Loader + `mods/*.jar`-Scan
  (jarHints) + Pack-gegen-Pack via modId aus `installed.json`.
- **i18n** (`client/i18n/Lang`): Spiel-Sprache (`options.languageCode`), DE bei
  `de*`, sonst EN. ALLE UI-Strings über Keys; Fallback EN, unbekannte Keys geben
  den Key zurück. Neue Strings IMMER in beiden Maps eintragen!

## Wichtige Dateien/Orte

- `config/flexhud.json` (Widgets+Stile+URL), `config/flexhud-timer.json`
- `.minecraft/flexhud/marketplace/{downloaded,packs/<id>/,marketplace-cache.json,installed.json,layout-backup/}`
- `marketplace/marketplace.json` (geladen per URL), `marketplace/packs/*/pack.json`,
  `*.flexconfig`, `marketplace/template-pack-entry.json`
- `scripts/`: Build-ExamplePack, Add/Test-MarketplaceEntry/Json, Release, Init-Repos
- 26.3-API-Fallen (per javap verifiziert, NICHT raten): `extractRenderState` statt
  `render()`, `graphics.text()` statt `drawString`, `minecraft.gui.setScreen()`,
  `KeyMappingHelper.registerKeyMapping` + `KeyMapping.Category`,
  `MouseButtonEvent`-Records, `sendSystemMessage()`, ModMenu `maven.modrinth:modmenu:21.0.0`
  als `compileOnly` (kein Cloth nötig). Details: THOUGHTS.md §3.

## Regeln für Änderungen

1. Client-only; StepCount nie als `implementation`-Dep (Bridge bleibt Reflection).
2. Zip-Slip-Check in `PackInstaller.unzip` nie entfernen.
3. `modId` + `jarHints` Pflicht lassen; Config-Felder nicht umbenennen (User-Layouts!).
4. ModMenu/Cloth optional halten; keine neuen Libs ohne Absprache.
5. Neue UI-Strings → beide Sprachen in Lang.java; neue Screens responsiv bauen
   (Breiten aus `width/height`, kein Überlappen – siehe Screenshot-Bugs in Historie).
6. Kommentare: nur Kritisches (Security, API-Verträge, Formate); Rest trimmen.
7. Nach Änderungen: `gradlew.bat build`, ggf. Skripte/Test-JSON, commit+push master.
