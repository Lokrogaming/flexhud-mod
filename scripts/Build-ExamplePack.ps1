<#
.SYNOPSIS
  Baut das StepCount-Bridge-Example-Pack als .zip (für Marketplace + GitHub-Release).
.DESCRIPTION
  Packt marketplace/packs/stepcount-bridge-pack/ zu dist/stepcount-bridge-pack-<version>.zip.
  Die Zip enthält pack.json im Root (Pflicht für den FlexHUD-Importer).
.EXAMPLE
  powershell -File scripts/Build-ExamplePack.ps1
  powershell -File scripts/Build-ExamplePack.ps1 -Version 1.0.1
#>
param(
  [string]$Version = "1.0.0"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$src = Join-Path $root "marketplace\packs\stepcount-bridge-pack"
$dist = Join-Path $root "dist"

if (-not (Test-Path -LiteralPath $src)) {
  throw "Pack-Ordner fehlt: $src"
}
$packJson = Join-Path $src "pack.json"
if (-not (Test-Path -LiteralPath $packJson)) {
  throw "pack.json fehlt in $src (Pflicht für FlexHUD-Import!)"
}

New-Item -ItemType Directory -Path $dist -Force | Out-Null
$out = Join-Path $dist "stepcount-bridge-pack-$Version.zip"
if (Test-Path -LiteralPath $out) { Remove-Item -LiteralPath $out -Force }

Compress-Archive -Path (Join-Path $src "*") -DestinationPath $out -Force
Write-Host "OK: $out"
Write-Host "Inhalt:"
Get-ChildItem -LiteralPath $src | Select-Object -ExpandProperty Name
