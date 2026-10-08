# StepCount-Bridge-Pack (FlexHUD Example-Pack)

Dieses Pack verbindet die **StepCount-Mod** mit **FlexHUD**.

## Was passiert beim Installieren?

1. FlexHUD lädt die `.zip` aus dem GitHub-Release herunter nach
   `flexhud/marketplace/downloaded/`.
2. Die Zip wird nach `flexhud/marketplace/packs/stepcount-bridge-pack/` entpackt.
3. Das Widget aus `pack.json` wird in deine FlexHUD-Config übernommen:
   ein frei positionierbares `Steps: {value}`-Widget im Sunset-Gradienten.
4. FlexHUD prüft per `modId` (`stepcount`) + Dateiscan (`mods/*.jar`),
   ob StepCount als `.jar` installiert ist – und warnt vor doppelten HUDs.

## Wichtig nach dem Install

- `/stepcount show` in StepCount AUSBLENDEN (sonst doppelte Anzeige),
- FlexHUD-Widget per Editor (Hotkey `H`) frei positionieren.

## Selbst bauen

Siehe `scripts/Build-ExamplePack.ps1` im FlexHUD-Repo.
