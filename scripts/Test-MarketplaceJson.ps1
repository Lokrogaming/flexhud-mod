<#
.SYNOPSIS
  Validiert marketplace.json (Schema, Pflichtfelder, doppelte IDs, URL-Format).
.DESCRIPTION
  Harte Fehler (Schema/Pflichtfelder/Duplikate) -> Exit 1.
  -CheckUrls macht zusätzlich einen HEAD-Request auf jede downloadUrl.
  -VerifyZips lädt jede Zip temporär herunter und prüft pack.json im Root.
.EXAMPLE
  powershell -File scripts/Test-MarketplaceJson.ps1
  powershell -File scripts/Test-MarketplaceJson.ps1 -CheckUrls
#>
param(
  [string]$MarketplaceJson = (Join-Path (Split-Path -Parent $PSScriptRoot) "marketplace\marketplace.json"),
  [switch]$CheckUrls,
  [switch]$VerifyZips
)

$ErrorActionPreference = "Stop"
$failures = @()

function Fail([string]$msg) {
  Write-Host "FEHLER: $msg" -ForegroundColor Red
  $script:failures += $msg
}
function Warn([string]$msg) { Write-Host "WARNUNG: $msg" -ForegroundColor Yellow }
function Ok([string]$msg) { Write-Host "OK: $msg" -ForegroundColor Green }

if (-not (Test-Path -LiteralPath $MarketplaceJson)) { Fail "Datei fehlt: $MarketplaceJson" }

$data = $null
try {
  $data = Get-Content -LiteralPath $MarketplaceJson -Raw -Encoding UTF8 | ConvertFrom-Json
  Ok "JSON parsebar"
} catch {
  Fail "JSON ungültig: $($_.Exception.Message)"
}

if ($data -ne $null) {
  $packs = $data.packs
  if ($null -eq $packs) { $packs = $data.entries }
  if ($null -eq $packs) {
    Fail "Weder 'packs' noch 'entries' Array gefunden (eine der beiden Formen ist Pflicht)."
  } else {
    Ok "$($packs.Count) Einträge gefunden"
    $ids = @{}
    foreach ($p in $packs) {
      $label = if ($p.id) { $p.id } else { "(ohne id)" }
      foreach ($f in @("id", "name", "downloadUrl", "modId", "jarHints")) {
        $v = $p.$f
        $empty = ($null -eq $v) -or ($v -is [string] -and $v.Trim() -eq "") -or ($v -is [array] -and $v.Count -eq 0)
        if ($empty) { Fail "[$label] Pflichtfeld fehlt/leer: $f" }
      }
      if ($p.id -and $p.id -notmatch '^[a-z0-9][a-z0-9._-]*$') {
        Fail "[$label] id-Format ungültig (nur a-z 0-9 . _ -, Kleinbuchstaben): $($p.id)"
      }
      if ($p.id) {
        if ($ids.ContainsKey($p.id)) { Fail "Doppelte id: $($p.id)" } else { $ids[$p.id] = $true }
      }
      if ($p.downloadUrl -and $p.downloadUrl -notmatch '^https?://') {
        Fail "[$label] downloadUrl muss mit http(s):// beginnen: $($p.downloadUrl)"
      }
      if ($p.packType -and $p.packType -notin @("widget-pack", "bridge", "theme")) {
        Warn "[$label] unbekannter packType '$($p.packType)' (erwartet: widget-pack|bridge|theme)"
      }
      if ($CheckUrls -and $p.downloadUrl -match '^https?://') {
        try {
          Invoke-WebRequest -Uri $p.downloadUrl -Method Head -TimeoutSec 15 -MaximumRedirection 5 -UseBasicParsing | Out-Null
          Ok "[$label] downloadUrl erreichbar"
        } catch {
          Warn "[$label] downloadUrl NICHT erreichbar: $($_.Exception.Message)"
        }
      }
    }
    if ($VerifyZips) {
      $tmp = Join-Path ([System.IO.Path]::GetTempPath()) ("flexhud-verify-" + [System.Guid]::NewGuid().ToString("N"))
      New-Item -ItemType Directory -Path $tmp -Force | Out-Null
      try {
        foreach ($p in $packs) {
          if (-not ($p.downloadUrl -match '^https?://')) { continue }
          $zip = Join-Path $tmp ($p.id + ".zip")
          try {
            Invoke-WebRequest -Uri $p.downloadUrl -OutFile $zip -TimeoutSec 120 -MaximumRedirection 5 -UseBasicParsing
            Add-Type -AssemblyName System.IO.Compression.FileSystem
            $archive = [System.IO.Compression.ZipFile]::OpenRead($zip)
            try {
              $hasPack = @($archive.Entries | Where-Object { $_.FullName -eq "pack.json" }).Count -gt 0
              if ($hasPack) { Ok "[$($p.id)] Zip enthält pack.json im Root" }
              else { Fail "[$($p.id)] Zip enthält KEINE pack.json im Root (Pflicht für Import!)" }
            } finally { $archive.Dispose() }
          } catch {
            Warn "[$($p.id)] Zip-Verify fehlgeschlagen: $($_.Exception.Message)"
          }
        }
      } finally {
        Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue
      }
    }
  }
}

if ($failures.Count -gt 0) {
  Write-Host ""
  Write-Host "$($failures.Count) Fehler – marketplace.json NICHT übernehmen!" -ForegroundColor Red
  exit 1
}
Write-Host ""
Write-Host "marketplace.json valide." -ForegroundColor Green
