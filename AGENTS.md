# AGENTS.md – Hinweise für Coding-Agenten (FlexHUD)

## Überblick
- Fabric-Mod, **client-side only**, MC 26.3, Loader 0.19.5, Java 25, Loom 1.18.3.
- Mod-ID `flexhud`, Package `com.lokro.flexhud`, Client-Code in `src/client/java/...`.
- Referenz-Mod: `../stepcount-mod` (StepCount-HUD, Gradient-Idee, Mojang-Mappings).

## Struktur
```
src/main/java/com/lokro/flexhud/FlexhudMod.java      # minimaler Server-Entrypoint
src/client/java/com/lokro/flexhud/client/
  FlexhudClient.java     # Registrierung (HUD, Ticks, Keys, Commands)
  config/                # FlexhudConfig (flexhud.json), WidgetConfig, WidgetStyle, Enums
  state/TimerState.java  # Stoppuhr (flexhud-timer.json)
  bridge/StepcountBridge.java  # Reflection-Bridge zu StepCount (KEIN Hard-Dep!)
  hud/WidgetHud.java     # rendert alle Widgets (ein HUD-Element)
  util/GradientUtil.java # Gradient/Animation (generalisiert aus StepCount)
  key/FlexhudKeys.java    # Hotkey H
  cmd/FlexhudCommands.java     # /flexhud, /timer
  gui/                   # FlexhudScreen, WidgetEditorScreen, StyleEditScreen, MarketplaceScreen
  market/                # MarketplaceEntry, MarketplaceCache, ConflictChecker, PackInstaller
src/client/java/com/lokro/flexhud/compat/FlexhudModMenu.java  # ModMenu-Adapter (optional)
marketplace/marketplace.json              # wird per URL geladen
marketplace/packs/stepcount-bridge-pack/  # Example-Pack (pack.json + README)
scripts/  Build-ExamplePack.ps1, Release-StepCountPack.ps1, Init-Repos.ps1
```

## Regeln beim Ändern
1. **Client-only**: nichts in `src/main` außer dem leeren Entrypoint. Kein Server-Code.
2. **StepCount nie als `implementation`-Dep** hinzufügen – Bridge bleibt Reflection.
3. **Zip-Slip-Check** in `PackInstaller.unzip` nicht entfernen.
4. **Pflichtfelder** `modId` + `jarHints` in `MarketplaceEntry`/marketplace.json beibehalten
   (Konflikterkennung hängt daran).
5. **ModMenu optional halten**: `compileOnly "maven.modrinth:modmenu:21.0.0"`
   (Repo `https://api.modrinth.com/maven`, Version per Modrinth-API für MC 26.3
   verifiziert). KEIN `modCompileOnly` (unbekannt in diesem Setup), KEIN Cloth
   (alle Screens sind Vanilla).
6. **26.3-API-Fallen** (per javap verifiziert, nicht raten!): Screens rendern via
   `extractRenderState(...)` (kein `render()`), Texte via `graphics.text(...)`
   (kein `drawString`), Screens via `minecraft.gui.setScreen(...)` öffnen,
   Keybinds via `KeyMappingHelper.registerKeyMapping` + `KeyMapping.Category`,
   Maus via `MouseButtonEvent`-Records, Chat via `sendSystemMessage(...)`.
   Details + Historie: `THOUGHTS.md` Abschnitt 3.
6. Config-Format ist User-Facing: Felder nicht umbenennen ohne Migration (Gson lädt
   sonst Defaults und User-Layouts gehen "verloren" → Frust).
7. Screens nutzen Vanilla-Widgets (Button/EditBox), keine neuen Libs ohne Absprache.

## Bauen & Testen
- `gradlew.bat build` (Windows) im `flexhud-mod/`-Ordner. Output: `build/libs/`.
- Schneller Check ohne MC-Start: `gradlew.bat compileJava compileClientJava`.
- Manuell in Minecraft: Hotkey `H`, `/flexhud`, `/timer`, `/flexhud stepcount show`,
  Marketplace öffnen → Example-Pack installieren → Editor-Drag prüfen.
- Beispiel-Pack bauen: `powershell -File scripts/Build-ExamplePack.ps1` → `dist/*.zip`.

## Marketplace-Pack-Schema (für neue Packs)
```jsonc
{
  "id": "mein-pack",            // PFLICHT, [a-z0-9-]
  "name": "Mein Pack",          // PFLICHT (Anzeige)
  "description": "...",
  "author": "...",
  "version": "1.0.0",
  "iconUrl": "https://...",
  "downloadUrl": "https://.../pack.zip",  // PFLICHT (GitHub-Release-Asset)
  "modId": "meine-mod",         // PFLICHT (Fabric-ID für Konflikterkennung)
  "jarHints": ["meine-mod"],    // PFLICHT (Dateiscan in mods/)
  "packType": "widget-pack",    // widget-pack | bridge | theme
  "installNote": "..."
}
```
Zip-Layout: `pack.json` (mit `widgets[]`) im Zip-Root + optionale README/Assets.

## Marketplace-Einträge (Maintainer-Workflow)
- Einreichungen kommen per GitHub-Issue (`.github/ISSUE_TEMPLATE/marketplace-submission.yml`).
- Eintragen NUR via `scripts/Add-MarketplaceEntry.ps1` (validiert Pflichtfelder,
  ID-Format, Duplikate; `-CheckUrl -VerifyZip` prüfen Link + pack.json im Root).
- Danach `scripts/Test-MarketplaceJson.ps1` (ggf. `-CheckUrls -VerifyZips`).
- Feld-Vorlage: `marketplace/template-pack-entry.json`.
- `.flexconfig`-Schema + Custom-Button-Aktionen: siehe README („Eigene .flexconfig“)
  und Beispiel-Pack `marketplace/packs/stepcount-bridge-pack/`.
- Deinstallieren: `PackInstaller.uninstall` (Dateien + importierte Widgets);
  Herkunft in `marketplace/installed.json` (`InstalledRegistry`), UI mit
  Zwei-Klick-Bestätigung im `MarketplaceScreen`.

## Dokumentation
- `README.md` = User-Doku + CurseForge/Modrinth-Description (zusammen halten!).
- `THOUGHTS.md` = Entscheidungen/Ideen/Backlog (bei jeder größeren Änderung ergänzen).
- `description.md` = TEMPORÄRER Agenten-Überblick (bei Reife einpflegen + löschen).
- `marketplace/marketplace.json` = nach Release-Änderungen auch die `downloadUrl` prüfen.
- i18n: alle UI-Strings über `client/i18n/Lang` (DE/EN), nie hartcodieren.
