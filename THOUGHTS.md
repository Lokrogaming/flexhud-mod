# FlexHUD – THOUGHTS (Gedanken, Ideen, offene Punkte für andere Agenten)

> Diese Datei sammelt ALLE Gedanken/Ideen, die beim Bau von FlexHUD aufgekommen sind.
> Zielgruppe: andere Coding-Agenten (und neugierige Menschen). Bitte beim Weiterarbeiten
> lesen + ergänzen statt stillschweigend umzubauen.

## 1. Warum "FlexHUD"?

- Anforderungen: Timer-HUD + frei verschiebbare Widgets + Style-Customizing + Marketplace.
- Namens-Kandidaten waren: HudForge, WidgetLab, PulseHUD, Overline, KryoHUD, FlexHUD.
- Gewählt: **FlexHUD** (Mod-ID `flexhud`), weil es exakt beschreibt, was die Mod tut
  (flexible HUD-Widgets), kurz ist, keine Namens-Kollision mit bekannten Mods hat
  und als CurseForge-/Modrinth-Slug funktioniert.
- Ordner: `flexhud-mod/`, Package: `com.lokro.flexhud` (konsistent mit StepCount `com.lokro.stepcount`).

## 2. Architektur-Entscheidungen (und warum)

1. **Ein HUD-Element für alle Widgets** (`WidgetHud.render`): Statt pro Widget ein
   `HudElementRegistry`-Element zu registrieren, rendert EIN Element alle aktivierten
   Widgets. Weniger Registrierungs-Overhead, eine Sichtbarkeitslogik, leicht erweiterbar.
2. **Relative Koordinaten (0..1)** statt Pixel: überlebt Auflösungs-/GUI-Scale-Wechsel.
   Editor rechnet beim Drag Preview↔Relativ um; HUD rechnet Relativ→Pixel.
3. **StepCount per Reflection, kein Hard-Dep**: `StepcountBridge` ruft
   `com.lokro.stepcount.StepCounter` via `Class.forName` auf. FlexHUD läuft solo;
   das STEPCOUNT-Widget zeigt ohne StepCount "n/a (StepCount fehlt)". Bricht auch nicht,
   wenn StepCount seine API umbenennt (dann nur Bridge-Fehlermeldung, kein Crash).
4. **Config als Gson-JSON** (`config/flexhud.json`, `config/flexhud-timer.json`):
   Keine Cloth-Pflicht. Vanilla-Screens funktionieren überall. Cloth/ModMenu sind
   reine `modCompileOnly`-Optionen.
5. **Marketplace-Cache**: `flexhud/marketplace/marketplace-cache.json` als Offline-Fallback,
   Refresh max. alle 5 Min. Downloads landen in `marketplace/downloaded/`,
   entpackt wird nach `marketplace/packs/<id>/`. Zip-Slip-Schutz ist drin
   (`out.startsWith(target)`-Check) – BITTE NICHT ENTFERNEN.
6. **Konflikterkennung doppelt** (`ConflictChecker`): `FabricLoader.isModLoaded(modId)`
   PLUS Dateiscan in `mods/*.jar*` über `jarHints`. Grund: Loader erkennt nur geladene
   Mods, der Scan findet auch `.jar.disabled` o.ä. Der Scan ist bewusst tolerant
   (substring, case-insensitive).
7. **ModMenu nur als dünner Adapter** (`compat/FlexhudModMenu`, ~10 Zeilen):
   Falls ModMenu/Cloth-Versionen für MC 26.3 nicht auflösen, baut die Mod auch ohne
   (siehe build.gradle-Kommentar). Garantierter Menü-Zugang bleibt: Hotkey `H` + `/flexhud`.

## 3. Bekannte Risiken / Verifikations-ToDos (Stand 08.10.2026: Build grün!)

- [x] **MC 26.3-Mappings** – per `javap` gegen den Loom-Cache verifiziert + `gradlew build` grün:
  - Screens rendern NICHT mehr via `render()`, sondern `extractRenderState(GuiGraphicsExtractor, int, int, float)` (plus `super`-Aufruf für Widgets).
  - `GuiGraphicsExtractor` hat KEIN `drawString` → `text(Font, String|Component, x, y, color[, shadow])`.
  - Screens öffnen/schließen via `minecraft.gui.setScreen(...)` (128 Vanilla-Caller; `Gui.screen()` als Getter). `Minecraft.setScreen` existiert NICHT mehr.
  - Keybinds: `KeyMappingHelper.registerKeyMapping` (Package `...keymapping.v1`, OHNE "bind"!), `new KeyMapping(String, int, Category)` mit `KeyMapping.Category.MISC` + `InputConstants.KEY_H` (kein LWJGL-Import nötig).
  - Maus-Events: `mouseClicked(MouseButtonEvent, boolean)`, `mouseDragged(MouseButtonEvent, double, double)`, `mouseReleased(MouseButtonEvent)` mit `event.x()/y()/button()`.
  - Chat-Feedback: `LocalPlayer.sendSystemMessage(Component)` (statt `displayClientMessage(msg, false)`).
  - `Util.getMillis()` lebt in `net.minecraft.util.Util` ✓ (wie in StepCount verwendet).
  - `Minecraft.execute(Runnable)` existiert nur noch geerbt (reicht); Commands laufen eh auf dem Client-Thread.
