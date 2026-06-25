# Upload PrismGlass to GitHub (run after: gh auth login -h github.com -p https -w)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

gh auth status
if ($LASTEXITCODE -ne 0) {
    Write-Host "Run first: gh auth login -h github.com -p https -w"
    exit 1
}

git branch -M main 2>$null

$remote = git remote get-url origin 2>$null
if (-not $remote) {
    gh repo create PrismGlass --public --source=. --remote=origin --description "PrismGlass Fabric client 1.20.1" --push
} else {
    git push -u origin main
}

$notes = @"
PrismGlass 1.0.0 for Minecraft 1.20.1 (Fabric).

Requires Fabric API 0.92.9+1.20.1.
Open menu: Right Shift.
"@

gh release create v1.0.0 "releases/prismglass-1.0.0.jar" --title "PrismGlass 1.0.0" --notes $notes

$user = gh api user -q .login
Write-Host "Release: https://github.com/$user/PrismGlass/releases/tag/v1.0.0"
