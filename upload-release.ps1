# Upload HackMineCrafft to GitHub (run after: gh auth login -h github.com -p https -w)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

gh auth status
if ($LASTEXITCODE -ne 0) {
    Write-Host "Run first: gh auth login -h github.com -p https -w"
    exit 1
}

git branch -M main 2>$null

$repo = "https://github.com/homychokk/HackMineCrafft.git"
$remote = git remote get-url origin 2>$null
if (-not $remote) {
    git remote add origin $repo
} elseif ($remote -ne $repo) {
    git remote set-url origin $repo
}

git push -u origin main --force

$notes = @"
HackMineCrafft 1.0.0 for Minecraft 1.20.1 (Fabric).

Requires Fabric API 0.92.9+1.20.1.
Open menu: Right Shift.
"@

gh release create v1.0.0 "releases/hackminecrafft-1.0.0.jar" --title "HackMineCrafft 1.0.0" --notes $notes 2>$null
if ($LASTEXITCODE -ne 0) {
    gh release upload v1.0.0 "releases/hackminecrafft-1.0.0.jar" --clobber 2>$null
    if ($LASTEXITCODE -ne 0) {
        gh release create v1.0.0 "releases/hackminecrafft-1.0.0.jar" --title "HackMineCrafft 1.0.0" --notes $notes
    }
}

Write-Host "Release: https://github.com/homychokk/HackMineCrafft/releases/tag/v1.0.0"
