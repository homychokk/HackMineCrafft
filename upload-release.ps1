# Загрузка PrismGlass на GitHub (нужен: gh auth login)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

gh auth status | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Сначала войди в GitHub: gh auth login -h github.com -p https -w"
    exit 1
}

git branch -M main 2>$null

if (-not (git remote get-url origin 2>$null)) {
    gh repo create PrismGlass --public --source=. --remote=origin --description "PrismGlass — Fabric client 1.20.1 with liquid glass menu" --push
} else {
    git push -u origin main
}

gh release create v1.0.0 "releases/prismglass-1.0.0.jar" `
    --title "PrismGlass 1.0.0" `
    --notes "Fabric 1.20.1 client with liquid glass menu.`n`nRequires Fabric API 0.92.9+1.20.1.`nOpen menu: Right Shift."

Write-Host "Done! https://github.com/$(gh api user -q .login)/PrismGlass/releases/tag/v1.0.0"
