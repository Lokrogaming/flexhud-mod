<#
.SYNOPSIS
  Initialisiert die Git-Repos für flexhud-mod (und optional stepcount-mod).
.DESCRIPTION
  Führt NUR lokale git-Befehle aus (init/add/commit). Das Anlegen auf GitHub
  ('gh repo create') wird als Befehl AUSGEGEBEN, aber nur mit -Execute ausgeführt.
  Bitte VOR dem Pushen Remotes + Sichtbarkeit (public/private) prüfen!
.EXAMPLE
  powershell -File scripts/Init-Repos.ps1
  powershell -File scripts/Init-Repos.ps1 -Execute -GithubUser lokro
#>
param(
  [string]$FlexHudRepo = "C:\Users\lokro\projekte\flexhud-mod",
  [string]$GithubUser = "lokro",
  [switch]$Execute
)

$ErrorActionPreference = "Stop"

function Run-Step([string]$Command) {
  Write-Host "  $Command"
  if ($Execute) { Invoke-Expression $Command }
}

Write-Host "=== flexhud-mod: git init + erster Commit ==="
Set-Location -LiteralPath $FlexHudRepo
if (-not (Test-Path -LiteralPath (Join-Path $FlexHudRepo ".git"))) {
  Run-Step "git init"
} else {
  Write-Host "  (.git existiert bereits – init übersprungen)"
}
Run-Step "git add -A"
Run-Step "git commit -m `"FlexHUD 0.1.0: Widgets, Timer, StepCount-Bridge, Marketplace, Editor`""

Write-Host ""
Write-Host "=== GitHub-Repo anlegen (prüfen!) ==="
Write-Host "  Vorgeschlagen: gh repo create $GithubUser/flexhud-mod --public --source . --push"
Write-Host "  (Mit -Execute wird NUR der git-Teil oben ausgeführt, Repo-Erstellung bleibt manuell – bewusst!)"

if ($Execute) {
  Write-Host ""
  Write-Host "Fertig lokal. Nächste Schritte: gh repo create (s.o.), dann marketplaceUrl prüfen (README/AGENTS.md)."
}
