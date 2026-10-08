<#
.SYNOPSIS
  Erstellt das GitHub-Release für das StepCount-Bridge-Pack (JAR + Pack-Zip).
.DESCRIPTION
  Ablauf (Reihenfolge wichtig!):
   1. Baut die StepCount-Mod (gradlew build) -> stepcount-mod-<ver>.jar
   2. Baut die Pack-Zip via flexhud-mod/scripts/Build-ExamplePack.ps1
   3. Erstellt per 'gh' ein Release auf dem STEPCOUNT-Repo mit beiden Assets.
  Standard ist DRY-RUN (zeigt nur Befehle). Mit -Execute wird wirklich released.
.EXAMPLE
  # Nur anzeigen:
  powershell -File scripts/Release-StepCountPack.ps1
  # Wirklich ausführen:
  powershell -File scripts/Release-StepCountPack.ps1 -Execute
#>
param(
  [string]$StepCountRepo = "C:\Users\lokro\projekte\stepcount-mod",
  [string]$FlexHudRepo = "C:\Users\lokro\projekte\flexhud-mod",
  [string]$GhRepo = "lokro/stepcount-mod",
  [string]$Tag = "flexhud-pack-v1.0.0",
  [string]$Title = "FlexHUD Bridge-Pack v1.0.0 (+ StepCount jar)",
  [string]$ModVersion = "1.0.0",
  [string]$PackVersion = "1.0.0",
  [switch]$Execute
)

$ErrorActionPreference = "Stop"

$jar = Join-Path $StepCountRepo "build\libs\stepcount-mod-$ModVersion.jar"
$packZip = Join-Path $FlexHudRepo "dist\stepcount-bridge-pack-$PackVersion.zip"
$notes = Join-Path $FlexHudRepo "marketplace\packs\stepcount-bridge-pack\README.md"

Write-Host "=== 1/3 StepCount bauen ==="
Write-Host "  cd $StepCountRepo; .\gradlew.bat build"
if ($Execute) {
  & (Join-Path $StepCountRepo "gradlew.bat") build
}
if ($Execute -and -not (Test-Path -LiteralPath $jar)) {
  throw "JAR fehlt nach Build: $jar"
}

Write-Host "=== 2/3 Pack-Zip bauen ==="
Write-Host "  powershell -File $FlexHudRepo\scripts\Build-ExamplePack.ps1 -Version $PackVersion"
if ($Execute) {
  & powershell -File (Join-Path $FlexHudRepo "scripts\Build-ExamplePack.ps1") -Version $PackVersion
}

Write-Host "=== 3/3 GitHub-Release ($GhRepo @$Tag) ==="
$cmd = "gh release create `"$Tag`" `"$jar`" `"$packZip`" --repo `"$GhRepo`" --title `"$Title`" --notes-file `"$notes`""
Write-Host "  $cmd"
Write-Host ""
Write-Host "Erwartete downloadUrl danach:"
Write-Host "  https://github.com/$GhRepo/releases/download/$Tag/stepcount-bridge-pack-$PackVersion.zip"
Write-Host "Diese URL steht bereits in marketplace/marketplace.json (Tag-Namen ggf. angleichen!)."

if ($Execute) {
  Invoke-Expression $cmd
  Write-Host "OK: Release erstellt. Jetzt marketplace.json-URL gegenprüfen + testen: /flexhud market"
} else {
  Write-Host "(DRY-RUN – nichts ausgeführt. Mit -Execute wirklich releasen.)"
}
