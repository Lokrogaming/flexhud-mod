<#
.SYNOPSIS
  Trägt einen einzelnen Pack-Eintrag (inkl. Download-Link) in marketplace.json ein.
.DESCRIPTION
  Maintainer-Workflow: Jemand reicht ein Pack ein (siehe .github/ISSUE_TEMPLATE/
  marketplace-submission.yml), du prüfst es und trägst es mit DIESEM Skript ein.
  Das Skript validiert Pflichtfelder (id, name, downloadUrl, modId, jarHints),
  prüft doppelte IDs und kann optional URL + Zip verifizieren.
  Danach: Test-MarketplaceJson.ps1 laufen lassen, committen, pushen.
.EXAMPLE
  powershell -File scripts/Add-MarketplaceEntry.ps1 `
    -Id "mein-pack" -Name "Mein Pack" -Description "..." -Author "Du" `
    -Version "1.0.0" -DownloadUrl "https://github.com/.../mein-pack-1.0.0.zip" `
    -ModId "meine-mod" -JarHints "meine-mod,meine_mod" -CheckUrl -VerifyZip
#>
param(
  [Parameter(Mandatory = $true)][string]$Id,
  [Parameter(Mandatory = $true)][string]$Name,
  [Parameter(Mandatory = $true)][string]$Description,
  [Parameter(Mandatory = $true)][string]$Author,
  [string]$Version = "1.0.0",
  [Parameter(Mandatory = $true)][string]$DownloadUrl,
  [Parameter(Mandatory = $true)][string]$ModId,
  [Parameter(Mandatory = $true)][string]$JarHints,
  [ValidateSet("widget-pack", "bridge", "theme")][string]$PackType = "widget-pack",
  [string]$IconUrl = "",
  [string]$InstallNote = "",
  [string]$MarketplaceJson = (Join-Path (Split-Path -Parent $PSScriptRoot) "marketplace\marketplace.json"),
  [switch]$CheckUrl,
  [switch]$VerifyZip,
  [switch]$Force
)

$ErrorActionPreference = "Stop"

if ($Id -notmatch '^[a-z0-9][a-z0-9._-]*$') {
  throw "Id-Format ungültig (nur a-z 0-9 . _ -, Kleinbuchstaben): $Id"
}
if ($DownloadUrl -notmatch '^https?://') {
  throw "DownloadUrl muss mit http(s):// beginnen: $DownloadUrl"
}
$hints = @($JarHints -split ',' | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" })
if ($hints.Count -eq 0) { throw "JarHints darf nicht leer sein (z.B. 'meine-mod,meine_mod')." }

if (-not (Test-Path -LiteralPath $MarketplaceJson)) { throw "marketplace.json fehlt: $MarketplaceJson" }
$data = Get-Content -LiteralPath $MarketplaceJson -Raw -Encoding UTF8 | ConvertFrom-Json
if ($null -eq $data.packs) { throw "marketplace.json: 'packs'-Array fehlt." }

$existing = @($data.packs | Where-Object { $_.id -eq $Id })
if ($existing.Count -gt 0 -and -not $Force) {
  throw "Eintrag mit id '$Id' existiert bereits. Zum Überschreiben -Force verwenden."
}

if ($CheckUrl) {
  try {
    Invoke-WebRequest -Uri $DownloadUrl -Method Head -TimeoutSec 15 -MaximumRedirection 5 -UseBasicParsing | Out-Null
    Write-Host "OK: downloadUrl erreichbar." -ForegroundColor Green
  } catch {
    throw "downloadUrl NICHT erreichbar: $($_.Exception.Message)"
  }
}

if ($VerifyZip) {
  $tmp = Join-Path ([System.IO.Path]::GetTempPath()) ("flexhud-add-" + [System.Guid]::NewGuid().ToString("N") + ".zip")
  try {
    Invoke-WebRequest -Uri $DownloadUrl -OutFile $tmp -TimeoutSec 120 -MaximumRedirection 5 -UseBasicParsing
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [System.IO.Compression.ZipFile]::OpenRead($tmp)
    try {
      $hasPack = @($archive.Entries | Where-Object { $_.FullName -eq "pack.json" }).Count -gt 0
      if (-not $hasPack) { throw "Zip enthält KEINE pack.json im Root – Pack wird von FlexHUD nicht importiert!" }
      Write-Host "OK: Zip enthält pack.json im Root." -ForegroundColor Green
    } finally { $archive.Dispose() }
  } finally {
    Remove-Item -LiteralPath $tmp -Force -ErrorAction SilentlyContinue
  }
}

$entry = [ordered]@{
  id          = $Id
  name        = $Name
  description = $Description
  author      = $Author
  version     = $Version
  iconUrl     = $IconUrl
  downloadUrl = $DownloadUrl
  modId       = $ModId
  jarHints    = $hints
  packType    = $PackType
  installNote = $InstallNote
}

if ($existing.Count -gt 0) {
  $data.packs = @($data.packs | Where-Object { $_.id -ne $Id }) + @($entry)
  Write-Host "Eintrag '$Id' ersetzt."
} else {
  $data.packs += $entry
  Write-Host "Eintrag '$Id' hinzugefügt."
}

$data | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $MarketplaceJson -Encoding UTF8
Write-Host "Gespeichert: $MarketplaceJson"
Write-Host "Nächste Schritte: Test-MarketplaceJson.ps1 laufen lassen, committen, pushen."