- [x] **ModMenu-Version** – `com.terraformersmc:modmenu:12.0.0-beta.1` existiert NICHT. Richtig für MC 26.3: **`maven.modrinth:modmenu:21.0.0`** (Repo `https://api.modrinth.com/maven`, per Modrinth-API verifiziert). Eingetragen als `compileOnly` (NICHT `modCompileOnly` – kennt Gradle in diesem Setup nicht). **Cloth wird nicht benötigt** (alle Screens Vanilla) → keine Dep.
- [ ] **Icon**: `assets/flexhud/icon.png` ist aktuell eine Kopie des StepCount-Icons
  (Platzhalter!). Vor CurseForge/Modrinth-Upload ersetzen.
- [ ] **`marketplace.json`-URL**: Default zeigt auf
  `raw.githubusercontent.com/lokro/flexhud-mod/...`. Nach Repo-Erstellung prüfen,
  ob User/Org-Name stimmt, sonst in `gradle.properties` + `FlexhudConfig` anpassen.
- [ ] **GitHub-Releases**: `scripts/Release-StepCountPack.ps1` nur vorbereitet,
  NICHT ausgeführt (kein Remote/Remote-Name bekannt, kein blindes Pushen).
  Ablauf steht im Skript + unten in Abschnitt 5.

## 4. Ideen-Backlog (später, NICHT jetzt)

- Mehr Widget-Typen: Koordinaten, Biome, FPS-Graph, Stoppuhr-Runden, Systemzeit-Datum,
  Ping, Text mit eigenem Inhalt, Bild-Widgets.
- Style: pro-Buchstaben-Offset-Animation, Outline, Regenbogen-Hintergrund, importierbare Themes.
- Editor: echtes In-Game-Overlay (Widgets direkt im HUD ziehen statt Vorschau-Box),
  Snap-to-Grid, Multi-Select, Undo.
- Marketplace: Suche/Filter, Kategorien, Icon-Caching + -Anzeige, Update-Check
  (installierte Version vs. JSON-Version), Deinstallieren-Button, Signatur/Checksummen
  (SHA-256 in JSON + Verify nach Download), Allowlist für Hosts.
- StepCount-Tiefe: `/flexhud stepcount` könnte auch `start` automatisch mit HUD-Show
  koppeln; optional StepCounts natives HUD per Config-Flag automatisch unterdrücken
  (statt nur Warnung) – aber Vorsicht, fremde Mod beeinflussen ist fragil.
- Performance (explizit später): Text-Caching pro Frame (VisualOrderText nur bei
  Änderung berechnen), Gradient-LUT statt pro-Buchstabe-Sampling, Download-Threadpool.
- i18n: alle Screen-Strings in lang-Keys auslagern (aktuell hardcoded DE).
- Cloth-Config-Screen als Alternative zum Hand-Screen (wenn Cloth-Version steht).

## 5. Release-Plan (Reihenfolge!)

1. `scripts/Init-Repos.ps1` (trocken lesen!): `git init` in flexhud-mod, erster Commit.
2. GitHub-Repos anlegen: `flexhud-mod` (neu) – StepCount-Repo NICHT umbenennen.
3. `marketplaceUrl` finalisieren (echte Raw-URL nach Push).
4. `gradlew build` in beiden Repos grün bekommen.
5. `scripts/Build-ExamplePack.ps1` → `dist/stepcount-bridge-pack-1.0.0.zip` prüfen
   (enthalten: pack.json + README).
6. `scripts/Release-StepCountPack.ps1 -Execute` im stepcount-Repo:
   erstellt Release `flexhud-pack-v1.0.0` mit `stepcount-mod-1.0.0.jar` + Pack-Zip.
   Erst DANN löst die downloadUrl in marketplace.json wirklich auf.
7. In Minecraft testen: Marketplace öffnen → Pack installieren → Warnung prüfen
   (StepCount-jar vorhanden!) → Widget per Editor positionieren.
8. README als CurseForge-/Modrinth-Description übernehmen (Abschnitte sind dafür
   vorbereitet), Icon ersetzen, Screenshots anhängen.

## 6. Für Menschen in einem Satz

FlexHUD = "Regale statt festgenagelter Bilder": Alle Anzeigen (Timer, Steps, Uhr …)
sind frei verschiebbare Kärtchen mit eigenem Style, plus eingebauter Laden für
fertige Kärtchen-Sets – und es warnt, wenn ein Kärtchen mit einer anderen Mod
doppelt hängt.
